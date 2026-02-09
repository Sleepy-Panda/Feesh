package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.data.PersonalBestData
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager

object PersonalBestCommand {
    const val COMMAND_NAME = "feeshPersonalBest"

    fun init() {
        RegisterUtils.command(COMMAND_NAME) {
            showPersonalBest()
        }
    }

    private fun showPersonalBest() {
        val pb: PersonalBestData = PersistentDataManager.feeshData.personalBest

        val chatBreak = "${GRAY}${ChatUtils.getChatBreak("-")}"
        ChatUtils.sendLocalChat(chatBreak)
        ChatUtils.sendLocalChat("${GREEN}${BOLD}Personal Best", true)
        ChatUtils.sendLocalChat("- Total sharks per festival: ${pb.sharksCaught.amount}")
        ChatUtils.sendLocalChat("- Great White Sharks per festival: ${pb.greatWhiteSharksCaught.amount}")
        ChatUtils.sendLocalChat(chatBreak)
    }
}
