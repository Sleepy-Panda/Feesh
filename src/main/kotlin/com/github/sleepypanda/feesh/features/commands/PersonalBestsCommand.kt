package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.data.PersonalBestData
import com.github.sleepypanda.feesh.utils.data.PersonalBestEntry
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent.ShowText
import net.minecraft.network.chat.Style

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
        sendPersonalBestChatLine(
            label = "${DARK_AQUA}🦈 Total sharks per festival",
            entry = pb.sharksCaught,
            description = "Biggest count of sharks caught within a single fishing festival."
        )
        sendPersonalBestChatLine(
            label = "${DARK_AQUA}🦈 Great White Sharks per festival",
            entry = pb.greatWhiteSharksCaught,
            description = "Biggest count of Great White sharks caught within a single fishing festival."
        )
        sendPersonalBestChatLine(
            label = "${BLUE}⚓ Double Hook streak",
            entry = pb.doubleHookStreak,
            description = "Longest chain of Double Hook sea creature catches."
        )
        sendPersonalBestChatLine(
            label = "${YELLOW}⛃ Treasure catch streak",
            entry = pb.treasureCatchesStreak,
            description = "Longest chain of treasure/junk catches."
        )
        sendPersonalBestChatLine(
            label = "${GOLD}⛃ Great treasures streak",
            entry = pb.greatTreasuresStreak,
            description = "Longest chain of caught treasures/junk being Great / Outstanding."
        )
        sendPersonalBestChatLine(
            label = "${LIGHT_PURPLE}⛃ Outstanding treasures streak",
            entry = pb.outstandingTreasuresStreak,
            description = "Longest chain of caught treasures/junk being Great / Outstanding."
        )
        sendPersonalBestChatLine(
            label = "${DARK_PURPLE}☯ Total Moby-Ducks consumed",
            entry = pb.totalMobyDucksConsumed,
            description = "Total Moby-Duck: Collector's Edition you have ever consumed within your playtime."
        )
        ChatUtils.sendLocalChat(chatBreak)
    }

    private fun sendPersonalBestChatLine(label: String, entry: PersonalBestEntry, description: String) {
        val amountStr = CommonUtils.formatNumberWithSpaces(entry.amount)
        val dateStr = CommonUtils.formatDate(entry.at).ifBlank { "N/A" }
        val tooltip = "${GRAY}$description\n\n${GRAY}Achieved at: ${WHITE}${dateStr}"
        val line = Component.literal("${GRAY}- $label${GRAY}: ${WHITE}${amountStr}")
            .setStyle(Style.EMPTY.withHoverEvent(ShowText(Component.literal(tooltip))))
        ChatUtils.sendLocalChat(line)
    }
}
