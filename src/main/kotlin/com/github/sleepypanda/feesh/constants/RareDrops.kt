package com.github.sleepypanda.feesh.constants

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

// Enum used to list selectable items in Alerts/Chat settings
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
        data class RareDropInfo(val id: String, val itemName: String, val rarityColorCode: String, val npcPrice: Int?, val isExtremelyRare: Boolean, val defaultSoundFileName: String) {
            val displayName: String get() = rarityColorCode + itemName
            val boldDisplayName: String get() = rarityColorCode + BOLD + itemName

            fun getTitle(): String {
                val baseTitle = this.boldDisplayName.substringBefore(" (") // Baby Yeti (Legendary) -> Baby Yeti
                return if (this.isExtremelyRare) "${GOLD}${OBFUSCATED}x${RESET} ${baseTitle} ${GOLD}${OBFUSCATED}x${RESET}" 
                else "${baseTitle}"
            }
        }

        val rareDrops = listOf(
            RareDropInfo(
                id = "PET_ITEM_LUCKY_CLOVER_DROP",
                itemName = RareDropTypes.LUCKY_CLOVER_CORE.displayName,
                rarityColorCode = EPIC.code,
                npcPrice = 50_000,
                isExtremelyRare = false,
                defaultSoundFileName = Sounds.FEESH_OH_MY_GOD
            ),
            RareDropInfo(
                id = "DEEP_SEA_ORB",
                itemName = RareDropTypes.DEEP_SEA_ORB.displayName,
                rarityColorCode = EPIC.code,
                npcPrice = 1,
                isExtremelyRare = false,
                defaultSoundFileName = Sounds.FEESH_OH_MY_GOD
            ),
            RareDropInfo(
                id = "RADIOACTIVE_VIAL",
                itemName = RareDropTypes.RADIOACTIVE_VIAL.displayName,
                rarityColorCode = MYTHIC.code,
                npcPrice = 5_000_000,
                isExtremelyRare = true,
                defaultSoundFileName = Sounds.FEESH_MINECRAFT_CHALLENGE_COMPLETED
            ),
            RareDropInfo(
                id = "MAGMA_CORE",
                itemName = RareDropTypes.MAGMA_CORE.displayName,
                rarityColorCode = RARE.code,
                npcPrice = 200_000,
                isExtremelyRare = false,
                defaultSoundFileName = Sounds.FEESH_OH_MY_GOD
            ),
            RareDropInfo(
                id = "TIKI_MASK",
                itemName = RareDropTypes.TIKI_MASK.displayName,
                rarityColorCode = LEGENDARY.code,
                npcPrice = 1_000_000,
                isExtremelyRare = true,
                defaultSoundFileName = Sounds.FEESH_MINECRAFT_CHALLENGE_COMPLETED
            ),
            RareDropInfo(
                id = "TITANOBOA_SHED",
                itemName = RareDropTypes.TITANOBOA_SHED.displayName,
                rarityColorCode = LEGENDARY.code,
                npcPrice = 500_000,
                isExtremelyRare = true,
                defaultSoundFileName = Sounds.FEESH_MINECRAFT_CHALLENGE_COMPLETED
            ),
            RareDropInfo(
                id = "SCUTTLER_SHELL",
                itemName = RareDropTypes.SCUTTLER_SHELL.displayName,
                rarityColorCode = MYTHIC.code,
                npcPrice = 1_000_000,
                isExtremelyRare = false,
                defaultSoundFileName = Sounds.FEESH_OH_MY_GOD
            ),
            RareDropInfo(
                id = "BURNT_TEXTS",
                itemName = RareDropTypes.BURNT_TEXTS.displayName,
                rarityColorCode = LEGENDARY.code,
                npcPrice = 1_000_000,
                isExtremelyRare = false,
                defaultSoundFileName = Sounds.FEESH_OH_MY_GOD
            ),
            RareDropInfo(
                id = "BABY_YETI;4",
                itemName = RareDropTypes.BABY_YETI_LEGENDARY.displayName,
                rarityColorCode = LEGENDARY.code,
                npcPrice = 1_000_000,
                isExtremelyRare = false,
                defaultSoundFileName = Sounds.FEESH_SHEESH
            ),
            RareDropInfo(
                id = "FLYING_FISH;4",
                itemName = RareDropTypes.FLYING_FISH_LEGENDARY.displayName,
                rarityColorCode = LEGENDARY.code,
                npcPrice = 250_000,
                isExtremelyRare = false,
                defaultSoundFileName = Sounds.FEESH_WOW
            ),
            RareDropInfo(
                id = "MEGALODON;4",
                itemName = RareDropTypes.MEGALODON_LEGENDARY.displayName,
                rarityColorCode = LEGENDARY.code,
                npcPrice = 2_500_000,
                isExtremelyRare = false,
                defaultSoundFileName = Sounds.FEESH_WOW
            ),
            RareDropInfo(
                id = "MEGALODON;3",
                itemName = RareDropTypes.MEGALODON_EPIC.displayName,
                rarityColorCode = EPIC.code,
                npcPrice = 500_000,
                isExtremelyRare = false,
                defaultSoundFileName = Sounds.FEESH_AUGH
            ),
            RareDropInfo(
                id = "SQUID;4",
                itemName = RareDropTypes.SQUID_LEGENDARY.displayName,
                rarityColorCode = LEGENDARY.code,
                npcPrice = 500_000,
                isExtremelyRare = false,
                defaultSoundFileName = Sounds.FEESH_WOW
            ),
            RareDropInfo(
                id = "SQUID;3",
                itemName = RareDropTypes.SQUID_EPIC.displayName,
                rarityColorCode = EPIC.code,
                npcPrice = 200_000,
                isExtremelyRare = false,
                defaultSoundFileName = Sounds.FEESH_AUGH
            ),
            RareDropInfo(
                id = "SQUID;2",
                itemName = RareDropTypes.SQUID_RARE.displayName,
                rarityColorCode = RARE.code,
                npcPrice = 100_000,
                isExtremelyRare = false,
                defaultSoundFileName = Sounds.FEESH_GOOFY_LAUGH
            ),
            RareDropInfo(
                id = "SQUID;1",
                itemName = RareDropTypes.SQUID_UNCOMMON.displayName,
                rarityColorCode = UNCOMMON.code,
                npcPrice = 500,
                isExtremelyRare = false,
                defaultSoundFileName = Sounds.FEESH_GOOFY_LAUGH
            ),
            RareDropInfo(
                id = "SQUID;0",
                itemName = RareDropTypes.SQUID_COMMON.displayName,
                rarityColorCode = COMMON.code,
                npcPrice = 100,
                isExtremelyRare = false,
                defaultSoundFileName = Sounds.FEESH_GOOFY_LAUGH
            ),
            RareDropInfo(
                id = "PHOENIX;?",
                itemName = RareDropTypes.PHOENIX.displayName,
                rarityColorCode = SPECIAL.code,
                npcPrice = null,
                isExtremelyRare = true,
                defaultSoundFileName = Sounds.FEESH_MINECRAFT_CHALLENGE_COMPLETED
            ),
            RareDropInfo(
                id = "DYE_CARMINE",
                itemName = RareDropTypes.CARMINE_DYE.displayName,
                rarityColorCode = DARK_RED.code,
                npcPrice = null,
                isExtremelyRare = true,
                defaultSoundFileName = Sounds.FEESH_GIGA_CHAD
            ),
            RareDropInfo(
                id = "DYE_MIDNIGHT",
                itemName = RareDropTypes.MIDNIGHT_DYE.displayName,
                rarityColorCode = DARK_PURPLE.code,
                npcPrice = null,
                isExtremelyRare = true,
                defaultSoundFileName = Sounds.FEESH_GIGA_CHAD
            ),
            RareDropInfo(
                id = "DYE_AQUAMARINE",
                itemName = RareDropTypes.AQUAMARINE_DYE.displayName,
                rarityColorCode = AQUA.code,
                npcPrice = null,
                isExtremelyRare = true,
                defaultSoundFileName = Sounds.FEESH_GIGA_CHAD
            ),
            RareDropInfo(
                id = "DYE_ICEBERG",
                itemName = RareDropTypes.ICEBERG_DYE.displayName,
                rarityColorCode = DARK_AQUA.code,
                npcPrice = null,
                isExtremelyRare = true,
                defaultSoundFileName = Sounds.FEESH_GIGA_CHAD
            ),
            RareDropInfo(
                id = "DYE_TREASURE",
                itemName = RareDropTypes.TREASURE_DYE.displayName,
                rarityColorCode = GOLD.code,
                npcPrice = null,
                isExtremelyRare = true,
                defaultSoundFileName = Sounds.FEESH_GIGA_CHAD
            ),
            RareDropInfo(
                id = "DYE_PERIWINKLE",
                itemName = RareDropTypes.PERIWINKLE_DYE.displayName,
                rarityColorCode = DARK_AQUA.code,
                npcPrice = null,
                isExtremelyRare = true,
                defaultSoundFileName = Sounds.FEESH_GIGA_CHAD
            ),
            RareDropInfo(
                id = "DYE_BONE",
                itemName = RareDropTypes.BONE_DYE.displayName,
                rarityColorCode = WHITE.code,
                npcPrice = null,
                isExtremelyRare = true,
                defaultSoundFileName = Sounds.FEESH_GIGA_CHAD
            ),
        )
    }
}