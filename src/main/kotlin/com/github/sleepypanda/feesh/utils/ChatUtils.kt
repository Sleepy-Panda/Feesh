package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.utils.enums.ColorCodes
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes
import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.text.Style
import net.minecraft.text.TextColor
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*

object ChatUtils {
    /*
     * Sends a message to the local chat. It is visible to the player only.
     * @param message The message to send.
     * @param addModPrefix Whether to add the [Feesh] prefix before the message. Default is false.
     */
    fun sendLocalChat(message: String, addModPrefix: Boolean = false) {
        if (message.isNullOrEmpty()) return
        val formattedMessage = if (addModPrefix) "${ColorCodes.GOLD}[Feesh] ${FormattingCodes.RESET}${message}" else message
        FeeshMod.mc.inGameHud.chatHud.addMessage(Text.literal(formattedMessage))
    }

    fun sendAllChat(message: String) {
        if (message.isNullOrEmpty()) return
        FeeshMod.mc.player?.networkHandler?.sendChatCommand("ac ${message}")
    }

    fun sendPartyChat(message: String) {
        if (message.isNullOrEmpty()) return
        FeeshMod.mc.player?.networkHandler?.sendChatCommand("pchat ${message}")
    }

    fun isDoubleHook(): Boolean {
        val pattern = Regex("§eIt's a §r§aDouble Hook§r§e!(?: Woot woot!)?")
        val chatHud = FeeshMod.mc.inGameHud.chatHud
        val history = chatHud.messageHistory
        val isDoubleHook = if (history.size >= 2) {
            val previousMessage = history[1]
            FeeshMod.LOGGER.info("FEESH PREV MSG : ${previousMessage}")
            pattern.matches(previousMessage)
            //previousMessage.removeFormatting().contains("It\'s a Double Hook!")
        } else {
            false
        }

        return isDoubleHook
    }

    fun String.removeFormatting(): String {
        if (this.isNullOrEmpty()) return ""
        return this.replace(Regex("§."), "")
    }

    fun Text.getFormatted(): String {
        val rawCodes = this.siblings.joinToString("") { sibling ->
            when {
                sibling.style.color != null -> "§" + getColorFormatChar(sibling.style.color!!)
                else -> ""
            } +
            when {
                sibling.style.isBold() -> FormattingCodes.BOLD.code
                sibling.style.isItalic() -> FormattingCodes.ITALIC.code
                sibling.style.isUnderlined() -> FormattingCodes.UNDERLINE.code
                sibling.style.isStrikethrough() -> FormattingCodes.STRIKETHROUGH.code
                sibling.style.isObfuscated() -> FormattingCodes.OBFUSCATED.code          
                else -> ""
            } + sibling.string
        }
        return rawCodes
    }

    //fun Text.formattedString(): String {
    //    val builder = StringBuilder()
//
    //    this.visit(
    //        { style, content ->
    //            builder.append(style.getFormatCodes())
    //            builder.append(content)
    //            Optional.empty<Any>()
    //        },
    //        Style.EMPTY
    //    )
    //    return builder.toString()
    //}
//
    //private fun Style.getFormatCodes() = buildString {
    //    this@getFormatCodes.color?.let(ChatUtils::getColorFormatChar)?.run { append("§").append(this) }
//
    //    if (this@getFormatCodes.isBold) append("§l")
    //    if (this@getFormatCodes.isItalic) append("§o")
    //    if (this@getFormatCodes.isUnderlined) append("§n")
    //    if (this@getFormatCodes.isStrikethrough) append("§m")
    //    if (this@getFormatCodes.isObfuscated) append("§k")
    //}

    private fun getColorFormatChar(color: TextColor): Char? {
        val formatting = colorToFormatChar[color]
        return formatting?.code
    }

    private val colorToFormatChar: Map<TextColor, Formatting> = Formatting.entries.mapNotNull { format ->
        TextColor.fromFormatting(format)?.let { it to format }
    }.toMap()
}