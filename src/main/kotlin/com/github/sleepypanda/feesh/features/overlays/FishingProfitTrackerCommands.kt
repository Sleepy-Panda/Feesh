package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.constants.FishingProfitDrops
import com.github.sleepypanda.feesh.features.overlays.base.TrackerViewMode
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object FishingProfitTrackerCommands {

    const val SET_ITEM_COUNT_COMMAND = "feeshSetItemCountFishingProfitTracker"
    const val SET_ITEM_COUNT_TOTAL_COMMAND = "feeshSetItemCountFishingProfitTrackerTotal"
    const val GET_ITEM_COMMAND = "feeshGetItemFishingProfitTracker"
    const val GET_ITEM_TOTAL_COMMAND = "feeshGetItemFishingProfitTrackerTotal"
    const val DELETE_ITEM_COMMAND = "feeshDeleteItemFishingProfitTracker"
    const val DELETE_ITEM_TOTAL_COMMAND = "feeshDeleteItemFishingProfitTrackerTotal"
    const val SET_TIME_COMMAND = "feeshSetTimeFishingProfitTracker"
    const val SET_TIME_TOTAL_COMMAND = "feeshSetTimeFishingProfitTrackerTotal"
    const val RESET_COSTS_COMMAND = "feeshResetCostsFishingProfitTracker"
    const val RESET_COSTS_TOTAL_COMMAND = "feeshResetCostsFishingProfitTrackerTotal"
    const val RESET_CATCHES_COMMAND = "feeshResetCatchesFishingProfitTracker"
    const val RESET_CATCHES_TOTAL_COMMAND = "feeshResetCatchesFishingProfitTrackerTotal"
    const val SET_CATCHES_COMMAND = "feeshSetCatchesFishingProfitTracker"
    const val SET_CATCHES_TOTAL_COMMAND = "feeshSetCatchesFishingProfitTrackerTotal"

    fun init() {
        RegisterUtils.command(SET_ITEM_COUNT_COMMAND) { args ->
            onSetItemCountCommand(args, TrackerViewMode.SESSION)
        }
        RegisterUtils.command(SET_ITEM_COUNT_TOTAL_COMMAND) { args ->
            onSetItemCountCommand(args, TrackerViewMode.TOTAL)
        }
        RegisterUtils.command(GET_ITEM_COMMAND) { args ->
            onGetItemCommand(args, TrackerViewMode.SESSION)
        }
        RegisterUtils.command(GET_ITEM_TOTAL_COMMAND) { args ->
            onGetItemCommand(args, TrackerViewMode.TOTAL)
        }
        RegisterUtils.command(DELETE_ITEM_COMMAND) { args ->
            onDeleteItemCommand(args, TrackerViewMode.SESSION)
        }
        RegisterUtils.command(DELETE_ITEM_TOTAL_COMMAND) { args ->
            onDeleteItemCommand(args, TrackerViewMode.TOTAL)
        }
        RegisterUtils.command(SET_TIME_COMMAND) { args ->
            onSetElapsedTimeCommand(args, TrackerViewMode.SESSION)
        }
        RegisterUtils.command(SET_TIME_TOTAL_COMMAND) { args ->
            onSetElapsedTimeCommand(args, TrackerViewMode.TOTAL)
        }
        RegisterUtils.command(RESET_COSTS_COMMAND) { args ->
            onResetCostsCommand(args, TrackerViewMode.SESSION)
        }
        RegisterUtils.command(RESET_COSTS_TOTAL_COMMAND) { args ->
            onResetCostsCommand(args, TrackerViewMode.TOTAL)
        }
        RegisterUtils.command(RESET_CATCHES_COMMAND) { args ->
            onResetCatchesCommand(args, TrackerViewMode.SESSION)
        }
        RegisterUtils.command(RESET_CATCHES_TOTAL_COMMAND) { args ->
            onResetCatchesCommand(args, TrackerViewMode.TOTAL)
        }
        RegisterUtils.command(SET_CATCHES_COMMAND) { args ->
            onSetCatchesCommand(args, TrackerViewMode.SESSION)
        }
        RegisterUtils.command(SET_CATCHES_TOTAL_COMMAND) { args ->
            onSetCatchesCommand(args, TrackerViewMode.TOTAL)
        }
    }

    internal fun onDeleteItemCommand(args: Array<String>, viewMode: TrackerViewMode) {
        CommonUtils.runWithCatching("Failed to delete item from Fishing profit tracker") {
            if (args.isEmpty()) {
                ChatUtils.sendLocalChat("${RED}Usage: /$DELETE_ITEM_COMMAND <itemID>", true)
                return
            }

            val itemId = args[0].trim()
            if (itemId.isBlank()) {
                ChatUtils.sendLocalChat("${RED}Item ID is required.", true)
                return
            }

            val sourceObj = FishingProfitTracker.getSourceObject(viewMode)
            if (!sourceObj.profitTrackerItems.containsKey(itemId)) {
                ChatUtils.sendLocalChat("${RED}Item ID is not found in the tracker, nothing to delete: $itemId", true)
                return
            }

            val entry = sourceObj.profitTrackerItems[itemId] ?: return
            val viewModeText = FishingProfitTracker.getViewModeDisplayText(viewMode)
            val dropInfo = FishingProfitDrops.items.find { it.itemId == itemId }
            val itemName = when {
                ItemUtils.isMaxedPet(itemId) -> ItemUtils.getPetNameByPetId(itemId)
                itemId == FishingProfitTracker.FISHED_COINS_ITEM_ID -> "Fished Coins"
                dropInfo == null -> entry.itemName
                else -> dropInfo.itemName
            }
            val displayName = FishingProfitTracker.getDisplayNameForGui(itemId, itemName)
            val isConfirmed = args.size == 2 && args.last() == "noconfirm"

            if (!isConfirmed) {
                val deleteCommand = when (viewMode) {
                    TrackerViewMode.SESSION -> "$DELETE_ITEM_COMMAND $itemId noconfirm"
                    TrackerViewMode.TOTAL -> "$DELETE_ITEM_TOTAL_COMMAND $itemId noconfirm"
                }
                ChatUtils.sendLocalChat("${WHITE}Do you want to delete ${WHITE}${entry.amount}x ${displayName}${WHITE} from the Fishing profit tracker ${viewModeText}${WHITE}?", true)
                ChatUtils.sendLocalChatWithCommand(
                    "${RED}${BOLD}[Click to confirm]",
                    deleteCommand,
                    false
                )
                return
            }

            sourceObj.profitTrackerItems.remove(itemId)
            FishingProfitTracker.saveData()
            FishingProfitTracker.refreshTotalItemsProfitsInMode(viewMode)
            FishingProfitTracker.updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Deleted ${WHITE}${entry.amount}x ${displayName}${WHITE} from the Fishing profit tracker ${viewModeText}${WHITE}.", true)
        }
    }

    internal fun onResetCostsCommand(args: Array<String>, viewMode: TrackerViewMode) {
        CommonUtils.runWithCatching("Failed to reset costs in Fishing profit tracker") {
            val viewModeText = FishingProfitTracker.getViewModeDisplayText(viewMode)
            val isConfirmed = args.isNotEmpty() && args.last() == "noconfirm"
            val sourceObj = FishingProfitTracker.getSourceObject(viewMode)

            if (sourceObj.costItems.isEmpty() && sourceObj.totalCost == 0.0) {
                ChatUtils.sendLocalChat("${RED}No costs to reset in Fishing profit tracker $viewModeText${RED}.", true)
                return
            }

            if (!isConfirmed) {
                val resetCommand = when (viewMode) {
                    TrackerViewMode.SESSION -> "$RESET_COSTS_COMMAND noconfirm"
                    TrackerViewMode.TOTAL -> "$RESET_COSTS_TOTAL_COMMAND noconfirm"
                }
                ChatUtils.sendLocalChat("${WHITE}Do you want to reset costs in Fishing profit tracker $viewModeText${WHITE}?", true)
                ChatUtils.sendLocalChatWithCommand(
                    "${RED}${BOLD}[Click to confirm]",
                    resetCommand,
                    false
                )
                return
            }

            sourceObj.costItems.clear()
            sourceObj.totalCost = 0.0
            FishingProfitTracker.saveData()
            FishingProfitTracker.updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Costs in Fishing profit tracker $viewModeText ${WHITE}were reset.", true)
        }
    }

    internal fun onResetCatchesCommand(args: Array<String>, viewMode: TrackerViewMode) {
        CommonUtils.runWithCatching("Failed to reset catches in Fishing profit tracker") {
            val viewModeText = FishingProfitTracker.getViewModeDisplayText(viewMode)
            val isConfirmed = args.isNotEmpty() && args.last() == "noconfirm"
            val sourceObj = FishingProfitTracker.getSourceObject(viewMode)

            if (sourceObj.catchesCount == 0) {
                ChatUtils.sendLocalChat("${RED}No catches to reset in Fishing profit tracker $viewModeText${RED}.", true)
                return
            }

            if (!isConfirmed) {
                val resetCommand = when (viewMode) {
                    TrackerViewMode.SESSION -> "$RESET_CATCHES_COMMAND noconfirm"
                    TrackerViewMode.TOTAL -> "$RESET_CATCHES_TOTAL_COMMAND noconfirm"
                }
                ChatUtils.sendLocalChat("${WHITE}Do you want to reset catches in Fishing profit tracker $viewModeText${WHITE}?", true)
                ChatUtils.sendLocalChatWithCommand(
                    "${RED}${BOLD}[Click to confirm]",
                    resetCommand,
                    false
                )
                return
            }

            sourceObj.catchesCount = 0
            FishingProfitTracker.saveData()
            FishingProfitTracker.updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Catches in Fishing profit tracker $viewModeText ${WHITE}were reset.", true)
        }
    }

    private fun onSetItemCountCommand(args: Array<String>, viewMode: TrackerViewMode) {

        fun getNewCount(value: String, currentCount: Int): Int? {
            val trimmed = value.trim()
            if (trimmed.isEmpty()) return null

            val newCount = when {
                trimmed.startsWith("+") -> {
                    val delta = trimmed.drop(1).toIntOrNull() ?: return null
                    if (delta <= 0) return null
                    currentCount + delta
                }
                trimmed.startsWith("-") -> {
                    val delta = trimmed.drop(1).toIntOrNull() ?: return null
                    if (delta <= 0) return null
                    currentCount - delta
                }
                else -> trimmed.toIntOrNull()
            } ?: return null

            if (newCount <= 0) return null
            return newCount
        }

        CommonUtils.runWithCatching("Failed to change item count in Fishing profit tracker") {
            if (args.size < 2) {
                val commandName = when (viewMode) {
                    TrackerViewMode.SESSION -> SET_ITEM_COUNT_COMMAND
                    TrackerViewMode.TOTAL -> SET_ITEM_COUNT_TOTAL_COMMAND
                }
                ChatUtils.sendLocalChat(
                    "${RED}Usage: /$commandName <itemID> <count> ${GRAY}(e.g. 64, +1, -1)",
                    true
                )
                return
            }

            val itemId = args[0].trim()
            if (itemId.isBlank()) {
                ChatUtils.sendLocalChat("${RED}Item ID is required.", true)
                return
            }

            val sourceObj = FishingProfitTracker.getSourceObject(viewMode)
            val currentCount = sourceObj.profitTrackerItems[itemId]?.amount ?: 0
            val count = getNewCount(args[1], currentCount)
            if (count == null) {
                ChatUtils.sendLocalChat(
                    "${RED}Invalid count. Use a positive integer, or +N / -N to adjust (result must stay positive).",
                    true
                )
                return
            }

            val dropInfo = FishingProfitDrops.items.find { it.itemId == itemId }
            if (dropInfo == null && !ItemUtils.isMaxedPet(itemId)) {
                ChatUtils.sendLocalChat("${RED}Item not found by ID: $itemId", true)
                return
            }

            val itemName = when {
                ItemUtils.isMaxedPet(itemId) -> ItemUtils.getPetNameByPetId(itemId)
                else -> dropInfo!!.itemName
            }
            val displayName = FishingProfitTracker.getDisplayNameForGui(itemId, itemName)

            val existing = sourceObj.profitTrackerItems[itemId]
            val previousCount = existing?.amount ?: 0

            if (existing == null) {
                sourceObj.profitTrackerItems[itemId] = FishingProfitTracker.ProfitTrackerItemEntry(
                    itemId = itemId,
                    itemName = itemName,
                    amount = count,
                    totalItemProfit = 0.0
                )
            } else {
                existing.amount = count
            }

            FishingProfitTracker.saveData()
            FishingProfitTracker.refreshTotalItemsProfitsInMode(viewMode)
            FishingProfitTracker.updateGuiLines()

            val viewModeText = FishingProfitTracker.getViewModeDisplayText(viewMode)
            ChatUtils.sendLocalChat("${WHITE}Count of ${displayName} ${WHITE}in Fishing profit tracker $viewModeText ${WHITE}is changed from ${AQUA}${previousCount} ${WHITE}to ${AQUA}${count}${WHITE}.", true)
        }
    }

    private fun onGetItemCommand(args: Array<String>, viewMode: TrackerViewMode) {

        fun findProfitTrackerItemsByIdOrName(
            sourceObj: FishingProfitTracker.FishingProfitSourceData,
            query: String
        ): List<FishingProfitTracker.ProfitTrackerItemEntry> {
            val matches = mutableMapOf<String, FishingProfitTracker.ProfitTrackerItemEntry>()
            sourceObj.profitTrackerItems[query]?.let { matches[it.itemId] = it }
            sourceObj.profitTrackerItems.values
                .filter { it.itemName.contains(query, ignoreCase = true) }
                .forEach { matches[it.itemId] = it }
            return matches.values.sortedByDescending { it.totalItemProfit }
        }

        fun formatMatchLine(entry: FishingProfitTracker.ProfitTrackerItemEntry): String {
            val displayName = FishingProfitTracker.getDisplayNameForGui(entry.itemId, entry.itemName)
            val countStr = CommonUtils.formatNumberWithSpaces(entry.amount)
            val profitStr = CommonUtils.toShortNumber(entry.totalItemProfit) ?: "0"
            return "${GRAY}- ${WHITE}${countStr}${GRAY}x ${displayName}${WHITE}: ${GOLD}${profitStr}${WHITE} coins. Item ID: ${AQUA}${entry.itemId}${WHITE}"
        }

        CommonUtils.runWithCatching("Failed to get item from Fishing profit tracker") {
            val commandName = when (viewMode) {
                TrackerViewMode.SESSION -> GET_ITEM_COMMAND
                TrackerViewMode.TOTAL -> GET_ITEM_TOTAL_COMMAND
            }
            val query = args.joinToString(" ").trim()
            if (query.isBlank()) {
                ChatUtils.sendLocalChat(
                    "${RED}Usage: /$commandName <itemID or itemName>",
                    true
                )
                return
            }
            if (query.length < 3) {
                ChatUtils.sendLocalChat("${RED}Search query must be at least 3 characters.", true)
                return
            }

            val viewModeText = FishingProfitTracker.getViewModeDisplayText(viewMode)
            val sourceObj = FishingProfitTracker.getSourceObject(viewMode)
            val matches = findProfitTrackerItemsByIdOrName(sourceObj, query)
            if (matches.isEmpty()) {
                ChatUtils.sendLocalChat(
                    "${RED}Item '${query}' is not found in the Fishing profit tracker $viewModeText${RED}!",
                    true
                )
                return
            }

            ChatUtils.sendLocalChat(
                "${WHITE}Matching items in the Fishing profit tracker $viewModeText${WHITE}:",
                true
            )

            val maxMatchesToShow = 5
            matches.take(maxMatchesToShow).forEach { ChatUtils.sendLocalChat(formatMatchLine(it)) }
            if (matches.size > maxMatchesToShow) {
                ChatUtils.sendLocalChat(
                    "${GRAY}Showing top ${maxMatchesToShow} of ${matches.size} matches - refine search parameter to see the relevant results."
                )
            }
        }
    }

    private fun onSetElapsedTimeCommand(args: Array<String>, viewMode: TrackerViewMode) {

        fun getNewElapsedSeconds(value: String, currentElapsedSeconds: Int): Int? {
            val trimmed = value.trim()
            if (trimmed.isEmpty()) return null

            val newSeconds = when {
                trimmed.startsWith("+") -> {
                    val delta = trimmed.drop(1).toIntOrNull() ?: return null
                    if (delta <= 0) return null
                    currentElapsedSeconds + delta
                }
                trimmed.startsWith("-") -> {
                    val delta = trimmed.drop(1).toIntOrNull() ?: return null
                    if (delta <= 0) return null
                    currentElapsedSeconds - delta
                }
                else -> trimmed.toIntOrNull()
            } ?: return null

            if (newSeconds < 0) return null
            return newSeconds
        }

        CommonUtils.runWithCatching("Failed to change elapsed time in Fishing profit tracker") {
            if (args.isEmpty()) {
                val commandName = when (viewMode) {
                    TrackerViewMode.SESSION -> SET_TIME_COMMAND
                    TrackerViewMode.TOTAL -> SET_TIME_TOTAL_COMMAND
                }
                ChatUtils.sendLocalChat(
                    "${RED}Usage: /$commandName <seconds> ${GRAY}(e.g. 10000, +500, -500)",
                    true
                )
                return
            }

            val sourceObj = FishingProfitTracker.getSourceObject(viewMode)
            val newElapsedSeconds = getNewElapsedSeconds(args[0], sourceObj.elapsedSeconds)
            if (newElapsedSeconds == null) {
                ChatUtils.sendLocalChat(
                    "${RED}Invalid value. Use seconds as a positive integer, or +N / -N to adjust (result must stay positive).",
                    true
                )
                return
            }

            val previousElapsedSeconds = sourceObj.elapsedSeconds
            if (previousElapsedSeconds != newElapsedSeconds) {
                sourceObj.elapsedSeconds = newElapsedSeconds
                FishingProfitTracker.saveData()
                FishingProfitTracker.updateGuiLines()
            }

            val viewModeText = FishingProfitTracker.getViewModeDisplayText(viewMode)
            val previousElapsedStr = CommonUtils.formatTimeElapsed(previousElapsedSeconds)
            val elapsedStr = CommonUtils.formatTimeElapsed(newElapsedSeconds)
            ChatUtils.sendLocalChat(
                "${WHITE}Elapsed time in Fishing profit tracker $viewModeText ${WHITE}is changed from ${AQUA}${previousElapsedStr} ${WHITE}to ${AQUA}${elapsedStr}${WHITE}.",
                true
            )
        }
    }

    private fun onSetCatchesCommand(args: Array<String>, viewMode: TrackerViewMode) {

        fun getNewCatchesCount(value: String, currentCatchesCount: Int): Int? {
            val trimmed = value.trim()
            if (trimmed.isEmpty()) return null

            val newCount = when {
                trimmed.startsWith("+") -> {
                    val delta = trimmed.drop(1).toIntOrNull() ?: return null
                    if (delta <= 0) return null
                    currentCatchesCount + delta
                }
                trimmed.startsWith("-") -> {
                    val delta = trimmed.drop(1).toIntOrNull() ?: return null
                    if (delta <= 0) return null
                    currentCatchesCount - delta
                }
                else -> trimmed.toIntOrNull()
            } ?: return null

            if (newCount < 0) return null
            return newCount
        }

        CommonUtils.runWithCatching("Failed to change catches count in Fishing profit tracker") {
            if (args.isEmpty()) {
                val commandName = when (viewMode) {
                    TrackerViewMode.SESSION -> SET_CATCHES_COMMAND
                    TrackerViewMode.TOTAL -> SET_CATCHES_TOTAL_COMMAND
                }
                ChatUtils.sendLocalChat(
                    "${RED}Usage: /$commandName <count> ${GRAY}(e.g. 1234, +1, -1)",
                    true
                )
                return
            }

            val sourceObj = FishingProfitTracker.getSourceObject(viewMode)
            val newCatchesCount = getNewCatchesCount(args[0], sourceObj.catchesCount)
            if (newCatchesCount == null) {
                ChatUtils.sendLocalChat(
                    "${RED}Invalid value. Use a non-negative integer, or +N / -N to adjust (result must stay non-negative).",
                    true
                )
                return
            }

            val previousCatchesCount = sourceObj.catchesCount
            if (previousCatchesCount != newCatchesCount) {
                sourceObj.catchesCount = newCatchesCount
                FishingProfitTracker.saveData()
                FishingProfitTracker.updateGuiLines()
            }

            val viewModeText = FishingProfitTracker.getViewModeDisplayText(viewMode)
            ChatUtils.sendLocalChat(
                "${WHITE}Catches count in Fishing profit tracker $viewModeText ${WHITE}is changed from ${AQUA}${CommonUtils.formatNumberWithSpaces(previousCatchesCount)} ${WHITE}to ${AQUA}${CommonUtils.formatNumberWithSpaces(newCatchesCount)}${WHITE}.",
                true
            )
        }
    }
}
