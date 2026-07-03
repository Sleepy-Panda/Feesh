package com.github.sleepypanda.feesh.utils.data

import java.util.Date

/**
 * Root object persisted in achievementsData.json.
 */
data class AchievementsFileData(
    val overallAchievements: MutableMap<String, AchievementProgress> = mutableMapOf(), // Achievements completed once
)

/**
 * Persistent information about the achievements and their status.
 * Later, maybe need to add some tracking for staged achievements with state.
 */
data class AchievementProgress(
    val id: String,
    var isAchieved: Boolean = false,
    var achievedAt: Date? = null,
)
