package com.github.sleepypanda.feesh.settings.models

enum class BulkResettableTrackerTypes(val displayName: String, val isEnabledByDefault: Boolean = false, val hasSessionMode: Boolean = false) {
    SEA_CREATURES_TRACKER("Sea creatures tracker", isEnabledByDefault = true, hasSessionMode = true),
    FISHING_PROFIT_TRACKER("Fishing profit tracker", isEnabledByDefault = true, hasSessionMode = true),
    SEA_CREATURES_PER_HOUR_TRACKER("Sea creatures per hour tracker", isEnabledByDefault = true),
    JERRY_WORKSHOP_TRACKER("Jerry's Workshop tracker"),
    BAYOU_TRACKER("Bayou tracker"),
    WATER_HOTSPOTS_TRACKER("Water Hotspots tracker"),
    CRIMSON_ISLE_TRACKER("Crimson Isle tracker"),
    GALATEA_WATER_TRACKER("Galatea water tracker"),
    LOTUS_ATOLL_TRACKER("Lotus Atoll tracker"),
    TREASURE_FISHING_TRACKER("Treasure fishing tracker", hasSessionMode = true),
    MAGMA_CORE_FISHING_TRACKER("Magma Core fishing tracker", hasSessionMode = true);

    override fun toString(): String = displayName
}
