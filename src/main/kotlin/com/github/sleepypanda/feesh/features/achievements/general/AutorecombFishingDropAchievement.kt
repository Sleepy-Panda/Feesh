package com.github.sleepypanda.feesh.features.achievements.general

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.features.achievements.AchievementCategory
import com.github.sleepypanda.feesh.features.achievements.AchievementDifficulty
import com.github.sleepypanda.feesh.features.achievements.AchievementsManager
import com.github.sleepypanda.feesh.features.achievements.BaseAchievement
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object AutorecombFishingDropAchievement : BaseAchievement(
    id = "autorecombobulate_fishing_drop",
    displayName = "Does it cost more now?",
    description = "Auto-recombobulate a fishing drop.",
    difficulty = AchievementDifficulty.MEDIUM,
    categories = listOf(AchievementCategory.GENERAL),
) {
    // Your Auto Recombobulator recombobulated Squid Boots!
    private val MESSAGE_REGEX = Regex("^Your Auto Recombobulator recombobulated (?<itemName>.*)!$")
    
    private val trashDropsList = listOf(
        "Slug Boots",
        "Moogma Leggings",
        "Flaming Chestplate",
        "Taurus Helmet",
        "Blade of the Volcano",
        "Staff of the Volcano",
        "Fairy's Polo",
        "Fairy's Fedora",
        "Fairy's Trousers",
        "Fairy's Galoshes",
        "Squid Boots",
        "Rabbit Hat",
        "Water Hydra Head",
        "Fish Affinity Talisman",
        "Lucky Hoof",
        "Tiki Mask"
    )

    override fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        CommonUtils.runWithCatching("Failed to check and handle achievement $id") {
            if (!AchievementsManager.isEnabled() || isAchieved()) return@runWithCatching
            if (!WorldUtils.isInSkyblock() || WorldUtils.isOnAlpha()) return@runWithCatching

            val match = MESSAGE_REGEX.matchEntire(event.unformattedText) ?: return@runWithCatching
            val drop = match.groups["itemName"]?.value ?: return@runWithCatching
            if (!trashDropsList.contains(drop)) return@runWithCatching

            completeAndAnnounce()
        }
    }
}
