package com.github.sleepypanda.feesh.events.models

import net.minecraft.world.entity.decoration.ArmorStand

/**
 * Event for when an Armor Stand is removed from the world (despawned/unloaded).
 * @param armorStand The ArmorStand that was removed.
 */
data class ArmorStandDespawnedEvent(
    val armorStand: ArmorStand
)
