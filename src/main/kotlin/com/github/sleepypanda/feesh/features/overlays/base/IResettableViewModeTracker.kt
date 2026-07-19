package com.github.sleepypanda.feesh.features.overlays.base

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.gui.GuiButton

/**
 * Interface for trackers with Session/Total view modes that can be reset separately.
 * Also see {@link IResettableTracker} for trackers without view modes.
 */
interface IResettableViewModeTracker : IViewModeTracker {
    val resetSessionCommand: String
    val resetTotalCommand: String

    fun hasSessionData(): Boolean
    fun hasTotalData(): Boolean
    fun resetSessionData(force: Boolean = false)
    fun resetTotalData(force: Boolean = false)
    
    fun refreshGui()

    /**
     * Called before resetting the tracker in any mode, using any method (bulk reset, keybind, GUI button, etc.).
     */
    fun onBeforeReset() {}

    fun getResetCommand(viewMode: TrackerViewMode): String {
        return when (viewMode) {
            TrackerViewMode.SESSION -> resetSessionCommand
            TrackerViewMode.TOTAL -> resetTotalCommand
        }
    }

    fun bulkResetSession() {
        onBeforeReset()
        resetSessionData()
        refreshGui()
    }

    fun resetOnGameClosed() {
        if (!hasSessionData()) return
        onBeforeReset()
        resetSessionData(force = true)
        FeeshMod.LOGGER.info("[Feesh] Automatically reset $trackerName [Session] on game closed.")
    }

    fun requestReset(viewMode: TrackerViewMode = getCurrentViewMode(), isConfirmed: Boolean = false) {
        resetViewModeWithConfirmation(viewMode, isConfirmed)
    }

    /**
     * Returns a GUI button for resetting the given tracker.
     * @param onClick The action to perform when the button is clicked.
     * @return The GUI button.
     */
    fun getResetGuiButton(buttonIndex: Int = 0, onClick: () -> Unit): GuiButton {
        return GuiButton(buttonIndex, "${GRAY}[${RED}Click to reset${GRAY}]", onClick)
    }

    /**
     * Registers reset commands (/feeshReset<TrackerName>[Session/Total]) for the given tracker.
     * @param tracker The tracker to register the reset command for.
     */
    fun registerViewModeResetCommands() {
        RegisterUtils.command(resetSessionCommand) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetViewModeWithConfirmation(TrackerViewMode.SESSION, isConfirmed)
        }
        RegisterUtils.command(resetTotalCommand) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetViewModeWithConfirmation(TrackerViewMode.TOTAL, isConfirmed)
        }
    }

    /**
     * Resets the tracker in the given view mode with confirmation.
     * @param viewMode The view mode to reset.
     * @param isConfirmed Whether to confirm the reset.
     * @param needsChatFeedback Whether to send a chat message when the reset is successful.
     */
    private fun resetViewModeWithConfirmation(
        viewMode: TrackerViewMode,
        isConfirmed: Boolean = false,
        needsChatFeedback: Boolean = true,
    ) {
        CommonUtils.runWithCatching("Failed to reset ${trackerName}") {
            val viewModeText = getViewModeDisplayText(viewMode)
            if (!isConfirmed) {
                ChatUtils.sendLocalChat("${WHITE}Do you want to reset ${trackerName} $viewModeText${WHITE}?", true)
                ChatUtils.sendLocalChatWithCommand(
                    "${RED}${BOLD}[Click to confirm]",
                    "${getResetCommand(viewMode)} noconfirm"
                )
                return
            }

            onBeforeReset()
            when (viewMode) {
                TrackerViewMode.SESSION -> resetSessionData()
                TrackerViewMode.TOTAL -> resetTotalData()
            }
            refreshGui()
            if (needsChatFeedback) {
                ChatUtils.sendLocalChat("${WHITE}${trackerName} $viewModeText ${WHITE}was reset.", true)
            }
        }
    }
}
