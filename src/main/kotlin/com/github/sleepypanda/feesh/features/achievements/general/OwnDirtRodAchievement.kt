package com.github.sleepypanda.feesh.features.achievements.general

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.features.achievements.AchievementCategory
import com.github.sleepypanda.feesh.features.achievements.AchievementDifficulty
import com.github.sleepypanda.feesh.features.achievements.AchievementsManager
import com.github.sleepypanda.feesh.features.achievements.BaseAchievement
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object OwnDirtRodAchievement : BaseAchievement(
    id = "own_dirt_rod",
    displayName = "Dirty business",
    description = "Have a Dirt Rod in your inventory.",
    difficulty = AchievementDifficulty.MEDIUM,
    categories = listOf(AchievementCategory.GENERAL),
) {
    private var tickCounter = 0
    private const val TICKS_PER_CHECK = 20

    override fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        CommonUtils.runWithCatching("Failed to check and handle achievement $id") {
            if (!AchievementsManager.isEnabled() || isAchieved()) return@runWithCatching
            if (!WorldUtils.isInSkyblock() || WorldUtils.isOnAlpha()) return@runWithCatching

            tickCounter++
            if (tickCounter < TICKS_PER_CHECK) return@runWithCatching
            tickCounter = 0

            val rodName = PlayerUtils.getFishingRodInHand()?.itemNameUnformatted ?: return@runWithCatching
            if (!rodName.contains("Dirt Rod", ignoreCase = true)) return@runWithCatching

            completeAndAnnounce()
        }
    }
}
