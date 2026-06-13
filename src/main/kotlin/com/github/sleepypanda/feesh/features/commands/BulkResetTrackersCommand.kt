package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.features.overlays.BayouTracker
import com.github.sleepypanda.feesh.features.overlays.CrimsonIsleTracker
import com.github.sleepypanda.feesh.features.overlays.FishingProfitTracker
import com.github.sleepypanda.feesh.features.overlays.GalateaWaterTracker
import com.github.sleepypanda.feesh.features.overlays.JerryWorkshopTracker
import com.github.sleepypanda.feesh.features.overlays.LotusAtollTracker
import com.github.sleepypanda.feesh.features.overlays.MagmaCoreFishingTracker
import com.github.sleepypanda.feesh.features.overlays.SeaCreaturesPerHourTracker
import com.github.sleepypanda.feesh.features.overlays.SeaCreaturesTracker
import com.github.sleepypanda.feesh.features.overlays.TreasureFishingTracker
import com.github.sleepypanda.feesh.features.overlays.WaterHotspotsTracker
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.models.BulkResettableTrackerTypes
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object BulkResetTrackersCommand {
    const val COMMAND_NAME = "feeshBulkResetTrackers"

    private val SESSION_VIEW_MODE_TEXT = "${GRAY}[${GREEN}Session${GRAY}]"

    fun init() {
        RegisterUtils.command(COMMAND_NAME) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetTrackers(isConfirmed)
        }
    }

    fun triggerBulkResetSelectedTrackers() {
        resetTrackers(isConfirmed = false)
    }

    private fun resetTrackers(isConfirmed: Boolean) {
        if (!WorldUtils.isInSkyblock()) return

        val selected = Overlays.bulkResetTrackersList
        if (selected.isEmpty()) {
            ChatUtils.sendLocalChat("${YELLOW}No trackers are selected. Choose trackers in Feesh settings -> Overlays -> Trackers to reset on keybind.", true)
            return
        }

        val toReset = selected.filter { hasData(it) }
        if (toReset.isEmpty()) {
            ChatUtils.sendLocalChat("The trackers have no data to reset.", true)
            return
        }

        val trackersText = toReset.joinToString("\n-", prefix = "\n") { getResetDisplayName(it) }

        if (!isConfirmed) {
            ChatUtils.sendLocalChatWithCommand(
                "${WHITE}Do you want to reset the following trackers?$trackersText\n${RED}${BOLD}[Click to confirm]",
                "$COMMAND_NAME noconfirm",
                true
            )
            return
        }

        CommonUtils.runWithCatching("Failed to reset selected trackers on keybind") {
            toReset.forEach { resetTracker(it) }
            ChatUtils.sendLocalChat("The trackers data was reset.", true)
        }
    }

    private fun getResetDisplayName(overlay: BulkResettableTrackerTypes): String {
        return if (overlay.hasSessionMode) {
            "${WHITE}${overlay.displayName} $SESSION_VIEW_MODE_TEXT"
        } else {
            "${WHITE}${overlay.displayName}"
        }
    }

    private fun hasData(tracker: BulkResettableTrackerTypes): Boolean {
        return when (tracker) {
            BulkResettableTrackerTypes.SEA_CREATURES_TRACKER -> SeaCreaturesTracker.hasSessionDataForBulkReset()
            BulkResettableTrackerTypes.FISHING_PROFIT_TRACKER -> FishingProfitTracker.hasSessionDataForBulkReset()
            BulkResettableTrackerTypes.SEA_CREATURES_PER_HOUR_TRACKER -> SeaCreaturesPerHourTracker.hasDataForBulkReset()
            BulkResettableTrackerTypes.JERRY_WORKSHOP_TRACKER -> JerryWorkshopTracker.hasDataForBulkReset()
            BulkResettableTrackerTypes.BAYOU_TRACKER -> BayouTracker.hasDataForBulkReset()
            BulkResettableTrackerTypes.WATER_HOTSPOTS_TRACKER -> WaterHotspotsTracker.hasDataForBulkReset()
            BulkResettableTrackerTypes.CRIMSON_ISLE_TRACKER -> CrimsonIsleTracker.hasDataForBulkReset()
            BulkResettableTrackerTypes.GALATEA_WATER_TRACKER -> GalateaWaterTracker.hasDataForBulkReset()
            BulkResettableTrackerTypes.LOTUS_ATOLL_TRACKER -> LotusAtollTracker.hasDataForBulkReset()
            BulkResettableTrackerTypes.TREASURE_FISHING_TRACKER -> TreasureFishingTracker.hasSessionDataForBulkReset()
            BulkResettableTrackerTypes.MAGMA_CORE_FISHING_TRACKER -> MagmaCoreFishingTracker.hasSessionDataForBulkReset()
        }
    }

    private fun resetTracker(tracker: BulkResettableTrackerTypes) {
        CommonUtils.runWithCatching("Failed to reset $tracker on keybind") {
            when (tracker) {
                BulkResettableTrackerTypes.SEA_CREATURES_TRACKER -> SeaCreaturesTracker.bulkResetSession()
                BulkResettableTrackerTypes.FISHING_PROFIT_TRACKER -> FishingProfitTracker.bulkResetSession()
                BulkResettableTrackerTypes.SEA_CREATURES_PER_HOUR_TRACKER -> SeaCreaturesPerHourTracker.bulkReset()
                BulkResettableTrackerTypes.JERRY_WORKSHOP_TRACKER -> JerryWorkshopTracker.bulkReset()
                BulkResettableTrackerTypes.BAYOU_TRACKER -> BayouTracker.bulkReset()
                BulkResettableTrackerTypes.WATER_HOTSPOTS_TRACKER -> WaterHotspotsTracker.bulkReset()
                BulkResettableTrackerTypes.CRIMSON_ISLE_TRACKER -> CrimsonIsleTracker.bulkReset()
                BulkResettableTrackerTypes.GALATEA_WATER_TRACKER -> GalateaWaterTracker.bulkReset()
                BulkResettableTrackerTypes.LOTUS_ATOLL_TRACKER -> LotusAtollTracker.bulkReset()
                BulkResettableTrackerTypes.TREASURE_FISHING_TRACKER -> TreasureFishingTracker.bulkResetSession()
                BulkResettableTrackerTypes.MAGMA_CORE_FISHING_TRACKER -> MagmaCoreFishingTracker.bulkResetSession()
            }
        }
    }
}
