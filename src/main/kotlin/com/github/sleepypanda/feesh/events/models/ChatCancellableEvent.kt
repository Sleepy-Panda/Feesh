package com.github.sleepypanda.feesh.events.models

import net.minecraft.text.Text

/*
 * Called when a chat message is about to be sent.
 * @param message The message to send.
 * @param isCancelled Whether the message should be cancelled.
 */
class ChatCancellableEvent(val message: Text, var isCancelled: Boolean = false)
