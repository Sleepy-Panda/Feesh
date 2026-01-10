package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.KeybindUtils
import net.minecraft.client.option.KeyBinding
import org.lwjgl.glfw.GLFW

object LootshareMessage {
    fun init() {       
        KeybindUtils.registerKeybind("key.feesh.lootshareToPartyChat", GLFW.GLFW_KEY_UNKNOWN) {
            sendLootshareMessage()
        }
    }

    private fun sendLootshareMessage() {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        ChatUtils.sendPartyChat("Lootshare!")
    }
}