package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtOps

object ItemUtils {
    /**
     * Returns the item's custom data component (CUSTOM_DATA), or null if absent.
     */
    fun getCustomData(stack: ItemStack): NbtComponent? {
        if (stack.isEmpty) return null
        return stack.get(DataComponentTypes.CUSTOM_DATA)
    }

    /**
     * Returns the "id" field from custom data (e.g. "PET" for pets).
     */
    fun getCustomDataId(customData: NbtComponent): String? {
        val obj = customDataToJsonObject(customData) ?: return null
        return obj.get("id")?.takeIf { it.isJsonPrimitive }?.asString
    }

    /**
     * Returns the "petInfo" JSON string from custom data. Caller should parse it for further processing.
     */
    fun getCustomDataPetInfo(customData: NbtComponent): String? {
        val obj = customDataToJsonObject(customData) ?: return null
        return obj.get("petInfo")?.takeIf { it.isJsonPrimitive }?.asString
    }

    private fun customDataToJsonObject(customData: NbtComponent): JsonObject? {
        val nbt = customData.copyNbt()
        if (nbt.isEmpty) return null
        val jsonEl = try {
            NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbt)
        } catch (_: Exception) {
            return null
        }
        return jsonEl?.takeIf { it.isJsonObject }?.asJsonObject
    }
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

