package com.github.sleepypanda.feesh.features.items.slottext.models

data class SlotTextLine(
    val text: String,
    val color: Int = 0xFFFFFFFF.toInt(),
    val bold: Boolean = false
)
