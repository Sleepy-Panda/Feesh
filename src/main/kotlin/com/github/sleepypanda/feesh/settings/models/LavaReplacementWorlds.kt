package com.github.sleepypanda.feesh.settings.models

import com.github.sleepypanda.feesh.utils.WorldUtils

enum class LavaReplacementWorlds(val worldName: String) {
    CRIMSON_ISLE(WorldUtils.CRIMSON_ISLE),
    CRYSTAL_HOLLOWS(WorldUtils.CRYSTAL_HOLLOWS);

    override fun toString(): String = worldName
}
