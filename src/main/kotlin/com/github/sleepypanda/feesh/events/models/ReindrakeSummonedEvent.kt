package com.github.sleepypanda.feesh.events.models

/**
 * Fired when a Reindrake is summoned in Jerry's Workshop.
 * @param playerNameAndRank Formatted rank and player name who summoned it.
 * @param isDoubleHook True when two Reindrakes were summoned.
 */
data class ReindrakeSummonedEvent(
    val playerNameAndRank: String,
    val isDoubleHook: Boolean
)
