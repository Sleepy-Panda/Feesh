package com.github.sleepypanda.feesh.events.models

/**
 * Event for when a fishing profit drop item is added to the player inventory.
 * Published when inventory scanning detects a count increase. Only for fishing profit drops.
 *
 * @param itemId SkyBlock item ID aligned with Bazaar/Auction APIs.
 * @param itemName Unformatted item name.
 * @param previousCount Count in inventory before the pickup.
 * @param newCount Count in inventory after the pickup.
 * @param amount Amount added (newCount - previousCount).
 */
data class InventoryProfitItemPickupEvent(
    val itemId: String,
    val itemNameUnformatted: String,
    val previousCount: Int,
    val newCount: Int,
    val amount: Int
)
