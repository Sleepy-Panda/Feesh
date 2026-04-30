package com.github.sleepypanda.feesh.events.models

import com.github.sleepypanda.feesh.constants.SeaCreatures.SeaCreatureInfo

data class OwnSeaCreatureCaughtEvent(
    val seaCreatureName: String,
    val isDoubleHook: Boolean,
    val catchMessage: String,
    val seaCreatureInfo: SeaCreatureInfo
)
