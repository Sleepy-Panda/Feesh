package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.text.Style
import net.minecraft.text.TextColor
import net.minecraft.text.Text
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.ClickEvent.RunCommand
import net.minecraft.text.HoverEvent.ShowText
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
        val formattedMessage = if (addModPrefix) "${GOLD}[Feesh] ${RESET}${message}" else message
        FeeshMod.mc.inGameHud.chatHud.addMessage(Text.literal(formattedMessage))
    }

    fun sendAllChat(message: String) {
        if (message.isNullOrEmpty()) return
        sendLocalChat(message) // TODO replace
        //FeeshMod.mc.player?.networkHandler?.sendChatCommand("ac ${message}")
    }

    fun sendPartyChat(message: String) {
        if (message.isNullOrEmpty()) return
        sendLocalChat(message) // TODO replace
        FeeshMod.mc.player?.networkHandler?.sendChatCommand("pchat ${message}")
    }

    fun command(command: String) {
        if (command.isNullOrEmpty()) return
        FeeshMod.mc.player?.networkHandler?.sendChatCommand(command)
    }

    /*
     * Sends a message to the local chat with a clickable command.
     * @param message The chat message to send.
     * @param command The command to execute when clicked (e.g. "warp jerry").
     * @param addModPrefix Whether to add the [Feesh] prefix before the message. Default is false.
     */
    fun sendLocalChatWithCommand(message: String, command: String, addModPrefix: Boolean = false) {
        if (message.isNullOrEmpty() || command.isNullOrEmpty()) return
        
        val style = Style.EMPTY
            .withClickEvent(RunCommand("/$command"))
            .withHoverEvent(ShowText(Text.literal("Click to execute /$command")))
        
        val text = Text.literal(message).setStyle(style)
        
        val finalText = if (addModPrefix) {
            Text.literal("${GOLD}[Feesh] ${RESET}").append(text)
        } else text
        
        FeeshMod.mc.inGameHud.chatHud.addMessage(finalText)
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
                sibling.style.isBold() -> BOLD.code
                sibling.style.isItalic() -> ITALIC.code
                sibling.style.isUnderlined() -> UNDERLINE.code
                sibling.style.isStrikethrough() -> STRIKETHROUGH.code
                sibling.style.isObfuscated() -> OBFUSCATED.code          
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