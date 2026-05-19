package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.RareDrops
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GameClosedEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.RareDropEvent
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.gui.GuiButton
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import java.util.Date

object WaterHotspotsTracker {
    data class WaterHotspotsTrackerData(
        val wikiTiki: CatchCounterData = CatchCounterData(),
        val tikiMasks: DropCounterData = DropCounterData()
    )

    const val RESET_COMMAND = "feeshResetWaterHotspots"

    private const val TICKS_PER_UPDATE = 20

    private var data = PersistentDataManager.feeshData.waterHotspotsTracker
    private var tickCounter = 0
    private val baseTitle = "${AQUA}${BOLD}Water Hotspots tracker"

    private val wikiTiki = SeaCreatures.allSeaCreatures.find { it.name == "Wiki Tiki" }!!
    private val tikiMask = RareDrops.rareDrops.find { it.itemName == "Tiki Mask" }!!

    private val gui = FeeshGui()
        .setCoordsDataKey("waterHotspotsTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${wikiTiki.displayName}${GRAY}: ${WHITE}1 000 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}500${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}5h 20m ago",
            "${tikiMask.displayName}s${GRAY}: ${WHITE}3",
            "${GRAY}Last on: ${WHITE}3h 45m ago",
            "${GRAY}Last on: ${WHITE}567 ${GRAY}Wiki Tikis ago"
        ))
        .setSettingsKey { Overlays.waterHotspotsTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.waterHotspotsTrackerCustomStyle }
        .setCondition {
            WorldUtils.isInWaterHotspotFishingWorld() &&
                FishingHookUtils.wasFishingHookActiveInHotspotSecondsAgo(300)
        }

    fun init() {
        registerCommands()
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(RareDropEvent::class, ::onRareDrop)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }

    private fun registerCommands() {
        RegisterUtils.command(RESET_COMMAND) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetWaterHotspotsTracker(isConfirmed)
        }
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!Overlays.waterHotspotsTrackerOverlay || !WorldUtils.isInSkyblock()) return

        val isInHotspotWorld = WorldUtils.isInWaterHotspotFishingWorld()
        val isInHotspot = isInHotspotWorld && isFishingInHotspot()
        if (!isInHotspot) return

        val seaCreatureName = event.seaCreatureName
        if (seaCreatureName == wikiTiki.name) {
            onWikiTiki(event.isDoubleHook)
        } else {
            data.wikiTiki.incrementCatches()
            saveData()
            updateGuiLines()
        }
    }

    private fun onWikiTiki(isDoubleHook: Boolean) {
        data.wikiTiki.updateAfterCatch(wikiTiki.boldDisplayName)
        data.tikiMasks.updateAfterCatch(isDoubleHook)
        saveData()
        updateGuiLines()
    }

    private fun onRareDrop(event: RareDropEvent) {
        if (!Overlays.waterHotspotsTrackerOverlay || !WorldUtils.isInSkyblock()) return
        if (!WorldUtils.isInWaterHotspotFishingWorld()) return

        if (event.itemName == tikiMask.itemName) {
            data.tikiMasks.updateAfterDrop(tikiMask.boldDisplayName, wikiTiki.displayName, event.magicFind)
            saveData()
            updateGuiLines()
        }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0
        updateGuiLines()
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!hasData()) return
        if (!Overlays.waterHotspotsTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInWaterHotspotFishingWorld() ||
            !FishingHookUtils.wasFishingHookActiveInHotspotSecondsAgo(300)
        ) return

        val lines = mutableListOf<LineInfo>()
        lines.add(LineInfo(baseTitle))
        lines.addAll(data.wikiTiki.getOverlayLines(wikiTiki.displayName))
        lines.addAll(data.tikiMasks.getOverlayLines(tikiMask.displayName, wikiTiki.displayName))
        gui.setLines(lines)
        gui.setButtons(listOf(GuiButton(0, "${GRAY}[${RED}Click to reset${GRAY}]", { resetWaterHotspotsTracker(false) })))
    }

    private fun hasData(): Boolean {
        return data.wikiTiki.hasData() || data.tikiMasks.hasData()
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetWaterHotspotsTrackerOnGameClosed &&
            hasData()
        ) {
            reset(force = true)
            FeeshMod.LOGGER.info("[Feesh] Automatically reset Water Hotspots tracker on game closed.")
        }
    }

    private fun reset(force: Boolean = false) {
        data.wikiTiki.reset()
        data.tikiMasks.reset()
        saveData(force)
    }

    private fun resetWaterHotspotsTracker(isConfirmed: Boolean) {
        CommonUtils.runWithCatching("Failed to reset Water Hotspots tracker.") {
            if (!isConfirmed) {
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to reset Water Hotspots tracker? ${RED}${BOLD}[Click to confirm]",
                    "${RESET_COMMAND} noconfirm",
                    true
                )
                return
            }

            reset()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Water Hotspots tracker was reset.", true)
        }
    }

    private fun saveData(force: Boolean = false) {
        if (force) {
            PersistentDataManager.forceSaveFeeshDataToFileSync()
        } else {
            PersistentDataManager.saveFeeshDataToFileAsync()
        }
    }

    private fun isFishingInHotspot(): Boolean {
        if (!WorldUtils.isInWaterHotspotFishingWorld()) return false
        return FishingHookUtils.wasFishingHookActiveInHotspotSecondsAgo(15)
    }

    fun setTikiMasks(count: Int, lastOn: Date?) {
        CommonUtils.runWithCatching(
            message = "Failed to set Tiki Masks.",
            onError = {
                ChatUtils.sendLocalChat("${RED}Failed to set Tiki Masks.", true)
            }
        ) {
            if (!WorldUtils.isInSkyblock()) return

            data.tikiMasks.initDropCount(count, lastOn)
            saveData()
            ChatUtils.sendLocalChat("${GRAY}Successfully changed Tiki Masks count to ${count} for the Water Hotspots tracker.", true)
        }
    }
}
