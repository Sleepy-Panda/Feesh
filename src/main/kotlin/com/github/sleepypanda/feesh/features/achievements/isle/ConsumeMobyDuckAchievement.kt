package com.github.sleepypanda.feesh.features.achievements.isle

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.MobyDuckConsumedEvent
import com.github.sleepypanda.feesh.features.achievements.AchievementCategory
import com.github.sleepypanda.feesh.features.achievements.AchievementDifficulty
import com.github.sleepypanda.feesh.features.achievements.AchievementsManager
import com.github.sleepypanda.feesh.features.achievements.BaseAchievement
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object ConsumeMobyDuckAchievement : BaseAchievement(
    id = "consume_moby_duck",
    displayName = "Wise choice",
    description = "Consume a Moby Duck for the first time to buff your Fishing Wisdom.",
    difficulty = AchievementDifficulty.EASY,
    categories = listOf(AchievementCategory.CRIMSON_ISLE, AchievementCategory.LAVA),
) {
    override fun init() {
        EventBus.subscribe(MobyDuckConsumedEvent::class, ::onMobyDuckConsumed)
    }

    private fun onMobyDuckConsumed(event: MobyDuckConsumedEvent) {
        CommonUtils.runWithCatching("Failed to check and handle achievement $id") {
            if (!AchievementsManager.isEnabled() || isAchieved()) return@runWithCatching
            if (!WorldUtils.isInSkyblock()) return@runWithCatching
    
            completeAndAnnounce()
        }
    }
}