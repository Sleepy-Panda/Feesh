package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.data.PersonalBestData
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager

object PersonalBestsCommand {
    const val COMMAND_NAME = "feeshPersonalBests"

    fun init() {
        RegisterUtils.command(COMMAND_NAME) {
            showPersonalBest()
        }
    }

    private fun showPersonalBest() {
        val pb: PersonalBestData = PersistentDataManager.feeshData.personalBest
        val chatBreak = "${GRAY}${ChatUtils.getChatBreak("-")}"

        ChatUtils.sendLocalChat(chatBreak)
        ChatUtils.sendLocalChat("${GREEN}${BOLD}Personal Bests", true)
        ChatUtils.sendLocalChat("- Total sharks per festival: ${CommonUtils.formatNumberWithSpaces(pb.sharksCaught.amount)}")
        ChatUtils.sendLocalChat("- Great White Sharks per festival: ${CommonUtils.formatNumberWithSpaces(pb.greatWhiteSharksCaught.amount)}")
        ChatUtils.sendLocalChat("- Double Hook streak: ${CommonUtils.formatNumberWithSpaces(pb.doubleHookStreak.amount)}")
        ChatUtils.sendLocalChat("- Total Moby-Ducks consumed: ${CommonUtils.formatNumberWithSpaces(pb.totalMobyDucksConsumed.amount)}")
        ChatUtils.sendLocalChat("- Total Blizzards started: ${CommonUtils.formatNumberWithSpaces(pb.totalBlizzardsStarted.amount)}")
        ChatUtils.sendLocalChat(chatBreak)
    }
}
