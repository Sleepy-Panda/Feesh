package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod

object WorldUtils {
    fun isInSkyblock(): Boolean {
        //val title = ScoreBoard.getTitle().lowercase()
        val serverAddress = FeeshMod.mc.currentServerEntry?.address ?: return false
        return serverAddress.contains("hypixel", ignoreCase = true)
    }
}