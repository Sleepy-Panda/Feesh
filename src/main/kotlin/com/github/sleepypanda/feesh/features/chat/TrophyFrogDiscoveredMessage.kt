package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.TrophyFrogDiscoveredEvent
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.CommonUtils

object TrophyFrogDiscoveredMessage {
    fun init() {
        EventBus.subscribe(TrophyFrogDiscoveredEvent::class, ::onDiscovered)
    }

    private fun onDiscovered(event: TrophyFrogDiscoveredEvent) {
        CommonUtils.runWithCatching("Failed to share Trophy Frog discovery to party chat") {
            if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL) return
            if (!Chat.shareTrophyFrogDiscovered) return

            ChatUtils.sendPartyChat("FROG DISCOVERED! ${event.detailsFormatted.removeFormatting()}")
        }
    }
}
