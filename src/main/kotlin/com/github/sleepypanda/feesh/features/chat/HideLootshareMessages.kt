package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object HideLootshareMessages {
    // LOOT SHARE You received loot for assisting Schnabbelschnutz!
    // LOOT SHARE You received 2 Titanoboa Shards for assisting CuzImCrzz!
    private val LOOTSHARE_ASSIST_PATTERN = Regex("^LOOT SHARE You received (?<loot>[\\w\\d ]+) for assisting (?<playerName>\\w+)!$")

    fun init() {
        EventBus.subscribe(ChatCancellableEvent::class, ::onChat)
    }

    private fun onChat(event: ChatCancellableEvent) {
        if (!Chat.hideLootshareMessagesInFrozenBlaze) return
        if (!WorldUtils.isInSkyblock() || !PlayerUtils.isInFrozenBlaze()) return

        CommonUtils.runWithCatching("Failed to hide lootshare chat message.") {
            if (LOOTSHARE_ASSIST_PATTERN.matches(event.unformattedText)) {
                event.isCancelled = true
            }
        }
    }
}
