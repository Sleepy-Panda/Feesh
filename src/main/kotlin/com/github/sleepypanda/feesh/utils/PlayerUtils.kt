package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormatted
import net.minecraft.text.Text
import net.minecraft.item.ItemStack
import net.minecraft.component.DataComponentTypes

object PlayerUtils {
    fun getName() : String {      
        val mc = FeeshMod.mc
        val displayNameText = mc.player?.displayName ?: return ""
        val displayName = displayNameText.getFormatted()
        //FeeshMod.LOGGER.info("FEESH DISPLAY NAME: ${displayName}")
        return displayName
    }

    fun hasFishingRodInHotbar(): Boolean {
        val player = FeeshMod.mc.player ?: return false
        for (i in 0..8) {
            val stack = player.inventory.getStack(i)
            if (isFishingRod(stack)) return true
        }
        return false
    }

    private fun isFishingRod(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty) return false

        val name = item.name.string      
        if (name.contains("Carnival Rod")) return false

        val loreLines = item.get(DataComponentTypes.LORE)?.lines?.map { it.string } ?: listOf()
        return loreLines.any { it.contains("FISHING ROD", ignoreCase = true) || it.contains("FISHING WEAPON", ignoreCase = true) }
    }
}