package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.TabListUtils
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GameClosedEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.features.overlays.base.IResettableTracker

object JerryWorkshopTracker : IResettableTracker {

    data class JerryWorkshopTrackerData(
        val yeti: CatchCounterData = CatchCounterData(),
        val reindrake: CatchCounterData = CatchCounterData()
    )

    const val RESET_COMMAND = "feeshResetJerryWorkshopTracker"

    override val trackerName = "Jerry Workshop tracker"
    override val resetCommand = RESET_COMMAND

    private const val TICKS_PER_UPDATE = 20

    private val data: JerryWorkshopTrackerData
        get() = PersistentDataManager.feeshData.jerryWorkshop
    private var tickCounter = 0

    private val baseTitle = "${AQUA}${BOLD}${trackerName}"
    private val yeti = SeaCreatures.allSeaCreatures.find { it.name == "Yeti" }!!
    private val reindrake = SeaCreatures.allSeaCreatures.find { it.name == "Reindrake" }!!

    private val gui = FeeshGui()
        .setCoordsDataKey("jerryWorkshopTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${yeti.displayName}${GRAY}: ${WHITE}10 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}50${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}1 minute ago",
            "${reindrake.displayName}${GRAY}: ${WHITE}100 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}500${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}1 hour ago",
            "${GRAY}Island closes in: 1h"
        ))
        .setSettingsKey { Overlays.jerryWorkshopTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.jerryWorkshopTrackerCustomStyle }
        .setCondition {
            !isTrackerDisabled()
        }

    fun init() {
        registerResetCommand()
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }

    override fun hasData(): Boolean {
        return data.yeti.hasData() || data.reindrake.hasData()
    }

    override fun resetData(force: Boolean) {
        data.yeti.reset()
        data.reindrake.reset()
        saveData(force)
    }

    override fun refreshGui() {
        updateGuiLines()
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!Overlays.jerryWorkshopTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return

        val seaCreatureName = event.seaCreatureName

        if (seaCreatureName == yeti.name) {
            onYeti()
        } else if (seaCreatureName == reindrake.name) {
            onReindrake()
        } else {
            onOtherSeaCreature()
        }
    }

    private fun onYeti() {
        data.yeti.updateAfterCatch(yeti.boldDisplayName)
        data.reindrake.incrementCatches()
        saveData()
        updateGuiLines()
    }

    private fun onReindrake() {
        data.reindrake.updateAfterCatch(reindrake.boldDisplayName)
        data.yeti.incrementCatches()
        saveData()
        updateGuiLines()
    }

    private fun onOtherSeaCreature() {
        data.yeti.incrementCatches()
        data.reindrake.incrementCatches()
        saveData()
        updateGuiLines()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        updateGuiLines()
    }

    private fun isTrackerDisabled(): Boolean {
        if (!Overlays.jerryWorkshopTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return true
        if (!FishingHookUtils.wasFishingHookSubmergedMinutesAgo(5)) return true
        if (PlayerUtils.isInTrophyArmor()) return true
        return false
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (isTrackerDisabled() || !hasData()) return

        val lines = mutableListOf<LineInfo>()
        lines.add(LineInfo(baseTitle))

        lines.addAll(data.yeti.getOverlayLines(yeti.displayName))
        lines.addAll(data.reindrake.getOverlayLines(reindrake.displayName))

        val islandOpen = TabListUtils.getLineAfter("Island open:")
        val islandClosesIn = TabListUtils.getLineAfter("Island closes in:")
        if (!islandOpen.isNullOrEmpty()) {
            val islandOpenLine = LineInfo("${GRAY}Island open: ${WHITE}${islandOpen}")
            lines.add(islandOpenLine)
        } else if (!islandClosesIn.isNullOrEmpty()) {
            val islandClosesInLine = LineInfo("${GRAY}Island closes in: ${WHITE}${islandClosesIn}")
            lines.add(islandClosesInLine)
        }

        gui.setLines(lines)
        gui.setButtons(listOf(getResetGuiButton { requestReset(false) }))
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetJerryWorkshopTrackerOnGameClosed) {
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
}
