package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.CatchEvent
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

object EfficiencyTracker : IResettableTracker {
    const val RESET_COMMAND = "feeshResetEfficiencyTracker"
    const val PAUSE_COMMAND = "feeshPauseEfficiencyTracker"

    override val trackerName = "Efficiency tracker"
    override val resetCommand = RESET_COMMAND

    private const val TICKS_PER_UPDATE = 20
    private const val HIDE_OVERLAY_MINUTES = 5

    private var catchesCount = 0
    private var seaCreatureCatchesCount = 0
    private var seaCreatureCountWithDh = 0
    private var seaCreatureCountWithDhAndBs = 0
    private var elapsedSeconds = 0

    private var isSessionActive = false

    private var tickCounter = 0
    private val baseTitle = "${AQUA}${BOLD}${trackerName}"

    private val gui = FeeshGui()
        .setCoordsDataKey("efficiencyTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${WHITE}500 ${GRAY}Catches/h (${WHITE}1000 ${GRAY}total)",
            "${WHITE}600 ${GRAY}SC/h (${WHITE}1200 ${GRAY}total)",
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
        EventBus.subscribe(CatchEvent::class, ::onCatch)
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreatureCaught)
        EventBus.subscribe(SeaCreatureCocoonedByYouEvent::class, ::onSeaCreatureCocooned)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    override fun hasData(): Boolean {
        return catchesCount > 0 || seaCreatureCatchesCount > 0 || seaCreatureCountWithDh > 0 || seaCreatureCountWithDhAndBs > 0 || elapsedSeconds > 0
    }

    override fun resetData(force: Boolean) {
        catchesCount = 0
        seaCreatureCatchesCount = 0
        seaCreatureCountWithDh = 0
        seaCreatureCountWithDhAndBs = 0
        isSessionActive = false
        elapsedSeconds = 0
    }

    override fun refreshGui() {
        updateGuiLines()
    }

    fun pause() {
        CommonUtils.runWithCatching("Failed to pause $trackerName") {
            if (!Overlays.efficiencyTrackerOverlay || !WorldUtils.isInSkyblock() || !isSessionActive) return

            pauseInternal()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}$trackerName is paused.", true)
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        pauseInternal()
        gui.clearLines()
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

    private fun onCatch(@Suppress("UNUSED_PARAMETER") event: CatchEvent) {
        CommonUtils.runWithCatching("Failed to track catch in $trackerName") {
            if (!isTrackerActive()) return
            if (!isStatEnabled(EfficiencyStatTypes.CATCHES_PER_HOUR)) return
            addCatch()
        }
    }

    private fun addCatch() {
        catchesCount += 1
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

            val prevIsActive = isSessionActive
            val isHookActive = FishingHookUtils.isFishingHookSubmerged()

            // Start fishing timer after pause or when tracker was empty
            if (isHookActive) {
                isSessionActive = true
                if (elapsedSeconds == 0) {
                    elapsedSeconds = 1
                }
                if (!prevIsActive) {
                    return
                }
            }

            if (!isTrackerActive()) {
                pauseInternal()
                return
            }

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

    private fun getTotalForStat(stat: EfficiencyStatTypes): Int {
        return when (stat) {
            EfficiencyStatTypes.CATCHES_PER_HOUR -> catchesCount
            EfficiencyStatTypes.SC_CATCHES_PER_HOUR -> seaCreatureCatchesCount
            EfficiencyStatTypes.SC_PER_HOUR -> seaCreatureCountWithDh
            EfficiencyStatTypes.SC_PER_HOUR_WITH_BS -> seaCreatureCountWithDhAndBs
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
            EfficiencyStatTypes.CATCHES_PER_HOUR -> "Catches/h"
            EfficiencyStatTypes.SC_CATCHES_PER_HOUR -> "SC catches/h"
            EfficiencyStatTypes.SC_PER_HOUR -> "SC/h"
            EfficiencyStatTypes.SC_PER_HOUR_WITH_BS -> "SC/h with BS"
        }
        return StatLineColumns(
            perHour = "${GRAY}$label: ${WHITE}${CommonUtils.formatNumberWithSpaces(perHour)}",
            total = "${WHITE}${CommonUtils.formatNumberWithSpaces(total)} ${GRAY}total",
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
