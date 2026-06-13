package com.github.sleepypanda.feesh.features.overlays.base

import com.github.sleepypanda.feesh.FeeshMod

/** 
 * Interface for trackers (without view modes) that can be reset via various methods.
 */
interface IResettableTracker : ITracker {
    val resetCommand: String

    fun hasData(): Boolean
    fun resetData(force: Boolean = false)
    
    fun refreshGui()

    fun bulkReset() {
        resetData()
        refreshGui()
    }

    fun resetOnGameClosed() {
        if (!hasData()) return
        resetData(force = true)
        FeeshMod.LOGGER.info("[Feesh] Automatically reset $trackerName on game closed.")
    }

    fun requestReset(isConfirmed: Boolean = false, needsChatFeedback: Boolean = true) {
        TrackerResetUtils.resetWithConfirmation(this, isConfirmed, needsChatFeedback)
    }
}
