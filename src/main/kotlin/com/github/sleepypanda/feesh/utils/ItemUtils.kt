package com.github.sleepypanda.feesh.utils

import net.minecraft.item.ItemStack
import net.minecraft.component.DataComponentTypes

object ItemUtils {
    fun isFishingRod(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty) return false

        val name = item.name.string
        if (name.contains("Carnival Rod")) return false

        val loreLines = item.get(DataComponentTypes.LORE)?.lines?.map { it.string } ?: listOf()
        return loreLines.any { it.contains("FISHING ROD", ignoreCase = true) || it.contains("FISHING WEAPON", ignoreCase = true) }
    }
}

