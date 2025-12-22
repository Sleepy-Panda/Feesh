package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod

object Player {
    fun getName() : String {      
        val mc = FeeshMod.mc
        return mc.player?.name?.string ?: ""
    }
}