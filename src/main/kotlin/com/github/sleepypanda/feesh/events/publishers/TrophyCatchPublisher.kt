package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.events.models.TrophyFrogCaughtEvent
import com.github.sleepypanda.feesh.events.models.TrophyFishCaughtEvent
import com.github.sleepypanda.feesh.events.models.TrophyFrogDiscoveredEvent
import com.github.sleepypanda.feesh.events.models.TrophyFishDiscoveredEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object TrophyCatchPublisher {
    // NEW DISCOVERY: Blessed Frog BRONZE
    private val NEW_FROG_DISCOVERY_PATTERN = Regex("^NEW DISCOVERY: (?<details>.+?)$")

    // NEW DISCOVERY: Mana Ray BRONZE
    private val NEW_FISH_DISCOVERY_PATTERN = Regex("^NEW DISCOVERY: (?<details>.+?)$")

    // TROPHY FROG! You caught a Bullfrog BRONZE!
    // TROPHY FROG! You caught a Wetlands Frog GOLD!
    val TROPHY_FROG_CATCH_PATTERN = Regex("^. TROPHY FROG! You caught (a|an) (?<name>.+?) (?<rarity>BRONZE|SILVER|GOLD|DIAMOND)!$")

    // TROPHY FISH! You caught a Lavahorse BRONZE!
    // TROPHY FISH! You caught a Steaming-Hot Flounder GOLD!
    val TROPHY_FISH_CATCH_PATTERN = Regex("^. TROPHY FISH! You caught (a|an) (?<name>.+?) (?<rarity>BRONZE|SILVER|GOLD|DIAMOND)!$")

    fun init() {
        EventBus.subscribe(ChatCancellableEvent::class, ::onChat)
    }

    private fun onChat(event: ChatCancellableEvent) {
        if (!WorldUtils.isInSkyblock()) return

        CommonUtils.runWithCatching("Failed to handle chat messages in trophy publisher.") {
            onTrophyFrogDiscovered(event)
            onTrophyFishDiscovered(event)
            onTrophyFrogCaught(event)
            onTrophyFishCaught(event)
        }
    }

    private fun onTrophyFrogCaught(event: ChatCancellableEvent) {
        if (WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL) return

        val match = TROPHY_FROG_CATCH_PATTERN.matchEntire(event.unformattedText) ?: return
        val name = match.groups["name"]?.value ?: return
        val rarity = match.groups["rarity"]?.value ?: return
        EventBus.publish(TrophyFrogCaughtEvent(name, rarity, event))
    }

    private fun onTrophyFishCaught(event: ChatCancellableEvent) {
        if (WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return

        val match = TROPHY_FISH_CATCH_PATTERN.matchEntire(event.unformattedText) ?: return
        val name = match.groups["name"]?.value ?: return
        val rarity = match.groups["rarity"]?.value ?: return
        EventBus.publish(TrophyFishCaughtEvent(name, rarity, event))
    }

    private fun onTrophyFrogDiscovered(event: ChatCancellableEvent) {
        if (WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL) return
        if (isObfuscatedTier1(event)) return // Frogs and Fish have same pattern, Obfuscated-1 is possible to get on Atoll

        NEW_FROG_DISCOVERY_PATTERN.matchEntire(event.unformattedText) ?: return

        val details = event.formattedText.split(": ").last()
        if (details.isEmpty()) return

        EventBus.publish(TrophyFrogDiscoveredEvent(details))
    }

    private fun onTrophyFishDiscovered(event: ChatCancellableEvent) {
        if (!isObfuscatedTier1(event) && WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return // Obfuscated-1 can be caught on any island

        NEW_FISH_DISCOVERY_PATTERN.matchEntire(event.unformattedText) ?: return

        val details = event.formattedText.split(": ").last()
        if (details.isEmpty()) return

        EventBus.publish(TrophyFishDiscoveredEvent(details))
    }

    private fun isObfuscatedTier1(event: ChatCancellableEvent): Boolean {
        return event.unformattedText.contains("Obfuscated-1") || event.unformattedText.contains("Obfuscated 1")
    }
}
