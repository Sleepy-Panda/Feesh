package com.github.sleepypanda.feesh.events.models

/**
 * Event for when fishing profit drop items are added to sacks (parsed from [Sacks] +... chat message).
 * Posted once per message with items that pass fishing-drop and GUI filters.
 *
 * @param items List of items added: each has item ID, unformatted name, amount, and sack name (e.g. "Fishing Sack").
 */
data class SacksProfitItemsPickupEvent(val items: List<SacksProfitPickupItem>) {
    data class SacksProfitPickupItem(
        val itemId: String,
        val itemNameUnformatted: String,
        val amount: Int,
        val sackName: String
    )
}
