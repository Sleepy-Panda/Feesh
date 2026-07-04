package com.github.sleepypanda.feesh.features.achievements

import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import java.util.Date

enum class AchievementDifficulty(val displayName: String, val color: ColorCodes, val sound: SoundEvent) {
    EASY("Easy", WHITE, SoundEvents.PLAYER_LEVELUP),
    MEDIUM("Medium", YELLOW, SoundEvents.PLAYER_LEVELUP),
    HARD("Hard", RED, SoundEvents.PLAYER_LEVELUP),
    PROFICIENT("ProFISHient", DARK_RED, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE),
    IMPOSSIBLE("Impossible", DARK_RED, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE),
}

enum class AchievementCategory(val displayName: String) {
    GENERAL("General"),
    WATER("Water"),
    LAVA("Lava"),
    HOTSPOTS("Hotspots"),
    CRIMSON_ISLE("Crimson Isle"),
    JERRY_WORKSHOP("Jerry Workshop"),
    BACKWATER_BAYOU("Backwater Bayou"),
    GALATEA("Galatea"),
    LOTUS_ATOLL("Lotus Atoll"),
    CRYSTAL_HOLLOWS("Crystal Hollows"),
    SPOOKY("Spooky"),
    FISHING_FESTIVAL("Fishing Festival"),
    TREASURE("Treasure"),
    TROPHY("Trophy"),
}

abstract class BaseAchievement(
    val id: String,
    val displayName: String,
    val description: String,
    val difficulty: AchievementDifficulty,
    val categories: List<AchievementCategory> = emptyList(),
) {
    abstract fun init()

    fun isAchieved(): Boolean = AchievementsManager.getProgress(id).isAchieved

    fun completeAndAnnounce() {
        complete()
        announce()
    }

    private fun complete() {
        CommonUtils.runWithCatching("Failed to mark achievement $id as achieved") {
            if (!AchievementsManager.isEnabled() || isAchieved()) return

            val progress = AchievementsManager.getProgress(id)
            progress.isAchieved = true
            progress.achievedAt = Date()
            AchievementsManager.persistentData[id] = progress
            AchievementsManager.save()
        }
    }

    private fun announce() {
        CommonUtils.runWithCatching("Failed to announce achievement $id") {
            // TODO: 2 seconds delay to avoid overlapping events
            if (!AchievementsManager.isEnabled() || !isAchieved()) return
    
            val achievement = when (difficulty) {
                AchievementDifficulty.PROFICIENT
                AchievementDifficulty.IMPOSSIBLE -> "${DARK_RED}${OBFUSCATED}x ${AQUA}${displayName} ${DARK_RED}${OBFUSCATED}x"
                else -> "${AQUA}${displayName}"
            }

            ChatUtils.sendLocalChat("${GREEN}${BOLD}Achievement unlocked: ${achievement} ${GRAY}[${difficulty.color}${difficulty.displayName}${GRAY}]", true)
            ChatUtils.sendLocalChat("${GRAY}$description", false)
            
            CommonUtils.showTitle(displayName, "${GREEN}${BOLD}ACHIEVEMENT UNLOCKED!")
            SoundUtils.playSound(difficulty.sound)
        }
    }
}
