package com.github.sleepypanda.feesh.events.models

/**
 * Event for when fishing profit drop items move through sacks (parsed from a [Sacks] chat message).
 * Posted once per message with [Sacks] fishing items.
 *
 * @param addedItems Items in the "Added items" hover.
 * @param removedItems Items in the "Removed items" hover.
 */
data class SacksProfitItemsPickupEvent(
    val addedItems: List<SacksProfitPickupItem>,
    val removedItems: List<SacksProfitPickupItem> = emptyList(),
) {
    data class SacksProfitPickupItem(
        val itemId: String,
        val itemNameUnformatted: String,
        val amount: Int,
        val sackName: String
    )
}
