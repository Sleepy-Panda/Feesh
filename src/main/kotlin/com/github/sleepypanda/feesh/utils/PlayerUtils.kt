package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod

object PlayerUtils {
    fun getName() : String {      
        val mc = FeeshMod.mc
        return mc.player?.name?.string ?: ""
    }
}