package com.github.sleepypanda.feesh.features.achievements.atoll

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.features.achievements.AchievementCategory
import com.github.sleepypanda.feesh.features.achievements.AchievementDifficulty
import com.github.sleepypanda.feesh.features.achievements.AchievementsManager
import com.github.sleepypanda.feesh.features.achievements.BaseAchievement
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object FullRideOnPuddleJumperAchievement : BaseAchievement(
    id = "puddle_jumper_slow_ride",
    displayName = "Slow ride, take it easy",
    description = "Ride a Puddle Jumper till its destination without missing a single jump.",
    difficulty = AchievementDifficulty.EASY,
    categories = listOf(AchievementCategory.LOTUS_ATOLL, AchievementCategory.WATER),
) {
    private const val MESSAGE_TO_CHECK = "[MOB] Puddle Jumper: Wow! You're a master jumper!"

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
