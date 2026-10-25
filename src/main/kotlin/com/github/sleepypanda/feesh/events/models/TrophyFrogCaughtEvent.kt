package com.github.sleepypanda.feesh.events.models

/**
 * Published when a trophy frog catch chat message is received.
 * @param trophyName Unformatted trophy frog name (e.g. "Bullfrog", "Wetlands Frog").
 * @param rarity Unformatted rarity (BRONZE, SILVER, GOLD, or DIAMOND).
 * @param chatEvent Cancellable chat event.
 */
class TrophyFrogCaughtEvent(
    val trophyName: String,
    val rarity: String,
    val chatEvent: ChatCancellableEvent
)
