package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.models.HpTrackableSeaCreatureTypes
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.EntityUtils.SeaCreatureParsedNametagInfo
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.animal.sniffer.Sniffer
import java.util.Date
import kotlin.math.ceil

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

    private val IMMUNE_MOB_TYPES = setOf(
        HpTrackableSeaCreatureTypes.FIERY_SCUTTLER,
        HpTrackableSeaCreatureTypes.THUNDER,
        HpTrackableSeaCreatureTypes.VANQUISHER,
        HpTrackableSeaCreatureTypes.YETI,
        HpTrackableSeaCreatureTypes.ALLIGATOR,
        HpTrackableSeaCreatureTypes.BLUE_RINGED_OCTOPUS,
        HpTrackableSeaCreatureTypes.WIKI_TIKI,
        HpTrackableSeaCreatureTypes.TITANOBOA,
        HpTrackableSeaCreatureTypes.ABYSSAL_MINER,
        HpTrackableSeaCreatureTypes.THE_LOCH_EMPEROR,
        HpTrackableSeaCreatureTypes.NESSIE,
        HpTrackableSeaCreatureTypes.WATER_HYDRA,
        HpTrackableSeaCreatureTypes.PHANTOM_FISHER,
        HpTrackableSeaCreatureTypes.GRIM_REAPER,
        HpTrackableSeaCreatureTypes.GREAT_WHITE_SHARK,
    )
    private val trackedMobTypeByName = HpTrackableSeaCreatureTypes.values().associateBy { it.displayName }
    private var enabledMobTypes = listOf<String>()

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
        .setApplyCustomStyleKey { Overlays.seaCreaturesHpCustomStyle }
        .setCondition {
            WorldUtils.isInSkyblock()
        }

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        updateEnabledMobTypes()
    }

    fun updateEnabledMobTypes() {
        enabledMobTypes = Overlays.seaCreaturesHpTrackedList.map { it.displayName }.distinct().toList()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        mobs.clear()
        seenMobEntityIds.clear()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        onClientTickOverlay()
        onClientTickCleanup()
    }

    private fun onClientTickOverlay() {
        tickCounter++
        if (tickCounter < TICKS_PER_CHECK ) return
        tickCounter = 0

        if (!Overlays.seaCreaturesHpOverlay ||
            !WorldUtils.isInSkyblock()
        ) {
            gui.clearLines()
            return
        }

        trackSeaCreaturesHp()
        updateGuiLines()
    }

    private fun onClientTickCleanup() {
        cleanupTickCounter++
        if (cleanupTickCounter < CLEANUP_DELAY_TICKS) return
        cleanupTickCounter = 0

        if (!Overlays.seaCreaturesHpOverlay ||
            !WorldUtils.isInSkyblock()
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
        CommonUtils.runWithCatching("Failed to track nearby sea creatures HP") {
            if (!Overlays.seaCreaturesHpOverlay || !WorldUtils.isInSkyblock()) return

            val world = WorldUtils.getWorldName() ?: return
            val knownSeaCreatureByName = SeaCreatures.allSeaCreatures.associateBy { it.name }
            val possibleScNames = enabledMobTypes.filter { enabledName ->
                val knownSeaCreature = knownSeaCreatureByName[enabledName]
                knownSeaCreature == null || knownSeaCreature.worlds.isEmpty() || knownSeaCreature.worlds.contains(world)
            }

            val seaCreatures = getSeaCreaturesInRange(possibleScNames, LOOTSHARE_DISTANCE)
            trackSeenEntityIds(seaCreatures)

            val currentMobs = seaCreatures
                .sortedBy { it.currentHpNumber } // Lowest HP comes first
                .take(Overlays.seaCreaturesHpOverlayMaxCount.coerceIn(1, 20)) // Top N
                .map { sc ->
                    val scType = trackedMobTypeByName[sc.baseMobName]
                    val hasImmunity = scType != null && IMMUNE_MOB_TYPES.contains(scType)
                    var isImmune = false
                    var immunitySecondsLeft = 0

                    if (hasImmunity) {
                        val mobEntity = EntityUtils.getMcEntityById(sc.mcEntityId - 1)
                        val ticksExisted = mobEntity?.tickCount ?: 0
                        val seenTimestamp = seenMobEntityIds[sc.mcEntityId - 1] ?: 0L
                        val now = Date().time
                        isImmune = ticksExisted <= IMMUNITY_TICKS && (now - seenTimestamp) <= IMMUNITY_MS
                        immunitySecondsLeft = if (isImmune) {
                            ceil((IMMUNITY_TICKS - ticksExisted) / 20.0).toInt().coerceAtLeast(1)
                        } else 0
                    }

                    if (sc.baseMobName == HpTrackableSeaCreatureTypes.NESSIE.displayName) {
                        val isNessieRunningAway = isNessieRunningAway(sc)
                        if (isNessieRunningAway) {
                            isImmune = true
                            immunitySecondsLeft = 0
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

            if (currentMobs.size > mobs.size && !addedMobNames.all { it.baseMobName == HpTrackableSeaCreatureTypes.REINDRAKE.displayName }) {
                // Reindrake flies around and goes out of nametags render distance periodically
                SoundUtils.playSound()
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
            mobs.isEmpty()
        ) return

        val lines = mutableListOf<String>()
        mobs.forEach { mob ->
            val immunityTimerText = if (mob.immunitySecondsLeft > 0) " ${WHITE}${mob.immunitySecondsLeft}s" else ""
            val immunityText = if (mob.isImmune) " ${RED}${BOLD}[Immune${immunityTimerText}${RED}${BOLD}]" else ""
            lines.add("${mob.nametag}$immunityText")
        }

        gui.setLines(lines.map { LineInfo(it) })
    }

    private fun getSeaCreaturesInRange(includedSeaCreatureNames: List<String>, distance: Double): List<SeaCreatureParsedNametagInfo> {
        val player = FeeshMod.mc.player ?: return emptyList()
        val world = FeeshMod.mc.level ?: return emptyList()

        val entities = world.entitiesForRendering().filterIsInstance<ArmorStand>()

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

        val scale = (mobEntity as Sniffer).scale

        return scale != 2.0f && sc.currentHpNumber == sc.maxHpNumber * 0.5
    }
}
