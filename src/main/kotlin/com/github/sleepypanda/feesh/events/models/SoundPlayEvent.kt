package com.github.sleepypanda.feesh.events.models

data class SoundPlayEvent(
    val soundPath: String,
    val volume: Float,
    val pitch: Float,
    val x: Double,
    val y: Double,
    val z: Double,
)