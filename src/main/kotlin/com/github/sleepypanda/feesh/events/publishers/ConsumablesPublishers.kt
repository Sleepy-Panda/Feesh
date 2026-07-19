package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.MobyDuckConsumedEvent
import com.github.sleepypanda.feesh.events.models.BlizzardInABottleConsumedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils

object ConsumablesPublishers {
    // You consumed a Moby-Duck: Collector's Edition and gained +30☯ Fishing Wisdom for 60m!
    private val MOBY_DUCK_CONSUMED_PATTERN = Regex("^You consumed a Moby-Duck: Collector's Edition and gained \\+30☯ Fishing Wisdom for 60m!$")

    private val BLIZZARD_IN_A_BOTTLE_CONSUMED_PATTERN = Regex("^BLIZZARD! (?<playerAndRank>.+?) opened a Blizzard in a Bottle, improving everyone's Fishing Stats for the next 10 minutes and causing it to snow!$")
    
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
    
            val match = BLIZZARD_IN_A_BOTTLE_CONSUMED_PATTERN.matchEntire(event.unformattedText)
            match?.let {
                val playerName = PlayerUtils.getUnformattedName()
                if (playerName.isNullOrEmpty()) return@onChat
                val playerAndRank = match.groups.get("playerAndRank")?.value ?: return@onChat
                if (playerAndRank.removeFormatting().contains(playerName, ignoreCase = false)) {
                    EventBus.publish(BlizzardInABottleConsumedEvent())
                }
                return@onChat
            }
        }
    }
}
