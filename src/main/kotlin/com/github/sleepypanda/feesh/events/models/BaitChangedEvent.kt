package com.github.sleepypanda.feesh.events.models

data class BaitChangedEvent(
    val oldBaitName: String,
    val oldBaitDisplayName: String,
    val newBaitName: String,
    val newBaitDisplayName: String
)