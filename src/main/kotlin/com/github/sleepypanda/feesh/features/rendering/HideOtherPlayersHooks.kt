package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.WorldUtils

object HideOtherPlayersHooks {
    @JvmStatic
    fun shouldHideOtherPlayersHooks(): Boolean {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return false
        if (!WorldRendering.hideOtherPlayersFishingHooks) return false
    
        return true
    }
}
