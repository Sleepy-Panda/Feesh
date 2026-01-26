package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.RareDrops
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.ClientTickEvent
import com.github.sleepypanda.feesh.events.RareDropEvent
import com.github.sleepypanda.feesh.events.GameClosedEvent
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import java.util.Date

object CrimsonIsleTracker {
    data class CrimsonIsleTrackerData(
        val thunder: CatchCounterData = CatchCounterData(),
        val lordJawbus: CatchCounterData = CatchCounterData(),
        val radioactiveVials: DropCounterData = DropCounterData()
    )

    private var data = PersistentDataManager.feeshData.crimsonIsle
    private var tickCounter = 0
    private val baseTitle = "${AQUA}${BOLD}Crimson Isle tracker"

    private const val TICKS_PER_UPDATE = 20
    private val RESET_COMMAND = "feeshResetCrimsonIsle"

    private val thunder = SeaCreatures.allSeaCreatures.find { it.name == "Thunder" }!!
    private val lordJawbus = SeaCreatures.allSeaCreatures.find { it.name == "Lord Jawbus" }!!
    private val radioactiveVial = RareDrops.rareDrops.find { it.itemName == "Radioactive Vial" }!!

    private val gui = FeeshGui()
        .setCoordsDataKey("crimsonIsleTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${thunder.displayName}${GRAY}: ${WHITE}10 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}500${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}1h 30m ago ${GRAY}(${WHITE}2025-01-15 14:30:00${GRAY})",
            "${lordJawbus.displayName}${GRAY}: ${WHITE}1 000 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}500${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}5h 20m ago ${GRAY}(${WHITE}2025-01-15 10:10:00${GRAY})",
            "${radioactiveVial.displayName}s${GRAY}: ${WHITE}5",
            "${GRAY}Last on: ${WHITE}2h 15m ${GRAY}(${WHITE}2025-01-15 13:15:00${GRAY})",
            "${GRAY}Last on: ${WHITE}1 234 ${GRAY}Lord Jawbus ago"
        ))
        .setSettingsKey { Overlays.crimsonIsleTrackerOverlay }
        .setCondition {
            WorldUtils.getWorldName() == WorldUtils.CRIMSON_ISLE && PlayerUtils.isFishingHookSeenMinutesAgo(5)
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
            resetCrimsonIsleTracker(isConfirmed)
        }
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!Overlays.crimsonIsleTrackerOverlay || !WorldUtils.isInSkyblock()) return

        val seaCreatureName = event.seaCreatureName
        val worldName = WorldUtils.getWorldName()
        val isInCrimsonIsle = worldName == WorldUtils.CRIMSON_ISLE

        if (!isInCrimsonIsle) return

        if (seaCreatureName == thunder.name) {
            onThunder(event.isDoubleHook)
        } else if (seaCreatureName == lordJawbus.name) {
            onLordJawbus(event.isDoubleHook)
        } else {
            onOtherSeaCreature()
        }
    }

    private fun onThunder(@Suppress("UNUSED_PARAMETER") isDoubleHook: Boolean) {
        data.thunder.updateAfterCatch(thunder.boldDisplayName)
        data.lordJawbus.incrementCatches()
        saveData()
        updateGuiLines()
    }

    private fun onLordJawbus(isDoubleHook: Boolean) {
        data.lordJawbus.updateAfterCatch(lordJawbus.boldDisplayName)
        data.thunder.incrementCatches()
        data.radioactiveVials.updateAfterCatch(isDoubleHook)
        saveData()
        updateGuiLines()
    }

    private fun onOtherSeaCreature() {
        data.thunder.incrementCatches()
        data.lordJawbus.incrementCatches()

        saveData()
        updateGuiLines()
    }

    private fun onRareDrop(event: RareDropEvent) {
        if (!Overlays.crimsonIsleTrackerOverlay || !WorldUtils.isInSkyblock()) return

        val itemName = event.itemName
        val worldName = WorldUtils.getWorldName()
        val isInCrimsonIsle = worldName == WorldUtils.CRIMSON_ISLE

        if (itemName == radioactiveVial.itemName && isInCrimsonIsle) {
            onRadioactiveVial(event.magicFind)
        }
    }

    private fun onRadioactiveVial(magicFind: Int?) {
        data.radioactiveVials.updateAfterDrop(radioactiveVial.boldDisplayName, lordJawbus.boldDisplayName, magicFind)
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

        if (!Overlays.crimsonIsleTrackerOverlay || !WorldUtils.isInSkyblock() || !PlayerUtils.isFishingHookSeenMinutesAgo(5) || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return
        if (!hasData()) return

        val lines = mutableListOf<String>()

        lines.add("${GRAY}[${RED}Click to reset${GRAY}] ${DARK_GRAY}(/${RESET_COMMAND})")
        lines.add(baseTitle)

        lines.addAll(data.thunder.getOverlayText(thunder.displayName))
        lines.addAll(data.lordJawbus.getOverlayText(lordJawbus.displayName))
        lines.addAll(data.radioactiveVials.getOverlayText(radioactiveVial.displayName, lordJawbus.displayName))

        gui.setLines(lines)
    }

    private fun hasData(): Boolean {
        return data.thunder.hasData() || data.lordJawbus.hasData() || data.radioactiveVials.hasData()
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetCrimsonIsleTrackerOnGameClosed &&
            Overlays.crimsonIsleTrackerOverlay &&
            hasData()) {
            reset()
            FeeshMod.LOGGER.info("[Feesh] Automatically reset Crimson Isle tracker on game closed.")
        }
    }

    private fun reset() {
        data.thunder.reset()
        data.lordJawbus.reset()
        data.radioactiveVials.reset()
        saveData()
    }

    private fun resetCrimsonIsleTracker(isConfirmed: Boolean) {
        try {
            if (!isConfirmed) {
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to reset Crimson Isle tracker? ${RED}${BOLD}[Click to confirm]",
                    "${RESET_COMMAND} noconfirm",
                    true
                )
                return
            }

            reset()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Crimson Isle tracker was reset.", true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to reset Crimson Isle tracker.", e)
        }
    }

    private fun saveData() {
        PersistentDataManager.saveFeeshDataToFileAsync()
    }
    
    fun setRadioactiveVials(count: Int, lastOn: Date?) {
        try {
            if (!WorldUtils.isInSkyblock()) return
            
            data.radioactiveVials.initDropCount(count, lastOn)         
            saveData()
            ChatUtils.sendLocalChat("${GRAY}Successfully changed Radioactive Vials count to ${count} in the Crimson Isle tracker.", true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to set Radioactive Vials.", e)
            ChatUtils.sendLocalChat("${RED}Failed to set Radioactive Vials.", true)
        }
    }
}
