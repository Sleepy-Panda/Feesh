package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.RareDropEvent
import com.github.sleepypanda.feesh.constants.RareDropTypes
import com.github.sleepypanda.feesh.constants.RareDrops
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.WorldUtils

// TODO It would be great to get drop number from fishing profit tracker instead of tracking it here,
// But it requires a little exploration how to do it in a clean way.

object RareDropMessage {
    data class RareDropNotificationItem(
        var count: Int = 0
    )

    data class RareDropNotificationsData(
        val items: MutableMap<String, RareDropNotificationItem> = mutableMapOf()
    )

    fun init() {
        EventBus.subscribe(RareDropEvent::class, ::onDrop)
    }

    fun reset() {
        PersistentDataManager.feeshData.rareDropNotifications.items.clear()
        saveData()
    }

    private fun onDrop(event: RareDropEvent) {
        CommonUtils.runWithCatching("Failed to send rare drop message") {
            if (!WorldUtils.isInSkyblock() || !Chat.messageOnRareDrops) return

            val itemName = event.itemName
            val type = RareDropTypes.values().find { it.displayName == itemName } ?: return
    
            if (!Chat.messageOnRareDropTypes.contains(type)) return
    
            var metadata = listOf<String>()
            val dropNumber = getDropNumber(itemName)
            if (dropNumber != null) {
                metadata += "#$dropNumber"
            }
            if (event.magicFind != null && Chat.includeMagicFindIntoRareDropMessage) {
                metadata += "+${event.magicFind} ✯ Magic Find"
            }
    
            val message = getDropMessage(itemName, metadata)
            ChatUtils.sendPartyChat(message) 
        }
    }

    private fun getDropNumber(itemName: String): Int? {
        if (!Chat.includeDropNumberIntoDropMessage || !Overlays.fishingProfitTrackerOverlay) return null

        val dropInfo = RareDrops.rareDrops.find { it.itemName == itemName } ?: return null
        val items = PersistentDataManager.feeshData.rareDropNotifications.items
        val currentCount = items[dropInfo.id]?.count ?: 0
        val newCount = currentCount + 1
        items[dropInfo.id] = RareDropNotificationItem(newCount)
        saveData()

        return newCount
    }

    private fun getDropMessage(itemName: String, metadata: List<String>): String {
        val article = CommonUtils.getArticle(itemName)
        val metadataString = if (metadata.isNotEmpty()) " (${metadata.joinToString(", ")})" else ""

        return "--> ${article} ${itemName} has dropped${metadataString} <--"
    }

    private fun saveData() {
        PersistentDataManager.saveFeeshDataToFileAsync()
    }
}
