package com.github.sleepypanda.feesh.features.achievements.isle

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.features.achievements.AchievementCategory
import com.github.sleepypanda.feesh.features.achievements.AchievementDifficulty
import com.github.sleepypanda.feesh.features.achievements.AchievementsManager
import com.github.sleepypanda.feesh.features.achievements.BaseAchievement
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object ChargeHurricaneBottleAchievement : BaseAchievement(
    id = "charge_hurricane_bottle",
    displayName = "Hurricane",
    description = "Charge a Hurricane Bottle.",
    difficulty = AchievementDifficulty.MEDIUM,
    categories = listOf(AchievementCategory.CRIMSON_ISLE, AchievementCategory.LAVA),
) {
    private val HURRICANE_BOTTLE_CHARGED_MESSAGE = Regex("^> Your Hurricane in a Bottle has fully charged\\!$")

    override fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        CommonUtils.runWithCatching("Failed to check and handle achievement $id") {
            if (!AchievementsManager.isEnabled() || isAchieved()) return@runWithCatching
            if (!WorldUtils.isInSkyblock()) return@runWithCatching
            if (!HURRICANE_BOTTLE_CHARGED_MESSAGE.matches(event.unformattedText)) return@runWithCatching
    
            completeAndAnnounce()
        }
    }
}
