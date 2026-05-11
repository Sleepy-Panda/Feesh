package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.TrophyFrogDiscoveredEvent
import com.github.sleepypanda.feesh.utils.WorldUtils

object TrophyFrogDiscoveredPublisher {
    // NEW DISCOVERY: Blessed Frog BRONZE
    private val NEW_DISCOVERY_PATTERN = Regex("^NEW DISCOVERY: (?<details>.+?)$")

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL) return

        NEW_DISCOVERY_PATTERN.matchEntire(event.unformattedText) ?: return

        val details = event.formattedText.split(": ").last()
        if (details.isEmpty()) return
        
        EventBus.publish(TrophyFrogDiscoveredEvent(details))
    }
}
