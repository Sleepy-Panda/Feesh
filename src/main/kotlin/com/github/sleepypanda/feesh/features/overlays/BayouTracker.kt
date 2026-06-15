package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.RareDrops
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
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.features.overlays.base.IResettableTracker
import java.util.Date

object BayouTracker : IResettableTracker {

    data class BayouTrackerData(
        val titanoboa: CatchCounterData = CatchCounterData(),
        val titanoboaSheds: DropCounterData = DropCounterData()
    )

    const val RESET_COMMAND = "feeshResetBayouTracker"

    override val trackerName = "Bayou tracker"
    override val resetCommand = RESET_COMMAND

    private const val TICKS_PER_UPDATE = 20

    private val data: BayouTrackerData
        get() = PersistentDataManager.feeshData.bayouTracker
    private var tickCounter = 0
    private val baseTitle = "${AQUA}${BOLD}${trackerName}"

    private val titanoboa = SeaCreatures.allSeaCreatures.find { it.name == "Titanoboa" }!!
    private val titanoboaShed = RareDrops.rareDrops.find { it.itemName == "Titanoboa Shed" }!!

    private val gui = FeeshGui()
        .setCoordsDataKey("bayouTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${titanoboa.displayName}${GRAY}: ${WHITE}10 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}500${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}1h 30m ago",
            "${titanoboaShed.displayName}s${GRAY}: ${WHITE}5",
            "${GRAY}Last on: ${WHITE}2h 15m ago",
            "${GRAY}Last on: ${WHITE}1 234 ${GRAY}Titanoboas ago"
        ))
        .setSettingsKey { Overlays.bayouTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.bayouTrackerCustomStyle }
        .setCondition {
            WorldUtils.getWorldName() == WorldUtils.BACKWATER_BAYOU &&
                FishingHookUtils.wasFishingHookSubmergedMinutesAgo(5)
        }

    fun init() {
        registerResetCommand()
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(RareDropEvent::class, ::onRareDrop)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }

    override fun hasData(): Boolean {
        return data.titanoboa.hasData() || data.titanoboaSheds.hasData()
    }

    override fun resetData(force: Boolean) {
        data.titanoboa.reset()
        data.titanoboaSheds.reset()
        saveData(force)
    }

    override fun refreshGui() {
        updateGuiLines()
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!Overlays.bayouTrackerOverlay || !WorldUtils.isInSkyblock()) return

        val worldName = WorldUtils.getWorldName()
        val isInBayou = worldName == WorldUtils.BACKWATER_BAYOU
        if (!isInBayou) return

        val seaCreatureName = event.seaCreatureName
        if (seaCreatureName == titanoboa.name) {
            onTitanoboa(event.isDoubleHook)
        } else {
            data.titanoboa.incrementCatches()
            saveData()
            updateGuiLines()
        }
    }

    private fun onTitanoboa(isDoubleHook: Boolean) {
        data.titanoboa.updateAfterCatch(titanoboa.boldDisplayName)
        data.titanoboaSheds.updateAfterCatch(isDoubleHook)
        saveData()
        updateGuiLines()
    }

    private fun onRareDrop(event: RareDropEvent) {
        if (!Overlays.bayouTrackerOverlay || !WorldUtils.isInSkyblock()) return
        if (WorldUtils.getWorldName() != WorldUtils.BACKWATER_BAYOU) return

        if (event.itemName == titanoboaShed.itemName) {
            data.titanoboaSheds.updateAfterDrop(titanoboaShed.boldDisplayName, titanoboa.displayName, event.magicFind)
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
        if (!Overlays.bayouTrackerOverlay || !WorldUtils.isInSkyblock() ||
            !FishingHookUtils.wasFishingHookSubmergedMinutesAgo(5)
        ) return
        if (WorldUtils.getWorldName() != WorldUtils.BACKWATER_BAYOU) return

        val lines = mutableListOf<LineInfo>()
        lines.add(LineInfo(baseTitle))
        lines.addAll(data.titanoboa.getOverlayLines(titanoboa.displayName))
        lines.addAll(data.titanoboaSheds.getOverlayLines(titanoboaShed.displayName, titanoboa.displayName))
        gui.setLines(lines)
        gui.setButtons(listOf(getResetGuiButton { requestReset(false) }))
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetBayouTrackerOnGameClosed) {
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

    fun setTitanoboaSheds(count: Int, lastOn: Date?) {
        CommonUtils.runWithCatching(
            message = "Failed to set Titanoboa Sheds.",
            onError = {
                ChatUtils.sendLocalChat("${RED}Failed to set Titanoboa Sheds.", true)
            }
        ) {
            if (!WorldUtils.isInSkyblock()) return

            data.titanoboaSheds.initDropCount(count, lastOn)
            saveData()
            ChatUtils.sendLocalChat("${GRAY}Successfully changed Titanoboa Sheds count to $count for the Bayou tracker.", true)
        }
    }
}
