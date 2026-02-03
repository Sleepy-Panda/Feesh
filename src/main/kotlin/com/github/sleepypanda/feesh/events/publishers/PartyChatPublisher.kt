package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.PartyChatEvent
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString

object PartyChatPublisher {
    val PCHAT_PATTERN = Regex("^§9[\\p{L}]+ §8> (?<rankAndPlayer>(.*))§f: (?<message>(.*))$")

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock()) return
        
        val match = PCHAT_PATTERN.matchEntire(event.message.getFormattedString()) ?: return
        val rankAndPlayer = match.groups.get("rankAndPlayer")?.value ?: return
        val message = match.groups.get("message")?.value ?: return
        EventBus.publish(PartyChatEvent(event.message, rankAndPlayer, message))
    }
}

