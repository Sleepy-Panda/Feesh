package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
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
import com.github.sleepypanda.feesh.constants.Sounds
import java.util.Date

object RainTimer {
    private const val TICKS_PER_READ = 20
    private const val SECONDS_ALERT_THRESHOLD = 10

    private val PATTERN_RAIN_ADDED = Regex("^You added a minute of rain!.*$")

    private var rainSecondsLeft: Int? = null
    private var rainTimer: String? = null // Time to display, e.g. "02m 30s"
    private var eventName: String? = null // Rain, Thunder, Blizzard
    private var isActiveEvent: Boolean = true // true = active (time left), false = upcoming event (starts in)
    private var tickCounter = 0
    private var lastAlertAt: Date? = null

    private val gui = FeeshGui()
        .setCoordsDataKey("rainTimer")
        .setSampleLines(listOf("${BLUE}Rain ${GRAY}ends in ${WHITE}02m 30s"))
        .setCondition { PlayerUtils.hasFishingRodInHotbar() && isRainArea() }
        .setSettingsKey { Overlays.rainTimerOverlay }
        .setApplyCustomStyleKey { Overlays.rainTimerCustomStyle }

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun isRainArea(): Boolean {
        val worldName = WorldUtils.getWorldName() ?: return false
        val areaName = WorldUtils.getZoneName() ?: return false
        return (worldName == WorldUtils.PARK && areaName == "Birch Park") ||
            (worldName == WorldUtils.SPIDERS_DEN && areaName == "Spider's Den") ||
            worldName == WorldUtils.JERRY_WORKSHOP ||
            worldName == WorldUtils.LOTUS_ATOLL
    }


    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.PARK) return
        if (!PATTERN_RAIN_ADDED.matches(event.unformattedText)) return

        trackRainStatus()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        rainSecondsLeft = null
        rainTimer = null
        eventName = null
        lastAlertAt = null
        isActiveEvent = false
        gui.clearLines()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_READ) return
        tickCounter = 0

        if ((!Overlays.rainTimerOverlay && !Alerts.alertOnRainEndingSoon) || !WorldUtils.isInSkyblock() || !PlayerUtils.hasFishingRodInHotbar() || !isRainArea()) {
            gui.clearLines()
            return
        }

        trackRainStatus()
        updateGuiLines()

        if (Alerts.alertOnRainEndingSoon && isActiveEvent && rainSecondsLeft != null && rainSecondsLeft!! in 1..SECONDS_ALERT_THRESHOLD) {
            if (lastAlertAt == null || Date().time - lastAlertAt!!.time >= 15_000) { // TabList update timer every few seconds, do not alert every second
                playRainEndingSoonAlert()
            }
        }
    }

    private fun trackRainStatus() {
        if (!Overlays.rainTimerOverlay && !Alerts.alertOnRainEndingSoon) return
        if (!WorldUtils.isInSkyblock() || !PlayerUtils.hasFishingRodInHotbar() || !isRainArea()) return

        val worldName = WorldUtils.getWorldName()
        if (worldName == WorldUtils.PARK) {
            val newValue = TabListUtils.getLineAfter("Rain:").trim()
            if (newValue.isNullOrEmpty() || newValue.contains("No rain!")) {
                rainTimer = null
                eventName = null
                isActiveEvent = false
            } else {
                rainTimer = newValue
                eventName = "Rain"
                isActiveEvent = true
            }
        } else if (worldName == WorldUtils.SPIDERS_DEN || worldName == WorldUtils.LOTUS_ATOLL) {
            val thunderValue = TabListUtils.getLineAfter("Thunder:").trim()
            val rainValue = TabListUtils.getLineAfter("Rain:").trim()
            val raw = when {
                thunderValue.isNotEmpty() -> Pair("Thunder", thunderValue)
                rainValue.isNotEmpty() -> Pair("Rain", rainValue)
                else -> null
            }
            if (raw != null) {
                eventName = raw.first
                isActiveEvent = raw.second.endsWith(" left")
                rainTimer = raw.second.removePrefix("in ").removeSuffix(" left")
            } else {
                rainTimer = null
                eventName = null
                isActiveEvent = false
            }
        } else if (worldName == WorldUtils.JERRY_WORKSHOP) {
            val newValue = TabListUtils.getLineAfter("Blizzard:").trim()
            if (newValue.isNullOrEmpty()) {
                rainTimer = null
                eventName = null
                isActiveEvent = false
            } else {
                rainTimer = newValue
                eventName = "Blizzard"
                isActiveEvent = true
            }
        }

        rainSecondsLeft = if (rainTimer.isNullOrEmpty()) null else parseRainTimeToSeconds(rainTimer!!)
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!Overlays.rainTimerOverlay || rainTimer.isNullOrEmpty() || rainSecondsLeft == null) return
        if (!WorldUtils.isInSkyblock() || !PlayerUtils.hasFishingRodInHotbar() || !isRainArea()) return

        val label = eventName ?: "Rain"
        val worldName = WorldUtils.getWorldName()
        val color = if (isActiveEvent && rainSecondsLeft!! in 0..SECONDS_ALERT_THRESHOLD) RED else WHITE
        val timePart = if ((worldName == WorldUtils.SPIDERS_DEN || worldName == WorldUtils.LOTUS_ATOLL) && !isActiveEvent) "${GRAY}starts in ${WHITE}${rainTimer}" else "${GRAY}ends in ${color}${rainTimer}"
        gui.setLines(listOf("${BLUE}${label} $timePart"))
    }

    private fun playRainEndingSoonAlert() {
        lastAlertAt = Date()
        val label = eventName ?: "Rain"
        CommonUtils.showTitle("${RED}$label ends soon")
        ChatUtils.sendLocalChat("${WHITE}$label ends soon.", true)
        if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(Sounds.FEESH_NOTIFICATION_BELL)
        else SoundUtils.playSound()
    }

    /**
     * Parses TabList rain time string (e.g. "02m 30s", "10s", "02m 30s left") to total seconds.
     * Returns -1 if parsing fails.
     */
    private fun parseRainTimeToSeconds(timeStr: String): Int {
        val t = timeStr.replace(" left", "").replace("in ", "").trim()
        return try {
            if (t.contains("m")) {
                val parts = t.split("m")
                val minutes = parts[0].trim().toIntOrNull() ?: return -1
                val secondsPart = parts.getOrNull(1)?.trim()?.replace("s", "")?.trim() ?: "0"
                val seconds = secondsPart.toIntOrNull() ?: 0
                minutes * 60 + seconds
            } else {
                t.replace("s", "").trim().toIntOrNull() ?: -1
            }
        } catch (e: Exception) {
            -1
        }
    }
}
