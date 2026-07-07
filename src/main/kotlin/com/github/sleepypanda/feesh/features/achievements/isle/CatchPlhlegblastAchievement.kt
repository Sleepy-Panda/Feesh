package com.github.sleepypanda.feesh.features.achievements.isle

import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.features.achievements.AchievementCategory
import com.github.sleepypanda.feesh.features.achievements.AchievementDifficulty
import com.github.sleepypanda.feesh.features.achievements.AchievementsManager
import com.github.sleepypanda.feesh.features.achievements.BaseAchievement
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object CatchPlhlegblastAchievement : BaseAchievement(
    id = "catch_plhlegblast",
    displayName = "Plhlegblast catcher",
    description = "Fish up a Plhlegblast.",
    difficulty = AchievementDifficulty.MEDIUM,
    categories = listOf(AchievementCategory.CRIMSON_ISLE, AchievementCategory.LAVA),
) {
    override fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreatureCaught)
    }

    private fun onSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        CommonUtils.runWithCatching("Failed to check and handle achievement $id") {
            if (!AchievementsManager.isEnabled() || isAchieved()) return@runWithCatching
            if (!WorldUtils.isInSkyblock() || WorldUtils.isOnAlpha() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return@runWithCatching
            if (!event.seaCreatureName.equals(SeaCreatureNames.PLHLEGBLAST, ignoreCase = true)) return@runWithCatching
    
            completeAndAnnounce()
        }
    }
}
