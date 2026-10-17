package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.VanquisherCocoonedByYouEvent
import com.github.sleepypanda.feesh.events.models.VanquisherSpawnedEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.SeaCreatureCocoonedByYouEvent

object RareCatchMessage {
    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
        EventBus.subscribe(VanquisherSpawnedEvent::class, ::onVanquisherSpawned)
        EventBus.subscribe(SeaCreatureCocoonedByYouEvent::class, ::onSeaCreatureCocooned)
        EventBus.subscribe(VanquisherCocoonedByYouEvent::class, ::onVanquisherCocooned)
    }
    
    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        shareSpawn(event.seaCreatureName, event.isDoubleHook)
    }

    private fun onVanquisherSpawned(event: VanquisherSpawnedEvent) {
        shareSpawn(event.vanquisher.name, isDoubleHook = false)
    }

    private fun shareSpawn(name: String, isDoubleHook: Boolean) {
        if (!WorldUtils.isInSkyblock() || !Chat.shareRareSeaCreatures) return

        val enabledScNames = Chat.shareSeaCreaturesList.map { it.displayName }
        if (!enabledScNames.contains(name)) return

        ChatUtils.sendPartyChat(getRareCatchMessage(name, isDoubleHook))
    }

    private fun getRareCatchMessage(seaCreatureName: String, isDoubleHook: Boolean): String {
        val article = CommonUtils.getArticle(seaCreatureName)
        val scName = seaCreatureName.uppercase()
        return if (isDoubleHook) "--> DOUBLE HOOK! Two ${scName}s have spawned <--" 
            else "--> ${article} ${scName} has spawned <--"
    }

    private fun onSeaCreatureCocooned(event: SeaCreatureCocoonedByYouEvent) {
        shareCocoon(event.seaCreatureName)
    }

    private fun onVanquisherCocooned(event: VanquisherCocoonedByYouEvent) {
        shareCocoon(event.vanquisher.name)
    }

    private fun shareCocoon(name: String) {
        if (!WorldUtils.isInSkyblock() || !Chat.shareRareSeaCreatures || !Chat.shareSeaCreaturesIncludeCocooned) return

        val enabledScNames = Chat.shareSeaCreaturesList.map { it.displayName }
        if (!enabledScNames.contains(name)) return

        ChatUtils.sendPartyChat(getSeaCreatureCocoonedMessage(name))
    }

    private fun getSeaCreatureCocoonedMessage(seaCreatureName: String): String {
        val article = CommonUtils.getArticle(seaCreatureName)
        val scName = seaCreatureName.uppercase()
        return "--> ${article} ${scName} was cocooned <--"
    }
}
