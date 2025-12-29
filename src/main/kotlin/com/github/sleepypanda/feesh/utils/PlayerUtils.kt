package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormatted
import net.minecraft.text.Text
import net.minecraft.item.Items

object PlayerUtils {
    fun getName() : String {      
        val mc = FeeshMod.mc
        val displayNameText = mc.player?.displayName ?: return ""
        val displayName = displayNameText.getFormatted()
        FeeshMod.LOGGER.info("FEESH DISPLAY NAME: ${displayName}")
        return displayName
    }

    fun hasFishingRodInHotbar(): Boolean {
        val player = FeeshMod.mc.player ?: return false
        for (i in 0..8) {
            val stack = player.inventory.getStack(i)
            if (stack.item == Items.FISHING_ROD) return true
        }
        return false
    }
}