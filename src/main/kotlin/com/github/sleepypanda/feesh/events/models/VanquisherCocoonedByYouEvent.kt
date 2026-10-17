package com.github.sleepypanda.feesh.events.models

import com.github.sleepypanda.feesh.constants.SeaCreatures.SeaCreatureInfo

/**
 * Called when a Vanquisher is cocooned by you (based on chat message).
 * @param message The unformatted chat message.
 * @param vanquisher The Vanquisher record.
 */
data class VanquisherCocoonedByYouEvent(
    val message: String,
    val vanquisher: SeaCreatureInfo,
)
