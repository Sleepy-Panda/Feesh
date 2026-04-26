package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.events.models.IceEssenceStatusBarEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object IceEssenceStatusBarPublisher {
    // Example: "+100 Ice Essence"
    private val ICE_ESSENCE_GAIN_PATTERN = Regex("\\+(?<amount>[\\d,]+)\\s+Ice Essence\\b")

    private var lastGainState = null as Int?

    fun init() {
        EventBus.subscribe(ChatCancellableEvent::class, ::onActionBarMessage)
    }

    private fun onActionBarMessage(event: ChatCancellableEvent) {
        if (!event.isOverlay) return
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return

        CommonUtils.runWithCatching("Failed to handle Ice Essence gain from action bar.") {
            val actionBarText = event.message.string.removeFormatting()
            val match = ICE_ESSENCE_GAIN_PATTERN.find(actionBarText) ?: run {
                lastGainState = null
                return@onActionBarMessage
            }

            val amount = match.groups["amount"]?.value?.replace(",", "")?.toIntOrNull() ?: run {
                lastGainState = null
                return@onActionBarMessage
            }

            // Same message change is triggered for a few seconds per one gain
            if (amount == lastGainState) return@onActionBarMessage

            lastGainState = amount

            EventBus.publish(IceEssenceStatusBarEvent(amount))
        }
    }
}
