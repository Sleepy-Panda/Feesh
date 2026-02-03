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
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent

object RareCatchMessage {
    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
    }
    
    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!WorldUtils.isInSkyblock() || !Chat.shareRareSeaCreatures) return

        val seaCreatureName = event.seaCreatureName
        var seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name == event.seaCreatureName } ?: return
        if (!seaCreatureInfo.isRare) return

        val type = try {
            RareSeaCreatureTypes.valueOf(seaCreatureName.uppercase().replace(" ", "_"))
        } catch (_: IllegalArgumentException) {
            return
        }

        if (!Chat.shareRareSeaCreaturesTypes.contains(type)) return
        val isDoubleHook = event.isDoubleHook

        val message = getRareCatchMessage(seaCreatureName, isDoubleHook)
        ChatUtils.sendPartyChat(message)
    }

    private fun getRareCatchMessage(seaCreatureName: String, isDoubleHook: Boolean): String {
        val article = CommonUtils.getArticle(seaCreatureName)
        val scName = seaCreatureName.uppercase()
        return if (isDoubleHook) "--> DOUBLE HOOK! Two ${scName}s have spawned <--" 
            else "--> ${article} ${scName} has spawned <--"
    }
}
