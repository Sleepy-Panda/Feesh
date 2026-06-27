package com.github.sleepypanda.feesh.settings.models

import com.github.sleepypanda.feesh.features.overlays.base.IResettableTracker
import com.github.sleepypanda.feesh.features.overlays.base.IResettableViewModeTracker
import com.github.sleepypanda.feesh.features.overlays.BayouTracker
import com.github.sleepypanda.feesh.features.overlays.CrimsonIsleTracker
import com.github.sleepypanda.feesh.features.overlays.FishingProfitTracker
import com.github.sleepypanda.feesh.features.overlays.GalateaWaterTracker
import com.github.sleepypanda.feesh.features.overlays.JerryWorkshopTracker
import com.github.sleepypanda.feesh.features.overlays.LotusAtollTracker
import com.github.sleepypanda.feesh.features.overlays.MagmaCoreFishingTracker
import com.github.sleepypanda.feesh.features.overlays.EfficiencyTracker
import com.github.sleepypanda.feesh.features.overlays.SeaCreaturesTracker
import com.github.sleepypanda.feesh.features.overlays.TreasureFishingTracker
import com.github.sleepypanda.feesh.features.overlays.WaterHotspotsTracker

enum class BulkResettableTrackerTypes(
    val displayName: String,
    val isEnabledByDefault: Boolean = false,
    val hasSessionMode: Boolean = false,
    val resettableTracker: IResettableTracker? = null,
    val resettableViewModeTracker: IResettableViewModeTracker? = null,
) {
    SEA_CREATURES_TRACKER("Sea creatures tracker", isEnabledByDefault = true, hasSessionMode = true, resettableViewModeTracker = SeaCreaturesTracker),
    FISHING_PROFIT_TRACKER("Fishing profit tracker", isEnabledByDefault = true, hasSessionMode = true, resettableViewModeTracker = FishingProfitTracker),
    SEA_CREATURES_PER_HOUR_TRACKER("Efficiency tracker", isEnabledByDefault = true, resettableTracker = EfficiencyTracker),
    JERRY_WORKSHOP_TRACKER("Jerry's Workshop tracker", resettableTracker = JerryWorkshopTracker),
    BAYOU_TRACKER("Bayou tracker", resettableTracker = BayouTracker),
    WATER_HOTSPOTS_TRACKER("Water Hotspots tracker", resettableTracker = WaterHotspotsTracker),
    CRIMSON_ISLE_TRACKER("Crimson Isle tracker", resettableTracker = CrimsonIsleTracker),
    GALATEA_WATER_TRACKER("Galatea water tracker", resettableTracker = GalateaWaterTracker),
    LOTUS_ATOLL_TRACKER("Lotus Atoll tracker", resettableTracker = LotusAtollTracker),
    TREASURE_FISHING_TRACKER("Treasure fishing tracker", hasSessionMode = true, resettableViewModeTracker = TreasureFishingTracker),
    MAGMA_CORE_FISHING_TRACKER("Magma Core fishing tracker", hasSessionMode = true, resettableViewModeTracker = MagmaCoreFishingTracker);

    override fun toString(): String = displayName
}
