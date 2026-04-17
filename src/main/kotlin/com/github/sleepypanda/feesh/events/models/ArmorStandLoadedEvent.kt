package com.github.sleepypanda.feesh.events.models

import net.minecraft.world.entity.decoration.ArmorStand

/**
 * Event for when an ArmorStand is loaded into the client world.
 * @param entity The ArmorStand that was loaded.
 */
data class ArmorStandLoadedEvent(
    val entity: ArmorStand
)
