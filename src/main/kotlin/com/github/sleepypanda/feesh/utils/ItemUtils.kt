package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import net.minecraft.item.ItemStack
import net.minecraft.component.DataComponentTypes

object ItemUtils {
    /*
     * Checks if the item is a Dirt Rod.
     * @param item The item to check.
     * @returns {Boolean} True if the item is a Dirt Rod, false otherwise.
     */
    fun isDirtRod(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty) return false
        return item.name.string.contains("Dirt Rod")
    }

    /*
     * Checks if the item is a Skyblock fishing rod.
     * @param item The item to check.
     * @returns {Boolean} True if the item is a fishing rod, false otherwise.
     */
    fun isFishingRod(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty) return false

        val name = item.name.string
        if (name.contains("Carnival Rod")) return false

        val loreLines = item.get(DataComponentTypes.LORE)?.lines?.map { it.string } ?: listOf()
        return loreLines.any { it.contains("FISHING ROD", ignoreCase = true) || it.contains("FISHING WEAPON", ignoreCase = true) }
    }

    /*
     * Gets the item ID for a level 1 pet, e.g. FLYING_FISH;4
     * @param petDisplayName The display name of the pet with formatting.
     * @returns {String} The item ID for the level 1 pet, e.g. FLYING_FISH;4
     */
    fun getLevel1PetId(petDisplayName: String): String {
        val rarityCode = CommonUtils.getRarityNumericCode(petDisplayName.substring(0, 2))
        val baseItemId = petDisplayName.removeFormatting().split(" ").joinToString("_").uppercase()
        val itemIdLevel1 = "${baseItemId};${rarityCode}"
        return itemIdLevel1
    }

    /*
     * Gets the item ID for a maxed (leveled up to 100 or 200) pet, e.g. FLYING_FISH;4+100
     * @param petDisplayName The display name of the pet with formatting.
     * @param level The max level the pet reached.
     * @returns {String} The item ID for the maxed pet, e.g. FLYING_FISH;4+100
     */
    fun getMaxedPetId(petDisplayName: String, level: Int): String {
        val baseItemId = getLevel1PetId(petDisplayName)
        val itemIdMaxLevel = "${baseItemId}+${level}"
        return itemIdMaxLevel
    }
}

