package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import net.minecraft.network.chat.Component
import java.text.SimpleDateFormat
import java.util.Date

data class CatchCounterData(
    var catchesSinceLast: Int = 0,
    var lastCatchTime: Date? = null,
    var averageCatches: Int? = null,
    var catchesHistory: List<Int> = emptyList()
) {
    fun getOverlayLines(seaCreatureDisplayName: String): List<LineInfo> {
        val averageCatchesText = if (averageCatches != null) "${GRAY}avg: ${WHITE}${CommonUtils.formatNumberWithSpaces(averageCatches!!)}" else "${GRAY}avg: ${WHITE}N/A"
        val catchesText = CommonUtils.formatNumberWithSpaces(catchesSinceLast)
        val catchesSinceLastText = if (catchesSinceLast == 1) "${WHITE}${catchesText} ${GRAY}catch ago" else "${WHITE}${catchesText} ${GRAY}catches ago"
        val counterLine = LineInfo("${seaCreatureDisplayName}${GRAY}: ${catchesSinceLastText} ${DARK_GRAY}(${averageCatchesText}${DARK_GRAY})")
        val lastOnLine = getLastOnOverlayLine()
        return listOf(counterLine, lastOnLine)
    }

    private fun getLastOnOverlayLine(): LineInfo {
        if (lastCatchTime != null) {
            val lastOn = CommonUtils.formatDate(lastCatchTime)
            val since = CommonUtils.formatTimeElapsed(lastCatchTime)
            return LineInfo(
                "${GRAY}Last on: ${WHITE}${since} ago", 
                tooltip = listOf(Component.literal("${GRAY}Last on: ${WHITE}${lastOn}"))
            )
        } else {
            return LineInfo("${GRAY}Last on: ${WHITE}N/A${GRAY}")
        }
    }

    fun hasData(): Boolean {
        return catchesSinceLast > 0 || lastCatchTime != null
    }

    fun updateAfterCatch(seaCreatureDisplayName: String) {
        val prevCatchTime = lastCatchTime

        catchesSinceLast++
        val catchesSinceLastValue = catchesSinceLast

        lastCatchTime = Date()
        catchesHistory = (listOf(catchesSinceLastValue) + catchesHistory).take(100) // Latest catch on top
        averageCatches = catchesHistory.average().toInt()
        catchesSinceLast = 0

        ChatUtils.sendLocalChat(getCatchChatMessage(seaCreatureDisplayName, prevCatchTime, catchesSinceLastValue), true)
    }

    private fun getCatchChatMessage(seaCreatureDisplayName: String, prevCatchTime: Date?, catchesSinceLast: Int): String {
        val b2bText = if (catchesSinceLast == 1) "${RED}B2B! " else ""
        val elapsedTimeText = if (prevCatchTime != null) " ${GRAY}(${WHITE}${CommonUtils.formatTimeElapsed(prevCatchTime)}${GRAY})" else ""
        val catchesText = "${WHITE}${CommonUtils.formatNumberWithSpaces(catchesSinceLast)} ${GRAY}${if (catchesSinceLast == 1) "catch" else "catches"}"
        return "${b2bText}${GRAY}It took ${catchesText}${elapsedTimeText} to get the ${seaCreatureDisplayName}${GRAY}."
    }

    fun incrementCatches() {
        catchesSinceLast++
    }

    fun reset() {
        catchesSinceLast = 0
        lastCatchTime = null
        averageCatches = 0
        catchesHistory = emptyList()
    }
}

data class DropsHistoryEntry(
    val catches: Int, // Number of sea creature catches since last drop
    val time: Date, // DateTime of the drop
    val magicFind: Int? // Magic find associated with the drop
)

data class DropCounterData(
    var count: Int = 0, // Total count of times the item was dropped
    var catchesSinceLast: Int = 0, // Number of sea creature catches since last drop
    var lastDropTime: Date? = null, // DateTime of the last drop
    var dropsHistory: List<DropsHistoryEntry> = emptyList(), // History of drops
) {
    fun hasData(): Boolean {
        return catchesSinceLast > 0 || count > 0
    }

    fun updateAfterCatch(isDoubleHook: Boolean) {
        val valueToAdd = if (isDoubleHook) 2 else 1
        catchesSinceLast += valueToAdd
    }

    fun updateAfterDrop(itemDisplayName: String, seaCreatureDisplayName: String, magicFind: Int?) {
        val prevDropTime = lastDropTime
        val prevCatchesSinceLast = catchesSinceLast

        count++
        lastDropTime = Date()
        dropsHistory = (listOf(DropsHistoryEntry(catchesSinceLast, Date(), magicFind)) + dropsHistory)
        catchesSinceLast = 0

        ChatUtils.sendLocalChat(getDropChatMessage(itemDisplayName, seaCreatureDisplayName, prevDropTime, prevCatchesSinceLast), true)
    }

    private fun getDropChatMessage(itemDisplayName: String, seaCreatureDisplayName: String, prevDropTime: Date?, prevCatchesSinceLast: Int): String {
        val elapsedTimeText = if (prevDropTime != null) " ${GRAY}(${WHITE}${CommonUtils.formatTimeElapsed(prevDropTime)}${GRAY})" else ""
        val catchesText = "${WHITE}${CommonUtils.formatNumberWithSpaces(prevCatchesSinceLast)} ${RESET}${seaCreatureDisplayName} ${GRAY}${if (prevCatchesSinceLast == 1) "catch" else "catches"}"
        return "${GRAY}It took ${catchesText}${elapsedTimeText} to get the ${itemDisplayName} ${WHITE}#${CommonUtils.formatNumberWithSpaces(count)}${GRAY}. Congratulations!"
    }

    fun reset() {
        count = 0
        catchesSinceLast = 0
        lastDropTime = null
        dropsHistory = emptyList()
    }

    fun getOverlayLines(dropDisplayName: String, seaCreatureName: String): List<LineInfo> {
        val lastDropTimeLine = if (dropsHistory.isNotEmpty()) {
            val lastDrop = dropsHistory[0]
            val timeElapsed = CommonUtils.formatTimeElapsed(lastDrop.time)
            val dateFormatted = CommonUtils.formatDate(lastDrop.time)
            LineInfo("${GRAY}Last on: ${WHITE}${timeElapsed} ago", tooltip = listOf(Component.literal("${GRAY}Last on: ${WHITE}${dateFormatted}")))
        } else LineInfo("${GRAY}Last on: ${WHITE}N/A")
        
        val catchesSinceLastDrop = catchesSinceLast
        
        val pluralizedDropName = if (count != 1) pluralize(dropDisplayName) else dropDisplayName
        val pluralizedSeaCreatureName = if (catchesSinceLastDrop != 1) pluralize(seaCreatureName) else seaCreatureName
        
        return listOf(
            LineInfo("${pluralizedDropName}${GRAY}: ${WHITE}${CommonUtils.formatNumberWithSpaces(count)}"),
            lastDropTimeLine,
            LineInfo("${GRAY}Last on: ${WHITE}${CommonUtils.formatNumberWithSpaces(catchesSinceLastDrop)} ${GRAY}${pluralizedSeaCreatureName.removeFormatting()} ago")
        )
    }

    fun initDropCount(dropCount: Int, dropLastOn: Date?) {
        count = dropCount
        lastDropTime = dropLastOn

        if (dropLastOn != null) {
            val history = dropsHistory.toMutableList()
            if (history.isNotEmpty()) {
                history[0] = history[0].copy(time = dropLastOn)
            } else {
                history.add(0, DropsHistoryEntry(catches = 0, time = dropLastOn, magicFind = null))
            }
            dropsHistory = history
        }
    }

    private fun pluralize(word: String): String {
        // This is a simplified version sufficient for my use case
        return when {
            word.endsWith("s", ignoreCase = true) || word.endsWith("x", ignoreCase = true) || 
            word.endsWith("z", ignoreCase = true) || word.endsWith("ch", ignoreCase = true) || 
            word.endsWith("sh", ignoreCase = true)
                -> "${word}es"
            word.endsWith("y", ignoreCase = true)
                -> word.dropLast(1) + "ies"
            word.endsWith("f", ignoreCase = true)
                -> word.dropLast(1) + "ves"
            else -> "${word}s"
        }
    }
}
