package com.github.sleepypanda.feesh.events.models

/**
 * Published once per successful catch (treasure, junk, trophy, sea creature, or trash like raw fish which has no catch message).
 */
data class CatchEvent(
    val isTreasureCatch: Boolean,
    val isJunkCatch: Boolean,
    val isSeaCreature: Boolean,
)
