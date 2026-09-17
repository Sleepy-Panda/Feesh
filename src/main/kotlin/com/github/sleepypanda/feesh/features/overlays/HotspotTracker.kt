package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.HotspotUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo

// Does not hide when im too far away from the hotspot
// Short perk, e.g. SCC instead of Sea Creature Chance?
object HotspotTracker {
    private const val TICKS_PER_UPDATE = 10
    private var tickCounter = 0

    private val gui = FeeshGui()
        .setCoordsDataKey("hotspotTracker")
        .setClickable(false)
        .setSampleLines(listOf(
            "${AQUA}+10☂ Fishing Speed"
        ))
        .setSettingsKey { Overlays.hotspotTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.hotspotTrackerCustomStyle }
        .setCondition {
            WorldUtils.isInHotspotFishingWorld() &&
                HotspotUtils.getLastFishedHotspot() != null
        }

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        gui.clearLines()
        tickCounter = 0
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        updateGuiLines()
    }

    private fun updateGuiLines() {
        CommonUtils.runWithCatching("Failed to update Hotspot tracker GUI lines") {
            if (!Overlays.hotspotTrackerOverlay ||
                !WorldUtils.isInSkyblock() ||
                !WorldUtils.isInHotspotFishingWorld()
            ) {
                gui.clearLines()
                return
            }

            val hotspot = HotspotUtils.getLastFishedHotspot()
            val perk = hotspot?.perk
            if (hotspot == null || perk.isNullOrEmpty()) {
                gui.clearLines()
                return
            }

            gui.setLines(listOf(LineInfo(perk)))
        }
    }
}
