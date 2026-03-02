package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PriceUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.Commands
import com.github.sleepypanda.feesh.utils.enums.PricingMode

object FearMongererShopPrices {
    const val COMMAND_NAME = "feeshFearMongererShopPrices"

    data class ShopItemCost(
        val greenCandyAmount: Int = 0,
        val purpleCandyAmount: Int = 0,
        val coinCost: Long = 0L,
        val extraItemId: String? = null,
        val extraItemAmount: Int = 1
    )

    data class ShopItem(
        val itemId: String,
        val itemName: String,
        val costs: ShopItemCost
    )

    data class ShopCategory(
        val baseItemId: String,
        val baseItemName: String,
        val items: List<ShopItem>
    )

    data class ShopProfit(
        val itemName: String,
        val baseItemName: String,
        val itemPrice: Double,
        val profitPerBaseItem: Double
    )

    private val SHOP_CATEGORIES = listOf(
        ShopCategory(
            baseItemId = "GREEN_CANDY",
            baseItemName = "${UNCOMMON}Green Candy",
            items = listOf(
                ShopItem("CANDY_TALISMAN", "${UNCOMMON}Candy Talisman", ShopItemCost(greenCandyAmount = 16)),
                ShopItem("CANDY_RING", "${RARE}Candy Ring", ShopItemCost(greenCandyAmount = 16 + 64)),
                ShopItem("BAT_FIREWORK", "${UNCOMMON}Bat Firework", ShopItemCost(greenCandyAmount = 100)),
                ShopItem("ENCHANTMENT_SUGAR_RUSH_1", "${RARE}Sugar Rush I", ShopItemCost(greenCandyAmount = 64, coinCost = 250_000L)),
                ShopItem("BAT_RING", "${EPIC}Bat Ring", ShopItemCost(greenCandyAmount = 64, extraItemId = "BAT_TALISMAN")),
            )
        ),
        ShopCategory(
            baseItemId = "PURPLE_CANDY",
            baseItemName = "${EPIC}Purple Candy",
            items = listOf(
                ShopItem("BAT_ARTIFACT", "${LEGENDARY}Bat Artifact", ShopItemCost(purpleCandyAmount = 64, extraItemId = "BAT_RING")),
                ShopItem("CANDY_ARTIFACT", "${EPIC}Candy Artifact", ShopItemCost(purpleCandyAmount = 32, extraItemId = "CANDY_RING")),
                ShopItem("CANDY_RELIC", "${LEGENDARY}Candy Relic", ShopItemCost(purpleCandyAmount = 1024 + 32)),
                ShopItem("HORSEMAN_CANDLE", "${EPIC}Horseman's Candle", ShopItemCost(purpleCandyAmount = 32)),
                ShopItem("SPOOKY_HELMET", "${EPIC}Spooky Helmet", ShopItemCost(purpleCandyAmount = 64)),
                ShopItem("SPOOKY_CHESTPLATE", "${EPIC}Spooky Chestplate", ShopItemCost(purpleCandyAmount = 64)),
                ShopItem("SPOOKY_LEGGINGS", "${EPIC}Spooky Leggings", ShopItemCost(purpleCandyAmount = 64)),
                ShopItem("SPOOKY_BOOTS", "${EPIC}Spooky Boots", ShopItemCost(purpleCandyAmount = 64)),
                //ShopItem("ENCHANTMENT_LIFE_STEAL_4", "${RARE}Life Steal IV", ShopItemCost(purpleCandyAmount = 32, coinCost = 1_500_000L)),
                ShopItem("BLACK_CAT;4", "${GRAY}[Lvl 1] ${LEGENDARY}Black Cat", ShopItemCost(purpleCandyAmount = 2000)),
                // Intimidation
                // 
            )
        )
    )

    fun init() {
        RegisterUtils.command(COMMAND_NAME) {
            calculateFearMongererShopPrices()
        }
    }

    private fun calculateFearMongererShopPrices() {
        try {
            if (!WorldUtils.isInSkyblock()) {
                ChatUtils.sendLocalChat("${RED}You must be on Hypixel Skyblock to use this command!", true)
                return
            }

            val chatBreak = "${GRAY}${ChatUtils.getChatBreak("-")}"
            ChatUtils.sendLocalChat(chatBreak)
            ChatUtils.sendLocalChat("${GREEN}${BOLD}Fear Mongerer shop prices", true)

            SHOP_CATEGORIES.forEach { category ->
                val baseItemPrice = getPrice(category.baseItemId)
                val candyAmountForCategory: (it: ShopItemCost) -> Int = when (category.baseItemId) {
                    "GREEN_CANDY" -> { it: ShopItemCost -> it.greenCandyAmount }
                    "PURPLE_CANDY" -> { it: ShopItemCost -> it.purpleCandyAmount }
                    else -> { it: ShopItemCost -> 0 }
                }
                val shopProfits = category.items
                    .filter { candyAmountForCategory(it.costs) > 0 }
                    .map { item ->
                        val itemPrice = getPrice(item.itemId)
                        val c = item.costs
                        val extraItemCost = if (c.extraItemId != null) getPrice(c.extraItemId) * c.extraItemAmount else 0.0
                        val otherCandyCost = when (category.baseItemId) {
                            "GREEN_CANDY" -> c.purpleCandyAmount * getPrice("PURPLE_CANDY")
                            "PURPLE_CANDY" -> c.greenCandyAmount * getPrice("GREEN_CANDY")
                            else -> 0.0
                        }
                        val effectiveValue = itemPrice - c.coinCost - extraItemCost - otherCandyCost
                        val candyAmount = candyAmountForCategory(c)
                        val profitPerBaseItem = if (candyAmount > 0) effectiveValue / candyAmount else 0.0
                        ShopProfit(
                            itemName = item.itemName,
                            baseItemName = category.baseItemName,
                            itemPrice = itemPrice,
                            profitPerBaseItem = profitPerBaseItem
                        )
                    }.sortedByDescending { it.profitPerBaseItem }

                val baseItemPriceStr = CommonUtils.toShortNumber(baseItemPrice) ?: "N/A"
                ChatUtils.sendLocalChat("\n${WHITE}Items from ${category.baseItemName}${WHITE} (${GOLD}$baseItemPriceStr ${RESET}per candy):")

                shopProfits.forEach { shopProfit ->
                    val itemPriceStr = CommonUtils.toShortNumber(shopProfit.itemPrice) ?: "N/A"
                    val profitPerBaseItemStr = CommonUtils.toShortNumber(shopProfit.profitPerBaseItem) ?: "N/A"
                    ChatUtils.sendLocalChat(" - ${shopProfit.itemName}${RESET}: ${GOLD}$itemPriceStr${RESET} (${GOLD}$profitPerBaseItemStr${RESET} per candy)")
                }
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to calculate Fear Mongerer shop price statistics.", e)
        }
    }

    private fun getPrice(itemId: String): Double {
        val bazaarPrices = PriceUtils.getBazaarItemPrices(itemId)
        var itemPrice = if (Commands.fearMongererShopPricesPriceMode == PricingMode.SELL_OFFER) bazaarPrices?.sellOffer else bazaarPrices?.instaSell

        if (bazaarPrices == null) {
            val auctionPrices = PriceUtils.getAuctionItemPrice(itemId)
            itemPrice = auctionPrices?.lbin
        }

        return itemPrice ?: 0.0
    }
}
