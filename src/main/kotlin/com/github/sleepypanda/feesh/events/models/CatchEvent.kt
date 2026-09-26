package com.github.sleepypanda.feesh.events.models

/**
 * Published once per successful catch (treasure, junk, trophy, sea creature, or trash like raw fish which has no catch message).
 * [itemNameFormatted] and [itemNameUnformatted] are set for trash catches when a dropped item is found near the bobber.
 */
data class CatchEvent(
    val isTreasureCatch: Boolean,
    val isJunkCatch: Boolean,
    val isSeaCreature: Boolean,
    val isTrash: Boolean = false,
    val itemNameFormatted: String? = null,
    val itemNameUnformatted: String? = null,
)
