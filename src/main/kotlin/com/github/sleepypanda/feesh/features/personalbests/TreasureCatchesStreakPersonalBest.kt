package com.github.sleepypanda.feesh.features.personalbests

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.CatchEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import java.util.Date
import net.minecraft.sounds.SoundEvents

object TreasureCatchesStreakPersonalBest {
    private var currentTreasureCatchStreak = 0

    fun init() {
        EventBus.subscribe(CatchEvent::class, ::onCatch)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        currentTreasureCatchStreak = 0
    }

    private fun onCatch(event: CatchEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        if (WorldUtils.isOnAlpha()) return

        if (event.isTreasureCatch || event.isJunkCatch) {
            currentTreasureCatchStreak++
            return
        }

        checkAndAnnouncePersonalBest(currentTreasureCatchStreak)
        currentTreasureCatchStreak = 0
    }

    private fun checkAndAnnouncePersonalBest(currentStreak: Int) {
        CommonUtils.runWithCatching("Failed to check and announce treasure catch PB") {
            if (currentStreak == 0) return

            val personalBestEntry = PersistentDataManager.feeshData.personalBest.treasureCatchesStreak
            val previousBest = personalBestEntry.amount
            if (currentStreak <= previousBest) return

            personalBestEntry.amount = currentStreak
            personalBestEntry.at = Date()
            PersistentDataManager.saveFeeshDataToFileAsync()

            ChatUtils.sendLocalChat(
                "${LIGHT_PURPLE}${BOLD}PERSONAL BEST!${RESET} Treasure catch streak: ${WHITE}${BOLD}${previousBest} ${GRAY}-> ${GREEN}${BOLD}$currentStreak",
                true
            )
            CommonUtils.showTitle("${LIGHT_PURPLE}${BOLD}PERSONAL BEST!", "Treasure catch streak: ${GREEN}$currentStreak", stay = 60)
            SoundUtils.playSound(SoundEvents.PLAYER_LEVELUP)
        }
    }
}
