package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypes
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.SeaCreatureSpawnedEvent

object RareCatchMessage {
    fun init() {
        // TODO: Add Vanquisher
        // TODO: DOUBLE HOOK
        EventBus.subscribe(SeaCreatureSpawnedEvent::class, ::onSeaCreature)
    }
    
    private fun onSeaCreature(event: SeaCreatureSpawnedEvent) {
        if (!WorldUtils.isInSkyblock() || !Chat.shareRareSeaCreatures) return

        val seaCreatureName = event.seaCreatureName
        var seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name == event.seaCreatureName } ?: return
        if (!seaCreatureInfo.isRare) return

        val type = try {
            RareSeaCreatureTypes.valueOf(seaCreatureName.uppercase().replace(" ", "_"))
        } catch (_: IllegalArgumentException) {
            return
        }

        if (!Chat.shareSeaCreaturesTypes.contains(type)) return
        val isDoubleHook = ChatUtils.isDoubleHook()

        val message = getRareCatchMessage(seaCreatureName, isDoubleHook)
        ChatUtils.sendPartyChat(message)
    }

    private fun getRareCatchMessage(name: String, isDoubleHook: Boolean): String {
        return if (isDoubleHook) "DOUBLE FEESH! ${name}" else "FEESH! ${name}"
    }
}
