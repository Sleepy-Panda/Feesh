package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*

object ChumBucketAutoPickupAlert {
    val PATTERN = Regex("^Automatically picked up the Chum Bucket you left back there\\!$")

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnChumBucketAutoPickup) return
        if (!PATTERN.matches(event.unformattedText)) return

        CommonUtils.showTitle("${GREEN}Chum Bucket ${YELLOW}is gone")
        SoundUtils.playSound()
    }
}
