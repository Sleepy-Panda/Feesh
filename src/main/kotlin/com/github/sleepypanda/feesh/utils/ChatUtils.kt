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
    val MOD_PREFIX = "${GRAY}[${AQUA}Feesh${GRAY}]"
    
    /*
     * Sends a message to the local chat. It is visible to the player only.
     * @param message The message to send.
     * @param addModPrefix Whether to add the [Feesh] prefix before the message. Default is false.
     */
    fun sendLocalChat(message: String, addModPrefix: Boolean = false) {
        if (message.isNullOrEmpty()) return
        val formattedMessage = if (addModPrefix) "${MOD_PREFIX} ${RESET}${message}" else message
        FeeshMod.mc.inGameHud.chatHud.addMessage(Text.literal(formattedMessage))
    }

    fun sendLocalChat(message: Text) {
        FeeshMod.mc.inGameHud.chatHud.addMessage(message)
    }

    fun sendAllChat(message: String) {
        if (message.isNullOrEmpty()) return
        FeeshMod.mc.player?.networkHandler?.sendChatCommand("ac ${message}")
    }

    fun sendPartyChat(message: String) {
        if (message.isNullOrEmpty()) return
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
            Text.literal("${MOD_PREFIX} ${RESET}").append(text)
        } else text
        
        FeeshMod.mc.inGameHud.chatHud.addMessage(finalText)
    }

    fun String.removeFormatting(): String {
        if (this.isNullOrEmpty()) return ""
        return this.replace(Regex("§."), "")
    }

    /**
     * Get a string with color and formatting codes.
     * Credits to SkyblockOverhaul
     */
    fun Text.getFormattedString(): String {
        val builder = StringBuilder()

        this.visit(
            { style, str ->
                builder.append(style.getFormatting())
                builder.append(str)
                Optional.empty<Any>()
            },
            Style.EMPTY
        )
        return builder.toString()
    }

    /**
     * Get color and formatting codes for the Style, e.g. §b§l
     */
    private fun Style.getFormatting() = buildString {
        val color = this@getFormatting.color
        if (color != null) append("§").append(getColorChar(color))

        val formatting = when {
            this@getFormatting.isBold() -> BOLD.code
            this@getFormatting.isItalic() -> ITALIC.code
            this@getFormatting.isUnderlined() -> UNDERLINE.code
            this@getFormatting.isStrikethrough() -> STRIKETHROUGH.code
            this@getFormatting.isObfuscated() -> OBFUSCATED.code          
            else -> ""
        }
        append(formatting)
    }

    private fun getColorChar(color: TextColor): Char? {
        val formatting = colorToChar[color]
        return formatting?.code
    }

    private val colorToChar: Map<TextColor, Formatting> = Formatting.entries.mapNotNull { format ->
        TextColor.fromFormatting(format)?.let { it to format }
    }.toMap()

    /**
     * Creates a chat break line (separator) that spans the full width of the chat.
     * The number of characters is calculated based on chat width, character width, and font.
     * @param character The character to use for the break line (default: "-")
     * @return A string with the break line
     */
    fun getChatBreak(character: String = "-"): String {
        if (character.isNullOrEmpty()) return ""

        val mc = FeeshMod.mc
        val textRenderer = mc.textRenderer ?: return ""

        val chatWidth = mc.inGameHud?.chatHud?.width ?: return ""
        val characterWidth = textRenderer.getWidth(Text.literal(character))
        val characterCount = if (characterWidth > 0) (chatWidth / characterWidth).coerceAtLeast(1).coerceAtMost(200) else 50

        return character.repeat(characterCount)
    }
}