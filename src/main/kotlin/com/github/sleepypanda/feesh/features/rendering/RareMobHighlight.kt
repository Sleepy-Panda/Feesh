package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.ArmorStandLoadedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.JvmField
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.mob.MagmaCubeEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.mob.SlimeEntity
import java.util.Date

object RareMobHighlight {
    @JvmField
    val highlightedEntities = mutableMapOf<Int, Int>()
    private val modScope = CoroutineScope(Dispatchers.Default)

    private val extraEntities = listOf("Wiki Tiki Laser Totem", "Jawbus Follower");
    private var lastWorldChange: Date? = null

    fun init() {
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChange)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(ArmorStandLoadedEvent::class, ::onArmorStandLoaded)
    }

    fun clearHighlightedEntities() {
        if (WorldRendering.highlightSeaCreatures || highlightedEntities.isEmpty()) return
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        val world = FeeshMod.mc.world ?: return

        highlightedEntities.forEach { (id, _) ->
            world.getEntityById(id)?.isGlowing = false
        }
        highlightedEntities.clear()
    }

    private fun onArmorStandLoaded(event: ArmorStandLoadedEvent) {
        if (!WorldRendering.highlightSeaCreatures || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        if (lastWorldChange != null && Date().time - lastWorldChange!!.time < 2000) return // Skip processing entities right after world loaded

        val entity = event.entity

        modScope.launch {
            delay(250)
            MinecraftClient.getInstance().execute {
                val mobNames = SeaCreatures.rareSeaCreatures.map { it.name } + extraEntities
                val cleanName = EntityUtils.parseSeaCreatureNametag(entity, mobNames)?.baseMobName ?: return@execute
                val rareScInfo = SeaCreatures.rareSeaCreatures.find { it.name == cleanName }

                if (rareScInfo == null && cleanName !in extraEntities) return@execute

                val shift = when {
                    cleanName.contains("Reindrake") -> 8 // Ender Dragon entity shifted from its armor stand
                    cleanName.contains("Titanoboa") -> 43 // It consists of chain of mixed slimes and armor stands, and zombie on 45th position
                    else -> 1
                }

                var mobEntity = entity.entityWorld.getEntityById(entity.id - shift) as? LivingEntity ?: return@execute
                if (cleanName.contains("Jawbus Follower") && mobEntity is SlimeEntity && mobEntity !is MagmaCubeEntity) { // Fire Eel
                    mobEntity = entity.entityWorld.getEntityById(entity.id - 11) as? LivingEntity ?: return@execute // -1 is for tail, we want to find Fire Eel's head
                }

                if (!mobEntity.isAlive) return@execute
                if (mobEntity is PlayerEntity && (mobEntity.uuid.version() == 4 || mobEntity.uuid.version() == 1)) return@execute // Some creatures are player entities, e.g. Alligator or Abyssal Miner
       
                val color = when {
                    rareScInfo?.rarityColorCode == ColorCodes.COMMON.code -> 0xFFFFFF
                    rareScInfo?.rarityColorCode == ColorCodes.UNCOMMON.code -> 0x55FF55
                    rareScInfo?.rarityColorCode == ColorCodes.RARE.code -> 0x8AA1FF
                    rareScInfo?.rarityColorCode == ColorCodes.EPIC.code -> 0xAA00AA
                    rareScInfo?.rarityColorCode == ColorCodes.LEGENDARY.code -> 0xFFAA00
                    rareScInfo?.rarityColorCode == ColorCodes.MYTHIC.code -> 0xFF55FF
                    rareScInfo?.rarityColorCode == ColorCodes.DIVINE.code -> 0x55FFFF
                    rareScInfo?.rarityColorCode == ColorCodes.SPECIAL.code -> 0xFF5555
                    cleanName in extraEntities -> 0xF01616
                    else -> 0x00FFFF
                }

                applyGlow(mobEntity, color)

                if (mobEntity.vehicle != null && mobEntity.vehicle is LivingEntity) { // The Loch Emperor's guardian, etc
                    applyGlow(mobEntity.vehicle as LivingEntity, color)
                }

                if (mobEntity.firstPassenger != null && mobEntity.firstPassenger is LivingEntity) { // Ragnarok's rider
                    applyGlow(mobEntity.firstPassenger as LivingEntity, color)
                }
            }
        }
    }

    private fun onClientTick(event: ClientTickEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        val world = event.mc.world ?: return

        if (highlightedEntities.isNotEmpty()) {
            highlightedEntities.keys.removeIf { id ->
                val entity = world.getEntityById(id)
                entity == null || !entity.isAlive
            }
        }
    }

    private fun applyGlow(target: LivingEntity, color: Int) {
        highlightedEntities[target.id] = color
    }

    private fun onWorldChange(event: WorldChangedEvent) {
        lastWorldChange = Date()
        if (highlightedEntities.isNotEmpty()) highlightedEntities.clear()
    }
}
