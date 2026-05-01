package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.ShopItemProfit
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.calculateShopItemProfit
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.getProfitChatLineWithHover
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.getPrice
import com.github.sleepypanda.feesh.features.commands.constants.GearCraftItems
import com.github.sleepypanda.feesh.features.commands.constants.GearCraftItems.GearCraftCategory
import com.github.sleepypanda.feesh.settings.categories.Commands
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import net.minecraft.network.chat.ClickEvent.RunCommand
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

// TODO: Settings description
// TODO: Printed command description
// TODO: Base item price
object GearCraftPricesCommand {
    const val COMMAND_NAME = "feeshGearCraftPrices"

    fun init() {
        RegisterUtils.command(COMMAND_NAME) { args ->
            calculateGearCraftPrices(args)
        }
    }

    private fun calculateGearCraftPrices(args: Array<String>) {
        CommonUtils.runWithCatching("Failed to calculate Gear craft price statistics") {
            if (!WorldUtils.isInSkyblock()) {
                ChatUtils.sendLocalChat("${RED}You must be on Hypixel Skyblock to use this command!", true)
                return
            }

            val priceMode = Commands.gearCraftPricesPriceMode
            val modeText = "${WHITE}${priceMode.displayName}"
            val aliasKey = args.joinToString("").lowercase().replace(Regex("[^a-z0-9]"), "")
            val categories = when {
                aliasKey.isEmpty() -> GearCraftItems.CATEGORIES
                else -> {
                    val category = getCategoryByAlias(aliasKey)
                    if (category == null) {
                        ChatUtils.sendLocalChat(
                            "${RED}Unknown material parameter \"${args.joinToString(" ")}\". ${GRAY}Try one of the following: ${WHITE}${GearCraftItems.SAMPLE_ALIASES}${GRAY}.",
                            true
                        )
                        return
                    }
                    listOf(category)
                }
            }

            val chatBreak = "${GRAY}${ChatUtils.getChatBreak("-")}"
            ChatUtils.sendLocalChat(chatBreak)
            ChatUtils.sendLocalChat("${GREEN}${BOLD}Gear craft prices", true)
            ChatUtils.sendLocalChat(
                "${GRAY}Sell price of crafted gear vs. selling materials as-is ($modeText${GRAY}). Hover a line to see the full breakdown; click a line to open Supercraft."
            )
            if (aliasKey.isEmpty()) {
                ChatUtils.sendLocalChat("${GRAY}Filter by material type: ${WHITE}/$COMMAND_NAME <${GearCraftItems.SAMPLE_ALIASES}>")
            }

            categories.forEach { category ->
                val baseItemId = category.baseItemId
                val baseItemName = getMaterialDisplayName(baseItemId)
                val baseItemPrice = getPrice(baseItemId, priceMode)
                val baseItemPriceStr = CommonUtils.toShortNumber(baseItemPrice) ?: "N/A"
                ChatUtils.sendLocalChat("\n${WHITE}Items from ${baseItemName}${WHITE} (${GOLD}$baseItemPriceStr${WHITE} per item):")

                if (category.description.isNotEmpty()) ChatUtils.sendLocalChat(category.description)
                
                val profits = category.items
                    .map { calculateShopItemProfit(it, priceMode) }
                    .sortedByDescending { it.profit }
                profits.forEach { profit ->
                    ChatUtils.sendLocalChat(buildLineWithRecipeOnClick(profit, modeText))
                }
            }
            ChatUtils.sendLocalChat(chatBreak)
        }
    }

    private fun getCategoryByAlias(key: String): GearCraftCategory? {
        if (key.isEmpty()) return null
        return GearCraftItems.CATEGORIES.find { cat -> cat.aliases.any { it == key } }
    }

    private fun getMaterialDisplayName(itemId: String?): String =
        if (itemId == null) "${GOLD}Coins" else GearCraftItems.getDisplayName(itemId)

    private fun buildLineWithRecipeOnClick(shopProfit: ShopItemProfit, modeText: String): Component {
        val base = getProfitChatLineWithHover(shopProfit, modeText, ::getMaterialDisplayName)
        val recipeName = shopProfit.itemName.removeFormatting().trim()
        val mutable = base as MutableComponent
        return mutable.copy().setStyle(mutable.style.withClickEvent(RunCommand("/recipe $recipeName")))
    }
}
