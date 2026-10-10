package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.features.chat.HotspotFoundMessage
import com.github.sleepypanda.feesh.features.chat.LootshareMessage
import com.github.sleepypanda.feesh.features.commands.PauseAllTrackersCommand
import com.github.sleepypanda.feesh.features.commands.BulkResetFishingSessionCommand
import com.github.sleepypanda.feesh.features.overlays.BarnFishingTimer
import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper as KeyBindingHelper

object KeybindUtils {
    val FEESH_CATEGORY: KeyMapping.Category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("feesh", "keybinds")) // Keys are localized in resources/assets/feesh/lang/en_us.json
    private val keybindCallbacks = mutableListOf<Pair<KeyMapping, () -> Unit>>()
    private var keybindsRegistered = false

    private fun registerKeyBindingCompat(keyBinding: KeyMapping) {
        KeyBindingHelper.registerKeyMapping(keyBinding)
    }

    fun init() {
        registerAllKeybinds()
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
    }

    private fun registerAllKeybinds() {
        if (keybindsRegistered) return

        registerKeybind("key.feesh.shareHotspotPartyChat", InputUtils.unboundKey()) {
            HotspotFoundMessage.shareNearestHotspotToParty()
        }
        registerKeybind("key.feesh.shareHotspotAllChat", InputUtils.unboundKey()) {
            HotspotFoundMessage.shareNearestHotspotToAll()
        }
        registerKeybind("key.feesh.lootshareToPartyChat", InputUtils.unboundKey()) {
            LootshareMessage.triggerLootshareMessage()
        }
        registerKeybind("key.feesh.resetBarnFishingTimer", InputUtils.unboundKey()) {
            BarnFishingTimer.triggerResetKeybind()
        }
        registerKeybind("key.feesh.pauseAllTrackers", InputUtils.pauseKey()) {
            PauseAllTrackersCommand.triggerPauseAllTrackers()
        }
        registerKeybind("key.feesh.bulkResetTrackers", InputUtils.unboundKey()) {
            BulkResetFishingSessionCommand.triggerBulkResetFishingSession()
        }

        keybindsRegistered = true
    }

    private fun registerKeybind(id: String, keyCode: Int, callback: () -> Unit): KeyMapping {
        val keyBinding = KeyMapping(
            id,
            InputUtils.keyType(),
            keyCode,
            FEESH_CATEGORY
        )
        registerKeyBindingCompat(keyBinding)
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
