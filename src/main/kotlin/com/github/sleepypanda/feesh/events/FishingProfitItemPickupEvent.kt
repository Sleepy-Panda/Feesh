package com.github.sleepypanda.feesh.events

/**
 * Event for when the player picks up items that are tracked for fishing profit
 * (detected by comparing current inventory snapshot with the previous one).
 *
 * @param itemId Item ID (aligned with FishingProfitDrops).
 * @param itemName Unformatted item name (from FishingProfitDrops).
 * @param difference Number of items picked up (newCount - previousCount).
 */
data class FishingProfitItemPickupEvent(
    val itemId: String,
    val itemName: String,
    val difference: Int
)
