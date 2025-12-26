package com.github.sleepypanda.feesh.events

import com.github.sleepypanda.feesh.constants.SeaCreatures

data class OwnSeaCreatureCaughtEvent(
    val seaCreatureName: String,
    val isDoubleHook: Boolean,
    val message: String
)

