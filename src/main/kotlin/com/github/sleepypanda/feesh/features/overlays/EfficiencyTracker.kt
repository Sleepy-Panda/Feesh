package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.SeaCreatureCocoonedByYouEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.models.EfficiencyStatTypes
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.gui.GuiButton
import com.github.sleepypanda.feesh.utils.gui.Table
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.features.overlays.base.IResettableTracker
import java.util.Date

// Tracker starts when fishing hook is submerged, like other overlays
// Tracker is paused when fishing hook is not submerged for 5 minutes
// If in trophy armor, we track only Casts/hour but do not track SC catches/hour, SC/hour (+ DH), and SC/hour (+ DH and BS) - they are hidden
// Tracker is hidden if all its stats disabled in settings or hidden
// How to not count sc related stats if in trophy armor or treasure fishing
// Check dirt rod
// Rework reeled in tracking, maybe check for rod clicks / fishing rod in hand
// Widget reappearing when started fishing after 5 minutes (while its hidden)
  // Check for trophy armor
// Build sample lines depending on enabled stats
// Cocooned counted in Sc tracker but not in paused sc/h

object EfficiencyTracker : IResettableTracker {
    const val RESET_COMMAND = "feeshResetEfficiencyTracker"
    const val PAUSE_COMMAND = "feeshPauseEfficiencyTracker"

    override val trackerName = "Efficiency tracker"
    override val resetCommand = RESET_COMMAND

    private const val TICKS_PER_UPDATE = 20
    private const val HIDE_OVERLAY_MINUTES = 5

    private var castsCount = 0
    private var seaCreatureCatchesCount = 0
    private var seaCreatureCountWithDh = 0
    private var seaCreatureCountWithDhAndBs = 0
    private var isSessionActive = false
    private var elapsedSeconds = 0
    private var lastIsFishingHookSubmerged = false
    private var tickCounter = 0
    private val baseTitle = "${AQUA}${BOLD}Efficiency tracker"

    private val gui = FeeshGui()
        .setCoordsDataKey("efficiencyTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${WHITE}42 ${GRAY}SC catches/h (${WHITE}100 ${GRAY}total)",
            "${WHITE}58 ${GRAY}SC/h (${WHITE}140 ${GRAY}total)",
            "",
            "${AQUA}Elapsed time: ${WHITE}1h 23m 45s",
        ))
        .setSettingsKey { Overlays.efficiencyTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.efficiencyTrackerCustomStyle }
        .setCondition {
            WorldUtils.isInFishingWorld() &&
            FishingHookUtils.wasFishingHookSubmergedMinutesAgo(HIDE_OVERLAY_MINUTES) &&
            !PlayerUtils.isInTrophyArmor()
        }

    fun init() {
        registerResetCommand()
        RegisterUtils.command(PAUSE_COMMAND) {
            pause()
        }
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreatureCaught)
        EventBus.subscribe(SeaCreatureCocoonedByYouEvent::class, ::onSeaCreatureCocooned)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    override fun hasData(): Boolean {
        return castsCount > 0 || seaCreatureCatchesCount > 0 || seaCreatureCountWithDh > 0 || seaCreatureCountWithDhAndBs > 0 || elapsedSeconds > 0
    }

    override fun resetData(force: Boolean) {
        castsCount = 0
        seaCreatureCatchesCount = 0
        seaCreatureCountWithDh = 0
        seaCreatureCountWithDhAndBs = 0
        isSessionActive = false
        elapsedSeconds = 0
        lastIsFishingHookSubmerged = false
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
        gui.clearLines()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        val currentIsFishingHookSubmerged = FishingHookUtils.isFishingHookSubmerged()
        val isHookNotExisting = FishingHookUtils.getActiveFishingHook() == null
        if (lastIsFishingHookSubmerged && isHookNotExisting) onRodReeledIn()
        lastIsFishingHookSubmerged = currentIsFishingHookSubmerged
        
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        refreshElapsedTimeOrPause() // Once per second!
        updateGuiLines()
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

    private fun refreshElapsedTimeOrPause() {
        CommonUtils.runWithCatching("Failed to refresh elapsed time in $trackerName") {
            if (!isTrackerVisible()) {
                pauseInternal()
                return
            }

            val isHookActive = FishingHookUtils.isFishingHookSubmerged()

            // Start fishing timer after pause or when tracker was empty
            if (isHookActive) {
                isSessionActive = true
            }
    
            if (!isSessionActive || !isTrackerVisible()) return

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

    private fun onRodReeledIn() {
        CommonUtils.runWithCatching("Failed to track rod reeled in in $trackerName") {
            if (!isTrackerVisible()) return
            if (!isSessionActive) return
            castsCount += 1
            updateGuiLines()
        }
    }

    private fun onSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        CommonUtils.runWithCatching("Failed to track sea creature catch in $trackerName") {
            if (!isTrackerVisible()) return
            if (event.seaCreatureName == "Vanquisher") return

            val isDoubleHooked = event.isDoubleHook

            seaCreatureCatchesCount += 1
            val dhValue = if (isDoubleHooked) 2 else 1
            seaCreatureCountWithDh += dhValue
            seaCreatureCountWithDhAndBs += dhValue

            updateGuiLines()
        }
    }

    private fun onSeaCreatureCocooned(event: SeaCreatureCocoonedByYouEvent) {
        CommonUtils.runWithCatching("Failed to track cocooned sea creature in $trackerName") {
            if (!isTrackerVisible()) return
            if (event.seaCreatureName == "Vanquisher") return

            seaCreatureCountWithDhAndBs += 1
            updateGuiLines()
        }
    }

    private fun getTotalForStat(stat: EfficiencyStatTypes): Int {
        return when (stat) {
            EfficiencyStatTypes.CASTS_PER_HOUR -> castsCount
            EfficiencyStatTypes.SC_CATCHES_PER_HOUR -> seaCreatureCatchesCount
            EfficiencyStatTypes.SC_PER_HOUR_WITH_DH -> seaCreatureCountWithDh
            EfficiencyStatTypes.SC_PER_HOUR_WITH_DH_AND_BS -> seaCreatureCountWithDhAndBs
        }
    }

    private fun calculatePerHour(total: Int): Int {
        val elapsedHours = elapsedSeconds / 3600.0
        return if (elapsedHours > 0) (total / elapsedHours).toInt() else 0
    }

    private data class StatLineColumns(val perHour: String, val total: String) {
        fun toCells(): List<String> = listOf(perHour, total)
    }

    private fun getColumnsSeparator(): String = " ${DARK_GRAY}| "

    private fun getStatLineColumns(stat: EfficiencyStatTypes): StatLineColumns {
        val total = getTotalForStat(stat)
        val perHour = calculatePerHour(total)
        val label = when (stat) {
            EfficiencyStatTypes.CASTS_PER_HOUR -> "Casts/h"
            EfficiencyStatTypes.SC_CATCHES_PER_HOUR -> "SC catches/h"
            EfficiencyStatTypes.SC_PER_HOUR_WITH_DH -> "SC/h with DH"
            EfficiencyStatTypes.SC_PER_HOUR_WITH_DH_AND_BS -> "SC/h with DH & BS"
        }
        return StatLineColumns(
            perHour = "${GRAY}$label: ${WHITE}${CommonUtils.formatNumberWithSpaces(perHour)}",
            total = "${GRAY}Total: ${WHITE}${CommonUtils.formatNumberWithSpaces(total)}",
        )
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!isTrackerVisible() || !hasData()) return

        val pausedText = if (isSessionActive) "" else " ${GRAY}[Paused]"
        val lines = mutableListOf<LineInfo>()
        lines.add(LineInfo(baseTitle))

        val selectedStats = Overlays.efficiencyTrackerStats
        val statRows = EfficiencyStatTypes.entries
            .filter { selectedStats.contains(it) }
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
