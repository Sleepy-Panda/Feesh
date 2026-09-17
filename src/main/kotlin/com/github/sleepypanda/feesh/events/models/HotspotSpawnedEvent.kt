package com.github.sleepypanda.feesh.events.models

/**
 * Fired when a fishing Hotspot armor stand and its perk nametag are both loaded.
 * @param position World position of the HOTSPOT armor stand.
 * @param perk Formatted perk nametag (the stand at hotspot id + 1).
 * @param hotspotArmorStandId Numeric entity id of the HOTSPOT armor stand.
 */
data class HotspotSpawnedEvent(
    val position: Position,
    val perk: String,
    val hotspotArmorStandId: Int,
) {
    data class Position(
        val x: Double,
        val y: Double,
        val z: Double,
    )
}
