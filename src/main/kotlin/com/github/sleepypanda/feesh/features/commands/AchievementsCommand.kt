package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.features.achievements.AchievementDifficulty
import com.github.sleepypanda.feesh.features.achievements.AchievementsManager
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent.ShowText
import net.minecraft.network.chat.Style

object AchievementsCommand {
    const val COMMAND_NAME = "feeshAchievements"

    fun init() {
        RegisterUtils.command(COMMAND_NAME) {
            showAchievements()
        }
    }

    private fun showAchievements() {
        if (!WorldUtils.isInSkyblock()) return
        val chatBreak = "${GRAY}${ChatUtils.getChatBreak("-")}"
        val allAchievements = AchievementsManager.getAllAchievements()
        val completed = AchievementsManager.getCompletedCount()
        val total = AchievementsManager.getTotalCount()
        val byDifficulty = allAchievements.groupBy { it.difficulty }

        ChatUtils.sendLocalChat(chatBreak)
        ChatUtils.sendLocalChat("${LIGHT_PURPLE}${BOLD}Achievements ${GRAY}($completed/$total)", true)

        for (difficulty in AchievementDifficulty.entries) {
            val entries = byDifficulty[difficulty] ?: continue
            ChatUtils.sendLocalChat("${difficulty.color}${BOLD}${difficulty.displayName}")

            for (achievement in entries.sortedBy { it.displayName.removeFormatting() }) {
                val progress = AchievementsManager.getProgress(achievement.id)
                val status = if (progress.isAchieved) "${GREEN}✔" else "${RED}✗"
                val categories = achievement.categories.joinToString(", ") { it.displayName }
                val categoriesPart = if (categories.isNotEmpty()) " ${DARK_GRAY}[${GRAY}$categories${DARK_GRAY}]" else ""
                val hoverText = buildHoverText(achievement.description, progress.achievedAt)

                val line = Component.literal("$status ${WHITE}${achievement.displayName}$categoriesPart")
                    .withStyle(Style.EMPTY.withHoverEvent(ShowText(Component.literal(hoverText))))

                ChatUtils.sendLocalChat(line)
            }
        }

        ChatUtils.sendLocalChat(chatBreak)
    }

    private fun buildHoverText(description: String, achievedAt: java.util.Date?): String {
        val achievedLine = if (achievedAt != null) {
            "\n${GRAY}Completed: ${WHITE}${CommonUtils.formatDate(achievedAt)}"
        } else ""
        return "${GRAY}$description$achievedLine"
    }
}
