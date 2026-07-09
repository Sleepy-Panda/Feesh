package com.github.sleepypanda.feesh.features.achievements

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import java.util.Date
import java.util.Timer
import kotlin.concurrent.timerTask

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
    val tip: String? = null,
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
        if (!AchievementsManager.isEnabled() || !isAchieved()) return

        Timer(true).schedule(timerTask {
            FeeshMod.mc.execute {
                CommonUtils.runWithCatching("Failed to announce achievement $id") {
                    if (!AchievementsManager.isEnabled() || !isAchieved()) return@runWithCatching

                    val achievementTitle = when (difficulty) {
                        AchievementDifficulty.PROFICIENT,
                        AchievementDifficulty.IMPOSSIBLE -> "${BLUE}${OBFUSCATED}x ${AQUA}${displayName} ${BLUE}${OBFUSCATED}x"
                        else -> "${AQUA}${displayName}"
                    }

                    ChatUtils.sendLocalChat("${LIGHT_PURPLE}${BOLD}ACHIEVEMENT! ${achievementTitle} ${GRAY}[${difficulty.color}${difficulty.displayName}${GRAY}]", true)
                    ChatUtils.sendLocalChat("${GRAY}$description", false)

                    CommonUtils.showTitle("${LIGHT_PURPLE}${BOLD}ACHIEVEMENT!", achievementTitle)
                    SoundUtils.playSound(difficulty.sound)
                }
            }
        }, 2_000L)
    }
}
