package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.constants.RareDrops
import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
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

object LotusAtollTracker : IResettableTracker {
    data class LotusAtollTrackerData(
        val puddleJumper: CatchCounterData = CatchCounterData(),
        val frogPrince: CatchCounterData = CatchCounterData(),
        val princesCrownJewels: DropCounterData = DropCounterData(),
    )

    const val RESET_COMMAND = "feeshResetLotusAtollTracker"

    override val trackerName = "Lotus Atoll tracker"
    override val resetCommand = RESET_COMMAND

    private const val TICKS_PER_UPDATE = 20

    private val data: LotusAtollTrackerData
        get() = PersistentDataManager.feeshData.lotusAtollTracker
    private var tickCounter = 0

    private val baseTitle = "${AQUA}${BOLD}${trackerName}"
    private val frogPrince = SeaCreatures.allSeaCreatures.find { it.name == SeaCreatureNames.FROG_PRINCE }!!
    private val puddleJumper = SeaCreatures.allSeaCreatures.find { it.name == SeaCreatureNames.PUDDLE_JUMPER }!!
    private val princesCrownJewel = RareDrops.rareDrops.find { it.itemName == "Prince's Crown Jewel" }!!

    private val gui = FeeshGui()
        .setCoordsDataKey("lotusAtollTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${puddleJumper.displayName}${GRAY}: ${WHITE}200 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}50${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}10m ago",
            "${frogPrince.displayName}${GRAY}: ${WHITE}1200 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}500${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}1h ago",
            "${princesCrownJewel.displayName}s${GRAY}: ${WHITE}3",
            "${GRAY}Last on: ${WHITE}5h ago",
            "${GRAY}Last on: ${WHITE}10 ${GRAY}Frog Princes ago",
        ))
        .setSettingsKey { Overlays.lotusAtollTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.lotusAtollTrackerCustomStyle }
        .setCondition {
            WorldUtils.getWorldName() == WorldUtils.LOTUS_ATOLL &&
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
        return data.frogPrince.hasData() || data.puddleJumper.hasData() || data.princesCrownJewels.hasData()
    }

    override fun resetData(force: Boolean) {
        data.frogPrince.reset()
        data.puddleJumper.reset()
        data.princesCrownJewels.reset()
        saveData(force)
    }

    override fun refreshGui() {
        updateGuiLines()
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        
        fun onFrogPrince() {
            data.frogPrince.updateAfterCatch(frogPrince.boldDisplayName)
            data.puddleJumper.incrementCatches()
            data.princesCrownJewels.updateAfterCatch(event.isDoubleHook)
            saveData()
            updateGuiLines()
        }
    
        fun onPuddleJumper() {
            data.puddleJumper.updateAfterCatch(puddleJumper.boldDisplayName)
            data.frogPrince.incrementCatches()
            saveData()
            updateGuiLines()
        }
    
        fun onOtherSeaCreature() {
            data.frogPrince.incrementCatches()
            data.puddleJumper.incrementCatches()
            saveData()
            updateGuiLines()
        }

        CommonUtils.runWithCatching("Failed to track catch for Lotus Atoll tracker.") {
            if (!Overlays.lotusAtollTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL) return

            val seaCreatureName = event.seaCreatureName
            when (seaCreatureName) {
                frogPrince.name -> onFrogPrince()
                puddleJumper.name -> onPuddleJumper()
                else -> onOtherSeaCreature()
            }
        }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0
        CommonUtils.runWithCatching("Failed to update Lotus Atoll tracker GUI lines.") {
            updateGuiLines()
        }
    }

    private fun onRareDrop(event: RareDropEvent) {
        CommonUtils.runWithCatching("Failed to track rare drop for Lotus Atoll tracker.") {
            if (!Overlays.lotusAtollTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL) return
            if (event.itemName != princesCrownJewel.itemName) return

            data.princesCrownJewels.updateAfterDrop(princesCrownJewel.boldDisplayName, frogPrince.displayName, event.magicFind)
            saveData()
            updateGuiLines()
        }
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!hasData()) return
        if (!Overlays.lotusAtollTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL) return
        if (!FishingHookUtils.wasFishingHookSubmergedMinutesAgo(5)) return

        val lines = mutableListOf<LineInfo>()
        lines.add(LineInfo(baseTitle))
        lines.addAll(data.puddleJumper.getOverlayLines(puddleJumper.displayName))
        lines.addAll(data.frogPrince.getOverlayLines(frogPrince.displayName))
        lines.addAll(data.princesCrownJewels.getOverlayLines(princesCrownJewel.displayName, frogPrince.displayName))

        gui.setLines(lines)
        gui.setButtons(listOf(getResetGuiButton { requestReset(false) }))
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetLotusAtollTrackerOnGameClosed) {
            resetOnGameClosed()
        }
    }

    fun setPrincesCrownJewels(count: Int, lastOn: Date?) {
        CommonUtils.runWithCatching(
            message = "Failed to set Prince's Crown Jewels.",
            onError = {
                ChatUtils.sendLocalChat("${RED}Failed to set Prince's Crown Jewels.", true)
            }
        ) {
            if (!WorldUtils.isInSkyblock()) return

            data.princesCrownJewels.initDropCount(count, lastOn)
            saveData()
            ChatUtils.sendLocalChat("${GRAY}Successfully changed Prince's Crown Jewels count to $count in the Lotus Atoll tracker.", true)
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
