package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypes
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.FeeshMod

object RareCatchAllChatMessage {
    fun init() {
        SeaCreatures.allSeaCreatures
            .filter { it.name == "Lord Jawbus" }
            .forEach { sc -> RegisterUtils.chat(sc.pattern) { _, _ -> onSeaCreature(sc.name) }
        }
    }

    private fun onSeaCreature(seaCreatureName: String) {
        if (seaCreatureName.isNullOrEmpty()) return
        if (!WorldUtils.isInSkyblock() || !Chat.shareRareSeaCreatures) return

        val type = try {
            RareSeaCreatureTypes.valueOf(seaCreatureName.uppercase().replace(" ", "_"))
        } catch (_: IllegalArgumentException) {
            return
        }

        if (!Chat.shareSeaCreaturesTypes.contains(type)) return
        val isDoubleHook = false

        val message = getAllChatMessage(seaCreatureName, isDoubleHook)
        ChatUtils.sendAllChat(message)
    }

    private fun getAllChatMessage(seaCreatureName: String, isDoubleHooked: Boolean): String {
        val player = FeeshMod.mc.player ?: return ""
        val location = "x: ${Math.round(player.getX())}, y: ${Math.round(player.getY())}, z: ${Math.round(player.getZ())}"
        val zone = null as String? //getZoneName()
        val messageId = null as String? //getMessageId()
    
        var message = ""
        //message += "${location} | ${seaCreatureName} ${isDoubleHooked ? 'x2' : '' }${!zone.isNullOrEmpty() ? ' at ' + zone : ''} | ${messageId}"
        return message
    }
}
