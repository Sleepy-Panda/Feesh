package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.TabListUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.constants.Sounds
import java.util.Date

object RainTimer {
    private const val TICKS_PER_READ = 20
    private const val SECONDS_ALERT_THRESHOLD = 10

    private var rainSecondsLeft: Int? = null
    private var rainTimeLeft: String? = null // e.g. "02m 30s"
    private var tickCounter = 0
    private var lastRainAlertAt: Date? = null

    private val gui = FeeshGui()
        .setCoordsDataKey("rainTimer")
        .setSampleLines(listOf("${AQUA}${BOLD}Rain: ${WHITE}02m 30s"))
        .setCondition { PlayerUtils.hasFishingRodInHotbar() && isRainWorld() }
        .setSettingsKey { Overlays.rainTimerOverlay }

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun isRainWorld(): Boolean {
        val worldName = WorldUtils.getWorldName() ?: return false
        return worldName == WorldUtils.PARK || worldName == WorldUtils.SPIDERS_DEN
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        rainSecondsLeft = null
        rainTimeLeft = null
        lastRainAlertAt = null
        gui.clearLines()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_READ) return
        tickCounter = 0

        if (!Overlays.rainTimerOverlay && !Alerts.alertOnRainEndingSoon) return
        if (!WorldUtils.isInSkyblock() || !PlayerUtils.hasFishingRodInHotbar() || !isRainWorld()) return

        val newValue = TabListUtils.getLineAfter("Rain:").trim()
        rainTimeLeft = if (newValue.isEmpty()) null else newValue
        rainSecondsLeft = if (rainTimeLeft.isNullOrEmpty()) null else parseRainTimeToSeconds(rainTimeLeft!!)

        if (Overlays.rainTimerOverlay) {
            updateGuiLines()
        }

        if (Alerts.alertOnRainEndingSoon && rainSecondsLeft != null) {
            if (rainSecondsLeft > 0 && rainSecondsLeft <= SECONDS_ALERT_THRESHOLD &&
                (lastRainAlertAt == null || Date().time - lastRainAlertAt!!.time >= 1000)
            ) {
                playRainEndingSoonAlert()
            }
        }
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!Overlays.rainTimerOverlay || rainTimeLeft.isNullOrEmpty()) return
        if (!WorldUtils.isInSkyblock() || !PlayerUtils.hasFishingRodInHotbar() || !isRainWorld()) return

        val color = if (rainSecondsLeft >= 0 && rainSecondsLeft <= SECONDS_ALERT_THRESHOLD) RED else WHITE
        gui.setLines(listOf("${AQUA}${BOLD}Rain: ${color}${rainTimeLeft}"))
    }

    private fun playRainEndingSoonAlert() {
        lastRainAlertAt = Date()
        CommonUtils.showTitle("${RED}Rain ending soon")
        ChatUtils.sendLocalChat("${WHITE}Rain ending soon.", true)
        if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(Sounds.FEESH_NOTIFICATION_BELL)
        else SoundUtils.playSound()
    }

    /**
     * Parses TabList rain time string (e.g. "02m 30s", "10s") to total seconds.
     * Returns -1 if parsing fails.
     */
    private fun parseRainTimeToSeconds(timeStr: String): Int {
        return try {
            if (timeStr.contains("m")) {
                val parts = timeStr.split("m")
                val minutes = parts[0].trim().toIntOrNull() ?: return -1
                val secondsPart = parts.getOrNull(1)?.trim()?.replace("s", "")?.trim() ?: "0"
                val seconds = secondsPart.toIntOrNull() ?: 0
                minutes * 60 + seconds
            } else {
                timeStr.replace("s", "").trim().toIntOrNull() ?: -1
            }
        } catch (e: Exception) {
            -1
        }
    }
}
