package com.github.sleepypanda.feesh.events.models

/**
 * Fired when a new Trophy Frog discovery message is received on Lotus Atoll.
 * @param detailsFormatted Colored discovery details (e.g. "Blessed Frog BRONZE" + formatting codes).
 */
class TrophyFrogDiscoveredEvent(val detailsFormatted: String)
