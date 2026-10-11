package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.features.overlays.FishingProfitTracker
import com.github.sleepypanda.feesh.features.overlays.base.TrackerViewMode
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.enums.PricingModeWithNpc

object FishingProfitTrackerPartyCommands {
    fun profitMessage(viewMode: TrackerViewMode): String? {
        val source = FishingProfitTracker.getSourceObject(viewMode)
        val useNetProfit = Overlays.shouldShowNetProfitInFishingProfitTracker
        val profit = if (useNetProfit) source.totalProfit - source.totalCost else source.totalProfit
        val elapsedSeconds = source.elapsedSeconds
        if (profit == 0.0 || elapsedSeconds == 0) return null

        val perHour = profit / (elapsedSeconds / 3600.0)
        val amountStr = CommonUtils.toShortNumber(profit) ?: return null
        val perHourStr = CommonUtils.toShortNumber(perHour) ?: "0"
        val timeStr = CommonUtils.formatTimeElapsed(elapsedSeconds)
        val scope = scopeLabel(viewMode)
        val label = if (useNetProfit) "Net profit" else "Profit"

        return "$scope $label is $amountStr ($perHourStr/h) for $timeStr ${priceModeTag()}"
    }

    fun profitItemMessage(viewMode: TrackerViewMode, query: String): String? {
        if (query.isEmpty()) return null

        val match = FishingProfitTracker.getSourceObject(viewMode).profitTrackerItems.values
            .filter { it.itemName.contains(query, ignoreCase = true) }
            .maxByOrNull { it.totalItemProfit }
            ?: return null

        val displayName = FishingProfitTracker.getDisplayNameForGui(match.itemId, match.itemName).removeFormatting()
        val countStr = CommonUtils.formatNumberWithSpaces(match.amount)
        val profitStr = CommonUtils.toShortNumber(match.totalItemProfit) ?: "0"

        return "${scopeLabel(viewMode)}: ${countStr}x $displayName: $profitStr ${priceModeTag()}"
    }

    private fun scopeLabel(viewMode: TrackerViewMode): String {
        return if (viewMode == TrackerViewMode.SESSION) "Session" else "Total"
    }

    private fun priceModeTag(): String {
        val mode = when (Overlays.fishingProfitTrackerPriceMode) {
            PricingModeWithNpc.SELL_OFFER -> "sell offer"
            PricingModeWithNpc.INSTA_SELL -> "insta sell"
            PricingModeWithNpc.NPC_SELL -> "npc sell"
        }
        return "[$mode]"
    }
}
