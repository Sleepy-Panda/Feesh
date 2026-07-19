package com.github.sleepypanda.feesh.events.models

import net.minecraft.network.chat.Component

/*
 * Called when an action bar message is received. Also triggers for cancelled messages.
 * @param message The message component received.
 * @param formattedText The formatted text of the message.
 * @param unformattedText The unformatted text of the message.
 * @param isCancelled Whether the message should be cancelled.
 */
class ActionBarCancellableEvent(val message: Component, val formattedText: String, val unformattedText: String, var isCancelled: Boolean = false)
