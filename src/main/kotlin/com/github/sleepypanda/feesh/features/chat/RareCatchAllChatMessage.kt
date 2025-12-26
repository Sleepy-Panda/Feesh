package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypesAllChat
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.OwnSeaCreatureCaughtEvent
import kotlin.random.Random

object RareCatchAllChatMessage {
    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!WorldUtils.isInSkyblock() || !Chat.shareRareSeaCreaturesAllChat) return

        val seaCreatureName = event.seaCreatureName

        val type = try {
            RareSeaCreatureTypesAllChat.valueOf(seaCreatureName.uppercase().replace(" ", "_"))
        } catch (_: IllegalArgumentException) {
            return
        }
        if (!Chat.shareRareSeaCreaturesTypesAllChat.contains(type)) return

        val isDoubleHook = event.isDoubleHook
        val message = getAllChatMessage(seaCreatureName, isDoubleHook)
        ChatUtils.sendAllChat(message)
    }

    private fun getAllChatMessage(seaCreatureName: String, isDoubleHooked: Boolean): String {
        val player = FeeshMod.mc.player ?: return ""
        val location = "x: ${Math.round(player.getX())}, y: ${Math.round(player.getY())}, z: ${Math.round(player.getZ())}"
        val scMessage = if (isDoubleHooked) "${seaCreatureName} x2" else "${seaCreatureName}"
        val zone = "at ..." //getZoneName() at ...
        val messageId = "@" + (1..10).map { ('0'..'z').random() }.joinToString("")
    
        var message = "${location} | ${scMessage} ${zone} | ${messageId}"
        return message
    }
}
