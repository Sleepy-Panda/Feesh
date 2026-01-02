package com.github.sleepypanda.feesh.constants

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

enum class RareDropTypes {
    LUCKY_CLOVER_CORE,
    DEEP_SEA_ORB,
    RADIOACTIVE_VIAL,
    MAGMA_CORE,
    TIKI_MASK,
    TITANOBOA_SHED,
    SCUTTLER_SHELL,
    BURNT_TEXTS,
    //SQUID_PET,
    //PHOENIX_PET,
    //MEGALODON_PET,
    //LEGENDARY_BABY_YETI_PET,
    //LEGENDARY_FLYING_FISH_PET,
    //CARMINE_DYE,
    //AQUAMARINE_DYE,
    //ICEBERG_DYE,
    //MIDNIGHT_DYE,
    //TREASURE_DYE,
    //PERIWINKLE_DYE,
    //BONE_DYE,
}

class RareDrops {
    companion object {
        data class RareDropInfo(val itemName: String, val id: String, val rarityColorCode: String) {
            val displayName: String get() = rarityColorCode + itemName
            val boldDisplayName: String get() = rarityColorCode + BOLD + itemName
        }

        val rareDrops = listOf(
            RareDropInfo("Lucky Clover Core", "PET_ITEM_LUCKY_CLOVER_DROP", EPIC.code),
            RareDropInfo("Deep Sea Orb", "DEEP_SEA_ORB", EPIC.code),
            RareDropInfo("Radioactive Vial", "RADIOACTIVE_VIAL", MYTHIC.code),
            RareDropInfo("Magma Core", "MAGMA_CORE", RARE.code),
            RareDropInfo("Tiki Mask", "TIKI_MASK", LEGENDARY.code),
            RareDropInfo("Titanoboa Shed", "TITANOBOA_SHED", LEGENDARY.code),
            RareDropInfo("Scuttler Shell", "SCUTTLER_SHELL", LEGENDARY.code),
            RareDropInfo("Burnt Texts", "BURNT_TEXTS", LEGENDARY.code),
        )
    }
}