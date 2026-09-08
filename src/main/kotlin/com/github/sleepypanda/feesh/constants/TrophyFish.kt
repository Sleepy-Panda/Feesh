package com.github.sleepypanda.feesh.constants

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

data class TrophyFishInfo(
    val itemId: String,
    val itemName: String,
    val itemDisplayName: String,
    val amountOfMagmaFish: Int,
)

class TrophyFish {
    companion object {
        // https://hypixelskyblock.minecraft.wiki/w/Trophy_Fish#List_of_Trophy_Fish

        val OBFUSCATED_1_BRONZE = TrophyFishInfo(
            itemId = "OBFUSCATED_FISH_1_BRONZE",
            itemName = "Obfuscated-1 BRONZE",
            itemDisplayName = "${COMMON}${OBFUSCATED}Obfuscated-1 ${DARK_GRAY}${BOLD} BRONZE",
            amountOfMagmaFish = 16,
        )
        
        val OBFUSCATED_1_SILVER = TrophyFishInfo(
            itemId = "OBFUSCATED_FISH_1_SILVER",
            itemName = "Obfuscated-1 SILVER",
            itemDisplayName = "${COMMON}${OBFUSCATED}Obfuscated-1 ${GRAY}${BOLD} SILVER",
            amountOfMagmaFish = 24,
        )
        
        val OBFUSCATED_1_GOLD = TrophyFishInfo(
            itemId = "OBFUSCATED_FISH_1_GOLD",
            itemName = "Obfuscated-1 GOLD",
            itemDisplayName = "${COMMON}${OBFUSCATED}Obfuscated-1 ${GOLD}${BOLD} GOLD",
            amountOfMagmaFish = 32,
        )
        
        val OBFUSCATED_1_DIAMOND = TrophyFishInfo(
            itemId = "OBFUSCATED_FISH_1_DIAMOND",
            itemName = "Obfuscated-1 DIAMOND",
            itemDisplayName = "${COMMON}${OBFUSCATED}Obfuscated-1 ${AQUA}${BOLD} DIAMOND",
            amountOfMagmaFish = 48,
        )
        
        val OBFUSCATED_2_BRONZE = TrophyFishInfo(
            itemId = "OBFUSCATED_FISH_2_BRONZE",
            itemName = "Obfuscated-2 BRONZE",
            itemDisplayName = "${UNCOMMON}${OBFUSCATED}Obfuscated-2 ${DARK_GRAY}${BOLD} BRONZE",
            amountOfMagmaFish = 40,
        )
        
        val OBFUSCATED_2_SILVER = TrophyFishInfo(
            itemId = "OBFUSCATED_FISH_2_SILVER",
            itemName = "Obfuscated-2 SILVER",
            itemDisplayName = "${UNCOMMON}${OBFUSCATED}Obfuscated-2 ${GRAY}${BOLD} SILVER",
            amountOfMagmaFish = 60,
        )
        
        val OBFUSCATED_2_GOLD = TrophyFishInfo(
            itemId = "OBFUSCATED_FISH_2_GOLD",
            itemName = "Obfuscated-2 GOLD",
            itemDisplayName = "${UNCOMMON}${OBFUSCATED}Obfuscated-2 ${GOLD}${BOLD} GOLD",
            amountOfMagmaFish = 80,
        )
        
        val OBFUSCATED_2_DIAMOND = TrophyFishInfo(
            itemId = "OBFUSCATED_FISH_2_DIAMOND",
            itemName = "Obfuscated-2 DIAMOND",
            itemDisplayName = "${UNCOMMON}${OBFUSCATED}Obfuscated-2 ${AQUA}${BOLD} DIAMOND",
            amountOfMagmaFish = 120,
        )
        
        val OBFUSCATED_3_BRONZE = TrophyFishInfo(
            itemId = "OBFUSCATED_FISH_3_BRONZE",
            itemName = "Obfuscated-3 BRONZE",
            itemDisplayName = "${RARE}${OBFUSCATED}Obfuscated-3 ${DARK_GRAY}${BOLD} BRONZE",
            amountOfMagmaFish = 400,
        )
        
        val OBFUSCATED_3_SILVER = TrophyFishInfo(
            itemId = "OBFUSCATED_FISH_3_SILVER",
            itemName = "Obfuscated-3 SILVER",
            itemDisplayName = "${RARE}${OBFUSCATED}Obfuscated-3 ${GRAY}${BOLD} SILVER",
            amountOfMagmaFish = 700,
        )
        
        val OBFUSCATED_3_GOLD = TrophyFishInfo(
            itemId = "OBFUSCATED_FISH_3_GOLD",
            itemName = "Obfuscated-3 GOLD",
            itemDisplayName = "${RARE}${OBFUSCATED}Obfuscated-3 ${GOLD}${BOLD} GOLD",
            amountOfMagmaFish = 1000,
        )
        
        val OBFUSCATED_3_DIAMOND = TrophyFishInfo(
            itemId = "OBFUSCATED_FISH_3_DIAMOND",
            itemName = "Obfuscated-3 DIAMOND",
            itemDisplayName = "${RARE}${OBFUSCATED}Obfuscated-3 ${AQUA}${BOLD} DIAMOND",
            amountOfMagmaFish = 1300,
        )
        
        val BLOBFISH_BRONZE = TrophyFishInfo(
            itemId = "BLOBFISH_BRONZE",
            itemName = "Blobfish BRONZE",
            itemDisplayName = "${COMMON}Blobfish ${DARK_GRAY}${BOLD}BRONZE",
            amountOfMagmaFish = 4,
        )
        
        val BLOBFISH_SILVER = TrophyFishInfo(
            itemId = "BLOBFISH_SILVER",
            itemName = "Blobfish SILVER",
            itemDisplayName = "${COMMON}Blobfish ${GRAY}${BOLD}SILVER",
            amountOfMagmaFish = 8,
        )
        
        val BLOBFISH_GOLD = TrophyFishInfo(
            itemId = "BLOBFISH_GOLD",
            itemName = "Blobfish GOLD",
            itemDisplayName = "${COMMON}Blobfish ${GOLD}${BOLD}GOLD",
            amountOfMagmaFish = 12,
        )
        
        val BLOBFISH_DIAMOND = TrophyFishInfo(
            itemId = "BLOBFISH_DIAMOND",
            itemName = "Blobfish DIAMOND",
            itemDisplayName = "${COMMON}Blobfish ${AQUA}${BOLD}DIAMOND",
            amountOfMagmaFish = 16,
        )
        
        val GUSHER_BRONZE = TrophyFishInfo(
            itemId = "GUSHER_BRONZE",
            itemName = "Gusher BRONZE",
            itemDisplayName = "${COMMON}Gusher ${DARK_GRAY}${BOLD}BRONZE",
            amountOfMagmaFish = 32,
        )
        
        val GUSHER_SILVER = TrophyFishInfo(
            itemId = "GUSHER_SILVER",
            itemName = "Gusher SILVER",
            itemDisplayName = "${COMMON}Gusher ${GRAY}${BOLD}SILVER",
            amountOfMagmaFish = 48,
        )
        
        val GUSHER_GOLD = TrophyFishInfo(
            itemId = "GUSHER_GOLD",
            itemName = "Gusher GOLD",
            itemDisplayName = "${COMMON}Gusher ${GOLD}${BOLD}GOLD",
            amountOfMagmaFish = 64,
        )
        
        val GUSHER_DIAMOND = TrophyFishInfo(
            itemId = "GUSHER_DIAMOND",
            itemName = "Gusher DIAMOND",
            itemDisplayName = "${COMMON}Gusher ${AQUA}${BOLD}DIAMOND",
            amountOfMagmaFish = 96,
        )
        
        val STEAMING_HOT_FLOUNDER_BRONZE = TrophyFishInfo(
            itemId = "STEAMING_HOT_FLOUNDER_BRONZE",
            itemName = "Steaming-Hot Flounder BRONZE",
            itemDisplayName = "${COMMON}Steaming-Hot Flounder ${DARK_GRAY}${BOLD}BRONZE",
            amountOfMagmaFish = 20,
        )
        
        val STEAMING_HOT_FLOUNDER_SILVER = TrophyFishInfo(
            itemId = "STEAMING_HOT_FLOUNDER_SILVER",
            itemName = "Steaming-Hot Flounder SILVER",
            itemDisplayName = "${COMMON}Steaming-Hot Flounder ${GRAY}${BOLD}SILVER",
            amountOfMagmaFish = 28,
        )
        
        val STEAMING_HOT_FLOUNDER_GOLD = TrophyFishInfo(
            itemId = "STEAMING_HOT_FLOUNDER_GOLD",
            itemName = "Steaming-Hot Flounder GOLD",
            itemDisplayName = "${COMMON}Steaming-Hot Flounder ${GOLD}${BOLD}GOLD",
            amountOfMagmaFish = 40,
        )
        
        val STEAMING_HOT_FLOUNDER_DIAMOND = TrophyFishInfo(
            itemId = "STEAMING_HOT_FLOUNDER_DIAMOND",
            itemName = "Steaming-Hot Flounder DIAMOND",
            itemDisplayName = "${COMMON}Steaming-Hot Flounder ${AQUA}${BOLD}DIAMOND",
            amountOfMagmaFish = 60,
        )
        
        val SULPHUR_SKITTER_BRONZE = TrophyFishInfo(
            itemId = "SULPHUR_SKITTER_BRONZE",
            itemName = "Sulphur Skitter BRONZE",
            itemDisplayName = "${COMMON}Sulphur Skitter ${DARK_GRAY}${BOLD}BRONZE",
            amountOfMagmaFish = 40,
        )
        
        val SULPHUR_SKITTER_SILVER = TrophyFishInfo(
            itemId = "SULPHUR_SKITTER_SILVER",
            itemName = "Sulphur Skitter SILVER",
            itemDisplayName = "${COMMON}Sulphur Skitter ${GRAY}${BOLD}SILVER",
            amountOfMagmaFish = 60,
        )
        
        val SULPHUR_SKITTER_GOLD = TrophyFishInfo(
            itemId = "SULPHUR_SKITTER_GOLD",
            itemName = "Sulphur Skitter GOLD",
            itemDisplayName = "${COMMON}Sulphur Skitter ${GOLD}${BOLD}GOLD",
            amountOfMagmaFish = 80,
        )
        
        val SULPHUR_SKITTER_DIAMOND = TrophyFishInfo(
            itemId = "SULPHUR_SKITTER_DIAMOND",
            itemName = "Sulphur Skitter DIAMOND",
            itemDisplayName = "${COMMON}Sulphur Skitter ${AQUA}${BOLD}DIAMOND",
            amountOfMagmaFish = 120,
        )
        
        val FLYFISH_BRONZE = TrophyFishInfo(
            itemId = "FLYFISH_BRONZE",
            itemName = "Flyfish BRONZE",
            itemDisplayName = "${UNCOMMON}Flyfish ${DARK_GRAY}${BOLD}BRONZE",
            amountOfMagmaFish = 32,
        )
        
        val FLYFISH_SILVER = TrophyFishInfo(
            itemId = "FLYFISH_SILVER",
            itemName = "Flyfish SILVER",
            itemDisplayName = "${UNCOMMON}Flyfish ${GRAY}${BOLD}SILVER",
            amountOfMagmaFish = 48,
        )
        
        val FLYFISH_GOLD = TrophyFishInfo(
            itemId = "FLYFISH_GOLD",
            itemName = "Flyfish GOLD",
            itemDisplayName = "${UNCOMMON}Flyfish ${GOLD}${BOLD}GOLD",
            amountOfMagmaFish = 64,
        )
        
        val FLYFISH_DIAMOND = TrophyFishInfo(
            itemId = "FLYFISH_DIAMOND",
            itemName = "Flyfish DIAMOND",
            itemDisplayName = "${UNCOMMON}Flyfish ${AQUA}${BOLD}DIAMOND",
            amountOfMagmaFish = 96,
        )
        
        val SLUGFISH_BRONZE = TrophyFishInfo(
            itemId = "SLUGFISH_BRONZE",
            itemName = "Slugfish BRONZE",
            itemDisplayName = "${UNCOMMON}Slugfish ${DARK_GRAY}${BOLD}BRONZE",
            amountOfMagmaFish = 40,
        )
        
        val SLUGFISH_SILVER = TrophyFishInfo(
            itemId = "SLUGFISH_SILVER",
            itemName = "Slugfish SILVER",
            itemDisplayName = "${UNCOMMON}Slugfish ${GRAY}${BOLD}SILVER",
            amountOfMagmaFish = 60,
        )
        
        val SLUGFISH_GOLD = TrophyFishInfo(
            itemId = "SLUGFISH_GOLD",
            itemName = "Slugfish GOLD",
            itemDisplayName = "${UNCOMMON}Slugfish ${GOLD}${BOLD}GOLD",
            amountOfMagmaFish = 80,
        )
        
        val SLUGFISH_DIAMOND = TrophyFishInfo(
            itemId = "SLUGFISH_DIAMOND",
            itemName = "Slugfish DIAMOND",
            itemDisplayName = "${UNCOMMON}Slugfish ${AQUA}${BOLD}DIAMOND",
            amountOfMagmaFish = 120,
        )
        
        val LAVAHORSE_BRONZE = TrophyFishInfo(
            itemId = "LAVAHORSE_BRONZE",
            itemName = "Lavahorse BRONZE",
            itemDisplayName = "${RARE}Lavahorse ${DARK_GRAY}${BOLD}BRONZE",
            amountOfMagmaFish = 12,
        )
        
        val LAVAHORSE_SILVER = TrophyFishInfo(
            itemId = "LAVAHORSE_SILVER",
            itemName = "Lavahorse SILVER",
            itemDisplayName = "${RARE}Lavahorse ${GRAY}${BOLD}SILVER",
            amountOfMagmaFish = 16,
        )
        
        val LAVAHORSE_GOLD = TrophyFishInfo(
            itemId = "LAVAHORSE_GOLD",
            itemName = "Lavahorse GOLD",
            itemDisplayName = "${RARE}Lavahorse ${GOLD}${BOLD}GOLD",
            amountOfMagmaFish = 20,
        )
        
        val LAVAHORSE_DIAMOND = TrophyFishInfo(
            itemId = "LAVAHORSE_DIAMOND",
            itemName = "Lavahorse DIAMOND",
            itemDisplayName = "${RARE}Lavahorse ${AQUA}${BOLD}DIAMOND",
            amountOfMagmaFish = 24,
        )
        
        val MANA_RAY_BRONZE = TrophyFishInfo(
            itemId = "MANA_RAY_BRONZE",
            itemName = "Mana Ray BRONZE",
            itemDisplayName = "${RARE}Mana Ray ${DARK_GRAY}${BOLD}BRONZE",
            amountOfMagmaFish = 40,
        )
        
        val MANA_RAY_SILVER = TrophyFishInfo(
            itemId = "MANA_RAY_SILVER",
            itemName = "Mana Ray SILVER",
            itemDisplayName = "${RARE}Mana Ray ${GRAY}${BOLD}SILVER",
            amountOfMagmaFish = 60,
        )
        
        val MANA_RAY_GOLD = TrophyFishInfo(
            itemId = "MANA_RAY_GOLD",
            itemName = "Mana Ray GOLD",
            itemDisplayName = "${RARE}Mana Ray ${GOLD}${BOLD}GOLD",
            amountOfMagmaFish = 80,
        )
        
        val MANA_RAY_DIAMOND = TrophyFishInfo(
            itemId = "MANA_RAY_DIAMOND",
            itemName = "Mana Ray DIAMOND",
            itemDisplayName = "${RARE}Mana Ray ${AQUA}${BOLD}DIAMOND",
            amountOfMagmaFish = 120,
        )
        
        val VANILLE_BRONZE = TrophyFishInfo(
            itemId = "VANILLE_BRONZE",
            itemName = "Vanille BRONZE",
            itemDisplayName = "${RARE}Vanille ${DARK_GRAY}${BOLD}BRONZE",
            amountOfMagmaFish = 80,
        )
        
        val VANILLE_SILVER = TrophyFishInfo(
            itemId = "VANILLE_SILVER",
            itemName = "Vanille SILVER",
            itemDisplayName = "${RARE}Vanille ${GRAY}${BOLD}SILVER",
            amountOfMagmaFish = 120,
        )
        
        val VANILLE_GOLD = TrophyFishInfo(
            itemId = "VANILLE_GOLD",
            itemName = "Vanille GOLD",
            itemDisplayName = "${RARE}Vanille ${GOLD}${BOLD}GOLD",
            amountOfMagmaFish = 160,
        )
        
        val VANILLE_DIAMOND = TrophyFishInfo(
            itemId = "VANILLE_DIAMOND",
            itemName = "Vanille DIAMOND",
            itemDisplayName = "${RARE}Vanille ${AQUA}${BOLD}DIAMOND",
            amountOfMagmaFish = 240,
        )
        
        val KARATE_FISH_BRONZE = TrophyFishInfo(
            itemId = "KARATE_FISH_BRONZE",
            itemName = "Karate Fish BRONZE",
            itemDisplayName = "${EPIC}Karate Fish ${DARK_GRAY}${BOLD}BRONZE",
            amountOfMagmaFish = 40,
        )
        
        val KARATE_FISH_SILVER = TrophyFishInfo(
            itemId = "KARATE_FISH_SILVER",
            itemName = "Karate Fish SILVER",
            itemDisplayName = "${EPIC}Karate Fish ${GRAY}${BOLD}SILVER",
            amountOfMagmaFish = 60,
        )
        
        val KARATE_FISH_GOLD = TrophyFishInfo(
            itemId = "KARATE_FISH_GOLD",
            itemName = "Karate Fish GOLD",
            itemDisplayName = "${EPIC}Karate Fish ${GOLD}${BOLD}GOLD",
            amountOfMagmaFish = 80,
        )
        
        val KARATE_FISH_DIAMOND = TrophyFishInfo(
            itemId = "KARATE_FISH_DIAMOND",
            itemName = "Karate Fish DIAMOND",
            itemDisplayName = "${EPIC}Karate Fish ${AQUA}${BOLD}DIAMOND",
            amountOfMagmaFish = 120,
        )
        
        val MOLDFIN_BRONZE = TrophyFishInfo(
            itemId = "MOLDFIN_BRONZE",
            itemName = "Moldfin BRONZE",
            itemDisplayName = "${EPIC}Moldfin ${DARK_GRAY}${BOLD}BRONZE",
            amountOfMagmaFish = 32,
        )
        
        val MOLDFIN_SILVER = TrophyFishInfo(
            itemId = "MOLDFIN_SILVER",
            itemName = "Moldfin SILVER",
            itemDisplayName = "${EPIC}Moldfin ${GRAY}${BOLD}SILVER",
            amountOfMagmaFish = 48,
        )
        
        val MOLDFIN_GOLD = TrophyFishInfo(
            itemId = "MOLDFIN_GOLD",
            itemName = "Moldfin GOLD",
            itemDisplayName = "${EPIC}Moldfin ${GOLD}${BOLD}GOLD",
            amountOfMagmaFish = 64,
        )
        
        val MOLDFIN_DIAMOND = TrophyFishInfo(
            itemId = "MOLDFIN_DIAMOND",
            itemName = "Moldfin DIAMOND",
            itemDisplayName = "${EPIC}Moldfin ${AQUA}${BOLD}DIAMOND",
            amountOfMagmaFish = 96,
        )
        
        val SOUL_FISH_BRONZE = TrophyFishInfo(
            itemId = "SOUL_FISH_BRONZE",
            itemName = "Soul Fish BRONZE",
            itemDisplayName = "${EPIC}Soul Fish ${DARK_GRAY}${BOLD}BRONZE",
            amountOfMagmaFish = 32,
        )
        
        val SOUL_FISH_SILVER = TrophyFishInfo(
            itemId = "SOUL_FISH_SILVER",
            itemName = "Soul Fish SILVER",
            itemDisplayName = "${EPIC}Soul Fish ${GRAY}${BOLD}SILVER",
            amountOfMagmaFish = 48,
        )
        
        val SOUL_FISH_GOLD = TrophyFishInfo(
            itemId = "SOUL_FISH_GOLD",
            itemName = "Soul Fish GOLD",
            itemDisplayName = "${EPIC}Soul Fish ${GOLD}${BOLD}GOLD",
            amountOfMagmaFish = 64,
        )
        
        val SOUL_FISH_DIAMOND = TrophyFishInfo(
            itemId = "SOUL_FISH_DIAMOND",
            itemName = "Soul Fish DIAMOND",
            itemDisplayName = "${EPIC}Soul Fish ${AQUA}${BOLD}DIAMOND",
            amountOfMagmaFish = 96,
        )
        
        val SKELETON_FISH_BRONZE = TrophyFishInfo(
            itemId = "SKELETON_FISH_BRONZE",
            itemName = "Skeleton Fish BRONZE",
            itemDisplayName = "${EPIC}Skeleton Fish ${DARK_GRAY}${BOLD}BRONZE",
            amountOfMagmaFish = 32,
        )
        
        val SKELETON_FISH_SILVER = TrophyFishInfo(
            itemId = "SKELETON_FISH_SILVER",
            itemName = "Skeleton Fish SILVER",
            itemDisplayName = "${EPIC}Skeleton Fish ${GRAY}${BOLD}SILVER",
            amountOfMagmaFish = 48,
        )
        
        val SKELETON_FISH_GOLD = TrophyFishInfo(
            itemId = "SKELETON_FISH_GOLD",
            itemName = "Skeleton Fish GOLD",
            itemDisplayName = "${EPIC}Skeleton Fish ${GOLD}${BOLD}GOLD",
            amountOfMagmaFish = 64,
        )
        
        val SKELETON_FISH_DIAMOND = TrophyFishInfo(
            itemId = "SKELETON_FISH_DIAMOND",
            itemName = "Skeleton Fish DIAMOND",
            itemDisplayName = "${EPIC}Skeleton Fish ${AQUA}${BOLD}DIAMOND",
            amountOfMagmaFish = 96,
        )
        
        val VOLCANIC_STONEFISH_BRONZE = TrophyFishInfo(
            itemId = "VOLCANIC_STONEFISH_BRONZE",
            itemName = "Volcanic Stonefish BRONZE",
            itemDisplayName = "${RARE}Volcanic Stonefish ${DARK_GRAY}${BOLD}BRONZE",
            amountOfMagmaFish = 20,
        )
        
        val VOLCANIC_STONEFISH_SILVER = TrophyFishInfo(
            itemId = "VOLCANIC_STONEFISH_SILVER",
            itemName = "Volcanic Stonefish SILVER",
            itemDisplayName = "${RARE}Volcanic Stonefish ${GRAY}${BOLD}SILVER",
            amountOfMagmaFish = 28,
        )
        
        val VOLCANIC_STONEFISH_GOLD = TrophyFishInfo(
            itemId = "VOLCANIC_STONEFISH_GOLD",
            itemName = "Volcanic Stonefish GOLD",
            itemDisplayName = "${RARE}Volcanic Stonefish ${GOLD}${BOLD}GOLD",
            amountOfMagmaFish = 40,
        )
        
        val VOLCANIC_STONEFISH_DIAMOND = TrophyFishInfo(
            itemId = "VOLCANIC_STONEFISH_DIAMOND",
            itemName = "Volcanic Stonefish DIAMOND",
            itemDisplayName = "${RARE}Volcanic Stonefish ${AQUA}${BOLD}DIAMOND",
            amountOfMagmaFish = 60,
        )
        
        val GOLDEN_FISH_BRONZE = TrophyFishInfo(
            itemId = "GOLDEN_FISH_BRONZE",
            itemName = "Golden Fish BRONZE",
            itemDisplayName = "${LEGENDARY}Golden Fish ${DARK_GRAY}${BOLD}BRONZE",
            amountOfMagmaFish = 400,
        )
        
        val GOLDEN_FISH_SILVER = TrophyFishInfo(
            itemId = "GOLDEN_FISH_SILVER",
            itemName = "Golden Fish SILVER",
            itemDisplayName = "${LEGENDARY}Golden Fish ${GRAY}${BOLD}SILVER",
            amountOfMagmaFish = 700,
        )
        
        val GOLDEN_FISH_GOLD = TrophyFishInfo(
            itemId = "GOLDEN_FISH_GOLD",
            itemName = "Golden Fish GOLD",
            itemDisplayName = "${LEGENDARY}Golden Fish ${GOLD}${BOLD}GOLD",
            amountOfMagmaFish = 1000,
        )
        
        val GOLDEN_FISH_DIAMOND = TrophyFishInfo(
            itemId = "GOLDEN_FISH_DIAMOND",
            itemName = "Golden Fish DIAMOND",
            itemDisplayName = "${LEGENDARY}Golden Fish ${AQUA}${BOLD}DIAMOND",
            amountOfMagmaFish = 1300,
        )
        
        val ALL_TROPHY_FISH = listOf(
            OBFUSCATED_1_BRONZE,
            OBFUSCATED_1_SILVER,
            OBFUSCATED_1_GOLD,
            OBFUSCATED_1_DIAMOND,
            OBFUSCATED_2_BRONZE,
            OBFUSCATED_2_SILVER,
            OBFUSCATED_2_GOLD,
            OBFUSCATED_2_DIAMOND,
            OBFUSCATED_3_BRONZE,
            OBFUSCATED_3_SILVER,
            OBFUSCATED_3_GOLD,
            OBFUSCATED_3_DIAMOND,
            BLOBFISH_BRONZE,
            BLOBFISH_SILVER,
            BLOBFISH_GOLD,
            BLOBFISH_DIAMOND,
            GUSHER_BRONZE,
            GUSHER_SILVER,
            GUSHER_GOLD,
            GUSHER_DIAMOND,
            STEAMING_HOT_FLOUNDER_BRONZE,
            STEAMING_HOT_FLOUNDER_SILVER,
            STEAMING_HOT_FLOUNDER_GOLD,
            STEAMING_HOT_FLOUNDER_DIAMOND,
            SULPHUR_SKITTER_BRONZE,
            SULPHUR_SKITTER_SILVER,
            SULPHUR_SKITTER_GOLD,
            SULPHUR_SKITTER_DIAMOND,
            FLYFISH_BRONZE,
            FLYFISH_SILVER,
            FLYFISH_GOLD,
            FLYFISH_DIAMOND,
            SLUGFISH_BRONZE,
            SLUGFISH_SILVER,
            SLUGFISH_GOLD,
            SLUGFISH_DIAMOND,
            LAVAHORSE_BRONZE,
            LAVAHORSE_SILVER,
            LAVAHORSE_GOLD,
            LAVAHORSE_DIAMOND,
            MANA_RAY_BRONZE,
            MANA_RAY_SILVER,
            MANA_RAY_GOLD,
            MANA_RAY_DIAMOND,
            VANILLE_BRONZE,
            VANILLE_SILVER,
            VANILLE_GOLD,
            VANILLE_DIAMOND,
            KARATE_FISH_BRONZE,
            KARATE_FISH_SILVER,
            KARATE_FISH_GOLD,
            KARATE_FISH_DIAMOND,
            MOLDFIN_BRONZE,
            MOLDFIN_SILVER,
            MOLDFIN_GOLD,
            MOLDFIN_DIAMOND,
            SOUL_FISH_BRONZE,
            SOUL_FISH_SILVER,
            SOUL_FISH_GOLD,
            SOUL_FISH_DIAMOND,
            SKELETON_FISH_BRONZE,
            SKELETON_FISH_SILVER,
            SKELETON_FISH_GOLD,
            SKELETON_FISH_DIAMOND,
            VOLCANIC_STONEFISH_BRONZE,
            VOLCANIC_STONEFISH_SILVER,
            VOLCANIC_STONEFISH_GOLD,
            VOLCANIC_STONEFISH_DIAMOND,
            GOLDEN_FISH_BRONZE,
            GOLDEN_FISH_SILVER,
            GOLDEN_FISH_GOLD,
            GOLDEN_FISH_DIAMOND
        )

        val ALL_TROPHY_FISH_BY_ID = ALL_TROPHY_FISH.associateBy { it.itemId }
    }
}