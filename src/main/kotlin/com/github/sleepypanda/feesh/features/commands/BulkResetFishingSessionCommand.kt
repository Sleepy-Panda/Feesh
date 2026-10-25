package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.models.BulkResettableTrackerTypes
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object BulkResetFishingSessionCommand {
    const val COMMAND_NAME = "feeshBulkResetFishingSession"

    private val SESSION_VIEW_MODE_TEXT = "${GRAY}[${GREEN}Session${GRAY}]"

    fun init() {
        RegisterUtils.command(COMMAND_NAME) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetFishingSession(isConfirmed)
        }
    }

    fun triggerBulkResetFishingSession() {
        resetFishingSession(isConfirmed = false)
    }

    private fun resetFishingSession(isConfirmed: Boolean) {
        if (!WorldUtils.isInSkyblock()) return

        val selected = Overlays.bulkResetTrackersList
        if (selected.isEmpty()) {
            ChatUtils.sendLocalChat("${YELLOW}No trackers are selected. Choose trackers in Feesh settings -> Overlays -> Trackers to reset on keybind.", true)
            return
        }

        val toReset = selected.filter { hasData(it) }
        if (toReset.isEmpty()) {
            ChatUtils.sendLocalChat("The fishing session has no data to reset.", true)
            return
        }

        val linePrefix = "\n${GRAY}- ${WHITE}"
        val trackersText = toReset.joinToString(linePrefix, prefix = linePrefix) { getResetDisplayName(it) }

        if (!isConfirmed) {
            ChatUtils.sendLocalChat("${WHITE}Do you want to reset the current fishing session?$trackersText", true)
            ChatUtils.sendLocalChatWithCommand(
                "${RED}${BOLD}[Click to confirm]",
                "$COMMAND_NAME noconfirm",
                false
            )
            return
        }

        CommonUtils.runWithCatching("Failed to reset fishing session") {
            toReset.forEach { resetTracker(it) }
            ChatUtils.sendLocalChat("The fishing session data was reset.", true)
        }
    }

    private fun getResetDisplayName(overlay: BulkResettableTrackerTypes): String {
        return if (overlay.hasSessionMode) {
            "${WHITE}${overlay.displayName} $SESSION_VIEW_MODE_TEXT"
        } else {
            "${WHITE}${overlay.displayName}"
        }
    }

    private fun hasData(tracker: BulkResettableTrackerTypes): Boolean {
        tracker.resettableViewModeTracker?.let { return it.hasSessionData() }
        return tracker.resettableTracker?.hasData() ?: false
    }

    private fun resetTracker(tracker: BulkResettableTrackerTypes) {
        tracker.resettableViewModeTracker?.let { viewModeTracker ->
            CommonUtils.runWithCatching("Failed to reset $tracker (Session) on keybind") {
                viewModeTracker.bulkResetSession()
            }
            return
        }

        tracker.resettableTracker?.let { resettableTracker ->
            CommonUtils.runWithCatching("Failed to reset $tracker on keybind") {
                resettableTracker.bulkReset()
            }
        }
    }
}
