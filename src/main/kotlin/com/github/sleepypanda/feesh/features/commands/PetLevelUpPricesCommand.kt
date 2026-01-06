package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PriceUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.FeeshMod

object PetLevelUpPricesCommand {
    private const val MAX_XP = 25_353_230.0
    
    data class PetInfo(
        val petDisplayName: String,
        val xpGainMultiplier: Int
    )
    
    data class PetPriceInfo(
        val petDisplayName: String,
        val level1Price: Double,
        val level100Price: Double,
        val coinsPerXp: Double,
        val diff: Double
    )
    
    private val PETS_TO_CHECK = listOf(
        PetInfo("${LEGENDARY}Blue Whale", 1),
        PetInfo("${LEGENDARY}Flying Fish", 1),
        PetInfo("${MYTHIC}Flying Fish", 1),
        PetInfo("${LEGENDARY}Baby Yeti", 1),
        PetInfo("${LEGENDARY}Penguin", 1),
        PetInfo("${LEGENDARY}Spinosaurus", 1),
        PetInfo("${LEGENDARY}Megalodon", 1),
        PetInfo("${LEGENDARY}Ammonite", 1),
        PetInfo("${LEGENDARY}Squid", 1),
        PetInfo("${LEGENDARY}Dolphin", 1),
        PetInfo("${LEGENDARY}Reindeer", 2), // 2x faster to level up
        PetInfo("${LEGENDARY}Hermit Crab", 1),
        PetInfo("${MYTHIC}Hermit Crab", 1)
    )

    fun init() {
        RegisterUtils.command("feeshPetLevelUpPrices") {
            calculateFishingPetPrices()
        }
    }
    
    private fun calculateFishingPetPrices() {
        try {
            if (!WorldUtils.isInSkyblock()) return
            
            val prices = PETS_TO_CHECK.map { pet ->
                val petName = pet.petDisplayName.removeFormatting()
                val rarityColorCode = pet.petDisplayName.substring(0, 2)
                val rarityCode = CommonUtils.getRarityNumericCode(rarityColorCode)
                
                val level1ItemId = petName.split(" ").joinToString("_").uppercase() + ";$rarityCode" // FLYING_FISH;4
                val level1AuctionPrice = PriceUtils.getAuctionItemPrice(level1ItemId)
                val level1Price = level1AuctionPrice?.lbin ?: 0.0
                
                val level100ItemId = level1ItemId + "+100" // FLYING_FISH;4+100
                val level100AuctionPrice = PriceUtils.getAuctionItemPrice(level100ItemId)
                val level100Price = level100AuctionPrice?.lbin ?: 0.0
                
                val diff = if (level1Price > 0 && level100Price > 0) level100Price - level1Price else 0.0
                val coinsPerXp = if (diff > 0) (diff / MAX_XP) * pet.xpGainMultiplier else 0.0
                
                PetPriceInfo(
                    petDisplayName = pet.petDisplayName,
                    level1Price = level1Price,
                    level100Price = level100Price,
                    coinsPerXp = coinsPerXp,
                    diff = diff
                )
            }.sortedByDescending { it.coinsPerXp }
            
            var message = "${GREEN}${BOLD}Pets level up prices:\n"
            message += "${DARK_GRAY}Profits for leveling up the fishing pets from level 1 to level 100.\n"
            ChatUtils.sendLocalChat(message)

            for (petInfo in prices) {
                val diffStr = CommonUtils.toShortNumber(petInfo.diff) ?: "N/A"
                val level1PriceStr = CommonUtils.toShortNumber(petInfo.level1Price) ?: "N/A"
                val level100PriceStr = CommonUtils.toShortNumber(petInfo.level100Price) ?: "N/A"
                val coinsPerXpStr = String.format("%.2f", petInfo.coinsPerXp)
                
                ChatUtils.sendLocalChat(" - ${petInfo.petDisplayName}${RESET}: ${GREEN}+$diffStr${RESET} (${GOLD}$level1PriceStr${RESET} -> ${GOLD}$level100PriceStr${RESET}) | ${GOLD}$coinsPerXpStr ${RESET}coins/XP\n")
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to calculate pet price statistics.", e)
        }
    }
}