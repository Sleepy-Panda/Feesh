package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object LootshareMessage {
    const val LOOTSHARE_MESSAGE = "Lootshare!"

    fun init() {
    }

    fun triggerLootshareMessage() {
        sendLootshareMessage()
    }

    private fun sendLootshareMessage() {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        ChatUtils.sendPartyChat(LOOTSHARE_MESSAGE)
    }
}