package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.events.models.TreasureCatchType
import com.github.sleepypanda.feesh.events.models.TreasureCatchEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object TreasureCatchPublisher {
    // ♪ MUSICAL CATCH! You caught a Music Disc - Cat!
    //  GOOD JUNK CATCH! You caught a Rusty Coin!
    //  GOOD CATCH! You caught a Shinyfish Shard!
    private val TREASURE_CATCH_PATTERN = Regex("^. (GOOD JUNK|GREAT JUNK|OUTSTANDING JUNK|GOOD|GREAT|OUTSTANDING|MUSICAL) CATCH!")

    fun init() {
        EventBus.subscribe(ChatCancellableEvent::class, ::onChat)
    }

    private fun onChat(event: ChatCancellableEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

        CommonUtils.runWithCatching("Failed to track treasure catch in TreasureCatchPublisher") {
            val match = TREASURE_CATCH_PATTERN.find(event.unformattedText) ?: return
            val type = TreasureCatchType.getTypeByChatLabel(match.groupValues[1]) ?: return
            EventBus.publish(TreasureCatchEvent(type, event.unformattedText))
        }
    }
}
