package com.github.sleepypanda.feesh.utils

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
}

