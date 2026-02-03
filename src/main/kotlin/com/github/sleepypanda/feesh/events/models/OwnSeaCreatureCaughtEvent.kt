package com.github.sleepypanda.feesh.events.models

data class OwnSeaCreatureCaughtEvent(
    val seaCreatureName: String,
    val isDoubleHook: Boolean,
    val message: String
)
