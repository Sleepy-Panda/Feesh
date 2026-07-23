package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GameClosedEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.features.overlays.base.IResettableTracker

object TorrhusCanyonTracker : IResettableTracker {
    data class TorrhusCanyonTrackerData(
        val silkbreeze: CatchCounterData = CatchCounterData(),
        val giantIsopod: CatchCounterData = CatchCounterData()
    )

    const val RESET_COMMAND = "feeshResetTorrhusCanyonTracker"

    override val trackerName = "Torrhus Canyon tracker"
    override val resetCommand = RESET_COMMAND

    private const val TICKS_PER_UPDATE = 20

    private val data: TorrhusCanyonTrackerData
        get() = PersistentDataManager.feeshData.torrhusCanyon
    private var tickCounter = 0

    private val baseTitle = "${AQUA}${BOLD}${trackerName}"
    private val silkbreeze = SeaCreatures.allSeaCreatures.find { it.name == SeaCreatureNames.SILKBREEZE }!!
    private val giantIsopod = SeaCreatures.allSeaCreatures.find { it.name == SeaCreatureNames.GIANT_ISOPOD }!!

    private val gui = FeeshGui()
        .setCoordsDataKey("torrhusCanyonTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${silkbreeze.displayName}${GRAY}: ${WHITE}200 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}75${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}10m ago",
            "${giantIsopod.displayName}${GRAY}: ${WHITE}1350 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}550${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}1h 5m ago"
        ))
        .setSettingsKey { Overlays.torrhusCanyonTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.torrhusCanyonTrackerCustomStyle }
        .setCondition {
            isTrackerVisible()
        }

    fun init() {
        registerResetCommand()
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }

    override fun hasData(): Boolean {
        return data.silkbreeze.hasData() || data.giantIsopod.hasData()
    }

    override fun resetData(force: Boolean) {
        data.silkbreeze.reset()
        data.giantIsopod.reset()
        saveData(force)
    }

    override fun refreshGui() {
        updateGuiLines()
    }
    
    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetTorrhusCanyonTrackerOnGameClosed) {
            resetOnGameClosed()
        }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        CommonUtils.runWithCatching("Failed to update $trackerName GUI lines") {
            updateGuiLines()
        }
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!Overlays.torrhusCanyonTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.TORRHUS_CANYON) return

        CommonUtils.runWithCatching("Failed to track sea creature catch in $trackerName") {
            when (event.seaCreatureName) {
                silkbreeze.name -> onSilkbreeze()
                giantIsopod.name -> onGiantIsopod()
                else -> onOtherSeaCreature()
            }    
        }
    }

    private fun onSilkbreeze() {
        data.silkbreeze.updateAfterCatch(silkbreeze.boldDisplayName)
        data.giantIsopod.incrementCatches()
        saveData()
        updateGuiLines()
    }

    private fun onGiantIsopod() {
        data.giantIsopod.updateAfterCatch(giantIsopod.boldDisplayName)
        data.silkbreeze.incrementCatches()
        saveData()
        updateGuiLines()
    }

    private fun onOtherSeaCreature() {
        data.silkbreeze.incrementCatches()
        data.giantIsopod.incrementCatches()
        saveData()
        updateGuiLines()
    }

    private fun isTrackerVisible(): Boolean {
        if (!Overlays.torrhusCanyonTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.TORRHUS_CANYON) return false
        if (!FishingHookUtils.wasFishingHookSubmergedMinutesAgo(5)) return false
        if (PlayerUtils.isInTrophyArmor()) return false
        return true
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!isTrackerVisible() || !hasData()) return

        val lines = mutableListOf<LineInfo>()
        lines.add(LineInfo(baseTitle))
        lines.addAll(data.silkbreeze.getOverlayLines(silkbreeze.displayName))
        lines.addAll(data.giantIsopod.getOverlayLines(giantIsopod.displayName))

        gui.setLines(lines)
        gui.setButtons(listOf(getResetGuiButton { requestReset(false) }))
    }

    private fun saveData(force: Boolean = false) {
        if (force) {
            PersistentDataManager.forceSaveFeeshDataToFileSync()
        } else {
            PersistentDataManager.saveFeeshDataToFileAsync()
        }
    }
}
