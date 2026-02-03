package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.RareDropEvent
import com.github.sleepypanda.feesh.constants.RareDropTypes
import com.github.sleepypanda.feesh.constants.RareDrops
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object RareDropMessage {
    fun init() {
        EventBus.subscribe(RareDropEvent::class, ::onDrop)
    }

    // TODO: Drop number #
    // TODO: settings.includeDropNumberIntoDropMessage
    private fun onDrop(event: RareDropEvent) {
        if (!WorldUtils.isInSkyblock() || !Chat.messageOnRareDrops) return

        val itemName = event.itemName
        val type = RareDropTypes.values().find { it.displayName == itemName } ?: return

        if (!Chat.messageOnRareDropTypes.contains(type)) return

        var metadata = listOf<String>()
        if (event.magicFind != null && Chat.includeMagicFindIntoRareDropMessage) {
            metadata += "+${event.magicFind} ✯ Magic Find"
        }

        val message = getDropMessage(itemName, metadata)
        ChatUtils.sendPartyChat(message)
    }

    private fun getDropMessage(itemName: String, metadata: List<String>): String {
        val article = CommonUtils.getArticle(itemName)
        val metadataString = if (metadata.isNotEmpty()) " (${metadata.joinToString(", ")})" else ""

        return "--> ${article} ${itemName} has dropped${metadataString} <--"
    }
}
