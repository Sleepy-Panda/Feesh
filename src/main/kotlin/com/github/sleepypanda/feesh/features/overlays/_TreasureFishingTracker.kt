package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.ClientTickEvent
import com.github.sleepypanda.feesh.events.ChatEvent
import com.github.sleepypanda.feesh.events.WorldChangedEvent
import com.github.sleepypanda.feesh.events.RareDropEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.MoveGuisScreen
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import java.text.SimpleDateFormat
import java.util.Date

data class TreasureCatchesData(
    var good: Int = 0,
    var great: Int = 0,
    var outstanding: Int = 0
)

data class TreasureDyeData(
    var count: Int = 0,
    var catchesSinceLast: Int = 0,
    var dropsHistory: MutableList<DropHistoryEntry> = mutableListOf()
)

data class DropHistoryEntry(
    val catches: Int,
    val lastDropTime: Date?
)

data class TreasureFishingSessionData(
    val catches: TreasureCatchesData = TreasureCatchesData()
)

data class TreasureFishingTotalData(
    val catches: TreasureCatchesData = TreasureCatchesData(),
    val treasureDyes: TreasureDyeData = TreasureDyeData()
)

data class TreasureFishingData(
    var session: TreasureFishingSessionData = TreasureFishingSessionData(),
    var total: TreasureFishingTotalData = TreasureFishingTotalData()
)

object TreasureFishingTracker {
    private var data = TreasureFishingData()
    private var lastTreasureCaughtAt: Date? = null
    private var tickCounter = 0
    private const val TICKS_PER_UPDATE = 20

    private val goodCatchPattern = Regex("^⛃ GOOD CATCH!")
    private val greatCatchPattern = Regex("^⛃ GREAT CATCH!")
    private val outstandingCatchPattern = Regex("^⛃ OUTSTANDING CATCH!")
    private val goodJunkCatchPattern = Regex("^⛃ GOOD JUNK CATCH!")
    private val greatJunkCatchPattern = Regex("^⛃ GREAT JUNK CATCH!")
    private val outstandingJunkCatchPattern = Regex("^⛃ OUTSTANDING JUNK CATCH!")
    private val treasureDyePattern = Regex("^RARE DROP! Treasure Dye.*", RegexOption.IGNORE_CASE)

    private val gui = FeeshGui()
        .setCoordsDataKey("treasureFishingTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            "${AQUA}${BOLD}Treasure fishing tracker",
            "${GRAY}- ${DARK_PURPLE}Good catch${GRAY}: ${WHITE}100",
            "${GRAY}- ${GOLD}Great catch${GRAY}: ${WHITE}50",
            "${GRAY}- ${LIGHT_PURPLE}Outstanding catch${GRAY}: ${WHITE}10",
            "${GRAY}Total Treasures: ${WHITE}160",
            "",
            "${GOLD}Treasure Dye${GRAY}: ${WHITE}2 ${GRAY}(${WHITE}50${GRAY} catches since last)"
        ))
        .setSettingsKey { Overlays.treasureFishingTrackerOverlay }
        .setCondition {
            WorldUtils.isInSkyblock() && WorldUtils.isInFishingWorld()
        }

    fun init() {
        registerChatHandlers()
        registerCommands()
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(RareDropEvent::class, ::onRareDrop)
    }

    private fun registerChatHandlers() {
        RegisterUtils.chat(goodCatchPattern) { _, _ -> trackTreasureCatch("good") }
        RegisterUtils.chat(goodJunkCatchPattern) { _, _ -> trackTreasureCatch("good") }
        RegisterUtils.chat(greatCatchPattern) { _, _ -> trackTreasureCatch("great") }
        RegisterUtils.chat(greatJunkCatchPattern) { _, _ -> trackTreasureCatch("great") }
        RegisterUtils.chat(outstandingCatchPattern) { _, _ -> trackTreasureCatch("outstanding") }
        RegisterUtils.chat(outstandingJunkCatchPattern) { _, _ -> trackTreasureCatch("outstanding") }

        // TODO Treasure Dye
        //RegisterUtils.chat(treasureDyePattern) { _, _ ->
        //    trackTreasureDyeDrop()
        //}
    }

    private fun registerCommands() {
        RegisterUtils.command("feeshResetTreasureFishing") { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetTreasureFishingTracker(isConfirmed)
        }

        RegisterUtils.command("feeshSetTrackerDrops") { args ->
            if (args.size < 2) {
                ChatUtils.sendLocalChat("${RED}Usage: /feeshSetTrackerDrops <ITEM_ID> <COUNT> [LAST_ON_DATE]", true)
                return@command
            }

            val itemId = args[0]
            if (itemId != "DYE_TREASURE") {
                ChatUtils.sendLocalChat("${RED}Invalid item ID. Only DYE_TREASURE is supported.", true)
                return@command
            }

            val count = args[1].toIntOrNull()
            if (count == null || count < 0) {
                ChatUtils.sendLocalChat("${RED}Invalid count. Must be a non-negative number.", true)
                return@command
            }

            val lastOn = if (args.size >= 3) {
                val dateStr = args.drop(2).joinToString(" ")
                parseDate(dateStr)
            } else null

            setTreasureDyes(count, lastOn)
        }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        refreshOverlay()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        lastTreasureCaughtAt = null
    }

    private fun onRareDrop(event: RareDropEvent) {
        if (!Overlays.treasureFishingTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        if (event.itemName.contains("Treasure Dye", ignoreCase = true)) {
            trackTreasureDyeDrop()
        }
    }

    fun setTreasureDyes(count: Int, lastOn: Date?) {
        try {
            if (!WorldUtils.isInSkyblock()) {
                return
            }

            val errorMessage = initDropCountOnOverlay(data.total.treasureDyes, count, lastOn)
            if (errorMessage != null) {
                ChatUtils.sendLocalChat(errorMessage, true)
                return
            }

            refreshOverlay()
            ChatUtils.sendLocalChat("${GRAY}Successfully changed Treasure Dyes count to ${count} for the Treasure fishing tracker.", true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to set Treasure Dyes", e)
            ChatUtils.sendLocalChat("${RED}Failed to set Treasure Dyes.", true)
        }
    }

    fun resetTreasureFishingTracker(isConfirmed: Boolean) {
        try {
            if (!isConfirmed) {
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to reset Treasure fishing tracker? ${RED}${BOLD}[Click to confirm]",
                    "feeshResetTreasureFishing noconfirm",
                    true
                )
                return
            }

            data = TreasureFishingData()
            lastTreasureCaughtAt = null

            refreshOverlay()
            ChatUtils.sendLocalChat("${WHITE}Treasure fishing tracker was reset.", true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to reset Treasure fishing tracker", e)
            ChatUtils.sendLocalChat("${RED}Failed to reset Treasure fishing tracker.", true)
        }
    }

    private fun trackTreasureCatch(treasureType: String) {
        try {
            if (!Overlays.treasureFishingTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

            lastTreasureCaughtAt = Date()
            when (treasureType) {
                "good" -> {
                    data.total.catches.good++
                    data.session.catches.good++
                }
                "great" -> {
                    data.total.catches.great++
                    data.session.catches.great++
                }
                "outstanding" -> {
                    data.total.catches.outstanding++
                    data.session.catches.outstanding++
                }
            }
            data.total.treasureDyes.catchesSinceLast++
            refreshOverlay()
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to track treasure catch", e)
        }
    }

    private fun trackTreasureDyeDrop() {
        try {
            if (!Overlays.treasureFishingTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) {
                return
            }

            val result = setDropStatisticsOnDrop(data.total.treasureDyes, "catchesSinceLast", "catches", null)
            refreshOverlay()

            //val dropNumber = data.total.treasureDyes.count
            //val message = getDropCatchesCounterChatMessage("${GOLD}Treasure Dye", "Treasure", result.lastDropTime, dropNumber, result.catches)
            //ChatUtils.sendLocalChat(message, true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to track Treasure Dye drop", e)
        }
    }

    private fun hasAnyData(): Boolean {
        return data.total.catches.good > 0 ||
                data.total.catches.great > 0 ||
                data.total.catches.outstanding > 0
    }

    private fun refreshOverlay() {
        gui.clearLines()

        if (!Overlays.treasureFishingTrackerOverlay ||
            !hasAnyData() ||
            !WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld() ||
            lastTreasureCaughtAt == null ||
            (Date().time - lastTreasureCaughtAt!!.time > 2 * 60 * 1000) ||
            FeeshMod.mc.currentScreen is MoveGuisScreen
        ) {
            return
        }

        val lines = mutableListOf<String>()
        lines.add("${AQUA}${BOLD}Treasure fishing tracker")
        lines.add("${GRAY}- ${DARK_PURPLE}Good catch${GRAY}: ${WHITE}${CommonUtils.formatNumberWithSpaces(data.total.catches.good)}")
        lines.add("${GRAY}- ${GOLD}Great catch${GRAY}: ${WHITE}${CommonUtils.formatNumberWithSpaces(data.total.catches.great)}")
        lines.add("${GRAY}- ${LIGHT_PURPLE}Outstanding catch${GRAY}: ${WHITE}${CommonUtils.formatNumberWithSpaces(data.total.catches.outstanding)}")

        val totalTreasureCatches = data.total.catches.good + data.total.catches.great + data.total.catches.outstanding
        lines.add("${GRAY}Total Treasures: ${WHITE}${CommonUtils.formatNumberWithSpaces(totalTreasureCatches)}")

        val treasureDyesText = getDropStatisticsOverlayText("${GOLD}Treasure Dye", "Treasure", data.total.treasureDyes, "catchesSinceLast")
        lines.add("")
        lines.add(treasureDyesText)

        // Add reset button text
        lines.add("${RED}${BOLD}[Click to reset] ${DARK_GRAY}(/feeshResetTreasureFishing)")

        gui.setLines(lines)
    }

    private data class DropStatisticsResult(
        val lastDropTime: Date?,
        val catches: Int
    )

    private fun initDropCountOnOverlay(dropData: TreasureDyeData, count: Int, lastOn: Date?): String? {
        if (count < 0) {
            return "${RED}Count cannot be negative."
        }

        if (lastOn != null) {
            val now = Date()
            if (lastOn.after(now)) {
                return "${RED}Last drop date cannot be in the future."
            }
        }

        dropData.count = count
        if (lastOn != null) {
            dropData.dropsHistory.clear()
            dropData.dropsHistory.add(DropHistoryEntry(0, lastOn))
        } else {
            dropData.dropsHistory.clear()
        }
        dropData.catchesSinceLast = 0

        return null
    }

    private fun setDropStatisticsOnDrop(
        dropData: TreasureDyeData,
        catchesSinceLastKey: String,
        catchesKey: String,
        lastDropTime: Date?
    ): DropStatisticsResult {
        val catchesSinceLast = dropData.catchesSinceLast
        val now = lastDropTime ?: Date()

        dropData.count++
        dropData.dropsHistory.add(DropHistoryEntry(catchesSinceLast, now))
        if (dropData.dropsHistory.size > 100) {
            dropData.dropsHistory.removeAt(0)
        }
        dropData.catchesSinceLast = 0

        return DropStatisticsResult(now, catchesSinceLast)
    }

    private fun getDropStatisticsOverlayText(
        itemName: String,
        category: String,
        dropData: TreasureDyeData,
        catchesSinceLastKey: String
    ): String {
        val count = dropData.count
        val catchesSinceLast = dropData.catchesSinceLast

        if (count == 0 && catchesSinceLast == 0) {
            return "${itemName}${GRAY}: ${WHITE}0"
        }

        val countText = "${itemName}${GRAY}: ${WHITE}${count}"
        val catchesText = if (catchesSinceLast > 0) {
            " ${GRAY}(${WHITE}${catchesSinceLast}${GRAY} catches since last)"
        } else {
            ""
        }

        return countText + catchesText
    }

    //private fun getDropCatchesCounterChatMessage(
    //    itemName: String,
    //    category: String,
    //    lastDropTime: Date?,
    //    dropNumber: Int,
    //    catches: Int
    //): String {
    //    val timeText = if (lastDropTime != null) {
    //        val elapsed = formatTimeElapsed(lastDropTime)
    //        " ${GRAY}(${WHITE}${elapsed}${GRAY} ago)"
    //    } else {
    //        ""
    //    }
//
    //    return "${GOLD}[Feesh] ${GRAY}You dropped ${itemName}${GRAY}! It's your ${WHITE}${dropNumber}${GRAY} ${category} drop. It took ${WHITE}${catches} ${GRAY}${if (catches == 1) "catch" else "catches"}${timeText}."
    //}


    private fun parseDate(dateStr: String): Date? {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            formatter.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }
}

