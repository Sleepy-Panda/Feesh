package com.github.sleepypanda.feesh.events.models

/**
 * Fired when an ArmorStand's custom name is updated (e.g. became non-empty after being loaded, or mob HP is changed).
 */
data class ArmorStandCustomNameChangedEvent(
    val entityId: Int,
    val isFirstLoaded: Boolean, // if first saw the armor stand, its previous custom name will be empty
    val previousCustomName: CustomName,
    val customName: CustomName,
    val position: Position,
) {
    data class CustomName(
        val formatted: String,
        val unformatted: String,
    )

    data class Position(
        val x: Double,
        val y: Double,
        val z: Double,
    )
}
