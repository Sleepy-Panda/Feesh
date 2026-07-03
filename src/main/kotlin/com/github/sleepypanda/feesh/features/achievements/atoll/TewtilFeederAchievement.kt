package com.github.sleepypanda.feesh.features.achievements.atoll

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.features.achievements.AchievementCategory
import com.github.sleepypanda.feesh.features.achievements.AchievementDifficulty
import com.github.sleepypanda.feesh.features.achievements.AchievementsManager
import com.github.sleepypanda.feesh.features.achievements.BaseAchievement
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object TewtilFeederAchievement : BaseAchievement(
    id = "tewtil_dietician",
    displayName = "Tewtil dietician",
    description = "Feed a Tewtil with tasty food until it explodes.",
    difficulty = AchievementDifficulty.EASY,
    categories = listOf(AchievementCategory.LOTUS_ATOLL, AchievementCategory.WATER),
) {
    private const val MESSAGE_TO_CHECK = "The Tewtil got so large that it exploded!"

    override fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        CommonUtils.runWithCatching("Failed to check and handle achievement $id") {
            if (!AchievementsManager.isEnabled() || isAchieved()) return@runWithCatching
            if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL) return@runWithCatching
            if (!event.unformattedText.contains(MESSAGE_TO_CHECK)) return@runWithCatching

            completeAndAnnounce()
        }
    }
}
