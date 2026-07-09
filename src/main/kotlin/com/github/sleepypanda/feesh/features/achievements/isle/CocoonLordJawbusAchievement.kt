package com.github.sleepypanda.feesh.features.achievements.isle

import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.SeaCreatureCocoonedByYouEvent
import com.github.sleepypanda.feesh.features.achievements.AchievementCategory
import com.github.sleepypanda.feesh.features.achievements.AchievementDifficulty
import com.github.sleepypanda.feesh.features.achievements.AchievementsManager
import com.github.sleepypanda.feesh.features.achievements.BaseAchievement
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object CocoonLordJawbusAchievement : BaseAchievement(
    id = "cocoon_lord_jawbus",
    displayName = "Lord Jawbus - Are you dead yet?",
    description = "Cocoon a Lord Jawbus.",
    difficulty = AchievementDifficulty.HARD,
    categories = listOf(AchievementCategory.CRIMSON_ISLE, AchievementCategory.LAVA),
) {
    override fun init() {
        EventBus.subscribe(SeaCreatureCocoonedByYouEvent::class, ::onSeaCreatureCocooned)
    }

    private fun onSeaCreatureCocooned(event: SeaCreatureCocoonedByYouEvent) {
        CommonUtils.runWithCatching("Failed to check and handle achievement $id") {
            if (!AchievementsManager.isEnabled() || isAchieved()) return@runWithCatching
            if (!WorldUtils.isInSkyblock() || WorldUtils.isOnAlpha() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return@runWithCatching
            if (!event.seaCreatureName.equals(SeaCreatureNames.LORD_JAWBUS, ignoreCase = true)) return@runWithCatching

            completeAndAnnounce()
        }
    }
}
