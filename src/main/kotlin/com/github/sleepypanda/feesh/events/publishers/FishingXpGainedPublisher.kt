package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ActionBarCancellableEvent
import com.github.sleepypanda.feesh.events.models.FishingXpGainedEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object FishingXpGainedPublisher {
    // Example: "+2,696.4 Fishing (2,811,372,886/0)"
    private val FISHING_XP_GAIN_PATTERN = Regex("\\+(?<amount>[\\d,]+\\.?\\d*)\\s+Fishing\\s+\\((?<total>[\\d,]+)/0\\)")

    private var lastGainText = null

    fun init() {
        EventBus.subscribe(ActionBarCancellableEvent::class, ::onActionBarMessage)
    }

    private fun onActionBarMessage(event: ActionBarCancellableEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

        CommonUtils.runWithCatching("Failed to handle Fishing XP gain from action bar.") {
            val actionBarText = event.unformattedText
            val match = FISHING_XP_GAIN_PATTERN.find(actionBarText) ?: run {
                lastGainText = null
                return@onActionBarMessage
            }

            val amount = match.groups["amount"]?.value?.replace(",", "")?.toDoubleOrNull() ?: run {
                lastGainText = null
                return@onActionBarMessage
            }

            // Same message change is triggered for a few seconds per one gain
            if (actionBarText == lastGainText) return@onActionBarMessage

            lastGainText = actionBarText

            EventBus.publish(FishingXpGainedEvent(amount))
        }
    }
}
