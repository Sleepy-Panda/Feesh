package com.github.sleepypanda.feesh.features.overlays.base

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

/**
 * Interface for trackers with view modes (Session/Total).
 */
interface IViewModeTracker : ITracker {
    fun getCurrentViewMode(): TrackerViewMode

    fun getViewModeDisplayText(viewMode: TrackerViewMode): String {
        return when (viewMode) {
            TrackerViewMode.SESSION -> "${GRAY}[${GREEN}Session${GRAY}]"
            TrackerViewMode.TOTAL -> "${GRAY}[${GREEN}Total${GRAY}]"
        }
    }
}