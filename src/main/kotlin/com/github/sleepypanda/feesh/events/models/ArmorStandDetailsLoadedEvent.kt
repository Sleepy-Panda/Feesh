package com.github.sleepypanda.feesh.events.models

import net.minecraft.world.entity.decoration.ArmorStand

/**
 * Fired on the client tick after [ArmorStandLoadedEvent], once the stand's custom name is readable.
 */
data class ArmorStandDetailsLoadedEvent(
    val entity: ArmorStand,
    val entityId: Int,
    val customNameFormatted: String,
    val customNameUnformatted: String
)
