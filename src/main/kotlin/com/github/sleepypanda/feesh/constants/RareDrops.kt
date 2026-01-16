package com.github.sleepypanda.feesh.constants

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

// This should be aligned with Rare Drops names using the following logic:
// Squid (Legendary) -> SQUID_LEGENDARY
// Deep Sea Orb -> DEEP_SEA_ORB
enum class RareDropTypes(val displayName: String) {
    LUCKY_CLOVER_CORE("Lucky Clover Core"),
    DEEP_SEA_ORB("Deep Sea Orb"),
    RADIOACTIVE_VIAL("Radioactive Vial"),
    MAGMA_CORE("Magma Core"),
    TIKI_MASK("Tiki Mask"),
    TITANOBOA_SHED("Titanoboa Shed"),
    SCUTTLER_SHELL("Scuttler Shell"),
    BURNT_TEXTS("Burnt Texts"),
    //SQUID_PET,
    LEGENDARY_MEGALODON_PET("Megalodon (Legendary)"),
    EPIC_MEGALODON_PET("Megalodon (Epic)"),
    PHOENIX_PET("Phoenix"),
    LEGENDARY_BABY_YETI_PET("Baby Yeti (Legendary)"),
    LEGENDARY_FLYING_FISH_PET("Flying Fish (Legendary)"),
    CARMINE_DYE("Carmine Dye"),
    AQUAMARINE_DYE("Aquamarine Dye"),
    ICEBERG_DYE("Iceberg Dye"),
    MIDNIGHT_DYE("Midnight Dye"),
    TREASURE_DYE("Treasure Dye"),
    PERIWINKLE_DYE("Periwinkle Dye"),
    BONE_DYE("Bone Dye");

    override fun toString(): String = displayName // Show display name in UI, but internally it uses name
}

class RareDrops {
    companion object {
        data class RareDropInfo(val itemName: String, val id: String, val rarityColorCode: String) {
            val displayName: String get() = rarityColorCode + itemName
            val boldDisplayName: String get() = rarityColorCode + BOLD + itemName

            // TODO: Separate display name for extremely rare drops (Dye, Mythic, etc.)
        }

        val rareDrops = listOf(
            RareDropInfo(RareDropTypes.LUCKY_CLOVER_CORE.displayName, "PET_ITEM_LUCKY_CLOVER_DROP", EPIC.code),
            RareDropInfo(RareDropTypes.DEEP_SEA_ORB.displayName, "DEEP_SEA_ORB", EPIC.code),
            RareDropInfo(RareDropTypes.RADIOACTIVE_VIAL.displayName, "RADIOACTIVE_VIAL", MYTHIC.code),
            RareDropInfo(RareDropTypes.MAGMA_CORE.displayName, "MAGMA_CORE", RARE.code),
            RareDropInfo(RareDropTypes.TIKI_MASK.displayName, "TIKI_MASK", LEGENDARY.code),
            RareDropInfo(RareDropTypes.TITANOBOA_SHED.displayName, "TITANOBOA_SHED", LEGENDARY.code),
            RareDropInfo(RareDropTypes.SCUTTLER_SHELL.displayName, "SCUTTLER_SHELL", LEGENDARY.code),
            RareDropInfo(RareDropTypes.BURNT_TEXTS.displayName, "BURNT_TEXTS", LEGENDARY.code),
            RareDropInfo(RareDropTypes.LEGENDARY_BABY_YETI_PET.displayName, "BABY_YETI;4", LEGENDARY.code),
            RareDropInfo(RareDropTypes.LEGENDARY_FLYING_FISH_PET.displayName, "FLYING_FISH;4", LEGENDARY.code),
            RareDropInfo(RareDropTypes.LEGENDARY_MEGALODON_PET.displayName, "MEGALODON;4", LEGENDARY.code),
            RareDropInfo(RareDropTypes.EPIC_MEGALODON_PET.displayName, "MEGALODON;3", EPIC.code),
            RareDropInfo("Squid (Legendary)", "SQUID;4", EPIC.code),
            RareDropInfo("Squid (Epic)", "SQUID;3", EPIC.code),
            RareDropInfo("Squid (Rare)", "SQUID;2", EPIC.code),
            RareDropInfo("Squid (Uncommon)", "SQUID;1", EPIC.code),
            RareDropInfo("Squid (Common)", "SQUID;0", EPIC.code),
            RareDropInfo(RareDropTypes.PHOENIX_PET.displayName, "PHOENIX;?", SPECIAL.code),
            RareDropInfo(RareDropTypes.CARMINE_DYE.displayName, "DYE_CARMINE", DARK_RED.code),
            RareDropInfo(RareDropTypes.MIDNIGHT_DYE.displayName, "DYE_MIDNIGHT", DARK_PURPLE.code),
            RareDropInfo(RareDropTypes.AQUAMARINE_DYE.displayName, "DYE_AQUAMARINE", AQUA.code),
            RareDropInfo(RareDropTypes.ICEBERG_DYE.displayName, "DYE_ICEBERG", DARK_AQUA.code),
            RareDropInfo(RareDropTypes.TREASURE_DYE.displayName, "DYE_TREASURE", GOLD.code),
            RareDropInfo(RareDropTypes.PERIWINKLE_DYE.displayName, "DYE_PERIWINKLE", DARK_AQUA.code),
            RareDropInfo(RareDropTypes.BONE_DYE.displayName, "DYE_BONE", WHITE.code),
        )
    }
}