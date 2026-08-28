package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.MobyDuckConsumedEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object ConsumablesPublishers {
    // You consumed a Moby-Duck: Collector's Edition and gained +30☯ Fishing Wisdom for 60m!
    private val MOBY_DUCK_CONSUMED_PATTERN = Regex("^You consumed a Moby-Duck: Collector's Edition and gained \\+30☯ Fishing Wisdom for 60m!$")
    
    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        CommonUtils.runWithCatching("Failed to handle various chat message publisher") {
            if (!WorldUtils.isInSkyblock()) return

            MOBY_DUCK_CONSUMED_PATTERN.matchEntire(event.unformattedText)?.let {
                EventBus.publish(MobyDuckConsumedEvent())
                return@onChat
            }
        }
    }
}
