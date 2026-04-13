package com.github.sleepypanda.feesh.utils.data

import com.github.sleepypanda.feesh.features.overlays.JerryWorkshopTracker
import com.github.sleepypanda.feesh.features.overlays.TreasureFishingTracker
import com.github.sleepypanda.feesh.features.overlays.SeaCreaturesTracker
import com.github.sleepypanda.feesh.features.overlays.BayouTracker
import com.github.sleepypanda.feesh.features.overlays.WaterHotspotsTracker
import com.github.sleepypanda.feesh.features.overlays.CrimsonIsleTracker
import com.github.sleepypanda.feesh.features.overlays.GalateaWaterTracker
import com.github.sleepypanda.feesh.features.overlays.ArchfiendDiceProfitTracker
import com.github.sleepypanda.feesh.features.overlays.FishingProfitTracker
import com.github.sleepypanda.feesh.features.overlays.MagmaCoreFishingTracker
import com.github.sleepypanda.feesh.features.chat.RareDropMessage
import com.github.sleepypanda.feesh.utils.data.PersonalBestData
import com.github.sleepypanda.feesh.features.overlays.CatchCounterData
import com.github.sleepypanda.feesh.features.overlays.DropCounterData

data class FeeshData(
    var isWelcomeMessageShown: Boolean = false,
    var lastChangelogShown: String = "0.0.0",
    var isFishingBagEnabled: Boolean? = null, // Null for unknown state,
    val jerryWorkshop: JerryWorkshopTracker.JerryWorkshopTrackerData = JerryWorkshopTracker.JerryWorkshopTrackerData(),
    val treasureFishing: TreasureFishingTracker.TreasureFishingData = TreasureFishingTracker.TreasureFishingData(),
    val seaCreatures: SeaCreaturesTracker.SeaCreaturesTrackerData = SeaCreaturesTracker.SeaCreaturesTrackerData(),
    val bayouTracker: BayouTracker.BayouTrackerData = BayouTracker.BayouTrackerData(),
    val waterHotspotsTracker: WaterHotspotsTracker.WaterHotspotsTrackerData = WaterHotspotsTracker.WaterHotspotsTrackerData(),
    val crimsonIsle: CrimsonIsleTracker.CrimsonIsleTrackerData = CrimsonIsleTracker.CrimsonIsleTrackerData(),
    val galateaWaterTracker: GalateaWaterTracker.GalateaWaterTrackerData = GalateaWaterTracker.GalateaWaterTrackerData(),
    val archfiendDiceProfit: ArchfiendDiceProfitTracker.ArchfiendDiceProfitData = ArchfiendDiceProfitTracker.ArchfiendDiceProfitData(),
    val fishingProfit: FishingProfitTracker.FishingProfitData = FishingProfitTracker.FishingProfitData(),
    val magmaCoreFishing: MagmaCoreFishingTracker.MagmaCoreFishingData = MagmaCoreFishingTracker.MagmaCoreFishingData(),
    val rareDropNotifications: RareDropMessage.RareDropNotificationsData = RareDropMessage.RareDropNotificationsData(),
    val personalBest: PersonalBestData = PersonalBestData(),

    // Legacy
    // Used to migrate data into bayouTracker and waterHotspotsTracker, then set to null. Can remove later when most users have this data migrated.
    val waterHotspotsAndBayou: LegacyWaterHotspotsAndBayouTrackerData? = null,
)

data class LegacyWaterHotspotsAndBayouTrackerData(
    val titanoboa: CatchCounterData = CatchCounterData(),
    val wikiTiki: CatchCounterData = CatchCounterData(),
    val titanoboaSheds: DropCounterData = DropCounterData(),
    val tikiMasks: DropCounterData = DropCounterData()
)