package com.github.sleepypanda.feesh.utils

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor

object ColorUtils {
    const val MOD_NAME_COLOR1 = 0x158AB7
    const val MOD_NAME_COLOR2 = 0x52CC9D

    /**
     * Creates text with a linear RGB gradient from [color1] to [color2] (color format: 0xRRGGBB).
     * Each character is colored with a next color based on the position in the text.
     */
    fun createGradientText(text: String, color1: Int, color2: Int, bold: Boolean = false): Component {
        if (text.isEmpty()) return Component.empty()

        fun styleFor(rgb: Int): Style {
            val style = Style.EMPTY.withColor(TextColor.fromRgb(rgb and 0xFFFFFF))
            return if (bold) style.withBold(true) else style
        }

        if (text.length == 1) {
            return Component.literal(text).setStyle(styleFor(color1))
        }

        val startR = (color1 shr 16) and 0xFF
        val startG = (color1 shr 8) and 0xFF
        val startB = color1 and 0xFF
        val endR = (color2 shr 16) and 0xFF
        val endG = (color2 shr 8) and 0xFF
        val endB = color2 and 0xFF
        val last = text.length - 1

        val result = Component.empty()
        for (i in text.indices) {
            val t = i.toFloat() / last
            val r = (startR + (endR - startR) * t).toInt()
            val g = (startG + (endG - startG) * t).toInt()
            val b = (startB + (endB - startB) * t).toInt()
            val rgb = (r shl 16) or (g shl 8) or b
            result.append(Component.literal(text[i].toString()).setStyle(styleFor(rgb)))
        }
        return result
    }
}
