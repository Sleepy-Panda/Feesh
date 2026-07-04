package com.github.sleepypanda.feesh.features.achievements.general

object OwnGiantRodAchievement : BaseAchievement(
    id = "own_giant_rod",
    displayName = "Giant rod",
    description = "Have a Giant Rod in your inventory.",
    difficulty = AchievementDifficulty.PROFICIENT,
    categories = listOf(AchievementCategory.GENERAL),
) {
    override fun init() {
        
    }
}
