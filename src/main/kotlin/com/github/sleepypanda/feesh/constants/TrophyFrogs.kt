package com.github.sleepypanda.feesh.constants

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

data class TrophyFrogInfo(
    val itemId: String,
    val itemName: String,
    val itemDisplayName: String,
    val amountOfLotus: Int,
)

class TrophyFrogs {
    companion object {
        // https://hypixelskyblock.minecraft.wiki/w/Trophy_Frogs#Donating

        val COMMON_FROG_BRONZE = TrophyFrogInfo(
            itemId = "COMMON_FROG_BRONZE",
            itemName = "Common Frog BRONZE",
            itemDisplayName = "${COMMON}Common Frog ${DARK_GRAY}${BOLD}BRONZE",
            amountOfLotus = 8,
        )

        val COMMON_FROG_SILVER = TrophyFrogInfo(
            itemId = "COMMON_FROG_SILVER",
            itemName = "Common Frog SILVER",
            itemDisplayName = "${COMMON}Common Frog ${GRAY}${BOLD}SILVER",
            amountOfLotus = 16,
        )

        val COMMON_FROG_GOLD = TrophyFrogInfo(
            itemId = "COMMON_FROG_GOLD",
            itemName = "Common Frog GOLD",
            itemDisplayName = "${COMMON}Common Frog ${GOLD}${BOLD}GOLD",
            amountOfLotus = 32,
        )

        val COMMON_FROG_DIAMOND = TrophyFrogInfo(
            itemId = "COMMON_FROG_DIAMOND",
            itemName = "Common Frog DIAMOND",
            itemDisplayName = "${COMMON}Common Frog ${AQUA}${BOLD}DIAMOND",
            amountOfLotus = 64,
        )

        val LEAP_FROG_BRONZE = TrophyFrogInfo(
            itemId = "LEAP_FROG_BRONZE",
            itemName = "Leap Frog BRONZE",
            itemDisplayName = "${COMMON}Leap Frog ${DARK_GRAY}${BOLD}BRONZE",
            amountOfLotus = 24,
        )

        val LEAP_FROG_SILVER = TrophyFrogInfo(
            itemId = "LEAP_FROG_SILVER",
            itemName = "Leap Frog SILVER",
            itemDisplayName = "${COMMON}Leap Frog ${GRAY}${BOLD}SILVER",
            amountOfLotus = 48,
        )

        val LEAP_FROG_GOLD = TrophyFrogInfo(
            itemId = "LEAP_FROG_GOLD",
            itemName = "Leap Frog GOLD",
            itemDisplayName = "${COMMON}Leap Frog ${GOLD}${BOLD}GOLD",
            amountOfLotus = 96,
        )

        val LEAP_FROG_DIAMOND = TrophyFrogInfo(
            itemId = "LEAP_FROG_DIAMOND",
            itemName = "Leap Frog DIAMOND",
            itemDisplayName = "${COMMON}Leap Frog ${AQUA}${BOLD}DIAMOND",
            amountOfLotus = 192,
        )

        val WETLANDS_FROG_BRONZE = TrophyFrogInfo(
            itemId = "WETLANDS_FROG_BRONZE",
            itemName = "Wetlands Frog BRONZE",
            itemDisplayName = "${UNCOMMON}Wetlands Frog ${DARK_GRAY}${BOLD}BRONZE",
            amountOfLotus = 20,
        )

        val WETLANDS_FROG_SILVER = TrophyFrogInfo(
            itemId = "WETLANDS_FROG_SILVER",
            itemName = "Wetlands Frog SILVER",
            itemDisplayName = "${UNCOMMON}Wetlands Frog ${GRAY}${BOLD}SILVER",
            amountOfLotus = 40,
        )

        val WETLANDS_FROG_GOLD = TrophyFrogInfo(
            itemId = "WETLANDS_FROG_GOLD",
            itemName = "Wetlands Frog GOLD",
            itemDisplayName = "${UNCOMMON}Wetlands Frog ${GOLD}${BOLD}GOLD",
            amountOfLotus = 80,
        )

        val WETLANDS_FROG_DIAMOND = TrophyFrogInfo(
            itemId = "WETLANDS_FROG_DIAMOND",
            itemName = "Wetlands Frog DIAMOND",
            itemDisplayName = "${UNCOMMON}Wetlands Frog ${AQUA}${BOLD}DIAMOND",
            amountOfLotus = 160,
        )

        val REALITY_HOPPER_BRONZE = TrophyFrogInfo(
            itemId = "REALITY_HOPPER_BRONZE",
            itemName = "Reality Hopper BRONZE",
            itemDisplayName = "${UNCOMMON}Reality Hopper ${DARK_GRAY}${BOLD}BRONZE",
            amountOfLotus = 20,
        )

        val REALITY_HOPPER_SILVER = TrophyFrogInfo(
            itemId = "REALITY_HOPPER_SILVER",
            itemName = "Reality Hopper SILVER",
            itemDisplayName = "${UNCOMMON}Reality Hopper ${GRAY}${BOLD}SILVER",
            amountOfLotus = 40,
        )

        val REALITY_HOPPER_GOLD = TrophyFrogInfo(
            itemId = "REALITY_HOPPER_GOLD",
            itemName = "Reality Hopper GOLD",
            itemDisplayName = "${UNCOMMON}Reality Hopper ${GOLD}${BOLD}GOLD",
            amountOfLotus = 80,
        )

        val REALITY_HOPPER_DIAMOND = TrophyFrogInfo(
            itemId = "REALITY_HOPPER_DIAMOND",
            itemName = "Reality Hopper DIAMOND",
            itemDisplayName = "${UNCOMMON}Reality Hopper ${AQUA}${BOLD}DIAMOND",
            amountOfLotus = 160,
        )

        val EXPLODING_FROG_BRONZE = TrophyFrogInfo(
            itemId = "EXPLODING_FROG_BRONZE",
            itemName = "Exploding Frog BRONZE",
            itemDisplayName = "${UNCOMMON}Exploding Frog ${DARK_GRAY}${BOLD}BRONZE",
            amountOfLotus = 12,
        )

        val EXPLODING_FROG_SILVER = TrophyFrogInfo(
            itemId = "EXPLODING_FROG_SILVER",
            itemName = "Exploding Frog SILVER",
            itemDisplayName = "${UNCOMMON}Exploding Frog ${GRAY}${BOLD}SILVER",
            amountOfLotus = 24,
        )

        val EXPLODING_FROG_GOLD = TrophyFrogInfo(
            itemId = "EXPLODING_FROG_GOLD",
            itemName = "Exploding Frog GOLD",
            itemDisplayName = "${UNCOMMON}Exploding Frog ${GOLD}${BOLD}GOLD",
            amountOfLotus = 48,
        )

        val EXPLODING_FROG_DIAMOND = TrophyFrogInfo(
            itemId = "EXPLODING_FROG_DIAMOND",
            itemName = "Exploding Frog DIAMOND",
            itemDisplayName = "${UNCOMMON}Exploding Frog ${AQUA}${BOLD}DIAMOND",
            amountOfLotus = 96,
        )

        val BLESSED_FROG_BRONZE = TrophyFrogInfo(
            itemId = "BLESSED_FROG_BRONZE",
            itemName = "Blessed Frog BRONZE",
            itemDisplayName = "${RARE}Blessed Frog ${DARK_GRAY}${BOLD}BRONZE",
            amountOfLotus = 32,
        )

        val BLESSED_FROG_SILVER = TrophyFrogInfo(
            itemId = "BLESSED_FROG_SILVER",
            itemName = "Blessed Frog SILVER",
            itemDisplayName = "${RARE}Blessed Frog ${GRAY}${BOLD}SILVER",
            amountOfLotus = 64,
        )

        val BLESSED_FROG_GOLD = TrophyFrogInfo(
            itemId = "BLESSED_FROG_GOLD",
            itemName = "Blessed Frog GOLD",
            itemDisplayName = "${RARE}Blessed Frog ${GOLD}${BOLD}GOLD",
            amountOfLotus = 128,
        )

        val BLESSED_FROG_DIAMOND = TrophyFrogInfo(
            itemId = "BLESSED_FROG_DIAMOND",
            itemName = "Blessed Frog DIAMOND",
            itemDisplayName = "${RARE}Blessed Frog ${AQUA}${BOLD}DIAMOND",
            amountOfLotus = 256,
        )

        val SEA_FROG_BRONZE = TrophyFrogInfo(
            itemId = "SEA_FROG_BRONZE",
            itemName = "Sea Frog BRONZE",
            itemDisplayName = "${RARE}Sea Frog ${DARK_GRAY}${BOLD}BRONZE",
            amountOfLotus = 40,
        )

        val SEA_FROG_SILVER = TrophyFrogInfo(
            itemId = "SEA_FROG_SILVER",
            itemName = "Sea Frog SILVER",
            itemDisplayName = "${RARE}Sea Frog ${GRAY}${BOLD}SILVER",
            amountOfLotus = 80,
        )

        val SEA_FROG_GOLD = TrophyFrogInfo(
            itemId = "SEA_FROG_GOLD",
            itemName = "Sea Frog GOLD",
            itemDisplayName = "${RARE}Sea Frog ${GOLD}${BOLD}GOLD",
            amountOfLotus = 160,
        )

        val SEA_FROG_DIAMOND = TrophyFrogInfo(
            itemId = "SEA_FROG_DIAMOND",
            itemName = "Sea Frog DIAMOND",
            itemDisplayName = "${RARE}Sea Frog ${AQUA}${BOLD}DIAMOND",
            amountOfLotus = 320,
        )

        val BULLFROG_BRONZE = TrophyFrogInfo(
            itemId = "BULLFROG_BRONZE",
            itemName = "Bullfrog BRONZE",
            itemDisplayName = "${RARE}Bullfrog ${DARK_GRAY}${BOLD}BRONZE",
            amountOfLotus = 40,
        )

        val BULLFROG_SILVER = TrophyFrogInfo(
            itemId = "BULLFROG_SILVER",
            itemName = "Bullfrog SILVER",
            itemDisplayName = "${RARE}Bullfrog ${GRAY}${BOLD}SILVER",
            amountOfLotus = 80,
        )

        val BULLFROG_GOLD = TrophyFrogInfo(
            itemId = "BULLFROG_GOLD",
            itemName = "Bullfrog GOLD",
            itemDisplayName = "${RARE}Bullfrog ${GOLD}${BOLD}GOLD",
            amountOfLotus = 160,
        )

        val BULLFROG_DIAMOND = TrophyFrogInfo(
            itemId = "BULLFROG_DIAMOND",
            itemName = "Bullfrog DIAMOND",
            itemDisplayName = "${RARE}Bullfrog ${AQUA}${BOLD}DIAMOND",
            amountOfLotus = 320,
        )

        val TREE_FROG_BRONZE = TrophyFrogInfo(
            itemId = "TREE_FROG_BRONZE",
            itemName = "Tree Frog BRONZE",
            itemDisplayName = "${EPIC}Tree Frog ${DARK_GRAY}${BOLD}BRONZE",
            amountOfLotus = 80,
        )

        val TREE_FROG_SILVER = TrophyFrogInfo(
            itemId = "TREE_FROG_SILVER",
            itemName = "Tree Frog SILVER",
            itemDisplayName = "${EPIC}Tree Frog ${GRAY}${BOLD}SILVER",
            amountOfLotus = 160,
        )

        val TREE_FROG_GOLD = TrophyFrogInfo(
            itemId = "TREE_FROG_GOLD",
            itemName = "Tree Frog GOLD",
            itemDisplayName = "${EPIC}Tree Frog ${GOLD}${BOLD}GOLD",
            amountOfLotus = 320,
        )

        val TREE_FROG_DIAMOND = TrophyFrogInfo(
            itemId = "TREE_FROG_DIAMOND",
            itemName = "Tree Frog DIAMOND",
            itemDisplayName = "${EPIC}Tree Frog ${AQUA}${BOLD}DIAMOND",
            amountOfLotus = 640,
        )

        val CAVE_FROG_BRONZE = TrophyFrogInfo(
            itemId = "CAVE_FROG_BRONZE",
            itemName = "Cave Frog BRONZE",
            itemDisplayName = "${EPIC}Cave Frog ${DARK_GRAY}${BOLD}BRONZE",
            amountOfLotus = 80,
        )

        val CAVE_FROG_SILVER = TrophyFrogInfo(
            itemId = "CAVE_FROG_SILVER",
            itemName = "Cave Frog SILVER",
            itemDisplayName = "${EPIC}Cave Frog ${GRAY}${BOLD}SILVER",
            amountOfLotus = 160,
        )

        val CAVE_FROG_GOLD = TrophyFrogInfo(
            itemId = "CAVE_FROG_GOLD",
            itemName = "Cave Frog GOLD",
            itemDisplayName = "${EPIC}Cave Frog ${GOLD}${BOLD}GOLD",
            amountOfLotus = 320,
        )

        val CAVE_FROG_DIAMOND = TrophyFrogInfo(
            itemId = "CAVE_FROG_DIAMOND",
            itemName = "Cave Frog DIAMOND",
            itemDisplayName = "${EPIC}Cave Frog ${AQUA}${BOLD}DIAMOND",
            amountOfLotus = 640,
        )

        val HIGHLANDS_FROG_BRONZE = TrophyFrogInfo(
            itemId = "HIGHLANDS_FROG_BRONZE",
            itemName = "Highlands Frog BRONZE",
            itemDisplayName = "${EPIC}Highlands Frog ${DARK_GRAY}${BOLD}BRONZE",
            amountOfLotus = 80,
        )

        val HIGHLANDS_FROG_SILVER = TrophyFrogInfo(
            itemId = "HIGHLANDS_FROG_SILVER",
            itemName = "Highlands Frog SILVER",
            itemDisplayName = "${EPIC}Highlands Frog ${GRAY}${BOLD}SILVER",
            amountOfLotus = 160,
        )

        val HIGHLANDS_FROG_GOLD = TrophyFrogInfo(
            itemId = "HIGHLANDS_FROG_GOLD",
            itemName = "Highlands Frog GOLD",
            itemDisplayName = "${EPIC}Highlands Frog ${GOLD}${BOLD}GOLD",
            amountOfLotus = 320,
        )

        val HIGHLANDS_FROG_DIAMOND = TrophyFrogInfo(
            itemId = "HIGHLANDS_FROG_DIAMOND",
            itemName = "Highlands Frog DIAMOND",
            itemDisplayName = "${EPIC}Highlands Frog ${AQUA}${BOLD}DIAMOND",
            amountOfLotus = 640,
        )

        val PUDDLE_JUMPER_BRONZE = TrophyFrogInfo(
            itemId = "PUDDLE_JUMPER_BRONZE",
            itemName = "Puddle Jumper BRONZE",
            itemDisplayName = "${LEGENDARY}Puddle Jumper ${DARK_GRAY}${BOLD}BRONZE",
            amountOfLotus = 128,
        )

        val PUDDLE_JUMPER_SILVER = TrophyFrogInfo(
            itemId = "PUDDLE_JUMPER_SILVER",
            itemName = "Puddle Jumper SILVER",
            itemDisplayName = "${LEGENDARY}Puddle Jumper ${GRAY}${BOLD}SILVER",
            amountOfLotus = 192,
        )

        val PUDDLE_JUMPER_GOLD = TrophyFrogInfo(
            itemId = "PUDDLE_JUMPER_GOLD",
            itemName = "Puddle Jumper GOLD",
            itemDisplayName = "${LEGENDARY}Puddle Jumper ${GOLD}${BOLD}GOLD",
            amountOfLotus = 256,
        )

        val PUDDLE_JUMPER_DIAMOND = TrophyFrogInfo(
            itemId = "PUDDLE_JUMPER_DIAMOND",
            itemName = "Puddle Jumper DIAMOND",
            itemDisplayName = "${LEGENDARY}Puddle Jumper ${AQUA}${BOLD}DIAMOND",
            amountOfLotus = 512,
        )

        val ALL_TROPHY_FROGS = listOf(
            COMMON_FROG_BRONZE,
            COMMON_FROG_SILVER,
            COMMON_FROG_GOLD,
            COMMON_FROG_DIAMOND,
            LEAP_FROG_BRONZE,
            LEAP_FROG_SILVER,
            LEAP_FROG_GOLD,
            LEAP_FROG_DIAMOND,
            WETLANDS_FROG_BRONZE,
            WETLANDS_FROG_SILVER,
            WETLANDS_FROG_GOLD,
            WETLANDS_FROG_DIAMOND,
            REALITY_HOPPER_BRONZE,
            REALITY_HOPPER_SILVER,
            REALITY_HOPPER_GOLD,
            REALITY_HOPPER_DIAMOND,
            EXPLODING_FROG_BRONZE,
            EXPLODING_FROG_SILVER,
            EXPLODING_FROG_GOLD,
            EXPLODING_FROG_DIAMOND,
            BLESSED_FROG_BRONZE,
            BLESSED_FROG_SILVER,
            BLESSED_FROG_GOLD,
            BLESSED_FROG_DIAMOND,
            SEA_FROG_BRONZE,
            SEA_FROG_SILVER,
            SEA_FROG_GOLD,
            SEA_FROG_DIAMOND,
            BULLFROG_BRONZE,
            BULLFROG_SILVER,
            BULLFROG_GOLD,
            BULLFROG_DIAMOND,
            TREE_FROG_BRONZE,
            TREE_FROG_SILVER,
            TREE_FROG_GOLD,
            TREE_FROG_DIAMOND,
            CAVE_FROG_BRONZE,
            CAVE_FROG_SILVER,
            CAVE_FROG_GOLD,
            CAVE_FROG_DIAMOND,
            HIGHLANDS_FROG_BRONZE,
            HIGHLANDS_FROG_SILVER,
            HIGHLANDS_FROG_GOLD,
            HIGHLANDS_FROG_DIAMOND,
            PUDDLE_JUMPER_BRONZE,
            PUDDLE_JUMPER_SILVER,
            PUDDLE_JUMPER_GOLD,
            PUDDLE_JUMPER_DIAMOND,
        )

        val ALL_TROPHY_FROGS_BY_ID = ALL_TROPHY_FROGS.associateBy { it.itemId }
    }
}
