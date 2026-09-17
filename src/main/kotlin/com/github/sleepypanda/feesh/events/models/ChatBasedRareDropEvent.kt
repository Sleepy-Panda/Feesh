package com.github.sleepypanda.feesh.events.models

/**
 * Event for a rare fishing drop happened (based on chat messages: RARE DROP! <Item name>).
 * @param itemNameUnformatted The unformatted name of the item that was dropped. [matches RareDrops.itemName].
 * @param itemNameFormatted The formatted name of the item that was dropped.
 * @param magicFind The magic find of the drop.
 * @param dropNumber Session ordinal number for this item.
 */
data class ChatBasedRareDropEvent(
    val itemNameUnformatted: String,
    val itemNameFormatted: String,
    val magicFind: Int? = null,
    val dropNumber: Int
)
