package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod

object TabListUtils {
    /*
     * Get the TabList line after the specified startsWith string. No formatting preserved.
     * E.g. getLineAfter("Area:") will return the trimmed text after the "Area:" string.
     * @param startsWith The string to search for.
     * @returns {String} The line after the specified startsWith string.
     */
    fun getLineAfter(startsWith: String): String {
        val networkHandler = FeeshMod.mc.networkHandler ?: return ""
        val playerList = networkHandler.playerList ?: return ""
        
        for (entry in playerList) {
            val displayName = entry.displayName ?: continue
            val text = displayName.string
            
            if (text.contains(startsWith)) {
                val areaIndex = text.indexOf(startsWith)
                if (areaIndex != -1) {
                    val worldName = text.substring(areaIndex + startsWith.length).trim()
                    return worldName.ifEmpty { "" }
                }
            }
        }
        
        return ""
    }
}