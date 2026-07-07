package com.github.sleepypanda.feesh.features.achievements.general

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.GuiOpenedEvent
import com.github.sleepypanda.feesh.features.achievements.AchievementCategory
import com.github.sleepypanda.feesh.features.achievements.AchievementDifficulty
import com.github.sleepypanda.feesh.features.achievements.AchievementsManager
import com.github.sleepypanda.feesh.features.achievements.BaseAchievement
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.getScreenCompat
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import java.util.Timer
import kotlin.concurrent.timerTask

object MaxFishingBestiaryAchievement : BaseAchievement(
    id = "max_fishing_bestiary",
    displayName = "You're shrimply the best!",
    description = "Max out all Fishing Bestiary families. Open /be to complete!",
    difficulty = AchievementDifficulty.PROFICIENT,
    categories = listOf(AchievementCategory.GENERAL),
) {
    private const val BESTIARY_GUI_TITLE = "Bestiary"
    private const val FISHING_SLOT_NAME = "Fishing"
    private const val FAMILIES_COMPLETED_100 = "Families Found: 100%" // TODO - Completed

    override fun init() {
        EventBus.subscribe(GuiOpenedEvent::class, ::onGuiOpened)
    }

    private fun onGuiOpened(event: GuiOpenedEvent) {
        CommonUtils.runWithCatching("Failed to check and handle achievement $id") {
            if (!AchievementsManager.isEnabled() || isAchieved()) return@runWithCatching
            if (!WorldUtils.isInSkyblock() || WorldUtils.isOnAlpha()) return@runWithCatching

            val screen = event.screen
            if (screen !is AbstractContainerScreen<*>) return@runWithCatching

            handleGuiOpened()
        }
    }

    private fun handleGuiOpened() {
        Timer(true).schedule(timerTask {
            CommonUtils.runWithCatching("Failed to handle GUI opened for achievement $id") {
                if (!AchievementsManager.isEnabled() || isAchieved() || !WorldUtils.isInSkyblock() || WorldUtils.isOnAlpha()) return@timerTask

                val currentScreen = FeeshMod.mc.getScreenCompat()
                if (currentScreen !is AbstractContainerScreen<*>) return@timerTask

                val title = currentScreen.title.getUnformattedString()
                // GUI titles are Bestiary -> Fishing or Bestiary
                if (!(title == BESTIARY_GUI_TITLE || (title.startsWith(BESTIARY_GUI_TITLE) && title.endsWith("Fishing")))) return@timerTask

                val fishingItem = currentScreen.menu.slots
                    .asSequence()
                    .map { it.item }
                    .firstOrNull { !it.isEmpty && it.hoverName.getUnformattedString() == FISHING_SLOT_NAME }
                    ?: return@timerTask

                val lore = ItemUtils.getUnformattedLoreLines(fishingItem)
                if (!lore.any { it.contains(FAMILIES_COMPLETED_100) }) return@timerTask

                completeAndAnnounce()
            }
        }, 250)
    }
}
