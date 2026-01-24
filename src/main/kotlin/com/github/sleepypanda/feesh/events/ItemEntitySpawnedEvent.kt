package com.github.sleepypanda.feesh.events

import net.minecraft.entity.ItemEntity

/**
 * Event for when an ItemEntity appears in the world.
 * @param itemEntity The ItemEntity that was spawned.
 */
data class ItemEntitySpawnedEvent(
    val itemEntity: ItemEntity
)
