package com.github.sleepypanda.feesh.features.achievements

import com.github.sleepypanda.feesh.features.achievements.isle.*
import com.github.sleepypanda.feesh.features.achievements.atoll.*
import com.github.sleepypanda.feesh.features.achievements.jerry.*
import com.github.sleepypanda.feesh.features.achievements.water.*
import com.github.sleepypanda.feesh.settings.categories.Achievements
import com.github.sleepypanda.feesh.utils.data.AchievementProgress
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager

object AchievementsManager {
    private val achievements = mutableListOf<BaseAchievement>() // All achievements provided by the mod
    private val achievementById = mutableMapOf<String, BaseAchievement>() // Achievements indexed by their id

    val persistentData get() = PersistentDataManager.achievementsData.overallAchievements

    fun init() {
        // Isle
        register(ConsumeMobyDuckAchievement)
        register(CatchLordJawbusAchievement)
        register(CatchRagnarokAchievement)
        register(CatchPlhlegblastAchievement)
        register(DoubleHookLordJawbusAchievement)
        register(DropRadioactiveVialAchievement)

        // Atoll
        register(FullRideOnPuddleJumperAchievement)
        register(TewtilFeederAchievement)

        // Jerry Workshop
        register(CatchReindrakeAchievement)
        register(StartBlizzardAchievement)

        // Water
        register(CatchCarrotKingAchievement)
        register(CatchBloatedMithrilGrubberAchievement)

        getIncompleteAchievements().forEach { it.init() }
    }

    private fun register(achievement: BaseAchievement) {
        achievements.add(achievement)
        achievementById[achievement.id] = achievement
    }

    fun getCompletedAchievements(): List<BaseAchievement> = achievements.filter { getProgress(it.id).isAchieved }
    fun getIncompleteAchievements(): List<BaseAchievement> = achievements.filter { !getProgress(it.id).isAchieved }

    fun getAllAchievements(): List<BaseAchievement> = achievements

    fun getAchievement(id: String): BaseAchievement? = achievementById[id]

    fun getProgress(id: String): AchievementProgress {
        // We do not persist data if not achieved, so we need to return a default value
        return persistentData.get(id) ?: AchievementProgress(id = id, isAchieved = false, achievedAt = null)
    }

    fun isEnabled(): Boolean = Achievements.achievementsEnabled

    fun save() {
        PersistentDataManager.saveAchievementsDataToFileAsync()
    }

    fun getTotalCount(): Int = achievements.size

    fun getCompletedCount(): Int = getCompletedAchievements().size
}
