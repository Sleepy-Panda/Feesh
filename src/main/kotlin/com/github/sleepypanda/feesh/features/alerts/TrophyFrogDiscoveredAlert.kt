package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.TrophyFrogDiscoveredEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object TrophyFrogDiscoveredAlert {
    fun init() {
        EventBus.subscribe(TrophyFrogDiscoveredEvent::class, ::onDiscovered)
    }

    private fun onDiscovered(event: TrophyFrogDiscoveredEvent) {
        CommonUtils.runWithCatching("Failed to play alert on Trophy Frog discovered") {
            if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL) return
            if (!Alerts.alertOnTrophyFrogDiscovered) return

            CommonUtils.showTitle(event.detailsFormatted, "${GREEN}${BOLD}FROG DISCOVERED!")
            // It has sound played by SB so no need to play our own
        }
    }
}
