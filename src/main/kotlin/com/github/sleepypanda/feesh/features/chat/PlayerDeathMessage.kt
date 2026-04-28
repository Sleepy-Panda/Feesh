package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.features.alerts.PlayerDeathAlert

object PlayerDeathMessage {
    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!Chat.messageOnPlayerDeath || !WorldUtils.isInSkyblock()) return
        if (!PlayerDeathAlert.YOU_DIED_PATTERN.matches(event.unformattedText)) return

        ChatUtils.sendPartyChat("--> I was killed, please wait for me until I come back <--")
    }
}
