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
import com.github.sleepypanda.feesh.utils.gui.GuiButton
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import java.util.Date

object BayouTracker {
    data class BayouTrackerData(
        val titanoboa: CatchCounterData = CatchCounterData(),
        val titanoboaSheds: DropCounterData = DropCounterData()
    )

    const val RESET_COMMAND = "feeshResetBayouTracker"

    private const val TICKS_PER_UPDATE = 20

    private var data = PersistentDataManager.feeshData.bayouTracker
    private var tickCounter = 0
    private val baseTitle = "${AQUA}${BOLD}Bayou tracker"

    private val titanoboa = SeaCreatures.allSeaCreatures.find { it.name == "Titanoboa" }!!
    private val titanoboaShed = RareDrops.rareDrops.find { it.itemName == "Titanoboa Shed" }!!

    private val gui = FeeshGui()
        .setCoordsDataKey("bayouTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${titanoboa.displayName}${GRAY}: ${WHITE}10 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}500${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}1h 30m ago ${GRAY}(${WHITE}2025-01-15 14:30:00${GRAY})",
            "${titanoboaShed.displayName}s${GRAY}: ${WHITE}5",
            "${GRAY}Last on: ${WHITE}2h 15m ${GRAY}(${WHITE}2025-01-15 13:15:00${GRAY})",
            "${GRAY}Last on: ${WHITE}1 234 ${GRAY}Titanoboas ago"
        ))
        .setSettingsKey { Overlays.bayouTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.bayouTrackerCustomStyle }
        .setCondition {
            WorldUtils.getWorldName() == WorldUtils.BACKWATER_BAYOU &&
                FishingHookUtils.wasFishingHookActiveMinutesAgo(5)
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
            resetBayouTracker(isConfirmed)
        }
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
            !FishingHookUtils.wasFishingHookActiveMinutesAgo(5)
        ) return
        if (WorldUtils.getWorldName() != WorldUtils.BACKWATER_BAYOU) return

        val lines = mutableListOf<String>()
        lines.add(baseTitle)
        lines.addAll(data.titanoboa.getOverlayText(titanoboa.displayName))
        lines.addAll(data.titanoboaSheds.getOverlayText(titanoboaShed.displayName, titanoboa.displayName))
        gui.setLines(lines)
        gui.setButtons(listOf(GuiButton(0, "${GRAY}[${RED}Click to reset${GRAY}]", { resetBayouTracker(false) })))
    }

    private fun hasData(): Boolean {
        return data.titanoboa.hasData() || data.titanoboaSheds.hasData()
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetBayouTrackerOnGameClosed &&
            Overlays.bayouTrackerOverlay &&
            hasData()
        ) {
            reset(force = true)
            FeeshMod.LOGGER.info("[Feesh] Automatically reset Bayou tracker on game closed.")
        }
    }

    private fun reset(force: Boolean = false) {
        data.titanoboa.reset()
        data.titanoboaSheds.reset()
        saveData(force)
    }

    private fun resetBayouTracker(isConfirmed: Boolean) {
        CommonUtils.runWithCatching("Failed to reset Bayou tracker.") {
            if (!isConfirmed) {
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to reset Bayou tracker? ${RED}${BOLD}[Click to confirm]",
                    "${RESET_COMMAND} noconfirm",
                    true
                )
                return
            }

            reset()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Bayou tracker was reset.", true)
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
            ChatUtils.sendLocalChat("${GRAY}Successfully changed Titanoboa Sheds count to ${count} for the Bayou tracker.", true)
        }
    }
}
