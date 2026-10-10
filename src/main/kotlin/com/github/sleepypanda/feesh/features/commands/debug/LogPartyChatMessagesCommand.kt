package com.github.sleepypanda.feesh.features.commands.debug

import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*

/*
 * Toggles duplicating outgoing party chat message into local chat for debug purposes.
 */
object LogPartyChatMessagesCommand {
    const val COMMAND_NAME = "feeshDebugLogPartyChatMessages"

    var isLoggingEnabled = false
        private set

    fun init() {
        RegisterUtils.command(COMMAND_NAME) {
            isLoggingEnabled = !isLoggingEnabled
            ChatUtils.sendLocalChat("${GREEN}Party chat logging: $isLoggingEnabled", true)
        }
    }
}
