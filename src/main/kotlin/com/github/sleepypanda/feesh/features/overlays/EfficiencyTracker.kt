package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.FishingXpGainedEvent
import com.github.sleepypanda.feesh.events.models.InteractActionType
import com.github.sleepypanda.feesh.events.models.OwnFishingHookDespawnedEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.PlayerInteractEvent
import com.github.sleepypanda.feesh.events.models.SeaCreatureCocoonedByYouEvent
import com.github.sleepypanda.feesh.events.models.SoundPlayedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.models.EfficiencyStatTypes
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.gui.GuiButton
import com.github.sleepypanda.feesh.utils.gui.Table
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.features.overlays.base.IResettableTracker
import java.util.Date

object EfficiencyTracker : IResettableTracker {
    const val RESET_COMMAND = "feeshResetEfficiencyTracker"
    const val PAUSE_COMMAND = "feeshPauseEfficiencyTracker"

    override val trackerName = "Efficiency tracker"
    override val resetCommand = RESET_COMMAND

    private const val TICKS_PER_UPDATE = 20
    private const val HIDE_OVERLAY_MINUTES = 5
    private const val CATCH_XP_ORB_SOUND_PATH = "entity.experience_orb.pickup"
    // ♪ MUSICAL CATCH! You caught a Music Disc - Cat!
    private val TREASURE_OR_JUNK_CATCH_PATTERN = Regex("^. (GOOD|GOOD JUNK|GREAT|GREAT JUNK|OUTSTANDING|OUTSTANDING JUNK|MUSICAL) CATCH!")
    private val TROPHY_CATCH_PATTERN = Regex("^. (TROPHY FISH|TROPHY FROG)!")

    private var catchesCount = 0
    private var seaCreatureCatchesCount = 0
    private var seaCreatureCountWithDh = 0
    private var seaCreatureCountWithDhAndBs = 0
    private var fishingXpTotal = 0.0
    private var elapsedSeconds = 0

    private var isSessionActive = false
    private var lastRodRightClickedAt: Date? = null
    private var lastCatchAt: Date? = null
    private var lastCatchXpOrbSoundAt: Date? = null

    private var tickCounter = 0
    private val baseTitle = "${AQUA}${BOLD}${trackerName}"

    private val gui = FeeshGui()
        .setCoordsDataKey("efficiencyTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${GRAY}Catches/h: ${WHITE}500 ${GRAY}(${WHITE}1000 ${GRAY}total)",
            "${GRAY}SC/h: ${WHITE}600 ${GRAY}(${WHITE}1200 ${GRAY}total)",
            "${GRAY}XP/h: ${WHITE}1.5M ${GRAY}(${WHITE}750k ${GRAY}total)",
            "",
            "${AQUA}Elapsed time: ${WHITE}30m",
        ))
        .setSettingsKey { Overlays.efficiencyTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.efficiencyTrackerCustomStyle }
        .setCondition {
            WorldUtils.isInFishingWorld() &&
            FishingHookUtils.wasFishingHookSubmergedMinutesAgo(HIDE_OVERLAY_MINUTES) &&
            hasVisibleStatLines()
        }

    fun init() {
        registerResetCommand()
        RegisterUtils.command(PAUSE_COMMAND) {
            pause()
        }
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreatureCaught)
        EventBus.subscribe(SeaCreatureCocoonedByYouEvent::class, ::onSeaCreatureCocooned)
        EventBus.subscribe(OwnFishingHookDespawnedEvent::class, ::onOwnFishingHookDespawned)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(PlayerInteractEvent::class, ::onPlayerInteract)
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(SoundPlayedEvent::class, ::onSoundPlayed)
        EventBus.subscribe(FishingXpGainedEvent::class, ::onFishingXpGained)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    override fun hasData(): Boolean {
        return catchesCount > 0 || seaCreatureCatchesCount > 0 || seaCreatureCountWithDh > 0 || seaCreatureCountWithDhAndBs > 0 || fishingXpTotal > 0 || elapsedSeconds > 0
    }

    override fun resetData(force: Boolean) {
        catchesCount = 0
        seaCreatureCatchesCount = 0
        seaCreatureCountWithDh = 0
        seaCreatureCountWithDhAndBs = 0
        fishingXpTotal = 0.0
        isSessionActive = false
        elapsedSeconds = 0

        lastRodRightClickedAt = null
        lastCatchAt = null
        lastCatchXpOrbSoundAt = null
    }

    override fun refreshGui() {
        updateGuiLines()
    }

    fun pause() {
        CommonUtils.runWithCatching("Failed to pause $trackerName") {
            if (!Overlays.efficiencyTrackerOverlay || !WorldUtils.isInSkyblock() || !isSessionActive) return

            pauseInternal()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}$trackerName is paused. Continue fishing to resume it.", true)
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        pauseInternal()
        lastRodRightClickedAt = null
        lastCatchAt = null
        lastCatchXpOrbSoundAt = null
        gui.clearLines()
    }

    // This function is a backup method for catches that do not have chat message (sea creatures, treasure, trophy, etc.)
    // Chat message appears before fishing hook despawns, so those catches are tracked in chat message handlers.
    // Some successful catches (raw fish, log, etc) do not have chat message, so we detect them by
    // - reel in fishing rod with right click while it's in fluid
    // - hear XP orb sound
    private fun onOwnFishingHookDespawned(@Suppress("UNUSED_PARAMETER") event: OwnFishingHookDespawnedEvent) {
        CommonUtils.runWithCatching("Failed to track fishing hook despawned in $trackerName") {
            if (!isTrackerActive()) return
            if (!isStatEnabled(EfficiencyStatTypes.CATCHES_PER_HOUR)) return
            if (isSomethingCaughtRecently()) return

            if (!wasWithinMs(lastRodRightClickedAt, 500L)) return
            if (!FishingHookUtils.wasFishingHookSubmergedMillisecondsAgo(500)) return
            if (!wasWithinMs(lastCatchXpOrbSoundAt, 300L)) return

            addCatch()
        }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        CommonUtils.runWithCatching("Failed to handle tick in $trackerName") {
            tickCounter++
            if (tickCounter < TICKS_PER_UPDATE) return
            tickCounter = 0

            refreshElapsedTimeOrPause() // Once per second!
            updateGuiLines()
        }
    }

    private fun onSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        CommonUtils.runWithCatching("Failed to track sea creature catch in $trackerName") {
            if (event.seaCreatureName == "Vanquisher") return
            if (!isTrackerActive()) return

            tryAddCatchFromChat() // For catches/h stat

            if (!hasAnySeaCreatureStatEnabled()) return

            val isDoubleHooked = event.isDoubleHook
            val scCount = if (isDoubleHooked) 2 else 1

            seaCreatureCatchesCount += 1
            seaCreatureCountWithDh += scCount
            seaCreatureCountWithDhAndBs += scCount
            updateGuiLines()
        }
    }

    private fun onSeaCreatureCocooned(event: SeaCreatureCocoonedByYouEvent) {
        CommonUtils.runWithCatching("Failed to track cocooned sea creature in $trackerName") {
            if (event.seaCreatureName == "Vanquisher") return
            if (!isTrackerVisible()) return
            if (!hasAnySeaCreatureStatEnabled()) return

            seaCreatureCountWithDhAndBs += 1
            updateGuiLines()
        }
    }

    private fun onPlayerInteract(event: PlayerInteractEvent) {
        CommonUtils.runWithCatching("Failed to handle fishing rod interaction in $trackerName") {
            if (!isTrackerActive()) return
            if (!isStatEnabled(EfficiencyStatTypes.CATCHES_PER_HOUR)) return
            if (!event.isMainHand || (event.actionType != InteractActionType.USE_ITEM && event.actionType != InteractActionType.USE_BLOCK)) return
            if (FishingHookUtils.getActiveFishingHook() == null) return

            val heldItem = FeeshMod.mc.player?.mainHandItem
            if (heldItem == null || heldItem.isEmpty) return
            if (!ItemUtils.isFishingRod(heldItem)) return

            lastRodRightClickedAt = Date()
        }
    }

    // Note: Treasure catch might happen together with SC catch if Precursor Drone pet equipped.
    // No double catches/h increase should happen in this case.
    private fun onChat(event: ChatEvent) {
        CommonUtils.runWithCatching("Failed to track catch chat message in $trackerName") {
            if (TREASURE_OR_JUNK_CATCH_PATTERN.containsMatchIn(event.unformattedText) ||
                TROPHY_CATCH_PATTERN.containsMatchIn(event.unformattedText)
            ) {
                tryAddCatchFromChat()
            }
        }
    }

    // Trash catches like raw fish usually play XP orb sound (volume 0.5 + 0.1 or just 0.1).
    // Used as backup signal on bobber despawn when there is no catch chat.
    private fun onSoundPlayed(event: SoundPlayedEvent) {
        CommonUtils.runWithCatching("Failed to track catch XP orb sound in $trackerName") {
            if (!isTrackerActive()) return
            if (!isStatEnabled(EfficiencyStatTypes.CATCHES_PER_HOUR)) return
            if (event.soundId.path != CATCH_XP_ORB_SOUND_PATH) return
            if (event.volume != 0.1f && event.volume != 0.5f) return

            val player = FeeshMod.mc.player ?: return
            val distanceSqr = EntityUtils.getDistanceSqr(player.x, player.y, player.z, event.x, event.y, event.z)
            if (distanceSqr > 1.0) return // Usually sounds played with distance (non-sqr) 0.1 and 0.7

            lastCatchXpOrbSoundAt = Date()
        }
    }

    private fun onFishingXpGained(event: FishingXpGainedEvent) {
        CommonUtils.runWithCatching("Failed to track Fishing XP gain in $trackerName") {
            if (!isTrackerActive()) return
            if (!isStatEnabled(EfficiencyStatTypes.XP_PER_HOUR)) return

            fishingXpTotal += event.amount
            updateGuiLines()
        }
    }

    private fun tryAddCatchFromChat() {
        if (!isStatEnabled(EfficiencyStatTypes.CATCHES_PER_HOUR)) return
        if (isSomethingCaughtRecently()) return
        addCatch()
    }

    private fun addCatch() {
        catchesCount += 1
        lastCatchAt = Date()
        updateGuiLines()
    }

    private fun isSomethingCaughtRecently(): Boolean {
        return wasWithinMs(lastCatchAt, 750L) // Flash V proc might be ~1s
    }

    private fun wasWithinMs(timestamp: Date?, windowMs: Long): Boolean {
        if (timestamp == null) return false
        return Date().time - timestamp.time <= windowMs
    }

    private fun isTrackerEnabledInWorld(): Boolean {
        if (!Overlays.efficiencyTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return false
        return true
    }

    private fun isTrackerVisible(): Boolean {
        if (!isTrackerEnabledInWorld()) return false
        if (!FishingHookUtils.wasFishingHookSubmergedMinutesAgo(HIDE_OVERLAY_MINUTES)) return false
        return true
    }

    private fun isTrackerActive(): Boolean {
        return isTrackerVisible() && isSessionActive
    }

    private fun isStatEnabled(stat: EfficiencyStatTypes): Boolean {
        if (!Overlays.efficiencyTrackerOverlay || !Overlays.efficiencyTrackerStats.contains(stat)) return false
        if (stat != EfficiencyStatTypes.CATCHES_PER_HOUR && PlayerUtils.isInTrophyArmor()) return false
        return true
    }

    private fun hasVisibleStatLines(): Boolean {
        return EfficiencyStatTypes.entries.any { isStatEnabled(it) }
    }

    private fun hasAnySeaCreatureStatEnabled(): Boolean {
        return (isStatEnabled(EfficiencyStatTypes.SC_CATCHES_PER_HOUR) ||
                isStatEnabled(EfficiencyStatTypes.SC_PER_HOUR) ||
                isStatEnabled(EfficiencyStatTypes.SC_PER_HOUR_WITH_BS)
        )
    }

    private fun refreshElapsedTimeOrPause() {
        CommonUtils.runWithCatching("Failed to refresh elapsed time in $trackerName") {
            if (!isTrackerVisible() || !hasVisibleStatLines()) {
                pauseInternal()
                return
            }

            val isHookActive = FishingHookUtils.isFishingHookSubmerged()

            // Start fishing timer after pause or when tracker was empty
            if (isHookActive) {
                isSessionActive = true
            }

            if (!isTrackerActive()) return

            val lastHookSeenAt = FishingHookUtils.lastSubmergedFishingHookSeenAt() ?: return
            val elapsedSinceHook = (Date().time - lastHookSeenAt.time) / 1000

            if (elapsedSinceHook < Overlays.trackersAutoPauseSeconds) {
                elapsedSeconds += 1
            } else {
                pause()
            }
        }
    }

    private fun pauseInternal() {
        isSessionActive = false
    }

    private fun getTotalForStat(stat: EfficiencyStatTypes): Double {
        return when (stat) {
            EfficiencyStatTypes.CATCHES_PER_HOUR -> catchesCount.toDouble()
            EfficiencyStatTypes.SC_CATCHES_PER_HOUR -> seaCreatureCatchesCount.toDouble()
            EfficiencyStatTypes.SC_PER_HOUR -> seaCreatureCountWithDh.toDouble()
            EfficiencyStatTypes.SC_PER_HOUR_WITH_BS -> seaCreatureCountWithDhAndBs.toDouble()
            EfficiencyStatTypes.XP_PER_HOUR -> fishingXpTotal
        }
    }

    private fun calculatePerHour(total: Double): Double {
        val elapsedHours = elapsedSeconds / 3600.0
        return if (elapsedHours > 0) total / elapsedHours else 0.0
    }

    private data class StatLineColumns(val perHour: String, val total: String) {
        fun toCells(): List<String> = listOf(perHour, total)
    }

    private fun getColumnsSeparator(): String = " ${DARK_GRAY}| "

    private fun formatStatNumber(stat: EfficiencyStatTypes, value: Double): String {
        if (stat == EfficiencyStatTypes.XP_PER_HOUR) {
            return CommonUtils.toShortNumber(value) ?: "0"
        }
        return CommonUtils.formatNumberWithSpaces(value.toInt())
    }

    private fun getStatLineColumns(stat: EfficiencyStatTypes): StatLineColumns {
        val total = getTotalForStat(stat)
        val perHour = calculatePerHour(total)
        val label = when (stat) {
            EfficiencyStatTypes.CATCHES_PER_HOUR -> "Catches/h"
            EfficiencyStatTypes.SC_CATCHES_PER_HOUR -> "SC catches/h"
            EfficiencyStatTypes.SC_PER_HOUR -> "SC/h"
            EfficiencyStatTypes.SC_PER_HOUR_WITH_BS -> "SC/h with BS"
            EfficiencyStatTypes.XP_PER_HOUR -> "XP/h"
        }
        return StatLineColumns(
            perHour = "${GRAY}$label: ${WHITE}${formatStatNumber(stat, perHour)}",
            total = "${WHITE}${formatStatNumber(stat, total)} ${GRAY}total",
        )
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!isTrackerVisible() || !hasData() || !hasVisibleStatLines()) return

        val pausedText = if (isSessionActive) "" else " ${GRAY}[Paused]"
        val lines = mutableListOf<LineInfo>()
        lines.add(LineInfo(baseTitle))

        val statRows = EfficiencyStatTypes.entries
            .filter { isStatEnabled(it) }
            .map { getStatLineColumns(it).toCells() }

        if (statRows.isNotEmpty()) {
            val tableLayout = Table.layout(FeeshMod.mc.font, statRows, getColumnsSeparator())
            statRows.indices.forEach { index ->
                lines.add(
                    LineInfo.withCells(
                        cells = tableLayout.rows[index],
                        tableWidth = tableLayout.tableWidth,
                    )
                )
            }
        }

        lines.add(LineInfo(""))
        lines.add(LineInfo("${AQUA}Elapsed time: ${WHITE}${CommonUtils.formatTimeElapsed(elapsedSeconds)}${pausedText}"))

        gui.setLines(lines)
        gui.setButtons(listOf(
            GuiButton(0, "${GRAY}[${YELLOW}Click to pause${GRAY}]", { pause() }),
            getResetGuiButton(1) { requestReset() }
        ))
    }
}
