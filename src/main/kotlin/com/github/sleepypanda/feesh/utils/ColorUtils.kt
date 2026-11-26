package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor

object ColorUtils {
    private val GRADIENT_VARIABLE_REGEX = Regex("""\{gradient:([^}]*)}""", RegexOption.IGNORE_CASE)

    private val gradientColoredComponentsCache = mutableMapOf<String, Component>()

    fun init() {
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        gradientColoredComponentsCache.clear()
    }

    /** Builds a gradient variable like "{gradient:#158AB7>#52CC9D}", from hex color codes (#RRGGBB). */
    fun buildGradientVariable(colors: Array<String>): String {
        var gradientVariable = ""
        CommonUtils.runWithCatching("Failed to build gradient variable") {
            if (colors.isNotEmpty()) {
                val joinedColors = colors.joinToString(">")
                gradientVariable = "{gradient:${joinedColors}}"
            }
        }
        return gradientVariable
    }

    /** Builds a gradient variable like "{gradient:#158AB7>#52CC9D}", from RGB colors (0xRRGGBB). */
    fun buildGradientVariable(colors: IntArray): String {
        val colorCodes = colors.map { it.toHexColorCode() }.toTypedArray()
        return buildGradientVariable(colorCodes)
    }

    /**
     * Parses gradient variable like `{gradient:#158AB7>#52CC9D}` into 0xRRGGBB colors.
     * Keeps only valid `#` + hex parts.
     */
    fun parseGradientColors(gradientVariable: String): IntArray {
        var colors = IntArray(0)
        CommonUtils.runWithCatching("Failed to parse gradient colors") {
            val spec = GRADIENT_VARIABLE_REGEX.find(gradientVariable)?.groupValues?.get(1) ?: return@runWithCatching
            colors = parseGradientColorSteps(spec)
        }
        return colors
    }

    /** Converts 0xRRGGBB color to `#RRGGBB` code. */
    private fun Int.toHexColorCode(): String = "#%06X".format(this and 0xFFFFFF)

    /** `#158AB7>#52CC9D` into 0xRRGGBB colors, keeping only valid `#` + hex parts. */
    private fun parseGradientColorSteps(spec: String): IntArray {
        return spec.split('>').mapNotNull { part ->
            val hexColor = part.trim()
            if (!hexColor.startsWith("#")) return@mapNotNull null
            hexColor.drop(1).toIntOrNull(16)
        }.toIntArray()
    }

    /**
     * Creates text component colored with a single RGB hex [color] (0xRRGGBB).
     * @param text The unformatted text to color (formatting codes are removed).
     */
    fun buildSingleColoredTextComponent(text: String, color: Int, bold: Boolean = false): MutableComponent {
        val clean = text.removeFormatting()
        if (clean.isEmpty()) return Component.empty()

        var style = Style.EMPTY.withColor(TextColor.fromRgb(color and 0xFFFFFF))
        if (bold) style = style.withBold(true)
        return Component.literal(clean).setStyle(style)
    }

    /**
     * Builds a component from a template string, which can use color codes, formatting codes, and gradient variables.
     * `{gradient:#158AB7>#52CC9D}` colors the following text until the next color code or gradient tag.
     * Formatting codes (bold, italic etc) right after the tag apply to that gradient.
     * Hex steps are separated by `>`. An invalid tag is left as literal text.
     * The full component built from template is cached until the world changes.
     */
    fun buildGradientColoredComponent(template: String): MutableComponent {
        var component = Component.empty()
        CommonUtils.runWithCatching("Failed to build gradient colored component") {
            if (template.isEmpty()) return@runWithCatching
            component = gradientColoredComponentsCache.getOrPut(template) {
                buildGradientColoredComponentUncached(template)
            }.copy() // Copy so we can modify component further
        }
        return component
    }

    private fun buildGradientColoredComponentUncached(template: String): Component {
        val result = Component.empty()
        var cursor = 0

        while (cursor < template.length) {
            val gradient = GRADIENT_VARIABLE_REGEX.find(template, cursor)
            val plainTextEnd = gradient?.range?.first ?: template.length
            if (plainTextEnd > cursor) {
                result.append(Component.literal(template.substring(cursor, plainTextEnd)))
            }
            if (gradient == null) break
            cursor = appendGradientSegment(result, template, gradient)
        }

        return result
    }

    /** Appends one gradient segment (tag + following text) and returns the index where the next segment starts. */
    private fun appendGradientSegment(result: MutableComponent, template: String, gradient: MatchResult): Int {
        val colors = parseGradientColorSteps(gradient.groupValues[1])
        if (colors.isEmpty()) {
            result.append(Component.literal(gradient.value))
            return gradient.range.last + 1
        }

        val format = getGradientFormat(template, gradient.range.last + 1)
        val textEnd = getIndexOfGradientTextEnd(template, format.textStart)
        val gradientText = template.substring(format.textStart, textEnd)
        if (gradientText.isNotEmpty()) {
            result.append(getGradientColoredComponent(gradientText, colors, format))
        }
        return textEnd
    }

    private val GRADIENT_FORMATTING_CODES = FormattingCodes.entries
        .filter { it != FormattingCodes.RESET }
        .associateBy { it.code }

    /** Formatting codes following a gradient tag, and where the gradient text starts. Color codes and `§r` are left in place so they end the run. */
    private data class GradientFormat(
        val codes: Set<FormattingCodes> = emptySet(),
        val textStart: Int = 0,
    )

    private fun getGradientFormat(template: String, start: Int): GradientFormat {
        var index = start
        val codes = mutableSetOf<FormattingCodes>()

        while (index + 1 < template.length) {
            val code = GRADIENT_FORMATTING_CODES[template.substring(index, index + 2).lowercase()] ?: break
            codes += code
            index += 2
        }

        return GradientFormat(codes, index)
    }

    private fun Style.apply(codes: Set<FormattingCodes>): Style {
        var style = this
        for (code in codes) {
            style = when (code) {
                FormattingCodes.BOLD -> style.withBold(true)
                FormattingCodes.ITALIC -> style.withItalic(true)
                FormattingCodes.UNDERLINE -> style.withUnderlined(true)
                FormattingCodes.STRIKETHROUGH -> style.withStrikethrough(true)
                FormattingCodes.OBFUSCATED -> style.withObfuscated(true)
                FormattingCodes.RESET -> style
            }
        }
        return style
    }

    private fun getGradientColoredComponent(cleanText: String, colors: IntArray, format: GradientFormat): Component {
        fun styleFor(rgb: Int): Style {
            return Style.EMPTY.withColor(TextColor.fromRgb(rgb and 0xFFFFFF)).apply(format.codes)
        }

        val steps = selectColorSteps(colors, cleanText.length)
        if (steps.size == 1) {
            return Component.literal(cleanText).setStyle(styleFor(steps[0]))
        }

        val last = cleanText.length - 1
        val result = Component.empty()
        for (i in cleanText.indices) {
            val t = i.toFloat() / last
            result.append(Component.literal(cleanText[i].toString()).setStyle(styleFor(interpolateAlongSteps(steps, t))))
        }
        return result
    }

    /**
     * First N colors to spread over the text.
     * Uses fewer steps than characters so the gradient stays visible: one color per letter looks solid, not gradient.
     * E.g. 3 letters get 2 colors, 1-2 letters get 1 color.
     */
    private fun selectColorSteps(colors: IntArray, textLength: Int): IntArray {
        val stepsCount = (textLength - 1).coerceIn(1, colors.size)
        return colors.copyOf(stepsCount)
    }

    private fun interpolateAlongSteps(steps: IntArray, t: Float): Int {
        if (t <= 0f) return steps.first()
        if (t >= 1f) return steps.last()

        val scaled = t * (steps.size - 1)
        val index = scaled.toInt().coerceIn(0, steps.size - 2)
        val localT = scaled - index
        return lerpColor(steps[index], steps[index + 1], localT)
    }

    /** Index where the active gradient run ends: a `§` code, another gradient tag, or the end of the template. */
    private fun getIndexOfGradientTextEnd(template: String, start: Int): Int {
        val section = template.indexOf('§', start).let { if (it == -1) template.length else it }
        val nextGradient = GRADIENT_VARIABLE_REGEX.find(template, start)?.range?.first ?: template.length
        return minOf(section, nextGradient)
    }

    private fun lerpColor(color1: Int, color2: Int, t: Float): Int {
        val r = ((color1 shr 16) and 0xFF) + ((((color2 shr 16) and 0xFF) - ((color1 shr 16) and 0xFF)) * t).toInt()
        val g = ((color1 shr 8) and 0xFF) + ((((color2 shr 8) and 0xFF) - ((color1 shr 8) and 0xFF)) * t).toInt()
        val b = (color1 and 0xFF) + (((color2 and 0xFF) - (color1 and 0xFF)) * t).toInt()
        return (r shl 16) or (g shl 8) or b
    }
}
