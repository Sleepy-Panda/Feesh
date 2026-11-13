package com.github.sleepypanda.feesh.events.models

import net.minecraft.network.chat.Component

/**
 * Called when an action bar message is received, even if it got cancelled by another listener/mod.
 * Set [isCancelled] to stop the message from being shown.
 * Listeners that only read the message should use [ActionBarEvent].
 * @param message The message component received.
 * @param formattedText The formatted text of the message.
 * @param unformattedText The unformatted text of the message.
 * @param isCancelled Whether the message should be hidden.
 */
class ActionBarCancellableEvent(val message: Component, val formattedText: String, val unformattedText: String, var isCancelled: Boolean = false)
