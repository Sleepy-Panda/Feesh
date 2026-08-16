package com.github.sleepypanda.feesh.events.models

/**
 * Published when a trophy fish catch chat message is received.
 * @param trophyName Unformatted trophy fish name (e.g. "Lavahorse", "Steaming-Hot Flounder").
 * @param rarity Unformatted rarity (BRONZE, SILVER, GOLD, or DIAMOND).
 * @param chatEvent Cancellable chat event.
 */
class TrophyFishCaughtEvent(
    val trophyName: String,
    val rarity: String,
    val chatEvent: ChatCancellableEvent
)
