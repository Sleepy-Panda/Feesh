package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.TabListUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.ClientTickEvent
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.settings.categories.Overlays
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

object JerryWorkshopTracker {
    data class CatchCounterData(var catchesSinceLast: Int = 0, var lastCatchTime: Date? = null, var averageCatches: Int = 0, var catchesHistory: List<Int> = emptyList())
    data class JerryWorkshopTrackerData(val yeti: CatchCounterData = CatchCounterData(), val reindrake: CatchCounterData = CatchCounterData())

    private var data = JerryWorkshopTrackerData()
    private var tickCounter = 0
    private const val TICKS_PER_UPDATE = 20

    private val title = "${AQUA}${BOLD}Jerry Workshop tracker"
    private val yeti = SeaCreatures.allSeaCreatures.find { it.name === "Yeti" }!!
    private val reindrake = SeaCreatures.allSeaCreatures.find { it.name === "Reindrake" }!!

    private val gui = FeeshGui()
        .setX(10)
        .setY(10)
        .setClickable(false)
        .setSampleLines(listOf(
            title, 
            "${GOLD}${yeti.name}${GRAY}: ${WHITE}10 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}50${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}1 minute ago",
            "${LIGHT_PURPLE}${reindrake.name}${GRAY}: ${WHITE}100 ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}500${DARK_GRAY})",
            "${GRAY}Last on: ${WHITE}1 hour ago"
        ))
        .setSettingsKey { Overlays.jerryWorkshopTrackerOverlay }
        .setCondition {
            WorldUtils.getWorldName() == WorldUtils.JERRY_WORKSHOP &&
            PlayerUtils.isFishingHookSeenMinutesAgo(5)
        }

    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!Overlays.jerryWorkshopTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return

        val seaCreatureName = event.seaCreatureName

        if (seaCreatureName == yeti.name) onYeti()
        else if (seaCreatureName == reindrake.name) onReindrake()
        else onOtherSeaCreature()
    }

    private fun onYeti() {
        if (!Overlays.jerryWorkshopTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return
        
        data.yeti.catchesSinceLast++
        val catchesSinceLast = data.yeti.catchesSinceLast
        ChatUtils.sendLocalChat(getMessage(catchesSinceLast, data.yeti.lastCatchTime, yeti.name, yeti.rarityColorCode), true)

        data.yeti.lastCatchTime = Date()
        data.yeti.catchesHistory = (listOf(catchesSinceLast) + data.yeti.catchesHistory).take(100)
        data.yeti.averageCatches = data.yeti.catchesHistory.average().toInt()
        data.yeti.catchesSinceLast = 0
        data.reindrake.catchesSinceLast++
    }

    private fun onReindrake() {
        if (!Overlays.jerryWorkshopTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return

        data.reindrake.catchesSinceLast++
        val catchesSinceLast = data.reindrake.catchesSinceLast
        ChatUtils.sendLocalChat(getMessage(catchesSinceLast, data.reindrake.lastCatchTime, reindrake.name, reindrake.rarityColorCode), true)

        data.reindrake.lastCatchTime = Date()
        data.reindrake.catchesHistory = (listOf(catchesSinceLast) + data.reindrake.catchesHistory).take(100)
        data.reindrake.averageCatches = data.reindrake.catchesHistory.average().toInt()
        data.reindrake.catchesSinceLast = 0
        data.yeti.catchesSinceLast++
    }

    private fun onOtherSeaCreature() {
        if (!Overlays.jerryWorkshopTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return

        data.yeti.catchesSinceLast++
        data.reindrake.catchesSinceLast++
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        if (!Overlays.jerryWorkshopTrackerOverlay || 
            !WorldUtils.isInSkyblock() || 
            WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP ||
            !PlayerUtils.isFishingHookSeenMinutesAgo(5)) {
            gui.clearLines()
            return
        }

        if (!hasData()) {
            gui.clearLines()
            return
        }

        updateGuiLines()
    }

    private fun updateGuiLines() {
        val lines = mutableListOf<String>()
        lines.add(title)

        val yetiLine = "${GOLD}${yeti.name}: ${WHITE}${data.yeti.catchesSinceLast} ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}${data.yeti.averageCatches}${DARK_GRAY})"
        lines.add(yetiLine)

        var yetiLastOnLine: String
        if (data.yeti.lastCatchTime != null) {
            val lastOn = formatDate(data.yeti.lastCatchTime)
            val since = formatTimeElapsed(data.yeti.lastCatchTime)
            yetiLastOnLine = "${GRAY}Last on: ${WHITE}${since} ago ${GRAY}(${WHITE}${lastOn}${GRAY})"
        } else {
            yetiLastOnLine = "${GRAY}Last on: ${WHITE}N/A${GRAY}"
        }
        lines.add(yetiLastOnLine)

        val reindrakeLine = "${LIGHT_PURPLE}${reindrake.name}: ${WHITE}${data.reindrake.catchesSinceLast} ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}${data.reindrake.averageCatches}${DARK_GRAY})"
        lines.add(reindrakeLine)

        var reindrakeLastOnLine: String
        if (data.reindrake.lastCatchTime != null) {
            val lastOn = formatDate(data.reindrake.lastCatchTime)
            val since = formatTimeElapsed(data.reindrake.lastCatchTime)
            reindrakeLastOnLine = "${GRAY}Last on: ${WHITE}${since} ago ${GRAY}(${WHITE}${lastOn}${GRAY})"
        } else {
            reindrakeLastOnLine = "${GRAY}Last on: ${WHITE}N/A${GRAY}"
        }
        lines.add(reindrakeLastOnLine)

        val islandOpen = TabListUtils.getLineAfter("Island open:")
        val islandClosesIn = TabListUtils.getLineAfter("Island closes in:")
        if (!islandOpen.isNullOrEmpty()) {
            val islandOpenLine = "${GRAY}Island open: ${WHITE}${islandOpen}"
            lines.add(islandOpenLine)
        } else if (!islandClosesIn.isNullOrEmpty()) {
            val islandClosesInLine = "${GRAY}Island closes in: ${WHITE}${islandClosesIn}"
            lines.add(islandClosesInLine)
        }

        gui.setLines(lines)
    }

    private fun hasData(): Boolean {
        return hasYetiData() || hasReindrakeData()
    }

    private fun hasYetiData(): Boolean {
        return data.yeti.catchesSinceLast > 0 || data.yeti.catchesHistory.isNotEmpty() || data.yeti.lastCatchTime != null
    }

    private fun hasReindrakeData(): Boolean {
        return data.reindrake.catchesSinceLast > 0 || data.reindrake.catchesHistory.isNotEmpty() || data.reindrake.lastCatchTime != null
    }

    private fun formatTimeElapsed(lastCatchTime: Date?): String {
        if (lastCatchTime == null) return ""

        val now = Date()
        val diffMillis = now.time - lastCatchTime.time
        val diffSeconds = diffMillis / 1000

        val days = TimeUnit.SECONDS.toDays(diffSeconds)
        val hours = TimeUnit.SECONDS.toHours(diffSeconds) % 24
        val minutes = TimeUnit.SECONDS.toMinutes(diffSeconds) % 60

        return when {
            days > 0 -> "${days}d ${hours}h ${minutes}m"
            hours > 0 -> "${hours}h ${minutes}m"
            minutes < 1 -> "less than 1m"
            else -> "${minutes}m"
        }
    }

    private fun formatDate(date: Date?): String {
        if (date == null) return ""
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return formatter.format(date)
    }

    private fun getMessage(catchesSinceLast: Int, lastCatchTime: Date?, seaCreatureName: String, rarityCode: String): String {
        val b2bText = if (catchesSinceLast == 1) "${RED}B2B! " else ""
        val elapsedTimeText = if (lastCatchTime != null) " ${GRAY}(${WHITE}${formatTimeElapsed(lastCatchTime)}${GRAY})" else ""
        val catchesText = "${WHITE}${catchesSinceLast} ${GRAY}${if (catchesSinceLast == 1) "catch" else "catches"}"
        val seaCreatureDisplayName = "${rarityCode}${seaCreatureName}"
        return "${b2bText}${GRAY}It took ${catchesText}${elapsedTimeText} to get the ${seaCreatureDisplayName}${GRAY}."
    }
}