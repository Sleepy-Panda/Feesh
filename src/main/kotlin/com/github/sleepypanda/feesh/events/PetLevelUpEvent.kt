package com.github.sleepypanda.feesh.events

/**
 * Event for when a pet is levelled up to max level (100 or 200).
 * @param petName The name of the pet without formatting.
 * @param displayName The display name of the pet with formatting.
 * @param level The level the pet reached.
 */
data class PetLevelUpEvent(
    val petName: String,
    val petDisplayName: String,
    val level: Int
)
