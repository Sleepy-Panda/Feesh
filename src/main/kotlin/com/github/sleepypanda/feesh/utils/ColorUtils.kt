package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import kotlin.math.roundToInt

object ColorUtils {
    private data class GradientCacheKey(
        val text: String,
        val colors: List<Int>,
        val bold: Boolean,
    )

    private val gradientCache = mutableMapOf<GradientCacheKey, Component>()

    fun init() {
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        gradientCache.clear()
    }

    /**
     * Creates text component with a linear RGB gradient across [colors] (0xRRGGBB). Supports 2+ color steps.
     * If the text has fewer characters than colors, evenly picks a subset that always includes the first and last color
     * (e.g. 2 chars amd 3 colors -> first & last only).
     * Results are cached; cleared on world change. Returned components are copies so callers can append safely.
     * @param text The unformatted text to color (formatting codes are removed).
     */
    fun buildGradientTextComponent(text: String, colors: IntArray, bold: Boolean = false): Component {
        val clean = text.removeFormatting()
        if (clean.isEmpty() || colors.isEmpty()) return Component.empty()

        val key = GradientCacheKey(clean, colors.toList(), bold)
        return gradientCache.getOrPut(key) { buildGradientTextComponentUncached(clean, colors, bold) }.copy()
    }

    private fun buildGradientTextComponentUncached(clean: String, colors: IntArray, bold: Boolean): Component {
        fun styleFor(rgb: Int): Style {
            val style = Style.EMPTY.withColor(TextColor.fromRgb(rgb and 0xFFFFFF))
            return if (bold) style.withBold(true) else style
        }

        val steps = selectColorSteps(colors, clean.length)
        if (clean.length == 1 || steps.size == 1) {
            return Component.literal(clean).setStyle(styleFor(steps[0]))
        }

        val last = clean.length - 1
        val result = Component.empty()
        for (i in clean.indices) {
            val t = i.toFloat() / last
            result.append(Component.literal(clean[i].toString()).setStyle(styleFor(interpolateAlongSteps(steps, t))))
        }
        return result
    }

    /** Picks [count] colors evenly from [colors], always keeping first and last when shrinking. */
    private fun selectColorSteps(colors: IntArray, count: Int): IntArray {
        if (count >= colors.size) return colors
        if (count <= 1) return intArrayOf(colors.first())
        return IntArray(count) { i ->
            val index = (i.toFloat() * (colors.size - 1) / (count - 1)).roundToInt()
            colors[index]
        }
    }

    private fun interpolateAlongSteps(steps: IntArray, t: Float): Int {
        if (t <= 0f) return steps.first()
        if (t >= 1f) return steps.last()

        val scaled = t * (steps.size - 1)
        val index = scaled.toInt().coerceIn(0, steps.size - 2)
        val localT = scaled - index
        return lerpColor(steps[index], steps[index + 1], localT)
    }

    private fun lerpColor(color1: Int, color2: Int, t: Float): Int {
        val r = ((color1 shr 16) and 0xFF) + ((((color2 shr 16) and 0xFF) - ((color1 shr 16) and 0xFF)) * t).toInt()
        val g = ((color1 shr 8) and 0xFF) + ((((color2 shr 8) and 0xFF) - ((color1 shr 8) and 0xFF)) * t).toInt()
        val b = (color1 and 0xFF) + (((color2 and 0xFF) - (color1 and 0xFF)) * t).toInt()
        return (r shl 16) or (g shl 8) or b
    }
}
