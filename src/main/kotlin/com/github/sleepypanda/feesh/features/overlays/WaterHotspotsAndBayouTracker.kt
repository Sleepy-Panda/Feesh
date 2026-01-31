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
import com.github.sleepypanda.feesh.utils.HotspotUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.ClientTickEvent
import com.github.sleepypanda.feesh.events.RareDropEvent
import com.github.sleepypanda.feesh.events.GameClosedEvent
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import java.util.Date

object WaterHotspotsAndBayouTracker {
    data class WaterHotspotsAndBayouTrackerData(
        val titanoboa: CatchCounterData = CatchCounterData(),
        val wikiTiki: CatchCounterData = CatchCounterData(),
        val titanoboaSheds: DropCounterData = DropCounterData(),
        val tikiMasks: DropCounterData = DropCounterData()
    )

    const val RESET_COMMAND = "feeshResetWaterHotspotsAndBayou"

    private const val TICKS_PER_UPDATE = 20

    private var data = PersistentDataManager.feeshData.waterHotspotsAndBayou
    private var tickCounter = 0
    private val baseTitle = "${AQUA}${BOLD}Water hotspots & Bayou tracker"

    private val titanoboa = SeaCreatures.allSeaCreatures.find { it.name == "Titanoboa" }!!
    private val wikiTiki = SeaCreatures.allSeaCreatures.find { it.name == "Wiki Tiki" }!!
    private val titanoboaShed = RareDrops.rareDrops.find { it.itemName == "Titanoboa Shed" }!!
    private val tikiMask = RareDrops.rareDrops.find { it.itemName == "Tiki Mask" }!!

    private val gui = FeeshGui()
        .setCoordsDataKey("waterHotspotsAndBayouTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${titanoboa.displayName}${GRAY}: ${WHITE}10 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}500${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}1h 30m ago ${GRAY}(${WHITE}2025-01-15 14:30:00${GRAY})",
            "${titanoboaShed.displayName}s${GRAY}: ${WHITE}5",
            "${GRAY}Last on: ${WHITE}2h 15m ${GRAY}(${WHITE}2025-01-15 13:15:00${GRAY})",
            "${GRAY}Last on: ${WHITE}1 234 ${GRAY}Titanoboas ago",
            "${wikiTiki.displayName}${GRAY}: ${WHITE}1 000 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}500${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}5h 20m ago ${GRAY}(${WHITE}2025-01-15 10:10:00${GRAY})",
            "${tikiMask.displayName}s${GRAY}: ${WHITE}3",
            "${GRAY}Last on: ${WHITE}3h 45m ${GRAY}(${WHITE}2025-01-15 11:45:00${GRAY})",
            "${GRAY}Last on: ${WHITE}567 ${GRAY}Wiki Tikis ago"
        ))
        .setSettingsKey { Overlays.waterHotspotsAndBayouTrackerOverlay }
        .setCondition {
            val isInBayou = WorldUtils.getWorldName() == WorldUtils.BACKWATER_BAYOU
            (WorldUtils.isInWaterHotspotFishingWorld() || isInBayou) && PlayerUtils.isFishingHookSeenMinutesAgo(5)
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
            resetWaterHotspotsAndBayouTracker(isConfirmed)
        }
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!Overlays.waterHotspotsAndBayouTrackerOverlay || !WorldUtils.isInSkyblock()) return

        val seaCreatureName = event.seaCreatureName
        val worldName = WorldUtils.getWorldName()
        val isInHotspotWorld = WorldUtils.isInWaterHotspotFishingWorld()
        val isInBayou = worldName == WorldUtils.BACKWATER_BAYOU
        val isInHotspot = isInHotspotWorld && isFishingInHotspot()

        if (seaCreatureName == titanoboa.name && isInBayou) {
            onTitanoboa(event.isDoubleHook)
        } else if (seaCreatureName == wikiTiki.name && isInHotspot) {
            onWikiTiki(event.isDoubleHook)
        } else if (isInHotspot || isInBayou) {
            onOtherSeaCreature(isInHotspot, isInBayou)
        }
    }

    private fun onTitanoboa(isDoubleHook: Boolean) {
        data.titanoboa.updateAfterCatch(titanoboa.boldDisplayName)
        data.wikiTiki.incrementCatches()
        data.titanoboaSheds.updateAfterCatch(isDoubleHook)
        saveData()
        updateGuiLines()
    }

    private fun onWikiTiki(isDoubleHook: Boolean) {
        data.wikiTiki.updateAfterCatch(wikiTiki.boldDisplayName)
        data.titanoboa.incrementCatches()
        data.tikiMasks.updateAfterCatch(isDoubleHook)
        saveData()
        updateGuiLines()
    }

    private fun onOtherSeaCreature(isInHotspot: Boolean, isInBayou: Boolean) {
        if (isInBayou) {
            data.titanoboa.incrementCatches()
        }
        if (isInHotspot) {
            data.wikiTiki.incrementCatches()
        }

        saveData()
        updateGuiLines()
    }

    private fun onRareDrop(event: RareDropEvent) {
        if (!Overlays.waterHotspotsAndBayouTrackerOverlay || !WorldUtils.isInSkyblock()) return

        val itemName = event.itemName
        val worldName = WorldUtils.getWorldName()
        val isInHotspotWorld = WorldUtils.isInWaterHotspotFishingWorld()
        val isInBayou = worldName == WorldUtils.BACKWATER_BAYOU

        if (itemName == titanoboaShed.itemName && isInBayou) {
            onTitanoboaShed(event.magicFind)
        } else if (itemName == tikiMask.itemName && isInHotspotWorld) {
            onTikiMask(event.magicFind)
        }
    }

    private fun onTitanoboaShed(magicFind: Int?) {
        data.titanoboaSheds.updateAfterDrop(titanoboaShed.boldDisplayName, titanoboa.displayName, magicFind)
        saveData()
        updateGuiLines()
    }

    private fun onTikiMask(magicFind: Int?) {
        data.tikiMasks.updateAfterDrop(tikiMask.boldDisplayName, wikiTiki.displayName, magicFind)
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
        if (!Overlays.waterHotspotsAndBayouTrackerOverlay || !WorldUtils.isInSkyblock() || !PlayerUtils.isFishingHookSeenMinutesAgo(5)) return

        val worldName = WorldUtils.getWorldName()
        val isInHotspotWorld = WorldUtils.isInWaterHotspotFishingWorld()
        val isInBayou = worldName == WorldUtils.BACKWATER_BAYOU
        val isInHotspot = isInHotspotWorld && isFishingInHotspot()
        if (!isInHotspot && !isInBayou) return

        val lines = mutableListOf<String>()

        lines.add("${GRAY}[${RED}Click to reset${GRAY}] ${DARK_GRAY}(/${RESET_COMMAND})")
        lines.add(baseTitle)

        if (isInBayou) {
            lines.addAll(data.titanoboa.getOverlayText(titanoboa.displayName))
        }

        if (isInBayou) {
            lines.addAll(data.titanoboaSheds.getOverlayText(titanoboaShed.displayName, titanoboa.displayName))
        }

        if (isInHotspot) {
            lines.addAll(data.wikiTiki.getOverlayText(wikiTiki.displayName))
        }

        if (isInHotspot) {
            lines.addAll(data.tikiMasks.getOverlayText(tikiMask.displayName, wikiTiki.displayName))
        }

        gui.setLines(lines)
    }

    private fun hasData(): Boolean {
        return data.titanoboa.hasData() || data.wikiTiki.hasData() || data.titanoboaSheds.hasData() || data.tikiMasks.hasData()
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetWaterHotspotsAndBayouTrackerOnGameClosed &&
            Overlays.waterHotspotsAndBayouTrackerOverlay &&
            hasData()) {
            reset()
            FeeshMod.LOGGER.info("[Feesh] Automatically reset Water hotspots & Bayou tracker on game closed.")
        }
    }

    private fun reset() {
        data.titanoboa.reset()
        data.wikiTiki.reset()
        data.titanoboaSheds.reset()
        data.tikiMasks.reset()
        saveData()
    }

    private fun resetWaterHotspotsAndBayouTracker(isConfirmed: Boolean) {
        try {
            if (!isConfirmed) {
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to reset Water hotspots & Bayou tracker? ${RED}${BOLD}[Click to confirm]",
                    "${RESET_COMMAND} noconfirm",
                    true
                )
                return
            }

            reset()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Water hotspots & Bayou tracker was reset.", true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to reset Water hotspots & Bayou tracker.", e)
        }
    }

    private fun saveData() {
        PersistentDataManager.saveFeeshDataToFileAsync()
    }

    private fun isFishingInHotspot(): Boolean {
        if (!WorldUtils.isInWaterHotspotFishingWorld()) return false
        return PlayerUtils.isFishingHookInHotspotSeenMinutesAgo(1)
    }
    
    fun setTitanoboaSheds(count: Int, lastOn: Date?) {
        try {
            if (!WorldUtils.isInSkyblock()) return
            
            data.titanoboaSheds.initDropCount(count, lastOn)         
            saveData()
            ChatUtils.sendLocalChat("${GRAY}Successfully changed Titanoboa Sheds count to ${count} for the Water Hotspots & Bayou tracker.", true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to set Titanoboa Sheds.", e)
            ChatUtils.sendLocalChat("${RED}Failed to set Titanoboa Sheds.", true)
        }
    }
    
    fun setTikiMasks(count: Int, lastOn: Date?) {
        try {
            if (!WorldUtils.isInSkyblock()) return

            data.tikiMasks.initDropCount(count, lastOn)       
            saveData()
            ChatUtils.sendLocalChat("${GRAY}Successfully changed Tiki Masks count to ${count} for the Water Hotspots & Bayou tracker.", true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to set Tiki Masks.", e)
            ChatUtils.sendLocalChat("${RED}Failed to set Tiki Masks.", true)
        }
    }
}
