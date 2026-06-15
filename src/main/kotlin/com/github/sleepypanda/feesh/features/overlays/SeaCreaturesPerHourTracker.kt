package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.gui.GuiButton
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.features.overlays.base.IResettableTracker
import java.util.Date

object SeaCreaturesPerHourTracker : IResettableTracker {
    const val RESET_COMMAND = "feeshResetSeaCreaturesPerHourTracker"
    const val PAUSE_COMMAND = "feeshPauseSeaCreaturesPerHourTracker"

    override val trackerName = "Sea creatures per hour tracker"
    override val resetCommand = RESET_COMMAND

    private const val TICKS_PER_UPDATE = 20
    private const val HIDE_OVERLAY_MINUTES = 5

    private var totalSeaCreaturesCaughtCount = 0
    private var lastSeaCreatureCaughtAt: Date? = null
    private var isSessionActive = false
    private var elapsedSeconds = 0
    private var tickCounter = 0
    private val baseTitle = "${AQUA}${BOLD}Sea creatures per hour"

    private val gui = FeeshGui()
        .setCoordsDataKey("seaCreaturesPerHourTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${WHITE}1 234 ${GRAY}sc/hour (${WHITE}2 000 ${GRAY}total)",
            "",
            "${AQUA}Elapsed time: ${WHITE}1h 23m 45s",
        ))
        .setSettingsKey { Overlays.seaCreaturesPerHourTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.seaCreaturesPerHourTrackerCustomStyle }
        .setCondition {
            WorldUtils.isInFishingWorld() &&
            FishingHookUtils.wasFishingHookSubmergedMinutesAgo(HIDE_OVERLAY_MINUTES)
        }

    fun init() {
        registerResetCommand()
        RegisterUtils.command(PAUSE_COMMAND) {
            pause()
        }
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreatureCaught)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    override fun hasData(): Boolean {
        return totalSeaCreaturesCaughtCount > 0 || elapsedSeconds > 0
    }

    override fun resetData(force: Boolean) {
        totalSeaCreaturesCaughtCount = 0
        lastSeaCreatureCaughtAt = null
        isSessionActive = false
        elapsedSeconds = 0
    }

    override fun refreshGui() {
        updateGuiLines()
    }

    fun pause() {
        CommonUtils.runWithCatching("Failed to pause Sea creatures per hour tracker") {
            if (!Overlays.seaCreaturesPerHourTrackerOverlay ||
                !WorldUtils.isInSkyblock() ||
                !isSessionActive) return

            isSessionActive = false
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Sea creatures per hour tracker is paused. Continue fishing to resume it.", true)
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        isSessionActive = false
        gui.clearLines()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        refreshElapsedTime() // Once per second!
        updateGuiLines()
    }

    private fun refreshElapsedTime() {
        CommonUtils.runWithCatching("Failed to refresh elapsed time") {
            if (!isSessionActive ||
                !Overlays.seaCreaturesPerHourTrackerOverlay ||
                !WorldUtils.isInSkyblock() ||
                !WorldUtils.isInFishingWorld()) {
                isSessionActive = false
                return
            }

            val now = Date()
            val lastCatch = lastSeaCreatureCaughtAt ?: run {
                isSessionActive = false
                return
            }

            val elapsedSecondsSinceLastCatch = (now.time - lastCatch.time) / 1000

            if (elapsedSecondsSinceLastCatch < Overlays.trackersAutoPauseSeconds) {
                isSessionActive = true
                elapsedSeconds += 1
                updateGuiLines()
            } else {
                isSessionActive = false
            }
        }
    }

    private fun onSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        CommonUtils.runWithCatching("Failed to track sea creature catch") {
            if (!Overlays.seaCreaturesPerHourTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
            if (event.seaCreatureName == "Vanquisher") return

            val isDoubleHooked = event.isDoubleHook
            isSessionActive = true

            val diff = if (isDoubleHooked && Overlays.seaCreaturesPerHourCountDoubleHookAsTwo) 2 else 1
            totalSeaCreaturesCaughtCount += diff
            lastSeaCreatureCaughtAt = Date()

            updateGuiLines()
        }
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!Overlays.seaCreaturesPerHourTrackerOverlay ||
            !WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld() ||
            (totalSeaCreaturesCaughtCount == 0) ||
            !FishingHookUtils.wasFishingHookSubmergedMinutesAgo(HIDE_OVERLAY_MINUTES)
        ) return

        val elapsedHours = elapsedSeconds / 3600.0
        val seaCreaturesPerHour = if (elapsedHours > 0) {
            (totalSeaCreaturesCaughtCount / elapsedHours).toInt()
        } else 0

        val pausedText = if (isSessionActive) "" else " ${GRAY}[Paused]"
        val lines = mutableListOf<String>()
        lines.add(baseTitle)
        val count = CommonUtils.formatNumberWithSpaces(seaCreaturesPerHour)
        val text = if (Overlays.seaCreaturesPerHourCountDoubleHookAsTwo) "${GRAY}sc/hour" else "${GRAY}catches/hour"
        lines.add("${WHITE}${count} ${text} (${WHITE}${CommonUtils.formatNumberWithSpaces(totalSeaCreaturesCaughtCount)} ${GRAY}total)")
        lines.add("")
        lines.add("${AQUA}Elapsed time: ${WHITE}${CommonUtils.formatTimeElapsed(elapsedSeconds)}${pausedText}")

        gui.setLines(lines.map { LineInfo(it) })
        gui.setButtons(listOf(
            GuiButton(0, "${GRAY}[${YELLOW}Click to pause${GRAY}]", { pause() }),
            getResetGuiButton(1) { requestReset() }
        ))
    }
}
