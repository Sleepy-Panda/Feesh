package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.TrophyFishDiscoveredEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object TrophyFishDiscoveredAlert {
    fun init() {
        EventBus.subscribe(TrophyFishDiscoveredEvent::class, ::onDiscovered)
    }

    private fun onDiscovered(event: TrophyFishDiscoveredEvent) {
        CommonUtils.runWithCatching("Failed to play alert on Trophy Fish discovered") {
            if (!WorldUtils.isInSkyblock()) return
            if (!Alerts.alertOnTrophyFishDiscovered) return

            CommonUtils.showTitle(event.detailsFormatted, "${GREEN}${BOLD}TROPHY FISH DISCOVERED!")
            // It has sound played by SB so no need to play our own
        }
    }
}
