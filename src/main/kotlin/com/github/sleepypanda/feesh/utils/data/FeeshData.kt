package com.github.sleepypanda.feesh.utils.data

import com.github.sleepypanda.feesh.features.overlays.JerryWorkshopTracker
import com.github.sleepypanda.feesh.features.overlays.TreasureFishingTracker
import com.github.sleepypanda.feesh.features.overlays.SeaCreaturesTracker

data class FeeshData(
    val isWelcomeMessageShown: Boolean = false,
    val lastVersionChangelogShown: String = "0.0.0",
    val isFishingBagEnabled: Boolean? = null, // Null for unknown state,
    val jerryWorkshop: JerryWorkshopTracker.JerryWorkshopTrackerData = JerryWorkshopTracker.JerryWorkshopTrackerData(),
    val treasureFishing: TreasureFishingTracker.TreasureFishingData = TreasureFishingTracker.TreasureFishingData(),
    val seaCreatures: SeaCreaturesTracker.SeaCreaturesTrackerData = SeaCreaturesTracker.SeaCreaturesTrackerData(),
)