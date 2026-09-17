package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.constants.AlertableRareDrops
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager

object RareDropAlertUtils {
    data class RareDropNotificationItem(
        var count: Int = 0
    )

    data class RareDropNotificationsData(
        val items: MutableMap<String, RareDropNotificationItem> = mutableMapOf()
    )

    fun findAlertableDropInfo(itemName: String) =
        AlertableRareDrops.rareDrops.find { it.itemName == itemName || it.alternateNames.contains(itemName) }

    fun trackAlertableDrop(itemId: String): Int {
        val items = PersistentDataManager.feeshData.rareDropNotifications.items
        val newCount = (items[itemId]?.count ?: 0) + 1
        items[itemId] = RareDropNotificationItem(newCount)
        saveData()
        return newCount
    }

    fun hasData(): Boolean =
        PersistentDataManager.feeshData.rareDropNotifications.items.isNotEmpty()

    fun reset(force: Boolean = false) {
        PersistentDataManager.feeshData.rareDropNotifications.items.clear()
        saveData(force)
    }

    private fun saveData(force: Boolean = false) {
        if (force) {
            PersistentDataManager.forceSaveFeeshDataToFileSync()
        } else {
            PersistentDataManager.saveFeeshDataToFileAsync()
        }
    }
}
