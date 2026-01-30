package com.github.sleepypanda.feesh.events

/**
 * Event for when items are added to sacks (parsed from [Sacks] +... chat message).
 * Posted once per message with all picked up items.
 *
 * @param items List of items added: each has name (without formatting), amount, and sack name (e.g. "Fishing Sack").
 */
data class SacksItemsPickupEvent(val items: List<SacksPickupItem>) {
    /**
     * Single item entry from a sack pickup.
     */
    data class SacksPickupItem(
        val itemName: String,
        val amount: Int,
        val sackName: String
    )
}
