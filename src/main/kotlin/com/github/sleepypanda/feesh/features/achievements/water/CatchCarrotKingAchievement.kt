package com.github.sleepypanda.feesh.features.achievements.water

import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.features.achievements.AchievementCategory
import com.github.sleepypanda.feesh.features.achievements.AchievementDifficulty
import com.github.sleepypanda.feesh.features.achievements.AchievementsManager
import com.github.sleepypanda.feesh.features.achievements.BaseAchievement
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object CatchCarrotKingAchievement : BaseAchievement(
    id = "catch_carrot_king",
    displayName = "Carrot eater",
    description = "Catch a Carrot King.",
    difficulty = AchievementDifficulty.EASY,
    categories = listOf(AchievementCategory.WATER),
) {
    override fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreatureCaught)
    }

    private fun onSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        CommonUtils.runWithCatching("Failed to check and handle achievement $id") {
            if (!AchievementsManager.isEnabled() || isAchieved()) return@runWithCatching
            if (!WorldUtils.isInSkyblock() || WorldUtils.isOnAlpha()) return@runWithCatching
            if (!event.seaCreatureName.equals(SeaCreatureNames.CARROT_KING, ignoreCase = true)) return@runWithCatching
    
            completeAndAnnounce()
        }
    }
}
