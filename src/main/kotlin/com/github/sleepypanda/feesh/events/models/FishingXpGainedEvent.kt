package com.github.sleepypanda.feesh.events.models

/**
 * Published when action/status bar contains a Fishing XP gain segment.
 * Example: "+2,696.4 Fishing (2,811,372,886/0)"
 */
class FishingXpGainedEvent(
    val amount: Double
)
