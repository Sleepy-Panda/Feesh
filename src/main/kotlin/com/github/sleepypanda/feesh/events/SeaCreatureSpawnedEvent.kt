package com.github.sleepypanda.feesh.events

import com.github.sleepypanda.feesh.constants.SeaCreatures

data class SeaCreatureSpawnedEvent(
    val seaCreatureName: String,
    val message: String
)

