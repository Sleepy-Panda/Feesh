package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.features.chat.HotspotFoundMessage
import com.github.sleepypanda.feesh.features.chat.LootshareMessage
import com.github.sleepypanda.feesh.features.commands.PauseAllTrackersCommand
import com.github.sleepypanda.feesh.features.overlays.BarnFishingTimer
import net.minecraft.client.KeyMapping
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.resources.ResourceLocation
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import org.lwjgl.glfw.GLFW

object KeybindUtils {
    val FEESH_CATEGORY = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("feesh", "keybinds")) // Keys are localized in resources/assets/feesh/lang/en_us.json
    private val keybindCallbacks = mutableListOf<Pair<KeyMapping, () -> Unit>>()
    private var keybindsRegistered = false

    fun init() {
        registerAllKeybinds()
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
    }

    private fun registerAllKeybinds() {
        if (keybindsRegistered) return

        registerKeybind("key.feesh.shareHotspotPartyChat", GLFW.GLFW_KEY_UNKNOWN) {
            HotspotFoundMessage.shareNearestHotspotToParty()
        }
        registerKeybind("key.feesh.shareHotspotAllChat", GLFW.GLFW_KEY_UNKNOWN) {
            HotspotFoundMessage.shareNearestHotspotToAll()
        }
        registerKeybind("key.feesh.lootshareToPartyChat", GLFW.GLFW_KEY_UNKNOWN) {
            LootshareMessage.triggerLootshareMessage()
        }
        registerKeybind("key.feesh.resetBarnFishingTimer", GLFW.GLFW_KEY_UNKNOWN) {
            BarnFishingTimer.triggerResetKeybind()
        }
        registerKeybind("key.feesh.pauseAllTrackers", GLFW.GLFW_KEY_PAUSE) {
            PauseAllTrackersCommand.triggerPauseAllTrackers()
        }

        keybindsRegistered = true
    }

    private fun registerKeybind(id: String, keyCode: Int = GLFW.GLFW_KEY_UNKNOWN, callback: () -> Unit): KeyMapping {
        val keyBinding = KeyMapping(
            id,
            InputConstants.Type.KEYSYM,
            keyCode,
            FEESH_CATEGORY
        )
        KeyBindingHelper.registerKeyBinding(keyBinding)
        keybindCallbacks.add(keyBinding to callback)
        return keyBinding
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        keybindCallbacks.forEach { (keyBinding, callback) ->
            if (keyBinding.consumeClick()) {
                callback()
            }
        }
    }
}
