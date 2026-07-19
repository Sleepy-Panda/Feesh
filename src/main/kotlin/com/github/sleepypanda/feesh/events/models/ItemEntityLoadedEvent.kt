package com.github.sleepypanda.feesh.events.models

import net.minecraft.world.entity.item.ItemEntity

/**
 * Event for when an ItemEntity is initially loaded into the client world.
 * @param itemEntity The ItemEntity that was loaded.
 */
data class ItemEntityLoadedEvent(
    val itemEntity: ItemEntity
)
