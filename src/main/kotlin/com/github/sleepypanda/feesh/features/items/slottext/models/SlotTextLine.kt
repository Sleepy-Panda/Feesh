package com.github.sleepypanda.feesh.features.items.slottext.models

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style

data class SlotTextLine(
    val text: String,
    val color: Int = 0xFFFFFFFF.toInt(),
    val isBold: Boolean = false
) {
    val component: Component = if (isBold) {
        Component.literal(text).withStyle(Style.EMPTY.withBold(true))
    } else {
        Component.literal(text)
    }
}
