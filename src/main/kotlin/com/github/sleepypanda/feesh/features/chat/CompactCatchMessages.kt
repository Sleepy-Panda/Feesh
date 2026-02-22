package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.settings.categories.Chat as ChatSettings
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent

object CompactCatchMessages {
    const val DEFAULT_DOUBLE_HOOK_TEMPLATE = "§b§lDOUBLE HOOK!"
    const val DEFAULT_CATCH_TEMPLATE = "§7{Article} {sc} §7has spawned!"

    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!WorldUtils.isInSkyblock() || !ChatSettings.compactSeaCreaturesMessages) return

        val seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name == event.seaCreatureName } ?: return
        val isDoubleHook = event.isDoubleHook

        val catchTemplate = (ChatSettings.compactCatchMessageTemplate.firstOrNull() ?: "").ifEmpty { DEFAULT_CATCH_TEMPLATE }
        val articleLower = CommonUtils.getArticle(seaCreatureInfo.name, makeLowerCase = true)
        val articleCapitalized = CommonUtils.getArticle(seaCreatureInfo.name, makeLowerCase = false)
        val catchPart = catchTemplate
            .replace("{Article}", articleCapitalized)
            .replace("{article}", articleLower)
            .replace("{sc}", seaCreatureInfo.boldDisplayName)

        val fullMessage = if (isDoubleHook) {
            val dhTemplate = (ChatSettings.compactDoubleHookMessageTemplate.firstOrNull() ?: "").ifEmpty { DEFAULT_DOUBLE_HOOK_TEMPLATE }
            "$dhTemplate $catchPart"
        } else {
            catchPart
        }
        ChatUtils.sendLocalChat(fullMessage)
    }
}
