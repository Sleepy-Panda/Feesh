package com.github.sleepypanda.feesh.utils.data

import com.github.sleepypanda.feesh.features.overlays.JerryWorkshopTracker
import com.github.sleepypanda.feesh.features.overlays.TreasureFishingTracker
import com.github.sleepypanda.feesh.features.overlays.SeaCreaturesTracker
import com.github.sleepypanda.feesh.features.overlays.WaterHotspotsAndBayouTracker
import com.github.sleepypanda.feesh.features.overlays.CrimsonIsleTracker
import com.github.sleepypanda.feesh.features.overlays.ArchfiendDiceProfitTracker
import com.github.sleepypanda.feesh.features.overlays.FishingProfitTracker
import com.github.sleepypanda.feesh.features.chat.RareDropMessage
import com.github.sleepypanda.feesh.utils.data.PersonalBestData

data class FeeshData(
    var isWelcomeMessageShown: Boolean = false,
    var lastChangelogShown: String = "0.0.0",
    var isFishingBagEnabled: Boolean? = null, // Null for unknown state,
    val jerryWorkshop: JerryWorkshopTracker.JerryWorkshopTrackerData = JerryWorkshopTracker.JerryWorkshopTrackerData(),
    val treasureFishing: TreasureFishingTracker.TreasureFishingData = TreasureFishingTracker.TreasureFishingData(),
    val seaCreatures: SeaCreaturesTracker.SeaCreaturesTrackerData = SeaCreaturesTracker.SeaCreaturesTrackerData(),
    val waterHotspotsAndBayou: WaterHotspotsAndBayouTracker.WaterHotspotsAndBayouTrackerData = WaterHotspotsAndBayouTracker.WaterHotspotsAndBayouTrackerData(),
    val crimsonIsle: CrimsonIsleTracker.CrimsonIsleTrackerData = CrimsonIsleTracker.CrimsonIsleTrackerData(),
    val archfiendDiceProfit: ArchfiendDiceProfitTracker.ArchfiendDiceProfitData = ArchfiendDiceProfitTracker.ArchfiendDiceProfitData(),
    val fishingProfit: FishingProfitTracker.FishingProfitData = FishingProfitTracker.FishingProfitData(),
    val rareDropNotifications: RareDropMessage.RareDropNotificationsData = RareDropMessage.RareDropNotificationsData(),
    val personalBest: PersonalBestData = PersonalBestData(),
)