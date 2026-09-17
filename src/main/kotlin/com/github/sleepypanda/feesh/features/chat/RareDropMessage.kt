package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatBasedRareDropEvent
import com.github.sleepypanda.feesh.constants.RareDropTypes
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RareDropAlertUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object RareDropMessage {
    fun init() {
        EventBus.subscribe(ChatBasedRareDropEvent::class, ::onDrop)
    }

    private fun onDrop(event: ChatBasedRareDropEvent) {
        CommonUtils.runWithCatching("Failed to send rare drop message") {
            if (!WorldUtils.isInSkyblock() || !Chat.messageOnRareDrops) return

            val dropInfo = RareDropAlertUtils.findAlertableDropInfo(event.itemNameUnformatted) ?: return
            val type = RareDropTypes.values().find { it.displayName == dropInfo.itemName } ?: return

            if (!Chat.messageOnRareDropTypes.contains(RareDropTypes.ALL) && !Chat.messageOnRareDropTypes.contains(type)) return

            var metadata = listOf<String>()
            if (Chat.includeDropNumberIntoDropMessage) {
                metadata += "#${event.dropNumber}"
            }
            if (event.magicFind != null && Chat.includeMagicFindIntoRareDropMessage) {
                metadata += "+${event.magicFind} ✯ Magic Find"
            }
    
            val message = getDropMessage(dropInfo.itemName, metadata)
            ChatUtils.sendPartyChat(message) 
        }
    }

    private fun getDropMessage(itemName: String, metadata: List<String>): String {
        val article = CommonUtils.getArticle(itemName)
        val metadataString = if (metadata.isNotEmpty()) " (${metadata.joinToString(", ")})" else ""

        return "--> ${article} ${itemName} has dropped${metadataString} <--"
    }
}
