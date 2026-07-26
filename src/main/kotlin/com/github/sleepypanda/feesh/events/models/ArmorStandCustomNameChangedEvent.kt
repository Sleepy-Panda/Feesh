package com.github.sleepypanda.feesh.events.models

import net.minecraft.world.entity.decoration.ArmorStand

/**
 * Fired when an ArmorStand's custom name is updated (e.g. became non-empty after being loaded, or mob HP is changed).
 */
data class ArmorStandCustomNameChangedEvent(
    val entity: ArmorStand,
    val entityId: Int,
    val isFirstLoaded: Boolean, // if first saw the armor stand, its previous custom name will be empty
    val previousCustomNameFormatted: String,
    val previousCustomNameUnformatted: String,
    val customNameFormatted: String,
    val customNameUnformatted: String
)
