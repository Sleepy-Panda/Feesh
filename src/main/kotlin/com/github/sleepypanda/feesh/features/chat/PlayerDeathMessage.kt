package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.features.alerts.PlayerDeathAlert

object PlayerDeathMessage {
    fun init() {
        RegisterUtils.chat(Regex(PlayerDeathAlert.YOU_DIED_PATTERN)) { _, _ -> onPlayerDeath() }
    }

    private fun onPlayerDeath() {
        if (!Chat.messageOnPlayerDeath || !WorldUtils.isInSkyblock()) return

        ChatUtils.sendPartyChat("--> I was killed, please wait for me until I come back <--")
    }
}
