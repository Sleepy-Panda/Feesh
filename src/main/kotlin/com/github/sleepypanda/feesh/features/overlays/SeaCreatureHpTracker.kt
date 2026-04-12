package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.EntityUtils.SeaCreatureParsedNametagInfo
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.entity.passive.SnifferEntity
import net.minecraft.entity.mob.SkeletonHorseEntity
import java.util.Date
import kotlin.math.ceil
import kotlin.math.floor

data class TrackedMobInfo(
    val baseMobName: String,
    val worlds: List<String>,
    val hasImmunity: Boolean = false
)

data class MobDisplayInfo(
    val nametag: String,
    val baseMobName: String,
    val isImmune: Boolean,
    val immunitySecondsLeft: Int
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
        TrackedMobInfo("Nessie", listOf(WorldUtils.GALATEA), hasImmunity = true),
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
    private val customEntityImmunityStarts = mutableMapOf<Int, Long>()

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
        .setApplyCustomStyleKey { Overlays.seaCreaturesHpCustomStyle }
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
        customEntityImmunityStarts.clear()
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
        cleanupOutdatedCustomTimerIds();
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

    private fun cleanupOutdatedCustomTimerIds() {
        if (customEntityImmunityStarts.isEmpty()) return

        val now = Date().time
        val expiredIds = customEntityImmunityStarts.filter { (_, timestamp) ->
            now - timestamp > EXPIRATION_TIME_MS
        }.keys

        expiredIds.forEach { id ->
            customEntityImmunityStarts.remove(id)
        }
    }

    private fun isExpired(timestamp: Long): Boolean {
        val now = Date().time
        return now - timestamp > EXPIRATION_TIME_MS
    }

    private fun trackSeaCreaturesHp() {
        CommonUtils.runWithCatching("Failed to track nearby sea creatures HP") {
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
                    var immunitySecondsLeft: Int = 0
                    val entityId = sc.mcEntityId - 1
                    val now = Date().time

                    if (hasImmunity) {
                        val mobEntity = EntityUtils.getMcEntityById(sc.mcEntityId - 1)
                        val ticksExisted = mobEntity?.age ?: 0
                        val seenTimestamp = seenMobEntityIds[sc.mcEntityId - 1] ?: 0L
                        val now = Date().time
                        isImmune = ticksExisted <= IMMUNITY_TICKS && (now - seenTimestamp) <= IMMUNITY_MS
                        immunitySecondsLeft = if (isImmune) {
                            ceil((IMMUNITY_TICKS - ticksExisted) / 20.0).toInt().coerceAtLeast(1)
                        } else 0
                    }

                    if (sc.baseMobName == "Nessie") {
                        val isNessieRunningAway = isNessieRunningAway(sc)
                        if (isNessieRunningAway) {
                            isImmune = true
                            immunitySecondsLeft = 0
                        }
                    }

                    if (sc.baseMobName == "Ragnarok") {
                        val mobEntity = EntityUtils.getMcEntityById(entityId)
                        val isOnGround = (mobEntity as SkeletonHorseEntity)?.isOnGround ?: false
                        val isAngry = (mobEntity as SkeletonHorseEntity)?.isAngry ?: false
                        val pitch = (mobEntity as SkeletonHorseEntity)?.pitch ?: 0

                        FeeshMod.LOGGER.info("Ragnarok ${sc.currentHpNumber.toString()}/${sc.maxHpNumber} pitch=$pitch, isImmobile=${(mobEntity as SkeletonHorseEntity)?.isImmobile}, isOnGround=$isOnGround, isAngry=$isAngry")
                        if (sc.currentHpNumber <= (sc.maxHpNumber * 0.5) && customEntityImmunityStarts[entityId] == null) {
                            customEntityImmunityStarts[entityId] = now
                        }

                        // I found no SkeletonHorseEntity attributes we can rely on, to detect immunity period.
                        // Attributes like isImmobile, isAngry or isOnGround might be used, but they do not work when Ragnarok is jumping during the immunity phase.
                        // Sometimes Ragnarok has isImmobile = true and others outside of immunity period, e.g. while jumping.
                        // So in the first version we just rely on hardcoded 12 seconds timer.

                        // TODO: If I came up late I can see it as immune even though its already not
                        // TODO Reset immunity if HP decreased
                        if (customEntityImmunityStarts[entityId] != null) {
                            val immunityDurationSeconds = 12
                            val elapsedSeconds = floor((now - customEntityImmunityStarts[entityId]!!) / 1000.0).toInt()
                            isImmune = elapsedSeconds <= immunityDurationSeconds
                            immunitySecondsLeft = if (isImmune) (immunityDurationSeconds - elapsedSeconds).coerceAtLeast(1) else 0
                        }
                    }

                    MobDisplayInfo(
                        nametag = sc.shortNametag,
                        baseMobName = sc.baseMobName,
                        isImmune = isImmune,
                        immunitySecondsLeft = immunitySecondsLeft
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
        }
    }

    // Track seen mobs, to not announce them again and to not show them as immune
    private fun trackSeenEntityIds(seaCreatures: List<SeaCreatureParsedNametagInfo>) {
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
            val immunityTimerText = if (mob.immunitySecondsLeft > 0) " ${WHITE}${mob.immunitySecondsLeft}s" else ""
            val immunityText = if (mob.isImmune) " ${RED}${BOLD}[Immune${immunityTimerText}${RED}${BOLD}]" else ""
            lines.add("${mob.nametag}$immunityText")
        }

        gui.setLines(lines)
    }

    private fun getSeaCreaturesInRange(includedSeaCreatureNames: List<String>, distance: Double): List<SeaCreatureParsedNametagInfo> {
        val player = FeeshMod.mc.player ?: return emptyList()
        val world = FeeshMod.mc.world ?: return emptyList()

        val entities = world.entities.filterIsInstance<ArmorStandEntity>()

        return entities
            .filter { entity ->
                EntityUtils.getDistance(player, entity) <= distance || entity.customName?.string?.contains("Reindrake") == true
            }
            .mapNotNull { entity ->
                EntityUtils.parseSeaCreatureNametag(entity, includedSeaCreatureNames)
            }
            .filter { seaCreatureInfo ->
                includedSeaCreatureNames.contains(seaCreatureInfo.baseMobName)
            }
    }

    private fun isNessieRunningAway(sc: SeaCreatureParsedNametagInfo): Boolean {
        val nessieEntityId = sc.mcEntityId - 1
        val mobEntity = EntityUtils.getMcEntityById(nessieEntityId) ?: return false

        val scale = (mobEntity as SnifferEntity).scale

        if (scale != 2.0f && sc.currentHpNumber == sc.maxHpNumber * 0.5) {
            return true
        }

        return false
    }
}
