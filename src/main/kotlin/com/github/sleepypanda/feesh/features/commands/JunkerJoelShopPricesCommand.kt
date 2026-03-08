package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.BaseItemCost
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.ShopItem
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.ShopItemCost
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.calculateShopItemProfit
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.getProfitChatLineWithHover
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.settings.categories.Commands

object JunkerJoelShopPricesCommand {
    const val COMMAND_NAME = "feeshJunkerJoelShopPrices"

    private const val RUSTY_COIN = "RUSTY_COIN"
    private const val BUSTED_BELT_BUCKLE = "BUSTED_BELT_BUCKLE"
    private const val OLD_LEATHER_BOOT = "OLD_LEATHER_BOOT"

    private val SHOP_ITEMS = listOf(
        ShopItem(
            "JUNK_TALISMAN",
            "${COMMON}Junk Talisman",
            ShopItemCost(baseItemCosts = listOf(BaseItemCost(RUSTY_COIN, 32)))
        ),
        ShopItem(
            "JUNK_RING",
            "${UNCOMMON}Junk Ring",
            ShopItemCost(
                baseItemCosts = listOf(
                    BaseItemCost(BUSTED_BELT_BUCKLE, 4),
                    BaseItemCost(RUSTY_COIN, 32)
                )
            )
        ),
        ShopItem(
            "JUNK_ARTIFACT",
            "${RARE}Junk Artifact",
            ShopItemCost(
                baseItemCosts = listOf(
                    BaseItemCost(OLD_LEATHER_BOOT, 1),
                    BaseItemCost(BUSTED_BELT_BUCKLE, 4),
                    BaseItemCost(RUSTY_COIN, 32)
                )
            )
        ),
        ShopItem(
            "STINGY_SINKER",
            "${RARE}Stingy Sinker",
            ShopItemCost(baseItemCosts = listOf(BaseItemCost(RUSTY_COIN, 64)))
        ),
        ShopItem(
            "SPEEDY_LINE",
            "${RARE}Speedy Line",
            ShopItemCost(baseItemCosts = listOf(BaseItemCost(BUSTED_BELT_BUCKLE, 8)))
        ),
        ShopItem(
            "BAYOU_TRAVEL_SCROLL",
            "${RARE}Travel Scroll to the Bayou",
            ShopItemCost(baseItemCosts = listOf(BaseItemCost(BUSTED_BELT_BUCKLE, 8)))
        ),
        ShopItem(
            "GOLD_BOTTLE_CAP",
            "${LEGENDARY}Gold Bottle Cap",
            ShopItemCost(
                baseItemCosts = listOf(
                    BaseItemCost(RUSTY_COIN, 512),
                    BaseItemCost(BUSTED_BELT_BUCKLE, 64),
                    BaseItemCost(OLD_LEATHER_BOOT, 8)
                )
            )
        )
    )

    fun init() {
        RegisterUtils.command(COMMAND_NAME) {
            calculateJunkerJoelShopPrices()
        }
    }

    private fun calculateJunkerJoelShopPrices() {
        try {
            if (!WorldUtils.isInSkyblock()) {
                ChatUtils.sendLocalChat("${RED}You must be on Hypixel Skyblock to use this command!", true)
                return
            }

            val priceMode = Commands.junkerJoelShopPricesPriceMode
            val modeText = priceMode.displayName
            val chatBreak = "${GRAY}${ChatUtils.getChatBreak("-")}"
            ChatUtils.sendLocalChat(chatBreak)
            ChatUtils.sendLocalChat("${GREEN}${BOLD}Junker Joel shop prices", true)
            ChatUtils.sendLocalChat("${DARK_GRAY}Profits for selling shop items compared with selling Rusty Coins, Busted Belt Buckles, Old Leather Boots as is. Hover a line to see the full breakdown. Price mode: ${WHITE}$modeText.\n")

            val shopProfits = SHOP_ITEMS
                .map { calculateShopItemProfit(it, priceMode) }
                .sortedByDescending { it.profit }

            val getMaterialName: (String?) -> String = { id ->
                when (id) {
                    null -> "${GOLD}Coins"
                    RUSTY_COIN -> "${UNCOMMON}Rusty Coin"
                    BUSTED_BELT_BUCKLE -> "${RARE}Busted Belt Buckle"
                    OLD_LEATHER_BOOT -> "${EPIC}Old Leather Boot"
                    else -> id
                }
            }
            shopProfits.forEach { shopProfit ->
                ChatUtils.sendLocalChat(getProfitChatLineWithHover(shopProfit, modeText, getMaterialName))
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to calculate Junker Joel shop price statistics.", e)
        }
    }
}
