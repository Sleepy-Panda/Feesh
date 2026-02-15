package com.github.sleepypanda.feesh.events.models

import net.minecraft.entity.decoration.ArmorStandEntity

/**
 * Event for when an Armor Stand is removed from the world (despawned/unloaded).
 * @param armorStand The ArmorStandEntity that was removed.
 */
data class ArmorStandDespawnedEvent(
    val armorStand: ArmorStandEntity
)
