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
import com.github.sleepypanda.feesh.utils.TabListUtils
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GameClosedEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.gui.GuiButton
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager

object JerryWorkshopTracker {
    data class JerryWorkshopTrackerData(
        val yeti: CatchCounterData = CatchCounterData(),
        val reindrake: CatchCounterData = CatchCounterData()
    )

    const val RESET_COMMAND = "feeshResetJerryWorkshop"

    private const val TICKS_PER_UPDATE = 20

    private var data = PersistentDataManager.feeshData.jerryWorkshop
    private var tickCounter = 0

    private val baseTitle = "${AQUA}${BOLD}Jerry Workshop tracker"
    private val yeti = SeaCreatures.allSeaCreatures.find { it.name == "Yeti" }!!
    private val reindrake = SeaCreatures.allSeaCreatures.find { it.name == "Reindrake" }!!

    private val gui = FeeshGui()
        .setCoordsDataKey("jerryWorkshopTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${yeti.displayName}${GRAY}: ${WHITE}10 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}50${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}1 minute ago ${GRAY}(${WHITE}2025-01-15 14:30:00${GRAY})",
            "${reindrake.displayName}${GRAY}: ${WHITE}100 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}500${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}1 hour ago ${GRAY}(${WHITE}2025-01-15 13:30:00${GRAY})",
            "${GRAY}Island closes in: 1h"
        ))
        .setSettingsKey { Overlays.jerryWorkshopTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.jerryWorkshopTrackerCustomStyle }
        .setCondition {
            WorldUtils.getWorldName() == WorldUtils.JERRY_WORKSHOP &&
            FishingHookUtils.wasFishingHookActiveMinutesAgo(5)
        }

    fun init() {
        registerCommands()
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }
    
    private fun registerCommands() {
        RegisterUtils.command(RESET_COMMAND) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetJerryWorkshopTracker(isConfirmed)
        }
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

    private fun updateGuiLines() {
        gui.clearLines()

        if (!hasData()) return
        if (!Overlays.jerryWorkshopTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP || !FishingHookUtils.wasFishingHookActiveMinutesAgo(5)) return

        val lines = mutableListOf<String>()
        lines.add(baseTitle)

        lines.addAll(data.yeti.getOverlayText(yeti.displayName))
        lines.addAll(data.reindrake.getOverlayText(reindrake.displayName))

        val islandOpen = TabListUtils.getLineAfter("Island open:")
        val islandClosesIn = TabListUtils.getLineAfter("Island closes in:")
        if (!islandOpen.isNullOrEmpty()) {
            val islandOpenLine = "${GRAY}Island open: ${WHITE}${islandOpen}"
            lines.add(islandOpenLine)
        } else if (!islandClosesIn.isNullOrEmpty()) {
            val islandClosesInLine = "${GRAY}Island closes in: ${WHITE}${islandClosesIn}"
            lines.add(islandClosesInLine)
        }

        gui.setLines(lines.map { LineInfo(it) })
        gui.setButtons(listOf(GuiButton(0, "${GRAY}[${RED}Click to reset${GRAY}]", { resetJerryWorkshopTracker(false) })))
    }

    private fun hasData(): Boolean {
        return data.yeti.hasData() || data.reindrake.hasData()
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetJerryWorkshopTrackerOnGameClosed &&
            Overlays.jerryWorkshopTrackerOverlay &&
            hasData()) {
            reset(force = true)
            FeeshMod.LOGGER.info("[Feesh] Automatically reset Jerry Workshop tracker on game closed.")
        }
    }

    private fun reset(force: Boolean = false) {
        data.yeti.reset()
        data.reindrake.reset()
        saveData(force)
    }

    private fun resetJerryWorkshopTracker(isConfirmed: Boolean) {
        CommonUtils.runWithCatching("Failed to reset Jerry Workshop tracker") {
            if (!isConfirmed) {
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to reset Jerry Workshop tracker? ${RED}${BOLD}[Click to confirm]",
                    "${RESET_COMMAND} noconfirm",
                    true
                )
                return
            }

            reset()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Jerry Workshop tracker was reset.", true)
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