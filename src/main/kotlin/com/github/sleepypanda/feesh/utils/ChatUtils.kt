package com.github.sleepypanda.feesh.utils

import net.minecraft.text.Style
import net.minecraft.text.TextColor
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*
import com.github.sleepypanda.feesh.FeeshMod

object ChatUtils {
    fun send(message: String) {
        if (message.isNotEmpty()) FeeshMod.mc.inGameHud.chatHud.addMessage(Text.literal(message))
    }

    fun Text.formattedString(): String {
        val builder = StringBuilder()

        this.visit(
            { style, content ->
                builder.append(style.getFormatCodes())
                builder.append(content)
                Optional.empty<Any>()
            },
            Style.EMPTY
        )
        return builder.toString()
    }

    private fun Style.getFormatCodes() = buildString {
        this@getFormatCodes.color?.let(ChatUtils::getColorFormatChar)?.run { append("§").append(this) }

        if (this@getFormatCodes.isBold) append("§l")
        if (this@getFormatCodes.isItalic) append("§o")
        if (this@getFormatCodes.isUnderlined) append("§n")
        if (this@getFormatCodes.isStrikethrough) append("§m")
        if (this@getFormatCodes.isObfuscated) append("§k")
    }

    private fun getColorFormatChar(color: TextColor): Char? {
        val formatting = colorToFormatChar[color]
        return formatting?.code
    }

    private val colorToFormatChar: Map<TextColor, Formatting> = Formatting.entries.mapNotNull { format ->
        TextColor.fromFormatting(format)?.let { it to format }
    }.toMap()
}