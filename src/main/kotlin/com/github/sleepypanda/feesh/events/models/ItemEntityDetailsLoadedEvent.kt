package com.github.sleepypanda.feesh.events.models

import net.minecraft.world.entity.item.ItemEntity

/**
 * Fired on the client tick after [ItemEntityLoadedEvent], once the item stack display name is readable.
 */
data class ItemEntityDetailsLoadedEvent(
    val entity: ItemEntity,
    val entityId: Int,
    val itemNameFormatted: String,
    val itemNameUnformatted: String
)
