package com.github.sleepypanda.feesh.events

/**
 * Event for when items are added to a sack (parsed from [Sacks] +... chat message).
 * Posted once per item type.
 *
 * @param itemName Name of the item (without formatting).
 * @param amount Number of items added.
 * @param sackName Name of the sack, e.g. "Fishing Sack".
 */
data class SacksItemPickupEvent(
    val itemName: String,
    val amount: Int,
    val sackName: String
)
