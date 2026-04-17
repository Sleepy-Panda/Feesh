package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PriceUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.settings.categories.Commands
import com.github.sleepypanda.feesh.utils.enums.PricingMode
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.ClickEvent.RunCommand
import net.minecraft.network.chat.HoverEvent.ShowText

object GearCraftPricesCommand {
    const val COMMAND_NAME = "feeshGearCraftPrices"

    data class CraftableItem(
        val itemId: String,
        val itemName: String,
        val amountOfItems: Int
    )
    
    data class CraftableCategory(
        val baseItemId: String,
        val baseItemName: String,
        val items: List<CraftableItem>
    )
    
    data class CraftProfit(
        val itemName: String,
        val baseItemName: String,
        val itemPrice: Double,
        val profitPerBaseItem: Double
    )
    
    private val CRAFTABLES = listOf(
        CraftableCategory(
            baseItemId = "MAGMA_LORD_FRAGMENT",
            baseItemName = "${LEGENDARY}Magma Lord Fragment",
            items = listOf(
                CraftableItem("MAGMA_LORD_HELMET", "${LEGENDARY}Magma Lord Helmet", 5),
                CraftableItem("MAGMA_LORD_CHESTPLATE", "${LEGENDARY}Magma Lord Chestplate", 8),
                CraftableItem("MAGMA_LORD_LEGGINGS", "${LEGENDARY}Magma Lord Leggings", 7),
                CraftableItem("MAGMA_LORD_BOOTS", "${LEGENDARY}Magma Lord Boots", 4),
                CraftableItem("MAGMA_LORD_GAUNTLET", "${EPIC}Magma Lord Gauntlet", 6)
            )
        ),
        CraftableCategory(
            baseItemId = "THUNDER_SHARDS",
            baseItemName = "${EPIC}Thunder Fragment",
            items = listOf(
                CraftableItem("THUNDER_HELMET", "${EPIC}Thunder Helmet", 5),
                CraftableItem("THUNDER_CHESTPLATE", "${EPIC}Thunder Chestplate", 8),
                CraftableItem("THUNDER_LEGGINGS", "${EPIC}Thunder Leggings", 7),
                CraftableItem("THUNDER_BOOTS", "${EPIC}Thunder Boots", 4),
                CraftableItem("THUNDERBOLT_NECKLACE", "${EPIC}Thunderbolt Necklace", 5)
            )
        ),
        CraftableCategory(
            baseItemId = "WALNUT",
            baseItemName = "${UNCOMMON}Walnut",
            items = listOf(
                CraftableItem("NUTCRACKER_HELMET", "${LEGENDARY}Nutcracker Helmet", 15),
                CraftableItem("NUTCRACKER_CHESTPLATE", "${LEGENDARY}Nutcracker Chestplate", 24),
                CraftableItem("NUTCRACKER_LEGGINGS", "${LEGENDARY}Nutcracker Leggings", 21),
                CraftableItem("NUTCRACKER_BOOTS", "${LEGENDARY}Nutcracker Boots", 12)
            )
        ),
        CraftableCategory(
            baseItemId = "DIVER_FRAGMENT",
            baseItemName = "${RARE}Emperor's Skull",
            items = listOf(
                CraftableItem("EMPEROR_TALISMAN", "${UNCOMMON}Emperor's Talisman", 4),
                CraftableItem("EMPEROR_RING", "${RARE}Emperor's Ring", 16),
                CraftableItem("EMPEROR_ARTIFACT", "${EPIC}Emperor's Artifact", 64)
            )
        )
    )

    fun init() {
        RegisterUtils.command(COMMAND_NAME) {
            calculateGearCraftPrices()
        }
    }
    
    private fun calculateGearCraftPrices() {
        CommonUtils.runWithCatching("Failed to calculate Gear craft price statistics") {
            if (!WorldUtils.isInSkyblock()) {
                ChatUtils.sendLocalChat("${RED}You must be on Hypixel Skyblock to use this command!", true)
                return
            }
                 
            val modeText = Commands.gearCraftPricesPriceMode.displayName
            val chatBreak = "${GRAY}${ChatUtils.getChatBreak("-")}"
            ChatUtils.sendLocalChat(chatBreak)
            ChatUtils.sendLocalChat("${GREEN}${BOLD}Gear craft prices", true)
            ChatUtils.sendLocalChat("${GRAY}Prices for crafted gear compared with price for selling base items ($modeText). Click a line to open Supercraft menu.")
            
            CRAFTABLES.forEach { category ->
                val baseItemPrice = getPrice(category.baseItemId)
                val craftProfits = category.items.map { item ->
                    val itemPrice = getPrice(item.itemId)
                    CraftProfit(
                        itemName = item.itemName,
                        baseItemName = category.baseItemName,
                        itemPrice = itemPrice,
                        profitPerBaseItem = if (item.amountOfItems > 0) itemPrice / item.amountOfItems else 0.0
                    )
                }.sortedByDescending { it.profitPerBaseItem }
                
                val baseItemPriceStr = CommonUtils.toShortNumber(baseItemPrice) ?: "N/A"
                ChatUtils.sendLocalChat("\n${WHITE}Gear crafted from ${category.baseItemName}${WHITE} (${GOLD}$baseItemPriceStr ${RESET}per item):")
                
                craftProfits.forEach { craftProfit ->
                    val itemPriceStr = CommonUtils.toShortNumber(craftProfit.itemPrice) ?: "N/A"
                    val profitPerBaseItemStr = CommonUtils.toShortNumber(craftProfit.profitPerBaseItem) ?: "N/A"
                    val itemNameWithoutFormatting = craftProfit.itemName.removeFormatting()
                    
                    val clickableText = Component.literal(" - ${craftProfit.itemName}${RESET}: ${GOLD}$itemPriceStr${RESET} (${GOLD}$profitPerBaseItemStr${RESET} per item)")
                        .setStyle(
                            Style.EMPTY
                                .withClickEvent(RunCommand("/recipe $itemNameWithoutFormatting"))
                                .withHoverEvent(ShowText(Component.literal("Click to Supercraft $itemNameWithoutFormatting")))
                        )
                    ChatUtils.sendLocalChat(clickableText)
                }
            }
        }
    }
    
    private fun getPrice(itemId: String): Double {
        val bazaarPrices = PriceUtils.getBazaarItemPrices(itemId)
        var itemPrice = if (Commands.gearCraftPricesPriceMode == PricingMode.SELL_OFFER) bazaarPrices?.sellOffer else bazaarPrices?.instaSell
        
        if (bazaarPrices == null) {
            val auctionPrices = PriceUtils.getAuctionItemPrice(itemId)
            itemPrice = auctionPrices?.lbin
        }
        
        return itemPrice ?: 0.0
    }
}
