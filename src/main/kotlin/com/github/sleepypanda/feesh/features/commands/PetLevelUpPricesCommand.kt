package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PriceUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object PetLevelUpPricesCommand {
    const val COMMAND_NAME = "feeshPetLevelUpPrices"
    private const val MAX_XP = 25_353_230.0
    
    data class PetInfo(
        val petDisplayName: String,
        val xpGainMultiplier: Int
    )
    
    data class PetPriceInfo(
        val petDisplayName: String,
        val level1Price: Double?,
        val level100Price: Double?,
        val coinsPerXp: Double?,
        val diff: Double?
    )
    
    private val PETS_TO_CHECK = listOf(
        PetInfo("${LEGENDARY}Blue Whale", 1),
        PetInfo("${LEGENDARY}Flying Fish", 1),
        PetInfo("${MYTHIC}Flying Fish", 1),
        PetInfo("${LEGENDARY}Baby Yeti", 1),
        PetInfo("${MYTHIC}Baby Yeti", 1),
        PetInfo("${LEGENDARY}Penguin", 1),
        PetInfo("${LEGENDARY}Spinosaurus", 1),
        PetInfo("${LEGENDARY}Megalodon", 1),
        PetInfo("${LEGENDARY}Ammonite", 1),
        PetInfo("${LEGENDARY}Squid", 1),
        PetInfo("${LEGENDARY}Dolphin", 1),
        PetInfo("${LEGENDARY}Reindeer", 2), // 2x faster to level up
        PetInfo("${LEGENDARY}Hermit Crab", 1),
        PetInfo("${MYTHIC}Hermit Crab", 1),
        PetInfo("${LEGENDARY}Seal", 1)
    )

    fun init() {
        RegisterUtils.command(COMMAND_NAME) {
            calculateFishingPetPrices()
        }
    }
    
    private fun calculateFishingPetPrices() {
        CommonUtils.runWithCatching("Failed to calculate fishing pet price statistics") {
            if (!WorldUtils.isInSkyblock()) {
                ChatUtils.sendLocalChat("${RED}You must be on Hypixel Skyblock to use this command!", true)
                return
            }
            
            val prices = PETS_TO_CHECK.map { pet ->
                val level1ItemId = ItemUtils.getLevel1PetId(pet.petDisplayName)
                val level1Price = PriceUtils.getAuctionItemPrice(level1ItemId)?.lbin
                val level100ItemId = ItemUtils.getMaxedPetId(pet.petDisplayName, 100)
                val level100Price = PriceUtils.getAuctionItemPrice(level100ItemId)?.lbin
                
                val diff = if (level1Price != null && level100Price != null) level100Price - level1Price else null
                val coinsPerXp = if (diff != null) (diff / MAX_XP) * pet.xpGainMultiplier else null
                
                PetPriceInfo(
                    petDisplayName = pet.petDisplayName,
                    level1Price = level1Price,
                    level100Price = level100Price,
                    coinsPerXp = coinsPerXp,
                    diff = diff
                )
            }.sortedByDescending { it.coinsPerXp }
            
            val chatBreak = "${GRAY}${ChatUtils.getChatBreak("-")}"
            ChatUtils.sendLocalChat(chatBreak)
            ChatUtils.sendLocalChat("${GREEN}${BOLD}Pets level up prices", true)
            ChatUtils.sendLocalChat("${GRAY}Profits for leveling up the fishing pets from level 1 to level 100.")

            for (petInfo in prices) {
                val diffCount = CommonUtils.toShortNumber(petInfo.diff) ?: "N/A"
                val diffColor = if (petInfo.diff != null && petInfo.diff < 0) RED else GREEN
                val diffText = if (petInfo.diff != null && petInfo.diff > 0) "+$diffCount" else diffCount
                val level1PriceStr = CommonUtils.toShortNumber(petInfo.level1Price) ?: "N/A"
                val level100PriceStr = CommonUtils.toShortNumber(petInfo.level100Price) ?: "N/A"
                val coinsPerXpStr = petInfo.coinsPerXp?.let { String.format("%.2f", it) } ?: "N/A"
                ChatUtils.sendLocalChat(" - ${petInfo.petDisplayName}${RESET}: ${diffColor}${diffText}${RESET} (${GOLD}$level1PriceStr${RESET} -> ${GOLD}$level100PriceStr${RESET}) | ${GOLD}$coinsPerXpStr ${RESET}coins/XP")
            }
        }
    }
}