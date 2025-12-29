package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object PlayerDeathMessage {
    const val PATTERN = "^ ☠ You were killed by (.*?)\\.$"

    fun init() {
        RegisterUtils.chat(Regex(PATTERN)) { _, matchResult -> onPlayerDeath(matchResult) }
    }

    private fun onPlayerDeath(matchResult: MatchResult) {
        if (!Chat.messageOnPlayerDeath || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return

        val killerName = matchResult.groupValues[1]
        if (killerName != "Thunder" && killerName != "Ragnarok" && killerName != "Lord Jawbus") return

        ChatUtils.sendPartyChat("--> I was killed, please wait for me until I come back <--")
    }
}
