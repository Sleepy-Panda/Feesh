package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.PriceUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.enums.PricingMode
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component

/**
 * Shared data structures and logic for shop price commands (Fear Mongerer, Junker Joel, etc.).
 * Profit = sell price of shop item minus sum of all costs spent on buying it (base items value + coins + extra items value).
 */
object BaseShopPrices {

    data class BaseItemCost(val itemId: String, val amount: Int)

    data class ShopItemCost(
        val baseItemCosts: List<BaseItemCost> = emptyList(),
        val coinCost: Long = 0L,
        val extraItemId: String? = null,
        val extraItemAmount: Int = 1
    ) {
        fun hasBaseItem(baseItemId: String): Boolean =
            baseItemCosts.any { it.itemId == baseItemId }
    }

    data class ShopItem(
        val itemId: String,
        val itemName: String,
        val costs: ShopItemCost
    )

    /** Costs breakdown (materials, coins). itemId null = coins. */
    data class CostBreakdownEntry(val itemId: String?, val amount: Int, val value: Double)

    fun getPrice(itemId: String, priceMode: PricingMode): Double {
        val bazaarPrices = PriceUtils.getBazaarItemPrices(itemId)
        var itemPrice = if (priceMode == PricingMode.SELL_OFFER) bazaarPrices?.sellOffer else bazaarPrices?.instaSell
        if (bazaarPrices == null) {
            val auctionPrices = PriceUtils.getAuctionItemPrice(itemId)
            itemPrice = auctionPrices?.lbin
        }
        return itemPrice ?: 0.0
    }

    data class ShopItemProfit(
        val itemName: String,
        val itemPrice: Double,
        val costComponents: List<Double>,
        val costBreakdown: List<CostBreakdownEntry>,
        val profit: Double
    )

    /**
     * Gets item sell price, cost breakdown (base items, coins, extra items), and profit.
     */
    fun calculateShopItemProfit(
        item: ShopItem,
        priceMode: PricingMode
    ): ShopItemProfit {
        val itemPrice = getPrice(item.itemId, priceMode)
        val c = item.costs
        val baseEntries = c.baseItemCosts.map { BaseItemCost(it.itemId, it.amount) to getPrice(it.itemId, priceMode) * it.amount }
        val baseCosts = baseEntries.map { it.second }
        val extraCost = if (c.extraItemId != null) getPrice(c.extraItemId, priceMode) * c.extraItemAmount else 0.0
        val costBreakdown = baseEntries.map { (base, value) -> CostBreakdownEntry(base.itemId, base.amount, value) } +
            (if (c.coinCost > 0L) listOf(CostBreakdownEntry(null, 1, c.coinCost.toDouble())) else emptyList()) +
            (if (c.extraItemId != null && extraCost > 0) listOf(CostBreakdownEntry(c.extraItemId, c.extraItemAmount, extraCost)) else emptyList())
        val costComponents = baseCosts +
            (if (c.coinCost > 0L) listOf(c.coinCost.toDouble()) else emptyList()) +
            (if (extraCost > 0) listOf(extraCost) else emptyList())
        val totalCost = costComponents.sum()
        val profit = itemPrice - totalCost
        return ShopItemProfit(
            itemName = item.itemName,
            itemPrice = itemPrice,
            costComponents = costComponents,
            costBreakdown = costBreakdown,
            profit = profit
        )
    }

    private fun getProfitChatLine(shopProfit: ShopItemProfit): String {
        val itemPriceStr = CommonUtils.toShortNumber(shopProfit.itemPrice) ?: "N/A"
        val profitColor = if (shopProfit.profit >= 0) GOLD else RED
        val profitStr = CommonUtils.toShortNumber(shopProfit.profit) ?: "N/A"
        return " - ${shopProfit.itemName}${RESET}: ${GOLD}$itemPriceStr ${WHITE}(${profitColor}$profitStr ${WHITE}profit)"
    }

    private fun getLineHover(
        shopProfit: ShopItemProfit,
        priceModeStr: String,
        getMaterialDisplayName: (String?) -> String
    ): String {
        val itemPriceStr = CommonUtils.toShortNumber(shopProfit.itemPrice) ?: "N/A"
        val profitStr = CommonUtils.toShortNumber(shopProfit.profit) ?: "N/A"
        val profitColor = if (shopProfit.profit >= 0) GOLD else RED
        val sb = StringBuilder()
        sb.append("${WHITE}Price mode: $priceModeStr").append("\n")
        sb.append("${shopProfit.itemName}${WHITE}: ${GOLD}$itemPriceStr").append("\n\n")
        sb.append("${WHITE}Materials:").append("\n")
        for (e in shopProfit.costBreakdown) {
            val displayName = getMaterialDisplayName(e.itemId)
            val valueStr = CommonUtils.toShortNumber(e.value) ?: "0"
            sb.append("- ${displayName} ${WHITE}${e.amount}x: ${GOLD}${valueStr}${RESET}").append("\n")
        }
        sb.append("\n${WHITE}Profit: ${profitColor}$profitStr")
        return sb.toString()
    }

    /**
     * Chat line with short text and hover showing full breakdown.
     */
    fun getProfitChatLineWithHover(
        shopProfit: ShopItemProfit,
        priceModeStr: String,
        getMaterialDisplayName: (String?) -> String
    ): Component {
        val line = getProfitChatLine(shopProfit)
        val hoverText = getLineHover(shopProfit, priceModeStr, getMaterialDisplayName)
        return Component.literal(line).setStyle(Style.EMPTY.withHoverEvent(HoverEvent.ShowText(Component.literal(hoverText))))
    }
}
