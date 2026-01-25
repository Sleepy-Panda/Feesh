package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.ClientTickEvent
import com.github.sleepypanda.feesh.events.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.sound.SoundEvents
import java.util.Date

data class SeaCreatureInfo(
    val mcEntityId: Int,
    val baseMobName: String,
    val shortNametag: String,
    val currentHpNumber: Double,
    val renderPos: Triple<Double, Double, Double>
)

data class TrackedMobInfo(
    val baseMobName: String,
    val worlds: List<String>,
    val hasImmunity: Boolean = false
)

data class MobDisplayInfo(
    val nametag: String,
    val baseMobName: String,
    val isImmune: Boolean
)

object SeaCreatureHpTracker {
    private const val LOOTSHARE_DISTANCE = 30.0
    private const val TICKS_PER_CHECK = 5
    private const val CLEANUP_DELAY_TICKS = 30 * 20
    private const val EXPIRATION_TIME_MS = 6 * 60 * 1000L // 6 minutes
    private const val IMMUNITY_TICKS = 20 * 5 // ~5 seconds
    private const val IMMUNITY_MS = 5000L

    private val TRACKED_MOBS = listOf(
        TrackedMobInfo("Fiery Scuttler", listOf(WorldUtils.CRIMSON_ISLE), hasImmunity = true),
        TrackedMobInfo("Lord Jawbus", listOf(WorldUtils.CRIMSON_ISLE)),
        TrackedMobInfo("Jawbus Follower", listOf(WorldUtils.CRIMSON_ISLE)),
        TrackedMobInfo("Thunder", listOf(WorldUtils.CRIMSON_ISLE), hasImmunity = true),
        TrackedMobInfo("Plhlegblast", listOf(WorldUtils.CRIMSON_ISLE)),
        TrackedMobInfo("Vanquisher", listOf(WorldUtils.CRIMSON_ISLE), hasImmunity = true),
        TrackedMobInfo("Ragnarok", listOf(WorldUtils.CRIMSON_ISLE)),
        TrackedMobInfo("Nutcracker", listOf(WorldUtils.JERRY_WORKSHOP)),
        TrackedMobInfo("Reindrake", listOf(WorldUtils.JERRY_WORKSHOP)),
        TrackedMobInfo("Yeti", listOf(WorldUtils.JERRY_WORKSHOP), hasImmunity = true),
        TrackedMobInfo("Alligator", WorldUtils.WATER_HOTSPOT_WORLDS, hasImmunity = true),
        TrackedMobInfo("Blue Ringed Octopus", WorldUtils.WATER_HOTSPOT_WORLDS, hasImmunity = true),
        TrackedMobInfo("Wiki Tiki", WorldUtils.WATER_HOTSPOT_WORLDS, hasImmunity = true),
        TrackedMobInfo("Wiki Tiki Laser Totem", WorldUtils.WATER_HOTSPOT_WORLDS),
        TrackedMobInfo("Titanoboa", listOf(WorldUtils.BACKWATER_BAYOU), hasImmunity = true),
        TrackedMobInfo("Abyssal Miner", listOf(WorldUtils.CRYSTAL_HOLLOWS), hasImmunity = true),
        TrackedMobInfo("The Loch Emperor", listOf(WorldUtils.GALATEA), hasImmunity = true),
        TrackedMobInfo("Water Hydra", WorldUtils.WATER_FISHING_WORLDS, hasImmunity = true),
        TrackedMobInfo("Phantom Fisher", WorldUtils.WATER_FISHING_WORLDS, hasImmunity = true),
        TrackedMobInfo("Grim Reaper", WorldUtils.WATER_FISHING_WORLDS, hasImmunity = true),
        TrackedMobInfo("Great White Shark", WorldUtils.WATER_FISHING_WORLDS, hasImmunity = true),
        TrackedMobInfo("Carrot King", WorldUtils.WATER_FISHING_WORLDS),
    )

    private val TRACKED_MOB_NAMES = TRACKED_MOBS.map { it.baseMobName }
    private val TRACKED_WORLD_NAMES = TRACKED_MOBS.flatMap { it.worlds }.distinct()

    private var mobs = mutableListOf<MobDisplayInfo>()
    private val seenMobEntityIds = mutableMapOf<Int, Long>()

    private var tickCounter = 0
    private var cleanupTickCounter = 0

    private val gui = FeeshGui()
        .setCoordsDataKey("seaCreaturesHpTracker")
        .setClickable(false)
        .setSampleLines(listOf(
            "${RED}♆${YELLOW}✰${GREEN}☮ ${RED}Jawbus Follower ${GREEN}3M${WHITE}/${GREEN}3M${RED}❤",
            "${RED}♆${GRAY}⚙${LIGHT_PURPLE}♣ ${RED}${BOLD}Lord Jawbus ${GREEN}1M${WHITE}/${GREEN}2M${RED}❤",
        ))
        .setSettingsKey { Overlays.seaCreaturesHpOverlay }
        .setCondition {
            TRACKED_WORLD_NAMES.contains(WorldUtils.getWorldName())
        }

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        mobs.clear()
        seenMobEntityIds.clear()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        onClientTick_Overlay()
        onClientTick_Cleanup()
    }

    private fun onClientTick_Overlay() {
        tickCounter++
        if (tickCounter < TICKS_PER_CHECK ) return
        tickCounter = 0

        if (!Overlays.seaCreaturesHpOverlay ||
            !WorldUtils.isInSkyblock() ||
            !TRACKED_WORLD_NAMES.contains(WorldUtils.getWorldName())
        ) {
            gui.clearLines()
            return
        }

        trackSeaCreaturesHp()
        updateGuiLines()
    }

    private fun onClientTick_Cleanup() {
        cleanupTickCounter++
        if (cleanupTickCounter < CLEANUP_DELAY_TICKS) return
        cleanupTickCounter = 0

        if (!Overlays.seaCreaturesHpOverlay ||
            !WorldUtils.isInSkyblock() ||
            !TRACKED_WORLD_NAMES.contains(WorldUtils.getWorldName())
        ) return

        cleanupOutdatedSeenEntityIds()
    }

    private fun cleanupOutdatedSeenEntityIds() {
        if (seenMobEntityIds.isEmpty()) return

        val now = Date().time
        val expiredIds = seenMobEntityIds.filter { (_, timestamp) ->
            now - timestamp > EXPIRATION_TIME_MS
        }.keys

        expiredIds.forEach { id ->
            seenMobEntityIds.remove(id)
        }
    }

    private fun isExpired(timestamp: Long): Boolean {
        val now = Date().time
        return now - timestamp > EXPIRATION_TIME_MS
    }

    private fun trackSeaCreaturesHp() {
        try {
            if (!Overlays.seaCreaturesHpOverlay ||
                !WorldUtils.isInSkyblock() ||
                !TRACKED_WORLD_NAMES.contains(WorldUtils.getWorldName())
            ) return

            val seaCreatures = getSeaCreaturesInRange(TRACKED_MOB_NAMES, LOOTSHARE_DISTANCE)
            trackSeenEntityIds(seaCreatures)

            val currentMobs = seaCreatures
                .sortedBy { it.currentHpNumber } // Lowest HP comes first
                .take(Overlays.seaCreaturesHpOverlayMaxCount.coerceIn(1, 20)) // Top N
                .map { sc ->
                    val trackedMob = TRACKED_MOBS.find { it.baseMobName == sc.baseMobName }
                    val hasImmunity = trackedMob?.hasImmunity ?: false
                    var isImmune = false

                    if (hasImmunity) {
                        val mobEntity = EntityUtils.getMcEntityById(sc.mcEntityId - 1)
                        val ticksExisted = mobEntity?.age ?: 0
                        val seenTimestamp = seenMobEntityIds[sc.mcEntityId - 1] ?: 0L
                        val now = Date().time
                        isImmune = ticksExisted <= IMMUNITY_TICKS && (now - seenTimestamp) <= IMMUNITY_MS
                    }

                    MobDisplayInfo(
                        nametag = sc.shortNametag,
                        baseMobName = sc.baseMobName,
                        isImmune = isImmune
                    )
                }

            val addedMobNames = currentMobs.filter { cm ->
                !mobs.any { m -> m.baseMobName == cm.baseMobName }
            }

            if (currentMobs.size > mobs.size && !addedMobNames.all { it.baseMobName == "Reindrake" }) {
                // Reindrake flies around and goes out of nametags render distance periodically
                SoundUtils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP)
            }

            mobs = currentMobs.toMutableList()
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to track nearby sea creatures HP", e)
        }
    }

    // Track seen mobs, to not announce them again and to not show them as immune
    private fun trackSeenEntityIds(seaCreatures: List<SeaCreatureInfo>) {
        if (seaCreatures.isEmpty()) return

        val now = Date().time
        seaCreatures.forEach { sc ->
            val id = sc.mcEntityId - 1 // Mob entity ID
            val existingTimestamp = seenMobEntityIds[id]

            if (existingTimestamp != null && isExpired(existingTimestamp)) {
                seenMobEntityIds.remove(id)
                seenMobEntityIds[id] = now
            } else if (existingTimestamp == null) {
                seenMobEntityIds[id] = now
            }
        }
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!Overlays.seaCreaturesHpOverlay ||
            !WorldUtils.isInSkyblock() ||
            !TRACKED_WORLD_NAMES.contains(WorldUtils.getWorldName()) ||
            mobs.isEmpty()
        ) return

        val lines = mutableListOf<String>()
        mobs.forEach { mob ->
            val immunityText = if (mob.isImmune) " ${RED}${BOLD}[Immune]" else ""
            lines.add("${mob.nametag}$immunityText")
        }

        gui.setLines(lines)
    }

    private fun getSeaCreaturesInRange(includedSeaCreatureNames: List<String>, distance: Double): List<SeaCreatureInfo> {
        val player = FeeshMod.mc.player ?: return emptyList()
        val world = FeeshMod.mc.world ?: return emptyList()

        val entities = world.entities.filterIsInstance<ArmorStandEntity>()

        return entities
            .filter { entity ->
                EntityUtils.getDistance(player, entity) <= distance
            }
            .mapNotNull { entity ->
                parseSeaCreatureNametag(entity, includedSeaCreatureNames)
            }
            .filter { seaCreatureInfo ->
                includedSeaCreatureNames.contains(seaCreatureInfo.baseMobName)
            }
    }

    // Original nametag samples:

    // §r§8[§r§7Lv1§r§8] §r§9⚓§r§a☮ §r§cSquid§r §r§a100§r§f/§r§a100§r§c❤
	// §r§8[§r§7Lv1§r§8] §r§9⚓§r§a☮ §r§k§5a§r§5Corrupted Squid§r§k§5a§r §r§a300§r§f/§r§a300§r§c❤
    // §e﴾ §8[§7Lv600§8] §c♆§7⚙§d♣ §c§lLord Jawbus§r§r §a69M§f/§a100M§c❤ §e﴿
    // §e﴾ §8[§7Lv600§8] §c♆§7⚙§d♣ §c§lLord Jawbus§r§r §e6.3M§f/§a100M§c❤ §e﴿ §b✯
    // §8[§7Lv250§8] §c♆§e✰§a☮ §cJawbus Follower§r §a3M§f/§a3M§c❤
	// MC 1.21.5: §r§8[§r§7Lv150§r§8] §r§9⚓§r§f🦴§r§5♃ §r§5§ka§r§5Corrupted The Loch Emperor§r§5§ka§r §r§e521.8k§r§f/§r§a2.4M§r§c❤ §r§b✯
	// MC 1.21.5: §r§8[§r§7Lv14§r§8] §r§2⸙§r§9⚓ §r§5§ka§r§5Corrupted Ent§r§5§ka§r §r§e1§r§f/§r§a75,000§r§c❤
    private fun parseSeaCreatureNametag(entity: ArmorStandEntity, includedSeaCreatureNames: List<String>): SeaCreatureInfo? {
        val customName = entity.customName ?: return null
        val plainName = customName.string.removeFormatting()

        if (plainName.isEmpty() ||
            !plainName.contains("[Lv") ||
            !plainName.contains("]") ||
            !plainName.contains("❤") ||
            !includedSeaCreatureNames.any { plainName.contains(it) }
        ) return null

        val formattedText = customName.getFormattedString()
        var name = formattedText
            .replace("§e﴾ ", "")
            .replace(" §e﴿", "")
            .replace("§5§ka", "")
            .trim()

        val shortName = name.split("] ").getOrNull(1)?.replace("Corrupted ", "") ?: return null
        
        val nameParts = shortName.split(" ")
        val namePartIndex = nameParts.indexOfFirst { it.contains("/") }
        val baseMobNameParts = if (namePartIndex >= 0) {
            nameParts.take(namePartIndex)
        } else {
            nameParts
        }
        
        val baseMobName = baseMobNameParts.joinToString(" ")
            .removeFormatting()
            .replace(Regex("[^a-zA-Z\\s'-]"), "")
            .trim()

        val hpPart = shortName.split("§f/").getOrNull(0) ?: return null
        val currentHp = hpPart.split(" ").lastOrNull() ?: return null
        val currentHpNumber = CommonUtils.parseShortNumber(currentHp.removeFormatting())

        return SeaCreatureInfo(
            mcEntityId = entity.id,
            baseMobName = baseMobName, // "Lord Jawbus" or "Squid"
            shortNametag = shortName, // §c♆§7⚙§d♣ §c§lLord Jawbus§r§r §a69M§f/§a100M§c❤ §b✯
            currentHpNumber = currentHpNumber,
            renderPos = Triple(entity.x, entity.y, entity.z)
        )
    }
}
