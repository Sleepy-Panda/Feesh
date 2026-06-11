package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.GRAY
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.NbtOps
import net.minecraft.world.item.FishingRodItem

object ItemUtils {
    /**
     * Returns the item's custom data component (CUSTOM_DATA), or null if absent.
     */
    fun getCustomData(stack: ItemStack): CustomData? {
        if (stack.isEmpty) return null
        return stack.get(DataComponents.CUSTOM_DATA)
    }

    /**
     * Returns the "id" field from custom data (e.g. "PET" for pets).
     */
    fun getCustomDataId(customData: CustomData): String? {
        val obj = customDataToJsonObject(customData) ?: return null
        return obj.get("id")?.takeIf { it.isJsonPrimitive }?.asString
    }

    /**
     * Returns the "petInfo" JSON string from custom data. Caller should parse it for further processing.
     */
    fun getCustomDataPetInfo(customData: CustomData): String? {
        val obj = customDataToJsonObject(customData) ?: return null
        return obj.get("petInfo")?.takeIf { it.isJsonPrimitive }?.asString
    }

    /*
     * Converts a custom data component to a JSON object.
     * @param customData The custom data component to convert.
     * @returns {JsonObject} The JSON object, or null if the conversion fails.
     */
    fun customDataToJsonObject(customData: CustomData): JsonObject? {
        val nbt = customData.copyTag()
        if (nbt.isEmpty) return null
        val jsonEl = try {
            NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbt)
        } catch (_: Exception) {
            return null
        }
        return jsonEl?.takeIf { it.isJsonObject }?.asJsonObject
    }

    /*
     * Gets the clean item name without formatting. It also removes the amount suffix from NPC menu if present (e.g. §9Fish Affinity Talisman §8x1 -> Fish Affinity Talisman).
     * @param itemName The formatted item name to clean.
     * @returns {String} The clean item name.
     */
    fun getCleanItemName(itemName: String): String {
        if (itemName.isBlank()) return ""
        var s = itemName
        if (Regex(".+ §8x\\d+$").matches(s)) { // Booster cookie menu or NPCs append the amount to the item name - e.g. §9Fish Affinity Talisman §8x1
            s = s.split(" ").dropLast(1).joinToString(" ")
        }
        return s.removeFormatting()
    }

    /*
     * Checks if the item is a Dirt Rod.
     * @param item The item to check.
     * @returns {Boolean} True if the item is a Dirt Rod, false otherwise.
     */
    fun isDirtRod(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty) return false
        return item.hoverName.string.contains("Dirt Rod")
    }

    /*
     * Checks if the item is a Skyblock fishing rod.
     * @param item The item stack to check.
     * @returns {Boolean} True if the item is a fishing rod, false otherwise.
     */
    fun isFishingRod(itemStack: ItemStack?): Boolean {
        if (itemStack == null || itemStack.isEmpty) return false        
        if (itemStack.item == null || itemStack.item !is FishingRodItem) return false

        val name = itemStack.hoverName?.string ?: return false
        if (name.contains("Carnival Rod")) return false

        val loreLines = itemStack.get(DataComponents.LORE)?.lines()?.map { it.string } ?: listOf()
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

    /*
     * Checks if the item ID is a maxed pet ID.
     * @param itemId The item ID to check.
     * @returns {Boolean} True if the item is a maxed pet, false otherwise.
     */
    fun isMaxedPet(itemId: String): Boolean =
        itemId.endsWith("+100") || itemId.endsWith("+200")

    /*
     * Gets the pet name by pet ID.
     * @param itemId The item ID to get the name for.
     * @returns {String} The name for the pet, e.g. Flying Fish
     */
    fun getPetNameByPetId(itemId: String): String {
        if (!isMaxedPet(itemId)) return ""
        val namePrefix = itemId.split(";")[0]
        val itemName = CommonUtils.fromUppercaseToCapitalizedFirstLetters(namePrefix, "_")
        return itemName
    }

    /*
     * Gets the item display name by pet ID.
     * @param itemId The item ID to get the display name for.
     * @returns {String} The item display name for the pet with formatting, e.g. "[Lvl 100] Flying Fish"
     */
    fun getItemDisplayNameByPetId(itemId: String): String {
        if (!isMaxedPet(itemId)) return ""
        val level = itemId.split("+")[1]
        val rarityNumericCode = itemId.split(";")[1].substringBefore("+").toInt()
        val rarityCode = CommonUtils.getRarityColorCode(rarityNumericCode)
        val namePrefix = itemId.split(";")[0]
        val itemName = CommonUtils.fromUppercaseToCapitalizedFirstLetters(namePrefix, "_")
        return "${GRAY}[Lvl ${level}] ${rarityCode}${itemName}"
    }

    /*
     * Gets the item display name by pet ID.
     * @param itemId The item ID to get the display name for.
     * @param petName The item name to get the display name for, e.g. "Flying Fish"
     * @returns {String} The item display name for the pet with formatting, e.g. "[Lvl 100] Flying Fish"
     */
    fun getItemDisplayNameByPetId(itemId: String, petName: String): String {
        if (!isMaxedPet(itemId)) return ""
        val level = itemId.split("+")[1]
        val rarityNumericCode = itemId.split(";")[1].substringBefore("+").toInt()
        val rarityCode = CommonUtils.getRarityColorCode(rarityNumericCode)
        return "${GRAY}[Lvl ${level}] ${rarityCode}${petName}"
    }

    /*
     * Gets the name of the enchanted book from the stack.
     * @param stack The item stack to get the name from.
     * @returns {String} The name of the enchanted book, or null if the stack is not an enchanted book.
     */
    fun getEnchantedBookName(stack: ItemStack): String? {
        val lore = stack.get(DataComponents.LORE)?.lines()?.map { it.string.removeFormatting() } ?: emptyList()
        val filteredLore = lore.filter { it.isNotBlank() && !it.contains("Combinable in Anvil") }
        val bookName = filteredLore.firstOrNull()?.trim() ?: return null
        if (bookName.isEmpty()) return null
        return bookName
    }
}
