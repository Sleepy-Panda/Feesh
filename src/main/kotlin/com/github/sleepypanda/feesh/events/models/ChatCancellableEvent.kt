package com.github.sleepypanda.feesh.events.models

import net.minecraft.network.chat.Component

/*
 * Called when a chat message is about to be sent.
 * @param message The message to send.
 * @param isCancelled Whether the message should be cancelled.
 * @param isOverlay Whether the message is an overlay (actionbar) message.
 */
class ChatCancellableEvent(val message: Component, var isCancelled: Boolean = false, val isOverlay: Boolean = false)
