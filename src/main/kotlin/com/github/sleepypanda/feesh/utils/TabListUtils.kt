package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import net.minecraft.network.chat.Component

object TabListUtils {
    /*
     * Get the TabList line after the specified startsWith string. No formatting preserved.
     * E.g. getLineAfter("Area:") will return the trimmed text after the "Area:" string.
     * @param startsWith The string to search for.
     * @returns {String} The line after the specified startsWith string.
     */
    fun getLineAfter(startsWith: String): String {
        val networkHandler = FeeshMod.mc.connection ?: return ""
        val playerList = networkHandler.getListedOnlinePlayers()
        
        for (entry in playerList) {
            val displayName = entry.tabListDisplayName ?: entry.profile?.name?.let { Component.literal(it) } ?: continue
            val text = displayName.string
            
            if (text.contains(startsWith)) {
                val entryIndex = text.indexOf(startsWith)
                if (entryIndex != -1) {
                    val value = text.substring(entryIndex + startsWith.length).removeFormatting().trim()
                    return value.ifEmpty { "" }
                }
            }
        }
        
        return ""
    }
}