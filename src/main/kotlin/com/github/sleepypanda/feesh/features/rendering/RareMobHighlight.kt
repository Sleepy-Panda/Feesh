package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ArmorStandDetailsLoadedEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.settings.models.HighlightableSeaCreatureTypes
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes
import kotlin.jvm.JvmField
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.monster.MagmaCube
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.monster.Slime

object RareMobHighlight {
    @JvmField
    val highlightedEntities = mutableMapOf<Int, Int>()

    fun init() {
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChange)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(ArmorStandDetailsLoadedEvent::class, ::onArmorStandDetailsLoaded)
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

    private fun onArmorStandDetailsLoaded(event: ArmorStandDetailsLoadedEvent) {
        if (!WorldRendering.highlightSeaCreatures || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

        val world = WorldUtils.getWorldName() ?: return
        val entity = event.entity
        val enabledScNamesBySetting = WorldRendering.highlightSeaCreaturesList.map { it.displayName }

        val cleanName = EntityUtils.parseSeaCreatureNametag(
            entity,
            enabledScNamesBySetting
        )?.baseMobName ?: return
        if (!enabledScNamesBySetting.contains(cleanName)) return

        val scInfo = SeaCreatures.allSeaCreatures.find { it.name == cleanName }

        val mobEntityShift = when (cleanName) {
            HighlightableSeaCreatureTypes.REINDRAKE.displayName -> 8 // Ender Dragon entity shifted from its armor stand
            HighlightableSeaCreatureTypes.TITANOBOA.displayName -> 43 // It consists of chain of mixed slimes and armor stands, and zombie on 45th position
            else -> 1
        }

        val entities: MutableList<LivingEntity> = mutableListOf()

        var mobEntity = entity.level().getEntity(entity.id - mobEntityShift) as? LivingEntity ?: return
        
        if (cleanName == HighlightableSeaCreatureTypes.JAWBUS_FOLLOWER.displayName && mobEntity is Slime && mobEntity !is MagmaCube) { // Fire Eel
            mobEntity = entity.level().getEntity(entity.id - 11) as? LivingEntity ?: return // -1 is for tail, we want to find Fire Eel's head
        }

        if (!mobEntity.isAlive) return
        if (mobEntity is Player && (mobEntity.uuid.version() == 4 || mobEntity.uuid.version() == 1)) return // Some creatures are player entities, e.g. Alligator or Abyssal Miner

        entities.add(mobEntity)

        val color = when {
            scInfo?.rarityColorCode == ColorCodes.COMMON.code -> 0xFFFFFF
            scInfo?.rarityColorCode == ColorCodes.UNCOMMON.code -> 0x55FF55
            scInfo?.rarityColorCode == ColorCodes.RARE.code -> 0x8AA1FF
            scInfo?.rarityColorCode == ColorCodes.EPIC.code -> 0xAA00AA
            scInfo?.rarityColorCode == ColorCodes.LEGENDARY.code -> 0xFFAA00
            scInfo?.rarityColorCode == ColorCodes.MYTHIC.code -> 0xFF55FF
            scInfo?.rarityColorCode == ColorCodes.DIVINE.code -> 0x55FFFF
            scInfo?.rarityColorCode == ColorCodes.SPECIAL.code -> 0xFF5555
            cleanName == HighlightableSeaCreatureTypes.JAWBUS_FOLLOWER.displayName || cleanName == HighlightableSeaCreatureTypes.WIKI_TIKI_LASER_TOTEM.displayName -> 0xF01616
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
                val prevEntity = entity.level().getEntity(entity.id - shift) as? LivingEntity ?: return@forEach
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

    private fun applyGlow(target: LivingEntity, color: Int) {
        highlightedEntities[target.id] = color
    }

    private fun onWorldChange(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        if (highlightedEntities.isNotEmpty()) highlightedEntities.clear()
    }
}
