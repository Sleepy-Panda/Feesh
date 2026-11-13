package com.github.sleepypanda.feesh.events.models

import net.minecraft.network.chat.Component

/**
 * Called when a chat message is received, even if it got cancelled by another listener/mod.
 * Subscribers cannot cancel the message. Use [ChatCancellableEvent] for that.
 * @param message The message component received.
 * @param formattedText The formatted text of the message.
 * @param unformattedText The unformatted text of the message.
 */
class ChatEvent(val message: Component, val formattedText: String, val unformattedText: String)
