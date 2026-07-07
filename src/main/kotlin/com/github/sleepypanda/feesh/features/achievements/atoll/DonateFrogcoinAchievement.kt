package com.github.sleepypanda.feesh.features.achievements.atoll

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.features.achievements.AchievementCategory
import com.github.sleepypanda.feesh.features.achievements.AchievementDifficulty
import com.github.sleepypanda.feesh.features.achievements.AchievementsManager
import com.github.sleepypanda.feesh.features.achievements.BaseAchievement
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object DonateFrogcoinAchievement : BaseAchievement(
    id = "donate_frogcoin",
    displayName = "Blessed",
    description = "Donate a Frogcoin in the Lotus Eater's Cave.",
    difficulty = AchievementDifficulty.EASY,
    categories = listOf(AchievementCategory.LOTUS_ATOLL, AchievementCategory.WATER),
) {
    private val MESSAGE_REGEX = Regex("WISE! You've been granted (.*) for 30m while on the Lotus Atoll!")

    override fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        CommonUtils.runWithCatching("Failed to check and handle achievement $id") {
            if (!AchievementsManager.isEnabled() || isAchieved()) return@runWithCatching
            if (!WorldUtils.isInSkyblock() || WorldUtils.isOnAlpha()) return@runWithCatching
            if (!MESSAGE_REGEX.matches(event.unformattedText)) return@runWithCatching
    
            completeAndAnnounce()
        }
    }
}
