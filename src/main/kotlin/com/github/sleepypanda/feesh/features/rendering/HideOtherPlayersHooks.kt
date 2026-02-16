package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.settings.categories.Rendering
import com.github.sleepypanda.feesh.utils.WorldUtils

object HideOtherPlayersHooks {
    fun shouldHideOtherPlayersHooks(): Boolean {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return false
        if (!Rendering.hideOtherPlayersFishingHooks) return false
    
        return true
    }
}
