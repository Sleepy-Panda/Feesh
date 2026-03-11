package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.BaseItemCost
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.ShopItem
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.ShopItemCost
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.calculateShopItemProfit
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.getProfitChatLineWithHover
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.getPrice
import com.github.sleepypanda.feesh.settings.categories.Commands
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object FearMongererShopPricesCommand {
    const val COMMAND_NAME = "feeshFearMongererShopPrices"

    private const val GREEN_CANDY = "GREEN_CANDY"
    private const val PURPLE_CANDY = "PURPLE_CANDY"
    private const val BAT_TALISMAN = "BAT_TALISMAN"

    data class ShopCategory(
        val baseItemId: String,
        val baseItemName: String,
        val baseItemDisplayName: String,
        val items: List<ShopItem>
    )

    private val SHOP_CATEGORIES = listOf(
        ShopCategory(
            baseItemId = GREEN_CANDY,
            baseItemName = "Green Candy",
            baseItemDisplayName = "${UNCOMMON}Green Candy",
            items = listOf(
                ShopItem(
                    "CANDY_TALISMAN",
                    "${UNCOMMON}Candy Talisman",
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(GREEN_CANDY, 16)))
                ),
                ShopItem(
                    "CANDY_RING",
                    "${RARE}Candy Ring",
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(GREEN_CANDY, 80)))
                ),
                ShopItem(
                    "BAT_FIREWORK",
                    "${UNCOMMON}Bat Firework",
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(GREEN_CANDY, 100)))
                ),
                ShopItem(
                    "ENCHANTMENT_SUGAR_RUSH_1",
                    "${RARE}Sugar Rush I",
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(GREEN_CANDY, 64)), coinCost = 250_000L)
                ),
                ShopItem(
                    "BAT_RING",
                    "${EPIC}Bat Ring",
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(GREEN_CANDY, 64)), extraItemId = BAT_TALISMAN)
                ),
                ShopItem(
                    "INTIMIDATION_RING",
                    "${UNCOMMON}Intimidation Ring",
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(GREEN_CANDY, 100)), coinCost = 10_000L)
                ),
            )
        ),
        ShopCategory(
            baseItemId = PURPLE_CANDY,
            baseItemName = "Purple Candy",
            baseItemDisplayName = "${EPIC}Purple Candy",
            items = listOf(
                ShopItem(
                    "BAT_ARTIFACT",
                    "${LEGENDARY}Bat Artifact",
                    ShopItemCost(
                        baseItemCosts = listOf(BaseItemCost(GREEN_CANDY, 64), BaseItemCost(PURPLE_CANDY, 64)),
                        extraItemId = BAT_TALISMAN
                    )
                ),
                ShopItem(
                    "CANDY_ARTIFACT",
                    "${EPIC}Candy Artifact",
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(GREEN_CANDY, 80), BaseItemCost(PURPLE_CANDY, 32)))
                ),
                ShopItem(
                    "CANDY_RELIC",
                    "${LEGENDARY}Candy Relic",
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(GREEN_CANDY, 80), BaseItemCost(PURPLE_CANDY, 1056)))
                ),
                ShopItem(
                    "HORSEMAN_CANDLE",
                    "${EPIC}Horseman's Candle",
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(PURPLE_CANDY, 32)))
                ),
                ShopItem(
                    "SPOOKY_HELMET",
                    "${EPIC}Spooky Helmet",
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(PURPLE_CANDY, 64)))
                ),
                ShopItem(
                    "SPOOKY_CHESTPLATE",
                    "${EPIC}Spooky Chestplate",
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(PURPLE_CANDY, 64)))
                ),
                ShopItem(
                    "SPOOKY_LEGGINGS",
                    "${EPIC}Spooky Leggings",
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(PURPLE_CANDY, 64)))
                ),
                ShopItem(
                    "SPOOKY_BOOTS",
                    "${EPIC}Spooky Boots",
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(PURPLE_CANDY, 64)))
                ),
                ShopItem(
                    "BLACK_CAT;4",
                    "${GRAY}[Lvl 1] ${LEGENDARY}Black Cat",
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(PURPLE_CANDY, 2000)))
                ),
                ShopItem(
                    "INTIMIDATION_ARTIFACT",
                    "${UNCOMMON}Intimidation Artifact",
                    ShopItemCost(
                        baseItemCosts = listOf(BaseItemCost(GREEN_CANDY, 100), BaseItemCost(PURPLE_CANDY, 100)),
                        coinCost = 10_000L
                    )
                ),
                ShopItem(
                    "ENCHANTMENT_LIFE_STEAL_4",
                    "${RARE}Life Steal IV",
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(PURPLE_CANDY, 32)), coinCost = 1_500_000L)
                ),
            )
        )
    )

    fun init() {
        RegisterUtils.command(COMMAND_NAME) {
            calculateFearMongererShopPrices()
        }
    }

    private fun calculateFearMongererShopPrices() {
        CommonUtils.runWithCatching("Failed to calculate Fear Mongerer shop price statistics") {
            if (!WorldUtils.isInSkyblock()) {
                ChatUtils.sendLocalChat("${RED}You must be on Hypixel Skyblock to use this command!", true)
                return
            }

            val priceMode = Commands.fearMongererShopPricesPriceMode
            val modeText = priceMode.displayName
            val chatBreak = "${GRAY}${ChatUtils.getChatBreak("-")}"
            ChatUtils.sendLocalChat(chatBreak)
            ChatUtils.sendLocalChat("${GREEN}${BOLD}Fear Mongerer shop prices", true)
            ChatUtils.sendLocalChat("${DARK_GRAY}Profits for selling shop items compared with selling Green/Purple Candies as is. Hover a line to see the full breakdown. Price mode: ${WHITE}$modeText.")

            SHOP_CATEGORIES.forEach { category ->
                val baseItemPrice = getPrice(category.baseItemId, priceMode)
                val shopProfits = category.items
                    .filter { it.costs.hasBaseItem(category.baseItemId) }
                    .map { calculateShopItemProfit(it, priceMode) }
                    .sortedByDescending { it.profit }

                val baseItemPriceStr = CommonUtils.toShortNumber(baseItemPrice) ?: "N/A"
                ChatUtils.sendLocalChat("\n${WHITE}Items from ${category.baseItemDisplayName}${WHITE} (${GOLD}$baseItemPriceStr ${WHITE}per candy):")

                val getMaterialName: (String?) -> String = { id ->
                    when (id) {
                        null -> "${GOLD}Coins"
                        GREEN_CANDY -> "${UNCOMMON}Green Candy"
                        PURPLE_CANDY -> "${EPIC}Purple Candy"
                        BAT_TALISMAN -> "${RARE}Bat Talisman"
                        else -> id
                    }
                }
                shopProfits.forEach { shopProfit ->
                    ChatUtils.sendLocalChat(getProfitChatLineWithHover(shopProfit, modeText, getMaterialName))
                }
            }
        }
    }
}
