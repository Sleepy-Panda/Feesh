package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.BaitUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo

object BaitTracker {
    private const val TICKS_PER_UPDATE = 20
    private var tickCounter = 0

    private val gui = FeeshGui()
        .setCoordsDataKey("baitTracker")
        .setClickable(false)
        .setSampleLines(listOf(
            "${BLUE}Whale Bait${GRAY}: ${WHITE}1 234",
        ))
        .setSettingsKey { Overlays.baitTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.baitTrackerCustomStyle }
        .setCondition {
            WorldUtils.isInFishingWorld() &&
                FishingHookUtils.wasFishingHookActiveMinutesAgo(5)
        }

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        gui.clearLines()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        updateGuiLines()
    }

    private fun updateGuiLines() {
        CommonUtils.runWithCatching("Failed to update Bait Tracker GUI lines") {
            if (!Overlays.baitTrackerOverlay ||
                !WorldUtils.isInSkyblock() ||
                !WorldUtils.isInFishingWorld() ||
                !FishingHookUtils.wasFishingHookActiveMinutesAgo(5)
            ) {
                gui.clearLines()
                return
            }
    
            val remaining = BaitUtils.getBaitRemaining()
            val displayName = BaitUtils.getBaitDisplayName()
            var baitText = ""
    
            if (remaining == null || displayName.isEmpty()) {
                baitText = "${WHITE}Bait${GRAY}: ${RED}N/A"
            } else {
                val color = if (remaining <= 16) RED else WHITE
                baitText = "${displayName}${GRAY}: ${color}${CommonUtils.formatNumberWithSpaces(remaining)}"
            }
    
            gui.setLines(listOf(LineInfo(baitText)))
        }
    }
}
