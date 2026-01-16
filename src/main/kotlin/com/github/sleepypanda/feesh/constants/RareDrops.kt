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

    LEGENDARY_MEGALODON_PET("Megalodon (Legendary)"),
    EPIC_MEGALODON_PET("Megalodon (Epic)"),
    LEGENDARY_BABY_YETI_PET("Baby Yeti (Legendary)"),
    LEGENDARY_FLYING_FISH_PET("Flying Fish (Legendary)"),
    LEGENDARY_SQUID_PET("Squid (Legendary)"),
    EPIC_SQUID_PET("Squid (Epic)"),
    RARE_SQUID_PET("Squid (Rare)"),
    UNCOMMON_SQUID_PET("Squid (Uncommon)"),
    COMMON_SQUID_PET("Squid (Common)"),

    PHOENIX_PET("Phoenix"),
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
        data class RareDropInfo(val itemName: String, val id: String, val rarityColorCode: String, val isExtremelyRare: Boolean) {
            val displayName: String get() = rarityColorCode + itemName
            val boldDisplayName: String get() = rarityColorCode + BOLD + itemName

            fun getTitle(): String {
                val baseTitle = "${this.boldDisplayName}"
                return if (this.isExtremelyRare) "${GOLD}${OBFUSCATED}x${RESET} ${baseTitle} ${GOLD}${OBFUSCATED}x${RESET}" 
                else "${baseTitle}"
            }
        }

        val rareDrops = listOf(
            RareDropInfo(RareDropTypes.LUCKY_CLOVER_CORE.displayName, "PET_ITEM_LUCKY_CLOVER_DROP", EPIC.code, false),
            RareDropInfo(RareDropTypes.DEEP_SEA_ORB.displayName, "DEEP_SEA_ORB", EPIC.code, false),
            RareDropInfo(RareDropTypes.RADIOACTIVE_VIAL.displayName, "RADIOACTIVE_VIAL", MYTHIC.code, true),
            RareDropInfo(RareDropTypes.MAGMA_CORE.displayName, "MAGMA_CORE", RARE.code, false),
            RareDropInfo(RareDropTypes.TIKI_MASK.displayName, "TIKI_MASK", LEGENDARY.code, true),
            RareDropInfo(RareDropTypes.TITANOBOA_SHED.displayName, "TITANOBOA_SHED", LEGENDARY.code, true),
            RareDropInfo(RareDropTypes.SCUTTLER_SHELL.displayName, "SCUTTLER_SHELL", LEGENDARY.code, false),
            RareDropInfo(RareDropTypes.BURNT_TEXTS.displayName, "BURNT_TEXTS", LEGENDARY.code, false),
            RareDropInfo(RareDropTypes.LEGENDARY_BABY_YETI_PET.displayName, "BABY_YETI;4", LEGENDARY.code, false),
            RareDropInfo(RareDropTypes.LEGENDARY_FLYING_FISH_PET.displayName, "FLYING_FISH;4", LEGENDARY.code, false),
            RareDropInfo(RareDropTypes.LEGENDARY_MEGALODON_PET.displayName, "MEGALODON;4", LEGENDARY.code, false),
            RareDropInfo(RareDropTypes.EPIC_MEGALODON_PET.displayName, "MEGALODON;3", EPIC.code, false),
            RareDropInfo(RareDropTypes.LEGENDARY_SQUID_PET.displayName, "SQUID;4", EPIC.code, false),
            RareDropInfo(RareDropTypes.EPIC_SQUID_PET.displayName, "SQUID;3", EPIC.code, false),
            RareDropInfo(RareDropTypes.RARE_SQUID_PET.displayName, "SQUID;2", EPIC.code, false),
            RareDropInfo(RareDropTypes.UNCOMMON_SQUID_PET.displayName, "SQUID;1", EPIC.code, false),
            RareDropInfo(RareDropTypes.COMMON_SQUID_PET.displayName, "SQUID;0", EPIC.code, false),
            RareDropInfo(RareDropTypes.PHOENIX_PET.displayName, "PHOENIX;?", SPECIAL.code, true),
            RareDropInfo(RareDropTypes.CARMINE_DYE.displayName, "DYE_CARMINE", DARK_RED.code, true),
            RareDropInfo(RareDropTypes.MIDNIGHT_DYE.displayName, "DYE_MIDNIGHT", DARK_PURPLE.code, true),
            RareDropInfo(RareDropTypes.AQUAMARINE_DYE.displayName, "DYE_AQUAMARINE", AQUA.code, true),
            RareDropInfo(RareDropTypes.ICEBERG_DYE.displayName, "DYE_ICEBERG", DARK_AQUA.code, true),
            RareDropInfo(RareDropTypes.TREASURE_DYE.displayName, "DYE_TREASURE", GOLD.code, true),
            RareDropInfo(RareDropTypes.PERIWINKLE_DYE.displayName, "DYE_PERIWINKLE", DARK_AQUA.code, true),
            RareDropInfo(RareDropTypes.BONE_DYE.displayName, "DYE_BONE", WHITE.code, true),
        )
    }
}