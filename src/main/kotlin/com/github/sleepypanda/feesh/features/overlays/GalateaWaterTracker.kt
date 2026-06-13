package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.utils.RegisterUtils
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
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.gui.GuiButton
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager

object GalateaWaterTracker {
    data class GalateaWaterTrackerData(
        val lochEmperor: CatchCounterData = CatchCounterData(),
        val nessie: CatchCounterData = CatchCounterData()
    )

    const val RESET_COMMAND = "feeshResetGalateaWater"

    private const val TICKS_PER_UPDATE = 20

    private var data = PersistentDataManager.feeshData.galateaWater
    private var tickCounter = 0

    private val baseTitle = "${AQUA}${BOLD}Galatea water tracker"
    private val lochEmperor = SeaCreatures.allSeaCreatures.find { it.name == "The Loch Emperor" }!!
    private val nessie = SeaCreatures.allSeaCreatures.find { it.name == "Nessie" }!!

    private val gui = FeeshGui()
        .setCoordsDataKey("galateaWaterTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${lochEmperor.displayName}${GRAY}: ${WHITE}200 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}150${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}1h ago",
            "${nessie.displayName}${GRAY}: ${WHITE}350 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}450${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}1h 30m ago"
        ))
        .setSettingsKey { Overlays.galateaWaterTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.galateaWaterTrackerCustomStyle }
        .setCondition {
            WorldUtils.getWorldName() == WorldUtils.GALATEA &&
                FishingHookUtils.wasFishingHookSubmergedMinutesAgo(5)
        }

    fun init() {
        registerCommands()
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }

    fun hasDataForBulkReset(): Boolean = hasData()

    fun bulkReset() {
        reset()
        updateGuiLines()
    }

    private fun registerCommands() {
        RegisterUtils.command(RESET_COMMAND) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetGalateaWaterTracker(isConfirmed)
        }
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!Overlays.galateaWaterTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.GALATEA) return

        val seaCreatureInfo = event.seaCreatureInfo
        if (seaCreatureInfo.types.contains(SeaCreatures.TYPE_GALATEA_LAVA)) return

        val seaCreatureName = event.seaCreatureName
        if (seaCreatureName == lochEmperor.name) {
            onLochEmperor()
        } else if (seaCreatureName == nessie.name) {
            onNessie()
        } else {
            onOtherSeaCreature()
        }
    }

    private fun onLochEmperor() {
        data.lochEmperor.updateAfterCatch(lochEmperor.boldDisplayName)
        data.nessie.incrementCatches()
        saveData()
        updateGuiLines()
    }

    private fun onNessie() {
        data.nessie.updateAfterCatch(nessie.boldDisplayName)
        data.lochEmperor.incrementCatches()
        saveData()
        updateGuiLines()
    }

    private fun onOtherSeaCreature() {
        data.lochEmperor.incrementCatches()
        data.nessie.incrementCatches()
        saveData()
        updateGuiLines()
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
        if (!Overlays.galateaWaterTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.GALATEA || !FishingHookUtils.wasFishingHookSubmergedMinutesAgo(5)) return

        val lines = mutableListOf<LineInfo>()
        lines.add(LineInfo(baseTitle))
        lines.addAll(data.lochEmperor.getOverlayLines(lochEmperor.displayName))
        lines.addAll(data.nessie.getOverlayLines(nessie.displayName))

        gui.setLines(lines)
        gui.setButtons(listOf(GuiButton(0, "${GRAY}[${RED}Click to reset${GRAY}]", { resetGalateaWaterTracker(false) })))
    }

    private fun hasData(): Boolean {
        return data.lochEmperor.hasData() || data.nessie.hasData()
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetGalateaWaterTrackerOnGameClosed &&
            hasData()
        ) {
            reset(force = true)
            FeeshMod.LOGGER.info("[Feesh] Automatically reset Galatea water tracker on game closed.")
        }
    }

    private fun reset(force: Boolean = false) {
        data.lochEmperor.reset()
        data.nessie.reset()
        saveData(force)
    }

    private fun resetGalateaWaterTracker(isConfirmed: Boolean) {
        CommonUtils.runWithCatching("Failed to reset Galatea water tracker.") {
            if (!isConfirmed) {
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to reset Galatea water tracker? ${RED}${BOLD}[Click to confirm]",
                    "${RESET_COMMAND} noconfirm",
                    true
                )
                return
            }

            reset()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Galatea water tracker was reset.", true)
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
