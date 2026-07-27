package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ArmorStandCustomNameChangedEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.settings.models.HighlightableSeaCreatureTypes
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes
import com.github.sleepypanda.feesh.utils.enums.HexColorCodes
import kotlin.jvm.JvmField
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Display.ItemDisplay
//#if MC >= 26.2
//$$ import net.minecraft.world.entity.monster.cubemob.MagmaCube
//$$ import net.minecraft.world.entity.monster.cubemob.Slime
//#else
import net.minecraft.world.entity.monster.MagmaCube
import net.minecraft.world.entity.monster.Slime
//#endif
import net.minecraft.world.entity.player.Player

object RareMobHighlight {
    @JvmField
    val highlightedEntities = mutableMapOf<Int, Int>()

    private var enabledMobTypes = listOf<String>()

    fun init() {
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChange)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(ArmorStandCustomNameChangedEvent::class, ::onArmorStandCustomNameChanged)
        updateEnabledMobTypes()
    }

    fun clearHighlightedEntities() {
        if (WorldRendering.highlightSeaCreatures || highlightedEntities.isEmpty()) return
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

        val world = FeeshMod.mc.level ?: return

        highlightedEntities.forEach { (id, _) ->
            world.getEntity(id)?.setGlowingTag(false)
        }
        highlightedEntities.clear()
    }

    fun updateEnabledMobTypes() {
        enabledMobTypes = WorldRendering.highlightSeaCreaturesList.map { it.displayName }.distinct().toList()
    }

    private fun onArmorStandCustomNameChanged(event: ArmorStandCustomNameChangedEvent) {
        if (!event.isFirstLoaded) return
        if (!WorldRendering.highlightSeaCreatures || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

        val world = FeeshMod.mc.level ?: return
        val cleanName = EntityUtils.parseSeaCreatureNametag(
            entityId = event.entityId,
            customNameFormatted = event.customName.formatted,
            customNameUnformatted = event.customName.unformatted,
            x = event.position.x,
            y = event.position.y,
            z = event.position.z,
            includedSeaCreatureNames = enabledMobTypes,
        )?.baseMobName ?: return
        if (!enabledMobTypes.contains(cleanName)) return

        val scInfo = SeaCreatures.allSeaCreatures.find { it.name == cleanName }

        val mobEntityShift = when (cleanName) {
            HighlightableSeaCreatureTypes.WEREWOLF.displayName -> 2 // Player entity shifted from its armor stand
            HighlightableSeaCreatureTypes.FIRE_EEL.displayName -> 11 // Find head instead of tail
            HighlightableSeaCreatureTypes.DROWNED_CAPTAIN.displayName -> 6 // Drowned entity shifted from its armor stand
            HighlightableSeaCreatureTypes.PUDDLE_JUMPER.displayName -> 2 // Frog entity shifted from its armor stand
            HighlightableSeaCreatureTypes.REINDRAKE.displayName -> 8 // Ender Dragon entity shifted from its armor stand
            HighlightableSeaCreatureTypes.TITANOBOA.displayName -> 43 // It consists of chain of mixed slimes and armor stands, and zombie on 45th position
            else -> 1
        }

        val entities: MutableList<Entity> = mutableListOf()

        // Volcanic Snail and Jumpin Jack are ItemDisplay entities instead of LivingEntity
        val potentialMobEntity = world.getEntity(event.entityId - mobEntityShift)
        var mobEntity = if (potentialMobEntity is LivingEntity || potentialMobEntity is ItemDisplay) potentialMobEntity else return

        if (cleanName == HighlightableSeaCreatureTypes.JAWBUS_FOLLOWER.displayName && mobEntity is Slime && mobEntity !is MagmaCube) { // Fire Eel
            mobEntity = world.getEntity(event.entityId - 11) as? LivingEntity ?: return // -1 is for tail, we want to find Fire Eel's head
        }

        if (mobEntity is LivingEntity && !mobEntity.isAlive) return
        if (mobEntity is Player && (mobEntity.uuid.version() == 4 || mobEntity.uuid.version() == 1)) return // Some creatures are player entities, e.g. Alligator or Abyssal Miner

        entities.add(mobEntity)

        val color = when {
            scInfo?.rarityColorCode == ColorCodes.COMMON.code -> HexColorCodes.COMMON.colorCode
            scInfo?.rarityColorCode == ColorCodes.UNCOMMON.code -> HexColorCodes.UNCOMMON.colorCode
            scInfo?.rarityColorCode == ColorCodes.RARE.code -> HexColorCodes.RARE.colorCode
            scInfo?.rarityColorCode == ColorCodes.EPIC.code -> HexColorCodes.EPIC.colorCode
            scInfo?.rarityColorCode == ColorCodes.LEGENDARY.code -> HexColorCodes.LEGENDARY.colorCode
            scInfo?.rarityColorCode == ColorCodes.MYTHIC.code -> HexColorCodes.MYTHIC.colorCode
            scInfo?.rarityColorCode == ColorCodes.DIVINE.code -> HexColorCodes.DIVINE.colorCode
            scInfo?.rarityColorCode == ColorCodes.SPECIAL.code -> HexColorCodes.SPECIAL.colorCode
            cleanName == HighlightableSeaCreatureTypes.FLIPFLOPPER.displayName || cleanName == HighlightableSeaCreatureTypes.SEASHINE.displayName ->
                HexColorCodes.DIVINE.colorCode
            cleanName == HighlightableSeaCreatureTypes.JAWBUS_FOLLOWER.displayName || cleanName == HighlightableSeaCreatureTypes.WIKI_TIKI_LASER_TOTEM.displayName ->
                HexColorCodes.SPECIAL.colorCode
            else -> 0x00FFFF
        }

        // The Loch Emperor's guardian, etc
        if (mobEntity.vehicle is LivingEntity) {
            entities.add(mobEntity.vehicle as LivingEntity)
        }

        // Ragnarok's rider
        if (mobEntity.firstPassenger is LivingEntity) {
            entities.add(mobEntity.firstPassenger as LivingEntity)
        }

        // Wiki Tiki is a special case, it consists of 4 entities and I want them all highlighted
        if (cleanName == HighlightableSeaCreatureTypes.WIKI_TIKI.displayName) {
            val wikiTikiEntitiesShifts = listOf(3, 5, 7)
            wikiTikiEntitiesShifts.forEach { shift ->
                val prevEntity = world.getEntity(event.entityId - shift) as? LivingEntity ?: return@forEach
                entities.add(prevEntity)
            }
        }

        entities.forEach { glowTarget ->
            applyGlow(glowTarget, color)
        }
    }

    private fun onClientTick(event: ClientTickEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        val world = event.mc.level ?: return

        if (highlightedEntities.isNotEmpty()) {
            highlightedEntities.keys.removeIf { id ->
                world.getEntity(id) == null
            }
        }
    }

    private fun applyGlow(target: Entity, color: Int) {
        highlightedEntities[target.id] = color
    }

    private fun onWorldChange(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        if (highlightedEntities.isNotEmpty()) highlightedEntities.clear()
    }
}
