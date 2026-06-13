package com.github.sleepypanda.feesh.features.overlays.base

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

/** Interface for trackers with Session/Total view modes that can be reset separately.
 * Also see IResettableTracker for trackers without view modes.
 */
interface IResettableViewModeTracker : ITracker {
    val resetSessionCommand: String
    val resetTotalCommand: String

    fun getCurrentViewMode(): TrackerViewMode

    fun hasSessionData(): Boolean
    fun hasTotalData(): Boolean
    fun resetSessionData(force: Boolean = false)
    fun resetTotalData(force: Boolean = false)
    
    fun refreshGui()

    fun onBeforeReset() {}

    fun getViewModeDisplayText(viewMode: TrackerViewMode): String {
        return when (viewMode) {
            TrackerViewMode.SESSION -> "${GRAY}[${GREEN}Session${GRAY}]"
            TrackerViewMode.TOTAL -> "${GRAY}[${GREEN}Total${GRAY}]"
        }
    }

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
        TrackerResetUtils.resetViewModeWithConfirmation(this, viewMode, isConfirmed)
    }
}
