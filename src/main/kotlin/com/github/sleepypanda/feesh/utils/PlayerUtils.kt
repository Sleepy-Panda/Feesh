package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormatted
import net.minecraft.text.Text

object PlayerUtils {
    fun getName() : String {      
        val mc = FeeshMod.mc
        val displayNameText = mc.player?.displayName ?: return ""
        val displayName = displayNameText.getFormatted()
        FeeshMod.LOGGER.info("FEESH DISPLAY NAME: ${displayName}")
        return displayName
    }
}