package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.WorldUtils

object ReplaceLavaWithWater {
    @JvmStatic
    fun shouldReplaceLavaWithWater(): Boolean {
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return false
        if (!WorldRendering.replaceLavaWithWater) return false
    
        return true
    }
}
