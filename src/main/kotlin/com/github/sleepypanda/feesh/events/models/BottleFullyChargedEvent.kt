package com.github.sleepypanda.feesh.events.models

/**
 * Published when a Thunder, Storm, or Hurricane bottle is fully charged.
 * @param itemId SkyBlock item ID of a filled bottle.
 * @param itemName Unformatted item name.
 * @param itemDisplayName Formatted item name.
 */
data class BottleFullyChargedEvent(
    val itemId: String,
    val itemName: String,
    val itemDisplayName: String,
)
