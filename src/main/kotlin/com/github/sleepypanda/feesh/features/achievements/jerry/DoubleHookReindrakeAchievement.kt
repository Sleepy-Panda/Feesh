package com.github.sleepypanda.feesh.features.achievements.jerry

import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.features.achievements.AchievementCategory
import com.github.sleepypanda.feesh.features.achievements.AchievementDifficulty
import com.github.sleepypanda.feesh.features.achievements.AchievementsManager
import com.github.sleepypanda.feesh.features.achievements.BaseAchievement
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object DoubleHookReindrakeAchievement : BaseAchievement(
    id = "double_hook_reindrake",
    displayName = "Double gifts for everyone!",
    description = "Double hook a Reindrake.",
    difficulty = AchievementDifficulty.HARD,
    categories = listOf(AchievementCategory.JERRY_WORKSHOP, AchievementCategory.WATER),
) {
    override fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreatureCaught)
    }

    private fun onSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        CommonUtils.runWithCatching("Failed to check and handle achievement $id") {
            if (!AchievementsManager.isEnabled() || isAchieved()) return@runWithCatching
            if (!WorldUtils.isInSkyblock() || WorldUtils.isOnAlpha() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return@runWithCatching
            if (!event.seaCreatureName.equals(SeaCreatureNames.REINDRAKE, ignoreCase = true) || !event.isDoubleHook) return@runWithCatching

            completeAndAnnounce()
        }
    }
}
