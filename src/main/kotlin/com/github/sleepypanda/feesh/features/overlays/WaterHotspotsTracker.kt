package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.RareDrops
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
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
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.features.overlays.base.IResettableTracker
import java.util.Date

object WaterHotspotsTracker : IResettableTracker {
    data class WaterHotspotsTrackerData(
        val wikiTiki: CatchCounterData = CatchCounterData(),
        val tikiMasks: DropCounterData = DropCounterData()
    )

    const val RESET_COMMAND = "feeshResetWaterHotspotsTracker"

    override val trackerName = "Water Hotspots tracker"
    override val resetCommand = RESET_COMMAND

    private const val TICKS_PER_UPDATE = 20

    private val data: WaterHotspotsTrackerData
        get() = PersistentDataManager.feeshData.waterHotspotsTracker
    private var tickCounter = 0
    private val baseTitle = "${AQUA}${BOLD}${trackerName}"

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
            !isTrackerDisabled()
        }

    fun init() {
        registerResetCommand()
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(RareDropEvent::class, ::onRareDrop)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }

    override fun hasData(): Boolean {
        return data.wikiTiki.hasData() || data.tikiMasks.hasData()
    }

    override fun resetData(force: Boolean) {
        data.wikiTiki.reset()
        data.tikiMasks.reset()
        saveData(force)
    }

    override fun refreshGui() {
        updateGuiLines()
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

    private fun isTrackerDisabled(): Boolean {
        if (!Overlays.waterHotspotsTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInWaterHotspotFishingWorld()) return true
        if (!FishingHookUtils.wasFishingHookSubmergedInHotspotSecondsAgo(300)) return true
        if (PlayerUtils.isInTrophyArmor()) return true
        return false
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (isTrackerDisabled() || !hasData()) return

        val lines = mutableListOf<LineInfo>()
        lines.add(LineInfo(baseTitle))
        lines.addAll(data.wikiTiki.getOverlayLines(wikiTiki.displayName))
        lines.addAll(data.tikiMasks.getOverlayLines(tikiMask.displayName, wikiTiki.displayName))
        gui.setLines(lines)
        gui.setButtons(listOf(getResetGuiButton { requestReset(false) }))
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetWaterHotspotsTrackerOnGameClosed) {
            resetOnGameClosed()
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
        return FishingHookUtils.wasFishingHookSubmergedInHotspotSecondsAgo(15)
    }

    fun setTikiMasks(count: Int, catchesSinceLast: Int, lastOn: Date?) {
        CommonUtils.runWithCatching(
            message = "Failed to set Tiki Masks.",
            onError = {
                ChatUtils.sendLocalChat("${RED}Failed to set Tiki Masks.", true)
            }
        ) {
            if (!WorldUtils.isInSkyblock()) return

            data.tikiMasks.initDropCount(count, lastOn, catchesSinceLast)
            saveData()
            ChatUtils.sendLocalChat("Successfully changed Tiki Masks for the Water Hotspots tracker.\nCount = ${count}, Wiki Tikis since last = ${catchesSinceLast}, last on = ${lastOn}.", true)
        }
    }
}
