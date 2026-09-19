package com.github.sleepypanda.feesh.features.personalbests

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.data.PersonalBestEntry
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import java.util.Date
import net.minecraft.sounds.SoundEvents

object GreatOrOutstandingTreasuresStreakPersonalBest {
    private val PATTERN_TREASURE_CATCH = Regex("^. (GOOD|GOOD JUNK|GREAT|GREAT JUNK|OUTSTANDING|OUTSTANDING JUNK) CATCH!")

    private var currentGreatStreak = 0
    private var currentOutstandingStreak = 0

    fun init() {
        EventBus.subscribe(ChatCancellableEvent::class, ::onChat)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        currentGreatStreak = 0
        currentOutstandingStreak = 0
    }

    private fun onChat(event: ChatCancellableEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        if (WorldUtils.isOnAlpha()) return

        val matchResult = PATTERN_TREASURE_CATCH.find(event.unformattedText) ?: return
        val treasureType = matchResult.groupValues[1].lowercase()

        when (treasureType) {
            "great", "great junk" -> {
                currentGreatStreak++
                checkAndAnnouncePersonalBest(currentOutstandingStreak, getOutstandingPbEntry(), "Outstanding treasures")
                currentOutstandingStreak = 0
            }
            "outstanding", "outstanding junk" -> {
                currentOutstandingStreak++
                checkAndAnnouncePersonalBest(currentGreatStreak, getGreatPbEntry(), "Great treasures")
                currentGreatStreak = 0
            }
            else -> {
                checkAndAnnouncePersonalBest(currentGreatStreak, getGreatPbEntry(), "Great treasures")
                checkAndAnnouncePersonalBest(currentOutstandingStreak, getOutstandingPbEntry(), "Outstanding treasures")
                currentGreatStreak = 0
                currentOutstandingStreak = 0
            }
        }
    }

    private fun getGreatPbEntry(): PersonalBestEntry =
        PersistentDataManager.feeshData.personalBest.greatTreasuresStreak

    private fun getOutstandingPbEntry(): PersonalBestEntry =
        PersistentDataManager.feeshData.personalBest.outstandingTreasuresStreak

    private fun checkAndAnnouncePersonalBest(currentStreak: Int, personalBestEntry: PersonalBestEntry, label: String) {
        CommonUtils.runWithCatching("Failed to check and announce $label streak PB") {
            if (currentStreak == 0) return

            val previousBest = personalBestEntry.amount
            if (currentStreak <= previousBest) return

            personalBestEntry.amount = currentStreak
            personalBestEntry.at = Date()
            PersistentDataManager.saveFeeshDataToFileAsync()

            ChatUtils.sendLocalChat(
                "${LIGHT_PURPLE}${BOLD}PERSONAL BEST!${RESET} $label streak: ${WHITE}${BOLD}${previousBest} ${GRAY}-> ${GREEN}${BOLD}$currentStreak",
                true
            )
            CommonUtils.showTitle("${LIGHT_PURPLE}${BOLD}PERSONAL BEST!", "$label streak: ${GREEN}$currentStreak")
            SoundUtils.playSound(SoundEvents.PLAYER_LEVELUP)
        }
    }
}
