package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.settings.models.LavaReplacementWorlds
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.client.color.block.BlockTintSource
import net.minecraft.client.renderer.BiomeColors
import net.minecraft.client.renderer.block.BlockAndTintGetter
import net.minecraft.client.renderer.block.FluidModel
import net.minecraft.client.resources.model.ModelDebugName
import net.minecraft.client.resources.model.sprite.Material
import net.minecraft.client.resources.model.sprite.MaterialBaker
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier
import net.minecraft.util.ARGB
import net.minecraft.world.level.block.state.BlockState

object LavaRendering {
    @Volatile
    private var lavaReplacementModel: FluidModel? = null

    /** Chunks already rebuilt for the current world while replacement is on. */
    private var appliedInWorld = false

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    /** Tint only colors the water replacement, so replacement has to be on. */
    @JvmStatic
    fun isActive(): Boolean {
        if (!WorldUtils.isInSkyblock() || !WorldRendering.replaceLavaWithWater) return false
        return isSelectedWorld()
    }

    private fun isSelectedWorld(): Boolean {
        val worldName = WorldUtils.getWorldName() ?: return false
        return WorldRendering.lavaReplacementWorlds.any { it.worldName == worldName }
    }

    /**
     * Scoreboard area arrives after some chunks mesh. Rebuild once the world is known.
     */
    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!isActive() || appliedInWorld) return
        reloadRenderedLava()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        appliedInWorld = false
    }

    @JvmStatic
    fun reloadRenderedLava() {
        if (!WorldUtils.isInSkyblock()) return
        val worldName = WorldUtils.getWorldName() ?: return
        val affectsThisWorld = LavaReplacementWorlds.values().any { it.worldName == worldName }
        if (!affectsThisWorld && !appliedInWorld) return
        appliedInWorld = isActive()

        FeeshMod.mc.schedule {
            if (FeeshMod.mc.level == null) {
                appliedInWorld = false
                return@schedule
            }
            //#if MC >= 26.2
            //$$ FeeshMod.mc.levelExtractor.allChanged()
            //#else
            FeeshMod.mc.levelRenderer.allChanged()
            //#endif
        }
    }

    @JvmStatic
    fun bakeLavaReplacementModel(materials: MaterialBaker) {
        val unbaked = FluidModel.Unbaked(
            waterMaterial("block/water_still"),
            waterMaterial("block/water_flow"),
            waterMaterial("block/water_overlay"),
            LavaTintSource,
        )
        lavaReplacementModel = unbaked.bake(materials, ModelDebugName { "Feesh Lava Replacement" })
    }

    @JvmStatic
    fun getLavaReplacementModel(): FluidModel? = lavaReplacementModel

    /** Water sprites on the translucent chunk layer, so lava is see-through like water. */
    private fun waterMaterial(path: String): Material =
        Material(Identifier.withDefaultNamespace(path)).withForceTranslucent(true)

    private object LavaTintSource : BlockTintSource {
        override fun color(state: BlockState): Int {
            if (WorldRendering.tintReplacedLava) return tintColor()
            return ARGB.opaque(0x3F76E4)
        }

        override fun colorInWorld(state: BlockState, level: BlockAndTintGetter, pos: BlockPos): Int {
            if (WorldRendering.tintReplacedLava) return tintColor()
            return BiomeColors.getAverageWaterColor(level, pos)
        }

        private fun tintColor(): Int = ARGB.opaque(WorldRendering.lavaTintColor)
    }
}
