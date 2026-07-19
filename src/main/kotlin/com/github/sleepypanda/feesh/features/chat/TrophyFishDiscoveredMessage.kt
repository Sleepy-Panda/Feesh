package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.TrophyFishDiscoveredEvent
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.CommonUtils

object TrophyFishDiscoveredMessage {
    fun init() {
        EventBus.subscribe(TrophyFishDiscoveredEvent::class, ::onDiscovered)
    }

    private fun onDiscovered(event: TrophyFishDiscoveredEvent) {
        CommonUtils.runWithCatching("Failed to share Trophy Fish discovery to party chat") {
            if (!WorldUtils.isInSkyblock()) return
            if (!Chat.shareTrophyFishDiscovered) return

            ChatUtils.sendPartyChat("TROPHY FISH DISCOVERED! ${event.detailsFormatted.removeFormatting()}")
        }
    }
}
