package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.constants.Sounds
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.gui.FeeshGui

object ConsumablesTimer {
    private const val MOBY_DUCK_EFFECTIVE_SECONDS = 60 * 60
    private const val SECONDS_BEFORE_EXPIRATION = 10
    private const val TICKS_PER_CHECK = 20

    // You consumed a Moby-Duck: Collector's Edition and gained +30☯ Fishing Wisdom for 60m!
    private val MOBY_DUCK_CONSUMED_PATTERN = Regex("^You consumed a Moby-Duck: Collector's Edition and gained \\+30☯ Fishing Wisdom for 60m!$")
    // Moby-Duck expires in 10s
    // Repeated on 60s, 10s, 5s, etc
    private val MOBY_DUCK_EXPIRING_PATTERN = Regex("^Moby-Duck expires in (\\d+)s$")
    // Moby-Duck has expired!
    private val MOBY_DUCK_EXPIRED_PATTERN = Regex("^Moby-Duck has expired!$")

    private data class MobyDuckData(
        var isActive: Boolean = false,
        var elapsedTime: Int = 0,
        var remainingTime: Int = 0,
        var wasAlertPlayed: Boolean = false
    )

    private var mobyDuckData = MobyDuckData()
    private var tickCounter = 0

    private val gui = FeeshGui()
        .setCoordsDataKey("consumablesTimer")
        .setClickable(false)
        .setSampleLines(
            listOf(
                "${DARK_PURPLE}Moby-Duck: ${WHITE}1h 00m 00s"
            )
        )
        .setSettingsKey { Overlays.consumablesTimerOverlay }
        .setApplyCustomStyleKey { Overlays.consumablesTimerCustomStyle }

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
    }

    private fun onChat(event: ChatEvent) {
        CommonUtils.runWithCatching("Failed to handle consumable chat event") {
            if (!WorldUtils.isInSkyblock()) return
            if (!isOverlayEnabled() && !isAlertEnabled()) return

            when {
                MOBY_DUCK_CONSUMED_PATTERN.matches(event.unformattedText) -> {
                    mobyDuckData = MobyDuckData(
                        isActive = true,
                        elapsedTime = 0,
                        remainingTime = MOBY_DUCK_EFFECTIVE_SECONDS,
                        wasAlertPlayed = false
                    )
                }
                MOBY_DUCK_EXPIRED_PATTERN.containsMatchIn(event.unformattedText) -> {
                    resetMobyDuck()
                    gui.clearLines()
                }
                else -> {
                    val match = MOBY_DUCK_EXPIRING_PATTERN.find(event.unformattedText) ?: return
                    val remainingSeconds = match.groupValues.getOrNull(1)?.toIntOrNull() ?: return
                    trackMobyDuckExpiring(remainingSeconds)
                }
            }
        }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        if (!WorldUtils.isInSkyblock()) return
        if (!isOverlayEnabled() && !isAlertEnabled()) return

        onSecondElapsed()

        if (isOverlayEnabled()) {
            updateGuiLines()
        } else {
            gui.clearLines()
        }
    }

    private fun isOverlayEnabled(): Boolean {
        return Overlays.consumablesTimerOverlay
    }

    private fun isAlertEnabled(): Boolean {
        return Alerts.alertOnConsumableExpiresSoon
    }

    // Mod-guessed timer is sometimes out of sync with actual server timer
    // So sometimes chat message appears later after guessed expiration
    private fun trackMobyDuckExpiring(remainingSeconds: Int) {
        if (isAlertEnabled() && remainingSeconds == SECONDS_BEFORE_EXPIRATION && !mobyDuckData.wasAlertPlayed
        ) {
            mobyDuckData.wasAlertPlayed = true
            CommonUtils.showTitle("${DARK_PURPLE}Moby-Duck ${RED}expires soon")
            if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(Sounds.FEESH_NOTIFICATION_BELL)
            else SoundUtils.playSound()
        }

        if (!mobyDuckData.isActive) return

        mobyDuckData.elapsedTime = MOBY_DUCK_EFFECTIVE_SECONDS - remainingSeconds
        mobyDuckData.remainingTime = remainingSeconds
    }

    private fun onSecondElapsed() {
        if (!mobyDuckData.isActive) return

        if (mobyDuckData.elapsedTime >= MOBY_DUCK_EFFECTIVE_SECONDS) {
            resetMobyDuck()
            gui.clearLines()
            return
        }

        mobyDuckData.elapsedTime += 1
        mobyDuckData.remainingTime = MOBY_DUCK_EFFECTIVE_SECONDS - mobyDuckData.elapsedTime
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!isOverlayEnabled() ||
            !WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld() ||
            !mobyDuckData.isActive ||
            mobyDuckData.remainingTime <= 0
        ) {
            return
        }

        val timeColor = if (mobyDuckData.remainingTime <= SECONDS_BEFORE_EXPIRATION) RED.code else WHITE.code
        val line = "${DARK_PURPLE.code}Moby-Duck: $timeColor${fromSecondsToTimeString(mobyDuckData.remainingTime)}"
        gui.setLines(listOf(line))
    }

    private fun resetMobyDuck() {
        mobyDuckData = MobyDuckData()
    }

    private fun fromSecondsToTimeString(totalSeconds: Int): String {
        if (totalSeconds <= 0) return ""

        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> "${hours}h ${minutes.toString().padStart(2, '0')}m ${seconds.toString().padStart(2, '0')}s"
            minutes > 0 -> "${minutes}m ${seconds.toString().padStart(2, '0')}s"
            else -> "${seconds}s"
        }
    }
}
