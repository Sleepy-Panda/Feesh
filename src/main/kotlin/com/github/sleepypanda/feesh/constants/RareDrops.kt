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

    MEGALODON_LEGENDARY("Megalodon (Legendary)"),
    MEGALODON_EPIC("Megalodon (Epic)"),
    BABY_YETI_LEGENDARY("Baby Yeti (Legendary)"),
    FLYING_FISH_LEGENDARY("Flying Fish (Legendary)"),
    SQUID_LEGENDARY("Squid (Legendary)"),
    SQUID_EPIC("Squid (Epic)"),
    SQUID_RARE("Squid (Rare)"),
    SQUID_UNCOMMON("Squid (Uncommon)"),
    SQUID_COMMON("Squid (Common)"),

    PHOENIX("Phoenix"),
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
        data class RareDropInfo(val itemName: String, val id: String, val rarityColorCode: String, val isExtremelyRare: Boolean, val defaultSoundFileName: String) {
            val displayName: String get() = rarityColorCode + itemName
            val boldDisplayName: String get() = rarityColorCode + BOLD + itemName

            fun getTitle(): String {
                val baseTitle = this.boldDisplayName.substringBefore(" (") // Baby Yeti (Legendary) -> Baby Yeti
                return if (this.isExtremelyRare) "${GOLD}${OBFUSCATED}x${RESET} ${baseTitle} ${GOLD}${OBFUSCATED}x${RESET}" 
                else "${baseTitle}"
            }
        }

        val rareDrops = listOf(
            RareDropInfo(RareDropTypes.LUCKY_CLOVER_CORE.displayName, "PET_ITEM_LUCKY_CLOVER_DROP", EPIC.code, false, Sounds.FEESH_OH_MY_GOD),
            RareDropInfo(RareDropTypes.DEEP_SEA_ORB.displayName, "DEEP_SEA_ORB", EPIC.code, false, Sounds.FEESH_OH_MY_GOD),
            RareDropInfo(RareDropTypes.RADIOACTIVE_VIAL.displayName, "RADIOACTIVE_VIAL", MYTHIC.code, true, Sounds.FEESH_MINECRAFT_CHALLENGE_COMPLETED),
            RareDropInfo(RareDropTypes.MAGMA_CORE.displayName, "MAGMA_CORE", RARE.code, false, Sounds.FEESH_RARE_DROP),
            RareDropInfo(RareDropTypes.TIKI_MASK.displayName, "TIKI_MASK", LEGENDARY.code, true, Sounds.FEESH_MINECRAFT_CHALLENGE_COMPLETED),
            RareDropInfo(RareDropTypes.TITANOBOA_SHED.displayName, "TITANOBOA_SHED", LEGENDARY.code, true, Sounds.FEESH_MINECRAFT_CHALLENGE_COMPLETED),
            RareDropInfo(RareDropTypes.SCUTTLER_SHELL.displayName, "SCUTTLER_SHELL", LEGENDARY.code, false, Sounds.FEESH_OH_MY_GOD),
            RareDropInfo(RareDropTypes.BURNT_TEXTS.displayName, "BURNT_TEXTS", LEGENDARY.code, false, Sounds.FEESH_OH_MY_GOD),
            RareDropInfo(RareDropTypes.BABY_YETI_LEGENDARY.displayName, "BABY_YETI;4", LEGENDARY.code, false, Sounds.FEESH_SHEESH),
            RareDropInfo(RareDropTypes.FLYING_FISH_LEGENDARY.displayName, "FLYING_FISH;4", LEGENDARY.code, false, Sounds.FEESH_WOW),
            RareDropInfo(RareDropTypes.MEGALODON_LEGENDARY.displayName, "MEGALODON;4", LEGENDARY.code, false, Sounds.FEESH_WOW),
            RareDropInfo(RareDropTypes.MEGALODON_EPIC.displayName, "MEGALODON;3", EPIC.code, false, Sounds.FEESH_AUGH),
            RareDropInfo(RareDropTypes.SQUID_LEGENDARY.displayName, "SQUID;4", LEGENDARY.code, false, Sounds.FEESH_WOW),
            RareDropInfo(RareDropTypes.SQUID_EPIC.displayName, "SQUID;3", EPIC.code, false, Sounds.FEESH_AUGH),
            RareDropInfo(RareDropTypes.SQUID_RARE.displayName, "SQUID;2", RARE.code, false, Sounds.FEESH_GOOFY_LAUGH),
            RareDropInfo(RareDropTypes.SQUID_UNCOMMON.displayName, "SQUID;1", UNCOMMON.code, false, Sounds.FEESH_GOOFY_LAUGH),
            RareDropInfo(RareDropTypes.SQUID_COMMON.displayName, "SQUID;0", COMMON.code, false, Sounds.FEESH_GOOFY_LAUGH),
            RareDropInfo(RareDropTypes.PHOENIX.displayName, "PHOENIX;?", SPECIAL.code, true, Sounds.FEESH_MINECRAFT_CHALLENGE_COMPLETED),
            RareDropInfo(RareDropTypes.CARMINE_DYE.displayName, "DYE_CARMINE", DARK_RED.code, true, Sounds.FEESH_GIGA_CHAD),
            RareDropInfo(RareDropTypes.MIDNIGHT_DYE.displayName, "DYE_MIDNIGHT", DARK_PURPLE.code, true, Sounds.FEESH_GIGA_CHAD),
            RareDropInfo(RareDropTypes.AQUAMARINE_DYE.displayName, "DYE_AQUAMARINE", AQUA.code, true, Sounds.FEESH_GIGA_CHAD),
            RareDropInfo(RareDropTypes.ICEBERG_DYE.displayName, "DYE_ICEBERG", DARK_AQUA.code, true, Sounds.FEESH_GIGA_CHAD),
            RareDropInfo(RareDropTypes.TREASURE_DYE.displayName, "DYE_TREASURE", GOLD.code, true, Sounds.FEESH_GIGA_CHAD),
            RareDropInfo(RareDropTypes.PERIWINKLE_DYE.displayName, "DYE_PERIWINKLE", DARK_AQUA.code, true, Sounds.FEESH_GIGA_CHAD),
            RareDropInfo(RareDropTypes.BONE_DYE.displayName, "DYE_BONE", WHITE.code, true, Sounds.FEESH_GIGA_CHAD),
        )
    }
}