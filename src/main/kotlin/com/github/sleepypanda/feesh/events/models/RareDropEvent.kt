package com.github.sleepypanda.feesh.events.models

/**
 * Event for when a rare fishing drop is found (based on chat messages).
 * @param itemName The name of the item that was dropped. Contains no formatting. Canonical RareDrops.itemName.
 * @param itemDisplayName The display name of the item that was dropped. Contains formatting.
 * @param magicFind The magic find of the drop.
 * @param dropNumber Session ordinal number for this item.
 */
data class RareDropEvent(
    val itemName: String,
    val itemDisplayName: String,
    val magicFind: Int? = null,
    val dropNumber: Int
)
