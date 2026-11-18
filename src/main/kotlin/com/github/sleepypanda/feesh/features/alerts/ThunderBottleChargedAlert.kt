package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.BottleFullyChargedEvent
import com.github.sleepypanda.feesh.utils.AlertUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.WHITE

object ThunderBottleChargedAlert {
    fun init() {
        EventBus.subscribe(BottleFullyChargedEvent::class, ::onBottleFullyCharged)
    }

    private fun onBottleFullyCharged(event: BottleFullyChargedEvent) {
        if (!Alerts.alertOnThunderBottleCharged || !WorldUtils.isInSkyblock()) return

        CommonUtils.runWithCatching("Failed to play bottle fully charged alert.") {
            AlertUtils.alert(
                title = AlertUtils.TitleConfig("${event.itemDisplayName} ${WHITE}is full"),
                sound = AlertUtils.SoundConfig.McSound(),
                priority = AlertUtils.AlertPriority.LOW,
            )    
        }
    }
}
