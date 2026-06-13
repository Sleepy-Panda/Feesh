package com.github.sleepypanda.feesh.features.overlays.base

import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.gui.GuiButton

object TrackerResetUtils {
    
    /**
     * Registers a reset command (/feeshReset<TrackerName>) for the given tracker.
     * @param tracker The tracker to register the reset command for.
     */
    fun registerResetCommand(tracker: IResettableTracker) {
        RegisterUtils.command(tracker.resetCommand) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetWithConfirmation(tracker, isConfirmed)
        }
    }

    /**
     * Resets the given tracker with confirmation.
     * @param tracker The tracker to reset.
     * @param errorContext The error message to use if the reset fails.
     * @param isConfirmed Whether to confirm the reset.
     * @param needsChatFeedback Whether to send a chat message when the reset is successful.
     */
    fun resetWithConfirmation(tracker: IResettableTracker, isConfirmed: Boolean = false, needsChatFeedback: Boolean = true) {
        CommonUtils.runWithCatching("Failed to reset ${tracker.trackerName}") {
            if (!isConfirmed) {
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to reset ${tracker.trackerName}?\n${RED}${BOLD}[Click to confirm]",
                    "${tracker.resetCommand} noconfirm",
                    true
                )
                return
            }

            tracker.resetData()
            tracker.refreshGui()
            if (needsChatFeedback) ChatUtils.sendLocalChat("${WHITE}${tracker.trackerName} was reset.", true)
        }
    }

    /**
     * Returns a GUI button for resetting the given tracker.
     * @param onClick The action to perform when the button is clicked.
     * @return The GUI button.
     */
    fun getResetGuiButton(buttonIndex: Int = 0, onClick: () -> Unit): GuiButton {
        return GuiButton(buttonIndex, "${GRAY}[${RED}Click to reset${GRAY}]", onClick)
    }

    fun registerViewModeResetCommands(tracker: IResettableViewModeTracker) {
        RegisterUtils.command(tracker.resetSessionCommand) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetViewModeWithConfirmation(tracker, TrackerViewMode.SESSION, isConfirmed)
        }
        RegisterUtils.command(tracker.resetTotalCommand) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetViewModeWithConfirmation(tracker, TrackerViewMode.TOTAL, isConfirmed)
        }
    }

    fun resetViewModeWithConfirmation(
        tracker: IResettableViewModeTracker,
        viewMode: TrackerViewMode,
        isConfirmed: Boolean = false,
        needsChatFeedback: Boolean = true,
    ) {
        CommonUtils.runWithCatching("Failed to reset ${tracker.trackerName}") {
            val viewModeText = tracker.getViewModeDisplayText(viewMode)
            if (!isConfirmed) {
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to reset ${tracker.trackerName} $viewModeText${WHITE}?\n${RED}${BOLD}[Click to confirm]",
                    "${tracker.getResetCommand(viewMode)} noconfirm",
                    true
                )
                return
            }

            tracker.onBeforeReset()
            when (viewMode) {
                TrackerViewMode.SESSION -> tracker.resetSessionData()
                TrackerViewMode.TOTAL -> tracker.resetTotalData()
            }
            tracker.refreshGui()
            if (needsChatFeedback) {
                ChatUtils.sendLocalChat("${WHITE}${tracker.trackerName} $viewModeText ${WHITE}was reset.", true)
            }
        }
    }
}
