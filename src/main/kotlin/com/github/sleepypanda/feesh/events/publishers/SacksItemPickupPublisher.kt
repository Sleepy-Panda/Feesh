package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.constants.FishingProfitDrops
import com.github.sleepypanda.feesh.constants.FishingProfitDropInfo
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.events.models.SacksProfitItemsPickupEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.GuiUtils
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Component
import java.util.Date

object SacksItemPickupPublisher {
    private val SACKS_TRIGGER = Regex("^\\[Sacks\\] \\+.*") // [Sacks] +2,362 items, -2,362 items. (Last 16s.)
    private val ITEM_LINE_REGEX = Regex("(\\+[\\d,]+) (.+) \\((.+)\\)") // +1,344 Pufferfish (Fishing Sack)

    // 30 seconds is the maximum time to receive "[Sacks] +..." message after items were added to the sack
    private const val MAX_SACKS_MESSAGE_DELAY_MS = 35_000L

    // You Supercrafted Polished Pumpkin x7!
    private val SUPERCRAFTED_PATTERN = Regex("^You Supercrafted .*")
    private var lastSupercraftedMessage: String? = null
    private var lastSupercraftedAt: Date? = null

    fun init() {
        EventBus.subscribe(ChatCancellableEvent::class, ::onChat) // Message might be cancelled by sacks hider e.g. from SH
    }

    private fun onChat(event: ChatCancellableEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

        CommonUtils.runWithCatching("Failed to handle chat message in sacks item pickup publisher.") {
            val text = event.unformattedText
            when {
                SACKS_TRIGGER.matches(text) -> {
                    if (shouldSkipWholeSacksMessage()) return@onChat
    
                    val items = parseItemsFromSacksMessage(event.message)
                        .mapNotNull { (itemName, amount, sackName) -> toProfitPickupItem(itemName, amount, sackName) }
    
                    if (items.isNotEmpty()) {
                        EventBus.publish(SacksProfitItemsPickupEvent(items = items))
                    }
                }

                SUPERCRAFTED_PATTERN.matches(text) -> {
                    lastSupercraftedMessage = text
                    lastSupercraftedAt = Date()
                }
            }
        }
    }

    private fun shouldSkipWholeSacksMessage(): Boolean {
        if (GuiUtils.isInSacksGui()) return true

        val lastGuisClosed = GuiUtils.lastGuisClosed
        val now = Date().time
        if (lastGuisClosed.lastSacksGuiClosedAt != null && now - lastGuisClosed.lastSacksGuiClosedAt!!.time < MAX_SACKS_MESSAGE_DELAY_MS) return true
        return false
    }

    private fun toProfitPickupItem(itemName: String, amount: Int, sackName: String): SacksProfitItemsPickupEvent.SacksProfitPickupItem? {
        if (amount <= 0 || itemName.isBlank()) return null

        val cleanName = ItemUtils.getCleanItemName(itemName)
        val dropInfo = FishingProfitDrops.getFishingProfitItemByName(cleanName) ?: return null
        if (dropInfo.ignoreFromInventory) return null
        if (shouldSkipItem(dropInfo)) return null

        return SacksProfitItemsPickupEvent.SacksProfitPickupItem(
            itemId = dropInfo.itemId,
            itemNameUnformatted = dropInfo.itemName,
            amount = amount,
            sackName = sackName
        )
    }

    private fun shouldSkipItem(dropInfo: FishingProfitDropInfo): Boolean {
        val lastGuisClosed = GuiUtils.lastGuisClosed
        val now = Date().time

        fun wasPotentiallyFilletedTrophy(itemId: String): Boolean {
            if (itemId.startsWith("MAGMA_FISH") &&
                lastGuisClosed.lastOdgerGuiClosedAt != null && now - lastGuisClosed.lastOdgerGuiClosedAt!!.time < MAX_SACKS_MESSAGE_DELAY_MS) {
                return true
            } else if (itemId.startsWith("LOTUS") &&
                lastGuisClosed.lastTrophyFrogsGuiClosedAt != null && now - lastGuisClosed.lastTrophyFrogsGuiClosedAt!!.time < MAX_SACKS_MESSAGE_DELAY_MS) {
                return true
            }
            return false
        }

        fun wasPotentiallySupercrafted(dropInfo: FishingProfitDropInfo): Boolean {
            val supercraftedMessage = lastSupercraftedMessage ?: return false
            if (lastSupercraftedAt == null || now - lastSupercraftedAt!!.time >= MAX_SACKS_MESSAGE_DELAY_MS) return false
            return isItemContainedInText(dropInfo.itemName, dropInfo.itemAlternateNames, supercraftedMessage)
        }

        if (wasPotentiallyFilletedTrophy(dropInfo.itemId)) return true
        if (wasPotentiallySupercrafted(dropInfo)) return true

        return false
    }

    /**
     * Parses sack notification hover text: "Added items:" with lines like "+1,344 Pufferfish (Fishing Sack)".
     */
    private fun parseItemsFromSacksMessage(message: Component): List<Triple<String, Int, String>> {
        val items = mutableListOf<Triple<String, Int, String>>()
        message.siblings.forEach { part ->
            if (!part.getUnformattedString().contains(" item")) return@forEach

            val hover = part.style?.hoverEvent ?: return@forEach

            if (hover is HoverEvent.ShowText) {
                val line = hover.value.getUnformattedString()
                if (!line.contains("Added items:")) return@forEach

                ITEM_LINE_REGEX.findAll(line).forEach { match ->
                    val diffStr = match.groupValues[1].replace("+", "").replace(",", "")
                    val amount = diffStr.toIntOrNull() ?: 0
                    val parsedItemName = match.groupValues[2].trim().removeFormatting()
                    val sackName = match.groupValues[3].removeFormatting()
                    if (amount > 0 && parsedItemName.isNotBlank()) {
                        items.add(Triple(parsedItemName, amount, sackName))
                    }
                }
            }
        }
        return items
    }

    private fun isItemContainedInText(itemName: String, itemAlternateNames: List<String>, text: String): Boolean {
        if (text.isEmpty()) return false
        if (text.contains(itemName, ignoreCase = true)) return true
        return itemAlternateNames.any { text.contains(it, ignoreCase = true) }
    }
}
