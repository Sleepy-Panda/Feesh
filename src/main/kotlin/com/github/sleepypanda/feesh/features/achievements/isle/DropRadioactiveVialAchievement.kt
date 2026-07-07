package com.github.sleepypanda.feesh.features.achievements.isle

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.features.achievements.AchievementCategory
import com.github.sleepypanda.feesh.features.achievements.AchievementDifficulty
import com.github.sleepypanda.feesh.features.achievements.AchievementsManager
import com.github.sleepypanda.feesh.features.achievements.BaseAchievement
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.constants.RareDrops
import com.github.sleepypanda.feesh.events.models.RareDropEvent

object DropRadioactiveVialAchievement : BaseAchievement(
    id = "drop_radioactive_vial",
    displayName = "I'm radioactive",
    description = "Drop a Radioactive Vial.",
    difficulty = AchievementDifficulty.HARD,
    categories = listOf(AchievementCategory.CRIMSON_ISLE, AchievementCategory.LAVA),
) {
    private val radioactiveVial = RareDrops.rareDrops.find { it.itemName == "Radioactive Vial" }!!

    override fun init() {
        EventBus.subscribe(RareDropEvent::class, ::onRareDrop)
    }

    private fun onRareDrop(event: RareDropEvent) {
        CommonUtils.runWithCatching("Failed to check and handle achievement $id") {
            if (!AchievementsManager.isEnabled() || isAchieved()) return@runWithCatching
            if (!WorldUtils.isInSkyblock() || WorldUtils.isOnAlpha() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return@runWithCatching
            if (!event.itemName.equals(radioactiveVial.itemName, ignoreCase = true)) return@runWithCatching
    
            completeAndAnnounce()
        }
    }
}
