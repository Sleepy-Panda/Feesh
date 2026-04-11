package com.github.sleepypanda.feesh.events.models

import net.minecraft.entity.decoration.ArmorStandEntity

/**
 * Fired on the client tick after [ArmorStandLoadedEvent], once the stand's custom name is readable.
 */
data class ArmorStandDetailsLoadedEvent(
    val entity: ArmorStandEntity,
    val entityId: Int,
    val customNameFormatted: String,
    val customNameUnformatted: String
)
