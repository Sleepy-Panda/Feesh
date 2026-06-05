package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.TrophyFrogDiscoveredEvent
import com.github.sleepypanda.feesh.events.models.TrophyFishDiscoveredEvent
import com.github.sleepypanda.feesh.utils.WorldUtils

object TrophyDiscoveredPublisher {
    // NEW DISCOVERY: Blessed Frog BRONZE
    private val NEW_FROG_DISCOVERY_PATTERN = Regex("^NEW DISCOVERY: (?<details>.+?)$")
    // NEW DISCOVERY: Mana Ray BRONZE
    private val NEW_FISH_DISCOVERY_PATTERN = Regex("^NEW DISCOVERY: (?<details>.+?)$")

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock()) return

        onTrophyFrogDiscovered(event)
        onTrophyFishDiscovered(event)
    }

    private fun onTrophyFrogDiscovered(event: ChatEvent) {
        if (WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL) return
        if (isObfuscatedTier1(event)) return // Frogs and Fish have same pattern, Obfuscated-1 is possible to get on Atoll

        NEW_FROG_DISCOVERY_PATTERN.matchEntire(event.unformattedText) ?: return

        val details = event.formattedText.split(": ").last()
        if (details.isEmpty()) return

        EventBus.publish(TrophyFrogDiscoveredEvent(details))
    }

    private fun onTrophyFishDiscovered(event: ChatEvent) {
        if (!isObfuscatedTier1(event) && WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return // Obfuscated-1 can be caught on any island

        NEW_FISH_DISCOVERY_PATTERN.matchEntire(event.unformattedText) ?: return

        val details = event.formattedText.split(": ").last()
        if (details.isEmpty()) return

        EventBus.publish(TrophyFishDiscoveredEvent(details))
    }

    private fun isObfuscatedTier1(event: ChatEvent): Boolean {
        return event.unformattedText.contains("Obfuscated-1") || event.unformattedText.contains("Obfuscated 1")
    }
}
