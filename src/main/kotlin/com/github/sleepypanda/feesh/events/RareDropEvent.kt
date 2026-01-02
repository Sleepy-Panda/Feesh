package com.github.sleepypanda.feesh.events

import com.github.sleepypanda.feesh.constants.SeaCreatures

/**
 * Event for when a rare drop is found (based on chat messages).
 * @param itemName The name of the item that was dropped. Contains no formatting.
 * @param magicFind The magic find percentage of the drop.
 */
data class RareDropEvent(
    val itemName: String,
    val magicFind: Int? = null
)

