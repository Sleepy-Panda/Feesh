package com.github.sleepypanda.feesh.features.achievements.jerry

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.BlizzardInABottleConsumedEvent
import com.github.sleepypanda.feesh.features.achievements.AchievementCategory
import com.github.sleepypanda.feesh.features.achievements.AchievementDifficulty
import com.github.sleepypanda.feesh.features.achievements.AchievementsManager
import com.github.sleepypanda.feesh.features.achievements.BaseAchievement
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object StartBlizzardAchievement : BaseAchievement(
    id = "start_blizzard",
    displayName = "Philanthropist",
    description = "Start a Blizzard for the first time.",
    difficulty = AchievementDifficulty.EASY,
    categories = listOf(AchievementCategory.JERRY_WORKSHOP, AchievementCategory.WATER),
) {
    override fun init() {
        EventBus.subscribe(BlizzardInABottleConsumedEvent::class, ::onBlizzardStarted)
    }

    private fun onBlizzardStarted(event: BlizzardInABottleConsumedEvent) {
        CommonUtils.runWithCatching("Failed to check and handle achievement $id") {
            if (!AchievementsManager.isEnabled() || isAchieved()) return@runWithCatching
            if (!WorldUtils.isInSkyblock()) return@runWithCatching
    
            completeAndAnnounce()
        }
    }
}