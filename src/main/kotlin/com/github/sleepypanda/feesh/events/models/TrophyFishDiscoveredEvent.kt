package com.github.sleepypanda.feesh.events.models

/**
 * Fired when a new Trophy Fish discovery message is received.
 * @param detailsFormatted Colored discovery details (e.g. "Mana Ray BRONZE" + formatting codes).
 */
class TrophyFishDiscoveredEvent(val detailsFormatted: String)
