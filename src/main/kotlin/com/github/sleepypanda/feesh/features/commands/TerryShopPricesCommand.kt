package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.BaseItemCost
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.ShopItem
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.ShopItemCost
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.calculateShopItemProfit
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.getProfitChatLineWithHover
import com.github.sleepypanda.feesh.settings.categories.Commands
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object TerryShopPricesCommand {
    const val COMMAND_NAME = "feeshTerryShopPrices"

    private const val HUNK_OF_ICE = "ICE_HUNK"
    private const val HUNK_OF_BLUE_ICE = "BLUE_ICE_HUNK"

    private val SHOP_ITEMS = listOf(
        ShopItem(
            "FROZEN_BAUBLE",
            "${EPIC}Frozen Bauble",
            ShopItemCost(
                baseItemCosts = listOf(
                    BaseItemCost(HUNK_OF_ICE, 64),
                    BaseItemCost(HUNK_OF_BLUE_ICE, 32)
                )
            )
        ),
        ShopItem(
            "TERRY_SNOWGLOBE",
            "${RARE}Terry's Snowglobe",
            ShopItemCost(
                baseItemCosts = listOf(
                    BaseItemCost(HUNK_OF_ICE, 32),
                    BaseItemCost(HUNK_OF_BLUE_ICE, 8)
                )
            )
        ),
        ShopItem(
            "RUSTY_ANCHOR",
            "${RARE}Rusty Anchor",
            ShopItemCost(baseItemCosts = listOf(BaseItemCost(HUNK_OF_BLUE_ICE, 64)))
        ),
        ShopItem(
            "SALT_CUBE",
            "${UNCOMMON}Salt Cube",
            ShopItemCost(baseItemCosts = listOf(BaseItemCost(HUNK_OF_ICE, 64)))
        )
    )

    fun init() {
        RegisterUtils.command(COMMAND_NAME) {
            calculateTerryShopPrices()
        }
    }

    private fun calculateTerryShopPrices() {
        CommonUtils.runWithCatching("Failed to calculate Terry shop price statistics") {
            if (!WorldUtils.isInSkyblock()) {
                ChatUtils.sendLocalChat("${RED}You must be on Hypixel Skyblock to use this command!", true)
                return
            }

            val priceMode = Commands.terryShopPricesPriceMode
            val modeText = priceMode.displayName
            val chatBreak = "${GRAY}${ChatUtils.getChatBreak("-")}"
            ChatUtils.sendLocalChat(chatBreak)
            ChatUtils.sendLocalChat("${GREEN}${BOLD}Terry shop prices", true)
            ChatUtils.sendLocalChat("${DARK_GRAY}Profits for selling shop items compared with selling Hunk of Ice and Hunk of Blue Ice as is. Hover a line to see the full breakdown. Price mode: ${WHITE}$modeText.\n")

            val shopProfits = SHOP_ITEMS
                .map { calculateShopItemProfit(it, priceMode) }
                .sortedByDescending { it.profit }

            val getMaterialName: (String?) -> String = { id ->
                when (id) {
                    null -> "${GOLD}Coins"
                    HUNK_OF_ICE -> "${UNCOMMON}Hunk of Ice"
                    HUNK_OF_BLUE_ICE -> "${RARE}Hunk of Blue Ice"
                    else -> id
                }
            }
            shopProfits.forEach { shopProfit ->
                ChatUtils.sendLocalChat(getProfitChatLineWithHover(shopProfit, modeText, getMaterialName))
            }
        }
    }
}
