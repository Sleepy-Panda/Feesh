package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.ChatEvent
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.SacksItemsPickupEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text

object SacksItemPickupPublisher {
    private val SACKS_TRIGGER = Regex("^\\[Sacks\\] \\+.*") // [Sacks] +2,362 items, -2,362 items. (Last 16s.)
    private val ITEM_LINE_REGEX = Regex("(\\+[\\d,]+) (.+) \\((.+)\\)") // +1,344 Pufferfish (Fishing Sack)

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock()) return

        val message = event.message
        if (!SACKS_TRIGGER.matches(message.string)) return

        val items = parseItemsFromSacksMessage(message)
            .filter { (itemName, amount, _) -> amount > 0 && itemName.isNotBlank() }
            .map { (itemName, amount, sackName) ->
                SacksItemsPickupEvent.SacksPickupItem(itemName = itemName, amount = amount, sackName = sackName)
            }
            
        if (items.isNotEmpty()) {
            EventBus.publish(SacksItemsPickupEvent(items = items))
        }
    }

    /**
     * Parses sack notification hover text: "Added items:" with lines like "+1,344 Pufferfish (Fishing Sack)".
     */
    private fun parseItemsFromSacksMessage(message: Text): List<Triple<String, Int, String>> {
        val items = mutableListOf<Triple<String, Int, String>>()
        message.siblings.forEach { part ->
            if (!part.string.contains(" item")) return@forEach

            val hover = part.style?.hoverEvent ?: return@forEach

            if (hover is HoverEvent.ShowText) {
                val line = hover.value.string
                if (!line.contains("Added items:")) return@forEach

                ITEM_LINE_REGEX.findAll(line).forEach { match ->
                    val diffStr = match.groupValues[1].replace("+", "").replace(",", "")
                    val amount = diffStr.toIntOrNull() ?: 0
                    val itemName = match.groupValues[2].trim()
                    val sackName = match.groupValues[3].removeFormatting()
                    if (amount > 0 && itemName.isNotBlank()) {
                        items.add(Triple(itemName, amount, sackName))
                    }
                }
            }
        }
        return items
    }
}
