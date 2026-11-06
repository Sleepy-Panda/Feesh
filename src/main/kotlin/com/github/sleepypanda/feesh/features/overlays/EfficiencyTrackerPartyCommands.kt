package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.models.EfficiencyStatTypes
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object EfficiencyTrackerPartyCommands {
    fun getCatchesPerHourChatMessage(): String? {
        if (!Overlays.efficiencyTrackerOverlay || !WorldUtils.isInSkyblock()) return null

        return getFormattedStatLine(EfficiencyStatTypes.CATCHES_PER_HOUR, "catches/h")
    }

    fun getSeaCreaturesPerHourChatMessage(): String? {
        if (!Overlays.efficiencyTrackerOverlay || !WorldUtils.isInSkyblock()) return null

        return getFormattedStatLine(EfficiencyStatTypes.SC_PER_HOUR, "SC/h")
    }

    private fun getFormattedStatLine(stat: EfficiencyStatTypes, label: String): String? {
        val details = EfficiencyTracker.getEfficiencyStatDetails(stat) ?: return null
        val perHourStr = CommonUtils.formatNumberWithSpaces(details.perHour)
        val totalStr = CommonUtils.formatNumberWithSpaces(details.total)
        val timeStr = CommonUtils.formatTimeElapsed(details.elapsedSeconds)
        return "$perHourStr $label ($totalStr total in $timeStr)"
    }
}
