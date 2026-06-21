package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import net.minecraft.client.gui.components.ChatComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.ClickEvent.OpenUrl
import net.minecraft.network.chat.ClickEvent.RunCommand
import net.minecraft.network.chat.HoverEvent.ShowText
import net.minecraft.ChatFormatting
import java.util.*

object ChatUtils {
    val MOD_PREFIX = "${GRAY}[${AQUA}Feesh${GRAY}]"

    private enum class ChatType { ALL_CHAT, PARTY_CHAT }

    private data class QueuedChatMessage(val chatType: ChatType, val message: String)

    private val messageQueue = ArrayDeque<QueuedChatMessage>()
    private var lastSendTime = null as Long?
    private const val COOLDOWN_MS = 1000L

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        processQueue()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        messageQueue.clear()
    }

    private fun processQueue() {
        if (!WorldUtils.isInSkyblock()) return
        
        val next = messageQueue.firstOrNull() ?: return
        val now = System.currentTimeMillis()

        if (lastSendTime == null || now - lastSendTime!! >= COOLDOWN_MS) {
            messageQueue.removeFirst()
            lastSendTime = now
            when (next.chatType) {
                ChatType.ALL_CHAT -> sendAllChatImmediate(next.message)
                ChatType.PARTY_CHAT -> sendPartyChatImmediate(next.message)
            }
        }
    }

    /*
     * Sends a message to the local chat. It is visible to the player only.
     * @param message The message to send.
     * @param addModPrefix Whether to add the [Feesh] prefix before the message. Default is false.
     */
    fun sendLocalChat(message: String, addModPrefix: Boolean = false) {
        if (message.isNullOrEmpty()) return
        val formattedMessage = if (addModPrefix) "${MOD_PREFIX} ${RESET}${message}" else message
        addLocalChatMessage(Component.literal(formattedMessage))
    }

    fun sendLocalChat(message: Component) {
        addLocalChatMessage(message)
    }
   
    private fun addLocalChatMessage(message: Component) {
        FeeshMod.mc.addClientChatMessageCompat(message)
    }

    /**
     * Queues a message to be sent to All chat. Messages are sent with 1 second cooldown
     * between them to avoid Hypixel's "You're sending messages too fast" rejection.
     */
    fun sendAllChat(message: String) {
        if (message.isNullOrEmpty()) return
        messageQueue.add(QueuedChatMessage(ChatType.ALL_CHAT, message.removeFormatting()))
    }

    /**
     * Queues a message to be sent to Party chat. Messages are sent with 1 second cooldown
     * between them to avoid Hypixel's "You're sending messages too fast" rejection.
     */
    fun sendPartyChat(message: String) {
        if (message.isNullOrEmpty()) return
        messageQueue.add(QueuedChatMessage(ChatType.PARTY_CHAT, message.removeFormatting()))
    }

    private fun sendAllChatImmediate(message: String) {
        FeeshMod.mc.player?.connection?.sendCommand("ac $message")
    }

    private fun sendPartyChatImmediate(message: String) {
        FeeshMod.mc.player?.connection?.sendCommand("pchat $message")
    }

    fun command(command: String) {
        if (command.isNullOrEmpty()) return
        FeeshMod.mc.player?.connection?.sendCommand(command)
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
            .withHoverEvent(ShowText(Component.literal("Click to execute /$command")))
        
        val text = Component.literal(message).setStyle(style)
        
        val finalText = if (addModPrefix) {
            Component.literal("${MOD_PREFIX} ${RESET}").append(text)
        } else text
        
        addLocalChatMessage(finalText)
    }

    /**
     * Sends a message to local chat with a clickable URL.
     * @param message The main message text (can include formatting codes).
     * @param linkText The clickable link text (can include formatting codes).
     * @param url The URL to open when the link is clicked.
     */
    fun sendLocalChatWithUrl(message: String, linkText: String, url: String, addModPrefix: Boolean = false) {
        if (message.isNullOrEmpty() || url.isNullOrEmpty() || linkText.isNullOrEmpty()) return

        val linkStyle = Style.EMPTY
            .withClickEvent(OpenUrl(java.net.URI.create(url)))
            .withHoverEvent(ShowText(Component.literal("Click to open $url")))

        val fullText = Component.literal(message).append(Component.literal("\n")).append(Component.literal(linkText).setStyle(linkStyle))
        val finalText = if (addModPrefix) {
            Component.literal("${MOD_PREFIX} ${RESET}").append(fullText)
        } else fullText

        addLocalChatMessage(finalText)
    }

    fun String.removeFormatting(): String {
        if (this.isNullOrEmpty()) return ""
        return this.replace(Regex("§."), "")
    }

    /**
     * Get a string with color and formatting codes.
     * Credits to SkyblockOverhaul
     */
    fun Component.getFormattedString(): String {
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
            this@getFormatting.isBold -> BOLD.code
            this@getFormatting.isItalic -> ITALIC.code
            this@getFormatting.isUnderlined -> UNDERLINE.code
            this@getFormatting.isStrikethrough -> STRIKETHROUGH.code
            this@getFormatting.isObfuscated -> OBFUSCATED.code
            else -> ""
        }
        append(formatting)
    }

    private fun getColorChar(color: TextColor): Char? {
        val formatting = colorToChar[color] ?: return null
        //#if MC >= 26.2
        //$$ return legacyFormattingChars[formatting]
        //#else
        return formatting.char
        //#endif
    }

    //#if MC >= 26.2
    //$$ private val legacyFormattingChars = mapOf(
    //$$     ChatFormatting.BLACK to '0',
    //$$     ChatFormatting.DARK_BLUE to '1',
    //$$     ChatFormatting.DARK_GREEN to '2',
    //$$     ChatFormatting.DARK_AQUA to '3',
    //$$     ChatFormatting.DARK_RED to '4',
    //$$     ChatFormatting.DARK_PURPLE to '5',
    //$$     ChatFormatting.GOLD to '6',
    //$$     ChatFormatting.GRAY to '7',
    //$$     ChatFormatting.DARK_GRAY to '8',
    //$$     ChatFormatting.BLUE to '9',
    //$$     ChatFormatting.GREEN to 'a',
    //$$     ChatFormatting.AQUA to 'b',
    //$$     ChatFormatting.RED to 'c',
    //$$     ChatFormatting.LIGHT_PURPLE to 'd',
    //$$     ChatFormatting.YELLOW to 'e',
    //$$     ChatFormatting.WHITE to 'f',
    //$$ )
    //#endif

    private val colorToChar: Map<TextColor, ChatFormatting> = ChatFormatting.entries.mapNotNull { format ->
        TextColor.fromLegacyFormat(format)?.let { it to format }
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
        val textRenderer = mc.font

        val chatWidth = ChatComponent.getWidth(mc.options.chatWidth().get())

        val characterWidth = textRenderer.width(Component.literal(character))
        val characterCount = if (characterWidth > 0) (chatWidth / characterWidth).coerceAtLeast(1).coerceAtMost(200) else 50

        return character.repeat(characterCount)
    }
}