package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.features.overlays.base.TrackerViewMode
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.PricingModeWithNpc

object FishingProfitTrackerPartyCommands {
    fun getProfitChatMessage(viewMode: TrackerViewMode): String? {
        if (!Overlays.fishingProfitTrackerOverlay || !WorldUtils.isInSkyblock()) return null

        val data = FishingProfitTracker.getDisplayTrackerData(viewMode)
        val useNetProfit = Overlays.shouldShowNetProfitInFishingProfitTracker
        val profit = if (useNetProfit) data.netProfit else data.totalProfit
        val perHour = if (useNetProfit) data.netProfitPerHour else data.totalProfitPerHour
        if (profit == 0.0 || data.elapsedSeconds == 0) return null

        val amountStr = CommonUtils.toShortNumber(profit) ?: return null
        val perHourStr = CommonUtils.toShortNumber(perHour) ?: "0"
        val timeStr = CommonUtils.formatTimeElapsed(data.elapsedSeconds)
        val viewModeLabel = getViewModeLabel(viewMode)
        val label = if (useNetProfit) "Net profit" else "Profit"

        return "$label is $amountStr ($perHourStr/h) in $timeStr [$viewModeLabel, ${getPriceModeLabel()}]"
    }

    fun getProfitItemChatMessage(viewMode: TrackerViewMode, query: String): String? {
        if (!Overlays.fishingProfitTrackerOverlay || !WorldUtils.isInSkyblock()) return null
        if (query.isEmpty()) return null

        val match = FishingProfitTracker.getSourceObject(viewMode).profitTrackerItems.values
            .filter { it.itemName.contains(query, ignoreCase = true) }
            .maxByOrNull { it.totalItemProfit }
            ?: return null

        val displayName = FishingProfitTracker.getDisplayNameForGui(match.itemId, match.itemName).removeFormatting()
        val countStr = CommonUtils.formatNumberWithSpaces(match.amount)
        val profitStr = CommonUtils.toShortNumber(match.totalItemProfit) ?: "0"
        val viewModeLabel = getViewModeLabel(viewMode)

        return "${countStr}x $displayName: $profitStr [$viewModeLabel, ${getPriceModeLabel()}]"
    }

    fun getTopProfitItemChatMessage(viewMode: TrackerViewMode): String? {
        if (!Overlays.fishingProfitTrackerOverlay || !WorldUtils.isInSkyblock()) return null

        val data = FishingProfitTracker.getDisplayTrackerData(viewMode)
        val top = data.entriesToShow.firstOrNull() ?: data.entriesToHide.firstOrNull() ?: return null
        val countStr = CommonUtils.formatNumberWithSpaces(top.amount)
        val profitStr = CommonUtils.toShortNumber(top.profit) ?: "0"
        val viewModeLabel = getViewModeLabel(viewMode)

        return "${countStr}x ${top.item.removeFormatting()}: $profitStr [$viewModeLabel, ${getPriceModeLabel()}]"
    }

    private fun getViewModeLabel(viewMode: TrackerViewMode): String {
        return if (viewMode == TrackerViewMode.SESSION) "Session" else "Total"
    }

    private fun getPriceModeLabel(): String {
        val mode = when (Overlays.fishingProfitTrackerPriceMode) {
            PricingModeWithNpc.SELL_OFFER -> "sell offer"
            PricingModeWithNpc.INSTA_SELL -> "insta sell"
            PricingModeWithNpc.NPC_SELL -> "npc sell"
        }
        return mode
    }
}
