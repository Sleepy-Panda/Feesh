package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object PlayerDeathMessage {
    const val PATTERN = "^ ☠ You were killed by (Ragnarok|Thunder|Lord Jawbus|Wiki Tiki|Titanoboa)\\.$"

    fun init() {
        RegisterUtils.chat(Regex(PATTERN)) { _, _ -> onPlayerDeath() }
    }

    private fun onPlayerDeath() {
        if (!Chat.messageOnPlayerDeath || !WorldUtils.isInSkyblock()) return

        ChatUtils.sendPartyChat("--> I was killed, please wait for me until I come back <--")
    }
}
