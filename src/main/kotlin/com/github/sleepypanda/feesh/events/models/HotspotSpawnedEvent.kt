package com.github.sleepypanda.feesh.events.models

import kotlin.math.floor

/**
 * Fired when a fishing Hotspot armor stand and its perk nametag are both loaded.
 * @param id Stable ID made from hotspot position and perk.
 * @param position World position of the HOTSPOT armor stand.
 * @param perkFormatted Formatted perk nametag (the stand at hotspot ID + 1).
 * @param perkUnformatted Unformatted perk nametag (the stand at hotspot ID + 1).
 * @param hotspotArmorStandId Numeric entity ID of the HOTSPOT armor stand.
 */
data class HotspotSpawnedEvent(
    val id: String,
    val position: Position,
    val perkFormatted: String,
    val perkUnformatted: String,
    val hotspotArmorStandId: Int,
) {
    data class Position(
        val x: Double,
        val y: Double,
        val z: Double,
    )

    companion object {
        fun getHotspotId(x: Double, y: Double, z: Double, perkUnformatted: String): String {
            return "${floor(x).toInt()}:${floor(y).toInt()}:${floor(z).toInt()}:$perkUnformatted"
        }
    }
}
