package com.github.sleepypanda.feesh.events

import net.minecraft.text.Text

/*
 * Called when a party chat message is received.
 * @param message The raw message.
 * @param rankAndPlayer The rank and player name.
 * @param messagePayload The message payload (the part after the rank and player name).
 */
class PartyChatEvent(val message: Text, val rankAndPlayer: String, val messagePayload: String)