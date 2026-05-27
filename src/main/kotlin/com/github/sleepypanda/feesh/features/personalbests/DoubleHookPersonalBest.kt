package com.github.sleepypanda.feesh.features.personalbests

import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import java.util.Date
import net.minecraft.sounds.SoundEvents

object DoubleHookPersonalBest {
    private var currentDoubleHookStreak = 0

    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreatureCaught)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        currentDoubleHookStreak = 0
    }

    private fun onSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        if (!WorldUtils.isInSkyblock()) return
        if (event.seaCreatureName == SeaCreatureNames.VANQUISHER) return

        if (event.isDoubleHook) {
            currentDoubleHookStreak++
            return
        }

        checkAndAnnouncePersonalBest(currentDoubleHookStreak)
        currentDoubleHookStreak = 0
    }

    private fun checkAndAnnouncePersonalBest(currentStreak: Int) {
        CommonUtils.runWithCatching("Failed to check and announce double hook PB") {
            if (currentStreak == 0) return

            val personalBestEntry = PersistentDataManager.feeshData.personalBest.doubleHookStreak
            if (currentStreak <= personalBestEntry.amount) return
    
            personalBestEntry.amount = currentStreak
            personalBestEntry.at = Date()
            PersistentDataManager.saveFeeshDataToFileAsync()
    
            ChatUtils.sendLocalChat(
                "${LIGHT_PURPLE}${BOLD}PERSONAL BEST!${RESET} Double Hook streak: ${GREEN}${BOLD}$currentStreak",
                true
            )
            CommonUtils.showTitle("${LIGHT_PURPLE}${BOLD}PERSONAL BEST!", "DH streak: ${GREEN}$currentStreak")
            SoundUtils.playSound(SoundEvents.PLAYER_LEVELUP)
        }
    }
}