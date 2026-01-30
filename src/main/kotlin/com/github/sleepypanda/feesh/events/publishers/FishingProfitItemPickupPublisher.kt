package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.FishingProfitDrops
import com.github.sleepypanda.feesh.constants.FishingProfitDropInfo
import com.github.sleepypanda.feesh.events.ClientTickEvent
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.FishingProfitItemPickupEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.google.gson.JsonParser
import net.minecraft.component.DataComponentTypes
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack

object FishingProfitItemPickupPublisher {
    private const val TICKS_INVENTORY = 5

    private var previousInventory: MutableMap<String, Int>? = null
    private var tickCounter = 0

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter % TICKS_INVENTORY != 0) return

        if (!WorldUtils.isInSkyblock()) return
        if (!WorldUtils.isInFishingWorld()) return

        if (previousInventory == null) {
            previousInventory = getTrackedItemsInCurrentInventory().toMutableMap()
            return
        }

        if (isPlayerMovingItem()) return

        val currentInventory = getTrackedItemsInCurrentInventory()
        val screen = FeeshMod.mc.currentScreen
        if (screen != null && screen is HandledScreen<*>) { // When in chest
            previousInventory = currentInventory.toMutableMap()
            return
        }

        for ((itemId, currentTotal) in currentInventory) {
            val previousTotal = previousInventory!![itemId] ?: 0
            if (currentTotal > previousTotal) {
                val difference = currentTotal - previousTotal
                val dropInfo = FishingProfitDrops.items.find { it.itemId == itemId }
                if (dropInfo != null) {
                    EventBus.publish(FishingProfitItemPickupEvent(itemId = itemId, itemName = dropInfo.itemName, difference = difference))
                }
            }
        }

        previousInventory = currentInventory.toMutableMap()
    }

    private fun getTrackedItemsInCurrentInventory(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        val player = FeeshMod.mc.player ?: return result

        for (i in 0..35) {
            val stack = player.inventory.getStack(i)
            if (stack.isEmpty) continue

            var slotItemName = getCleanItemName(stack.name.getFormattedString())
            if (slotItemName.isNullOrBlank()) continue

            if (slotItemName == "Enchanted Book") {
                val description = getBookName(stack)
                slotItemName += " ($description)"
            }

            if (slotItemName.endsWith("Exp Boost")) {
                val description = getExpBoostRarity(stack)
                slotItemName += " ($description)"
            }

            if (slotItemName.startsWith("[Lvl 1] ")) {
                val rarity = getPetRarity(stack)
                slotItemName += " (${rarity})"
            }

            val dropInfo = getTrackedItemByName(slotItemName)
            if (dropInfo != null) {
                result[dropInfo.itemId] = (result[dropInfo.itemId] ?: 0) + stack.count
            }
        }
        
        return result
    }

    private fun getCleanItemName(itemName: String): String {
        if (itemName.isBlank()) return ""
        var s = itemName
        if (Regex(".+ §8x\\d+$").matches(s)) { // Booster cookie menu or NPCs append the amount to the item name - e.g. §9Fish Affinity Talisman §8x1
            s = s.split(" ").dropLast(1).joinToString(" ")
        }
        return s.removeFormatting()
    }

    private fun getTrackedItemByName(itemName: String): FishingProfitDropInfo? {
        val lower = itemName.lowercase()
        return FishingProfitDrops.items.find {
            it.itemName.lowercase() == lower || it.itemAlternateNames.any { alt -> alt.lowercase() == lower }
        }
    }

    private fun isPlayerMovingItem(): Boolean {
        val player = FeeshMod.mc.player ?: return false
        val cursor = player.currentScreenHandler.cursorStack
        return !cursor.isEmpty
    }

    private fun getBookName(stack: ItemStack): String {
        val loreLines = stack.get(DataComponentTypes.LORE)?.lines?.map { it.string } ?: emptyList()
        if (loreLines.size > 0) {
            val description = loreLines[0]
            return description
        }
        return ""
    }

    private fun getExpBoostRarity(stack: ItemStack): String {
        val loreLines = stack.get(DataComponentTypes.LORE)?.lines?.map { it.string } ?: emptyList()
        val petItemLine = loreLines.find { it.endsWith("PET ITEM") }
        if (petItemLine != null) {
            val description = petItemLine.split(" ").firstOrNull() ?: ""
            return description
        }
        return ""
    }

    private fun getPetRarity(stack: ItemStack): String {
        val customData = ItemUtils.getCustomData(stack)
        if (customData != null && ItemUtils.getCustomDataId(customData) == "PET") {
            val petInfoStr = ItemUtils.getCustomDataPetInfo(customData)
            val rarity = petInfoStr?.let { s ->
                try {
                    JsonParser.parseString(s).asJsonObject.get("tier")?.takeIf { it.isJsonPrimitive }?.asString
                } catch (_: Exception) {
                    null
                }
            }
            return rarity?.uppercase() ?: ""
        }
        return ""
    }
}
