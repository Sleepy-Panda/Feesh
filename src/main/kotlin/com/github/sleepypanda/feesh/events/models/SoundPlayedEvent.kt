package com.github.sleepypanda.feesh.events.models

data class SoundPlayedEvent(
    val soundName: String,
    val volume: Float,
)
