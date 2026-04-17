package com.github.sleepypanda.feesh.events.models

import net.minecraft.network.chat.Component

/*
 * Called when a chat message is about to be sent.
 * @param message The message to send.
 * @param isCancelled Whether the message should be cancelled.
 */
class ChatCancellableEvent(val message: Component, var isCancelled: Boolean = false)
