package com.github.sleepypanda.feesh.events.models

import com.github.sleepypanda.feesh.constants.SeaCreatures.SeaCreatureInfo

/**
 * Called when a sea creature is cocooned by you (based on chat message).
 * @param seaCreatureName The unformatted name of the sea creature.
 * @param catchMessage The unformatted chat message of the sea creature.
 * @param seaCreatureInfo The info about the sea creature.
 */
data class SeaCreatureCocoonedByYouEvent(
    val seaCreatureName: String,
    val catchMessage: String,
    val seaCreatureInfo: SeaCreatureInfo
)
