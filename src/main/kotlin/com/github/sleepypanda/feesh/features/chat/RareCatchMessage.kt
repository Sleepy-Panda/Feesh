package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.SeaCreatureCocoonedByYouEvent

object RareCatchMessage {
    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
        EventBus.subscribe(SeaCreatureCocoonedByYouEvent::class, ::onSeaCreatureCocooned)
    }
    
    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!WorldUtils.isInSkyblock() || !Chat.shareRareSeaCreatures) return

        val seaCreatureName = event.seaCreatureName
        val enabledScNames = Chat.shareSeaCreaturesList.map { it.displayName }
        if (!enabledScNames.contains(seaCreatureName)) return

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

    private fun onSeaCreatureCocooned(event: SeaCreatureCocoonedByYouEvent) {
        if (!WorldUtils.isInSkyblock() || !Chat.shareRareSeaCreatures || !Chat.shareSeaCreaturesIncludeCocooned) return

        val seaCreatureName = event.seaCreatureName
        val enabledScNames = Chat.shareSeaCreaturesList.map { it.displayName }
        if (!enabledScNames.contains(seaCreatureName)) return

        ChatUtils.sendPartyChat(getSeaCreatureCocoonedMessage(seaCreatureName))
    }

    private fun getSeaCreatureCocoonedMessage(seaCreatureName: String): String {
        val article = CommonUtils.getArticle(seaCreatureName)
        val scName = seaCreatureName.uppercase()
        return "--> ${article} ${scName} was cocooned <--"
    }
}
