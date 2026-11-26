package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.utils.AlertUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*

object ChumBucketAutoPickupAlert {
    const val CHUM_BUCKET_PICKED_UP_MESSAGE = "Automatically picked up the Chum Bucket you left back there!"

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnChumBucketAutoPickup) return
        if (event.unformattedText != CHUM_BUCKET_PICKED_UP_MESSAGE) return

        AlertUtils.alert(
            title = AlertUtils.TitleConfig("${GREEN}Chum Bucket ${YELLOW}is gone"),
            sound = AlertUtils.SoundConfig.McSound(),
            priority = AlertUtils.AlertPriority.LOW,
        )
    }
}
