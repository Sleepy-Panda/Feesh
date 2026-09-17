package com.github.sleepypanda.feesh.events.models

import com.github.sleepypanda.feesh.constants.AlertableRareDrops
import com.github.sleepypanda.feesh.constants.AlertableRareDropInfo

/**
 * Event for a rare fishing drop happened (based on chat messages: RARE DROP! <Item name>).
 * Only published when the drop matches an entry in [AlertableRareDrops.rareDrops].
 * @param dropInfo Matched [AlertableRareDropInfo] for this drop.
 * @param itemNameUnformatted The unformatted name of the item that was dropped. [matches RareDrops.itemName].
 * @param itemNameFormattedOriginal The formatted name of the item that was dropped, taken from chat message as is.
 * @param magicFind The magic find of the drop.
 * @param dropNumber Session ordinal number for this item.
 */
data class ChatBasedRareDropEvent(
    val dropInfo: AlertableRareDropInfo,
    val itemNameUnformatted: String,
    val itemNameFormattedOriginal: String,
    val magicFind: Int? = null,
    val dropNumber: Int
)
