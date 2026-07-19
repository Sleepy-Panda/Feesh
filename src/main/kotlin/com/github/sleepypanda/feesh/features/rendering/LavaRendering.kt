package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.WorldUtils
//#if MC >= 26.1
//$$ import net.minecraft.client.color.block.BlockTintSource
//$$ import net.minecraft.client.renderer.BiomeColors
//$$ import net.minecraft.client.renderer.block.BlockAndTintGetter
//$$ import net.minecraft.client.renderer.block.FluidModel
//$$ import net.minecraft.client.resources.model.ModelDebugName
//$$ import net.minecraft.client.resources.model.sprite.Material
//$$ import net.minecraft.client.resources.model.sprite.MaterialBaker
//$$ import net.minecraft.core.BlockPos
//$$ import net.minecraft.resources.Identifier
//$$ import net.minecraft.util.ARGB
//$$ import net.minecraft.world.level.block.state.BlockState
//#endif

object LavaRendering {
    //#if MC >= 26.1
    //$$ @Volatile
    //$$ private var lavaReplacementModel: FluidModel? = null
    //#endif

    @JvmStatic
    fun shouldReplaceLavaWithWater(): Boolean {
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return false
        if (!WorldRendering.replaceLavaWithWater) return false

        return true
    }

    @JvmStatic
    fun shouldTintLava(): Boolean {
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return false
        if (!WorldRendering.replaceLavaWithTinted) return false

        return true
    }

    @JvmStatic
    fun getLavaTintColor(): Int {
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return 0
        if (!WorldRendering.replaceLavaWithTinted) return 0

        return WorldRendering.lavaTintColor
    }

    /**
     * Schedules chunks rebuild around the player so lava/tinted water updates.
     */
    @JvmStatic
    fun reloadRenderedLava() {
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return

        FeeshMod.mc.schedule {
            //#if MC >= 26.2
            //$$ FeeshMod.mc.levelExtractor.allChanged()
            //#else
            FeeshMod.mc.levelRenderer.allChanged()
            //#endif
        }
    }

    //#if MC >= 26.1
    //$$ @JvmStatic
    //$$ fun bakeLavaReplacementModel(materials: MaterialBaker) {
    //$$     val unbaked = FluidModel.Unbaked(
    //$$         Material(Identifier.withDefaultNamespace("block/water_still"), false),
    //$$         Material(Identifier.withDefaultNamespace("block/water_flow"), false),
    //$$         Material(Identifier.withDefaultNamespace("block/water_overlay"), false),
    //$$         LavaTintSource,
    //$$     )
    //$$     lavaReplacementModel = unbaked.bake(materials, ModelDebugName { "Feesh Lava Replacement" })
    //$$ }
    //$$
    //$$ @JvmStatic
    //$$ fun getLavaReplacementModel(): FluidModel? = lavaReplacementModel
    //$$
    //$$ @JvmStatic
    //$$ fun isLavaReplacementModel(model: FluidModel): Boolean = model === lavaReplacementModel
    //$$
    //$$ /**
    //$$  * Dynamic tint: custom color when tint mode is on, otherwise biome water color.
    //$$  * Matches 1.21 priority: plain water replace wins over tint.
    //$$  */
    //$$ private object LavaTintSource : BlockTintSource {
    //$$     override fun colorInWorld(state: BlockState, level: BlockAndTintGetter, pos: BlockPos): Int {
    //$$         if (shouldReplaceLavaWithWater()) {
    //$$             return BiomeColors.getAverageWaterColor(level, pos)
    //$$         }
    //$$         if (shouldTintLava()) {
    //$$             val tint = getLavaTintColor()
    //$$             if (tint != 0) return ARGB.opaque(tint)
    //$$         }
    //$$         return BiomeColors.getAverageWaterColor(level, pos)
    //$$     }
    //$$
    //$$     override fun color(state: BlockState): Int {
    //$$         if (shouldReplaceLavaWithWater()) {
    //$$             return ARGB.opaque(0x3F76E4)
    //$$         }
    //$$         if (shouldTintLava()) {
    //$$             val tint = getLavaTintColor()
    //$$             if (tint != 0) return ARGB.opaque(tint)
    //$$         }
    //$$         return ARGB.opaque(0x3F76E4)
    //$$     }
    //$$ }
    //#endif
}
