package com.github.sleepypanda.feesh.events.models

import net.minecraft.entity.decoration.ArmorStandEntity

/**
 * Event for when a ArmorStandEntity is loaded into the client world.
 * @param entity The ArmorStandEntity that was loaded.
 */
data class ArmorStandLoadedEvent(
    val entity: ArmorStandEntity
)