package com.github.sleepypanda.feesh.features.commands.constants

import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.ShopItem
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.ShopItemCost
import com.github.sleepypanda.feesh.features.commands.BaseShopPrices.BaseItemCost
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object GearCraftItems {
    const val MAGMA_LORD_FRAGMENT_ID = "MAGMA_LORD_FRAGMENT"
    const val THUNDER_SHARDS_ID = "THUNDER_SHARDS"
    const val THUNDER_HELMET_ID = "THUNDER_HELMET"
    const val THUNDER_CHESTPLATE_ID = "THUNDER_CHESTPLATE"
    const val THUNDER_LEGGINGS_ID = "THUNDER_LEGGINGS"
    const val THUNDER_BOOTS_ID = "THUNDER_BOOTS"
    const val TAURUS_HELMET_ID = "TAURUS_HELMET"
    const val FLAMING_CHESTPLATE_ID = "FLAMING_CHESTPLATE"
    const val MOOGMA_LEGGINGS_ID = "MOOGMA_LEGGINGS"
    const val SLUG_BOOTS_ID = "SLUG_BOOTS"
    const val WALNUT_ID = "WALNUT"
    const val SNOW_SUIT_HELMET_ID = "SNOW_SUIT_HELMET"
    const val SNOW_SUIT_CHESTPLATE_ID = "SNOW_SUIT_CHESTPLATE"
    const val SNOW_SUIT_LEGGINGS_ID = "SNOW_SUIT_LEGGINGS"
    const val SNOW_SUIT_BOOTS_ID = "SNOW_SUIT_BOOTS"
    const val EMPEROR_SKULL_ID = "DIVER_FRAGMENT"
    const val AGARIMOO_TONGUE_ID = "AGARIMOO_TONGUE"
    const val THUNDERBOLT_NECKLACE_ID = "THUNDERBOLT_NECKLACE"
    const val MAGMA_LORD_HELMET_ID = "MAGMA_LORD_HELMET"
    const val MAGMA_LORD_CHESTPLATE_ID = "MAGMA_LORD_CHESTPLATE"
    const val MAGMA_LORD_LEGGINGS_ID = "MAGMA_LORD_LEGGINGS"
    const val MAGMA_LORD_BOOTS_ID = "MAGMA_LORD_BOOTS"
    const val MAGMA_LORD_GAUNTLET_ID = "MAGMA_LORD_GAUNTLET"
    const val NUTCRACKER_HELMET_ID = "NUTCRACKER_HELMET"
    const val NUTCRACKER_CHESTPLATE_ID = "NUTCRACKER_CHESTPLATE"
    const val NUTCRACKER_LEGGINGS_ID = "NUTCRACKER_LEGGINGS"
    const val NUTCRACKER_BOOTS_ID = "NUTCRACKER_BOOTS"
    const val EMPEROR_TALISMAN_ID = "EMPEROR_TALISMAN"
    const val EMPEROR_RING_ID = "EMPEROR_RING"
    const val EMPEROR_ARTIFACT_ID = "EMPEROR_ARTIFACT"
    const val AGARIMOO_TALISMAN_ID = "AGARIMOO_TALISMAN"
    const val AGARIMOO_RING_ID = "AGARIMOO_RING"
    const val AGARIMOO_ARTIFACT_ID = "AGARIMOO_ARTIFACT"

    val MAGMA_LORD_FRAGMENT_NAME = "${LEGENDARY}Magma Lord Fragment"
    val THUNDER_FRAGMENT_NAME = "${EPIC}Thunder Fragment"
    val TAURUS_HELMET_NAME = "${RARE}Taurus Helmet"
    val FLAMING_CHESTPLATE_NAME = "${RARE}Flaming Chestplate"
    val MOOGMA_LEGGINGS_NAME = "${RARE}Moogma Leggings"
    val SLUG_BOOTS_NAME = "${RARE}Slug Boots"
    val WALNUT_NAME = "${UNCOMMON}Walnut"
    val SNOW_SUIT_HELMET_NAME = "${EPIC}Snow Suit Helmet"
    val SNOW_SUIT_CHESTPLATE_NAME = "${EPIC}Snow Suit Chestplate"
    val SNOW_SUIT_LEGGINGS_NAME = "${EPIC}Snow Suit Leggings"
    val SNOW_SUIT_BOOTS_NAME = "${EPIC}Snow Suit Boots"
    val EMPEROR_SKULL_NAME = "${RARE}Emperor's Skull"
    val AGARIMOO_TONGUE_NAME = "${UNCOMMON}Agarimoo Tongue"
    val THUNDER_HELMET_NAME = "${EPIC}Thunder Helmet"
    val THUNDER_CHESTPLATE_NAME = "${EPIC}Thunder Chestplate"
    val THUNDER_LEGGINGS_NAME = "${EPIC}Thunder Leggings"
    val THUNDER_BOOTS_NAME = "${EPIC}Thunder Boots"
    val THUNDERBOLT_NECKLACE_NAME = "${EPIC}Thunderbolt Necklace"
    val MAGMA_LORD_HELMET_NAME = "${LEGENDARY}Magma Lord Helmet"
    val MAGMA_LORD_CHESTPLATE_NAME = "${LEGENDARY}Magma Lord Chestplate"
    val MAGMA_LORD_LEGGINGS_NAME = "${LEGENDARY}Magma Lord Leggings"
    val MAGMA_LORD_BOOTS_NAME = "${LEGENDARY}Magma Lord Boots"
    val MAGMA_LORD_NECKLACE_NAME = "${LEGENDARY}Magma Lord Necklace"
    val NUTCRACKER_HELMET_NAME = "${LEGENDARY}Nutcracker Helmet"
    val NUTCRACKER_CHESTPLATE_NAME = "${LEGENDARY}Nutcracker Chestplate"
    val NUTCRACKER_LEGGINGS_NAME = "${LEGENDARY}Nutcracker Leggings"
    val NUTCRACKER_BOOTS_NAME = "${LEGENDARY}Nutcracker Boots"
    val EMPEROR_TALISMAN_NAME = "${UNCOMMON}Emperor's Talisman"
    val EMPEROR_RING_NAME = "${RARE}Emperor's Ring"
    val EMPEROR_ARTIFACT_NAME = "${EPIC}Emperor's Artifact"
    val AGARIMOO_TALISMAN_NAME = "${COMMON}Agarimoo Talisman"
    val AGARIMOO_RING_NAME = "${UNCOMMON}Agarimoo Ring"
    val AGARIMOO_ARTIFACT_NAME = "${RARE}Agarimoo Artifact"

    val SAMPLE_ALIASES = setOf("magmalord", "thunder", "walnut", "skull", "agarimoo").joinToString(" | ")

    val DISPLAY_NAMES_BY_ID = mapOf(
        MAGMA_LORD_FRAGMENT_ID to MAGMA_LORD_FRAGMENT_NAME,
        THUNDER_SHARDS_ID to THUNDER_FRAGMENT_NAME,
        TAURUS_HELMET_ID to TAURUS_HELMET_NAME,
        FLAMING_CHESTPLATE_ID to FLAMING_CHESTPLATE_NAME,
        MOOGMA_LEGGINGS_ID to MOOGMA_LEGGINGS_NAME,
        SLUG_BOOTS_ID to SLUG_BOOTS_NAME,
        WALNUT_ID to WALNUT_NAME,
        SNOW_SUIT_HELMET_ID to SNOW_SUIT_HELMET_NAME,
        SNOW_SUIT_CHESTPLATE_ID to SNOW_SUIT_CHESTPLATE_NAME,
        SNOW_SUIT_LEGGINGS_ID to SNOW_SUIT_LEGGINGS_NAME,
        SNOW_SUIT_BOOTS_ID to SNOW_SUIT_BOOTS_NAME,
        EMPEROR_SKULL_ID to EMPEROR_SKULL_NAME,
        AGARIMOO_TONGUE_ID to AGARIMOO_TONGUE_NAME,
        THUNDER_HELMET_ID to THUNDER_HELMET_NAME,
        THUNDER_CHESTPLATE_ID to THUNDER_CHESTPLATE_NAME,
        THUNDER_LEGGINGS_ID to THUNDER_LEGGINGS_NAME,
        THUNDER_BOOTS_ID to THUNDER_BOOTS_NAME,
        THUNDERBOLT_NECKLACE_ID to THUNDERBOLT_NECKLACE_NAME,
        MAGMA_LORD_HELMET_ID to MAGMA_LORD_HELMET_NAME,
        MAGMA_LORD_CHESTPLATE_ID to MAGMA_LORD_CHESTPLATE_NAME,
        MAGMA_LORD_LEGGINGS_ID to MAGMA_LORD_LEGGINGS_NAME,
        MAGMA_LORD_BOOTS_ID to MAGMA_LORD_BOOTS_NAME,
        MAGMA_LORD_GAUNTLET_ID to MAGMA_LORD_NECKLACE_NAME,
        NUTCRACKER_HELMET_ID to NUTCRACKER_HELMET_NAME,
        NUTCRACKER_CHESTPLATE_ID to NUTCRACKER_CHESTPLATE_NAME,
        NUTCRACKER_LEGGINGS_ID to NUTCRACKER_LEGGINGS_NAME,
        NUTCRACKER_BOOTS_ID to NUTCRACKER_BOOTS_NAME,
        EMPEROR_TALISMAN_ID to EMPEROR_TALISMAN_NAME,
        EMPEROR_RING_ID to EMPEROR_RING_NAME,
        EMPEROR_ARTIFACT_ID to EMPEROR_ARTIFACT_NAME,
        AGARIMOO_TALISMAN_ID to AGARIMOO_TALISMAN_NAME,
        AGARIMOO_RING_ID to AGARIMOO_RING_NAME,
        AGARIMOO_ARTIFACT_ID to AGARIMOO_ARTIFACT_NAME
    )

    fun getDisplayName(itemId: String): String = DISPLAY_NAMES_BY_ID[itemId] ?: itemId

    data class GearCraftCategory(
        val title: String,
        val baseItemId: String,
        val description: String,
        val aliases: Set<String>,
        val items: List<ShopItem>
    )

    val CATEGORIES = listOf(
        GearCraftCategory(
            title = MAGMA_LORD_FRAGMENT_NAME,
            baseItemId = MAGMA_LORD_FRAGMENT_ID,
            description = "",
            aliases = setOf("magmalord", "magma", "magmalordfragment", "ml"),
            items = listOf(
                ShopItem(
                    MAGMA_LORD_HELMET_ID,
                    MAGMA_LORD_HELMET_NAME,
                    ShopItemCost(
                        baseItemCosts = listOf(
                            BaseItemCost(MAGMA_LORD_FRAGMENT_ID, 5),
                        )
                    )
                ),
                ShopItem(
                    MAGMA_LORD_CHESTPLATE_ID,
                    MAGMA_LORD_CHESTPLATE_NAME,
                    ShopItemCost(
                        baseItemCosts = listOf(
                            BaseItemCost(MAGMA_LORD_FRAGMENT_ID, 8),
                        )
                    )
                ),
                ShopItem(
                    MAGMA_LORD_LEGGINGS_ID,
                    MAGMA_LORD_LEGGINGS_NAME,
                    ShopItemCost(
                        baseItemCosts = listOf(
                            BaseItemCost(MAGMA_LORD_FRAGMENT_ID, 7),
                        )
                    )
                ),
                ShopItem(
                    MAGMA_LORD_BOOTS_ID,
                    MAGMA_LORD_BOOTS_NAME,
                    ShopItemCost(
                        baseItemCosts = listOf(
                            BaseItemCost(MAGMA_LORD_FRAGMENT_ID, 4),
                        )
                    )
                ),
                ShopItem(
                    MAGMA_LORD_GAUNTLET_ID,
                    MAGMA_LORD_NECKLACE_NAME,
                    ShopItemCost(
                        baseItemCosts = listOf(
                BaseItemCost(MAGMA_LORD_FRAGMENT_ID, 5),
                        )
                    )
                )
            )
        ),
        GearCraftCategory(
            title = THUNDER_FRAGMENT_NAME,
            baseItemId = THUNDER_SHARDS_ID,
            description = "",
            aliases = setOf("thunder", "thunderfragment", "thunderfragments"),
            items = listOf(
                ShopItem(
                    THUNDER_HELMET_ID,
                    THUNDER_HELMET_NAME,
                    ShopItemCost(
                        baseItemCosts = listOf(
                            BaseItemCost(THUNDER_SHARDS_ID, 5),
                        )
                    )
                ),
                ShopItem(
                    THUNDER_CHESTPLATE_ID,
                    THUNDER_CHESTPLATE_NAME,
                    ShopItemCost(
                        baseItemCosts = listOf(
                            BaseItemCost(THUNDER_SHARDS_ID, 8),
                        )
                    )
                ),
                ShopItem(
                    THUNDER_LEGGINGS_ID,
                    THUNDER_LEGGINGS_NAME,
                    ShopItemCost(
                        baseItemCosts = listOf(
                            BaseItemCost(THUNDER_SHARDS_ID, 7),
                        )
                    )
                ),
                ShopItem(
                    THUNDER_BOOTS_ID,
                    THUNDER_BOOTS_NAME,
                    ShopItemCost(
                        baseItemCosts = listOf(
                            BaseItemCost(THUNDER_SHARDS_ID, 4),
                        )
                    )
                ),
                ShopItem(
                    THUNDERBOLT_NECKLACE_ID,
                    THUNDERBOLT_NECKLACE_NAME,
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(THUNDER_SHARDS_ID, 5)))
                )
            )
        ),
        GearCraftCategory(
            title = WALNUT_NAME,
            baseItemId = WALNUT_ID,
            description = "",
            aliases = setOf("walnut"),
            items = listOf(
                ShopItem(
                    NUTCRACKER_HELMET_ID,
                    NUTCRACKER_HELMET_NAME,
                    ShopItemCost(
                        baseItemCosts = listOf(
                            BaseItemCost(WALNUT_ID, 15),
                            BaseItemCost(SNOW_SUIT_HELMET_ID, 1)
                        )
                    )
                ),
                ShopItem(
                    NUTCRACKER_CHESTPLATE_ID,
                    NUTCRACKER_CHESTPLATE_NAME,
                    ShopItemCost(
                        baseItemCosts = listOf(
                            BaseItemCost(WALNUT_ID, 24),
                            BaseItemCost(SNOW_SUIT_CHESTPLATE_ID, 1)
                        )
                    )
                ),
                ShopItem(
                    NUTCRACKER_LEGGINGS_ID,
                    NUTCRACKER_LEGGINGS_NAME,
                    ShopItemCost(
                        baseItemCosts = listOf(
                            BaseItemCost(WALNUT_ID, 21),
                            BaseItemCost(SNOW_SUIT_LEGGINGS_ID, 1)
                        )
                    )
                ),
                ShopItem(
                    NUTCRACKER_BOOTS_ID,
                    NUTCRACKER_BOOTS_NAME,
                    ShopItemCost(
                        baseItemCosts = listOf(
                            BaseItemCost(WALNUT_ID, 12),
                            BaseItemCost(SNOW_SUIT_BOOTS_ID, 1)
                        )
                    )
                )
            )
        ),
        GearCraftCategory(
            title = EMPEROR_SKULL_NAME,
            baseItemId = EMPEROR_SKULL_ID,
            description = "",
            aliases = setOf("emperor", "skull", "emperorsskull", "emperorskull"),
            items = listOf(
                ShopItem(
                    EMPEROR_TALISMAN_ID,
                    EMPEROR_TALISMAN_NAME,
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(EMPEROR_SKULL_ID, 4)))
                ),
                ShopItem(
                    EMPEROR_RING_ID,
                    EMPEROR_RING_NAME,
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(EMPEROR_SKULL_ID, 16)))
                ),
                ShopItem(
                    EMPEROR_ARTIFACT_ID,
                    EMPEROR_ARTIFACT_NAME,
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(EMPEROR_SKULL_ID, 64)))
                )
            )
        ),
        GearCraftCategory(
            title = AGARIMOO_TONGUE_NAME,
            baseItemId = AGARIMOO_TONGUE_ID,
            description = "",
            aliases = setOf("agarimoo", "tongue", "agarimootongue"),
            items = listOf(
                ShopItem(
                    AGARIMOO_TALISMAN_ID,
                    AGARIMOO_TALISMAN_NAME,
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(AGARIMOO_TONGUE_ID, 9)))
                ),
                ShopItem(
                    AGARIMOO_RING_ID,
                    AGARIMOO_RING_NAME,
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(AGARIMOO_TONGUE_ID, 9 + 96)))
                ),
                ShopItem(
                    AGARIMOO_ARTIFACT_ID,
                    AGARIMOO_ARTIFACT_NAME,
                    ShopItemCost(baseItemCosts = listOf(BaseItemCost(AGARIMOO_TONGUE_ID, 9 + 96 + 512)))
                )
            )
        )
    )
}