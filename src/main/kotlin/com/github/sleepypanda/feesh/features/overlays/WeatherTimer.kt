package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.TabListUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.enums.ColorCodes
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import java.util.Date

// New tablist line format after weather update:
// Thunderstorm: in 7m <icon>
// Tropical Rain: in 2m 30s <icon>
// Tropical Rain: for 1m 0s <icon>
// Tropical Rain: for 5s <icon>
// Park:
// Rain: No rain!
// Rain: 1m 24s

// OLD tablist line format before weather update:
// Thunder: in 38m
// Thunder: in 2m 30s
// Thunder: 2m 30s left
// Blizzard: 10m 50s
// Blizzard: 0s
// Park: same format as new

object WeatherTimer {
    private const val TICKS_PER_READ = 20
    private const val SECONDS_ALERT_THRESHOLD = 10

    private val PATTERN_RAIN_ADDED = Regex("^You added a minute of rain!.*$")

    private val WEATHER_LINE_REGEX = Regex(
        "^(Tropical Rain|Acid Rain|Thunderstorm|Thunder|Snowstorm|Hellstorm|Voidstorm|Wispfall|Ashfall|Moonfall|Rockfall|Blossoming|Blooming|Blizzard|Breeze|Mist|Smog|Rain):\\s(.+)"
    )
    // "in 7m", "for 19m", "for 1m 30s", "1m 30s left", "10s"
    private val TIMER_VALUE_REGEX = Regex(
        """
        ^
        (?:(?<prefix>in|for)\s+)?
        (?<time>
            (?<minutes>\d+)m(?:\s+(?<seconds>\d+)s)?
            |
            (?<secondsOnly>\d+)s
        )
        (?:\s+left)?
        """,
        RegexOption.COMMENTS,
    )

    private var weatherTimerStr: String? = null // Time to display, e.g. "02m 30s"
    private var weatherSecondsLeft: Int? = null
    private var eventName: String? = null // Tropical Rain, Thunder, Blizzard, etc
    private var isActiveEvent: Boolean = true // true = active (time left), false = upcoming event (starts in)
    private var tickCounter = 0
    private var lastAlertAt: Date? = null

    private val gui = FeeshGui()
        .setCoordsDataKey("weatherTimer")
        .setSampleLines(listOf("${AQUA}${BOLD}Rain ${GRAY}ends in ${WHITE}02m 30s"))
        .setCondition { WorldUtils.isInWeatherWorld() }
        .setSettingsKey { Overlays.weatherTimerOverlay }
        .setApplyCustomStyleKey { Overlays.weatherTimerCustomStyle }

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.PARK) return
        if (!PATTERN_RAIN_ADDED.matches(event.unformattedText)) return

        trackWeatherStatus()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        weatherSecondsLeft = null
        weatherTimerStr = null
        eventName = null
        lastAlertAt = null
        isActiveEvent = false
        gui.clearLines()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_READ) return
        tickCounter = 0

        CommonUtils.runWithCatching("Failed to update weather timer state") {
            if ((!Overlays.weatherTimerOverlay && !Alerts.alertOnWeatherEndingSoon) || !WorldUtils.isInSkyblock() || !WorldUtils.isInWeatherWorld()) {
                gui.clearLines()
                return@onClientTick
            }
    
            trackWeatherStatus()
            updateGuiLines()
    
            if (Alerts.alertOnWeatherEndingSoon && isActiveEvent && weatherSecondsLeft != null && weatherSecondsLeft!! in 1..SECONDS_ALERT_THRESHOLD) {
                if (lastAlertAt == null || Date().time - lastAlertAt!!.time >= 15_000) { // TabList updates timer once in a few seconds, do not alert every second
                    playWeatherEndingSoonAlert()
                }
            }    
        }
    }

    private fun trackWeatherStatus() {
        if (!Overlays.weatherTimerOverlay && !Alerts.alertOnWeatherEndingSoon) return
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInWeatherWorld()) return

        if (WorldUtils.getWorldName() == WorldUtils.PARK) {
            trackParkWeather()
        } else {
            trackTabListWeather()
        }
    }

    private fun trackParkWeather() {
        val newValue = TabListUtils.getLineAfter("Rain:").trim()
        if (newValue.isEmpty() || newValue.contains("No rain!")) {
            weatherTimerStr = null
            weatherSecondsLeft = null
            eventName = null
            isActiveEvent = false
        } else {
            weatherTimerStr = newValue
            weatherSecondsLeft = parseWeatherTimeToSeconds(newValue)
            eventName = "Rain"
            isActiveEvent = true
        }
    }

    private fun trackTabListWeather() {
        val parsed = findWeatherOnTabList()
        if (parsed == null) {
            weatherTimerStr = null
            weatherSecondsLeft = null
            eventName = null
            isActiveEvent = false
            return
        }

        eventName = parsed.first
        isActiveEvent = parsed.second
        weatherTimerStr = parsed.third
        weatherSecondsLeft = parseWeatherTimeToSeconds(weatherTimerStr!!)
    }

    private fun findWeatherOnTabList(): Triple<String, Boolean, String>? {
        for (line in TabListUtils.getUnformattedLines()) {
            val match = WEATHER_LINE_REGEX.find(line.trim()) ?: continue
            val name = match.groupValues[1]
            val rawValue = match.groupValues[2].trim()
            val timeMatch = TIMER_VALUE_REGEX.find(rawValue) ?: continue
            val time = timeMatch.groups["time"]?.value ?: continue
            val isUpcoming = timeMatch.groups["prefix"]?.value == "in"
            return Triple(name, !isUpcoming, time)
        }
        return null
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!Overlays.weatherTimerOverlay || weatherTimerStr.isNullOrEmpty() || weatherSecondsLeft == null || weatherSecondsLeft!! <= 0) return
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInWeatherWorld()) return

        val label = eventName ?: "Weather event"
        val color = if (isActiveEvent && weatherSecondsLeft!! in 0..SECONDS_ALERT_THRESHOLD) RED else WHITE
        val timePart = if (!isActiveEvent) "${GRAY}starts in ${WHITE}${weatherTimerStr}" else "${GRAY}ends in ${color}${weatherTimerStr}"
        val lineText = "${getWeatherEventColor(label)}${BOLD}${label} $timePart"
        gui.setLines(listOf(LineInfo(lineText)))
    }

    private fun playWeatherEndingSoonAlert() {
        lastAlertAt = Date()
        val label = eventName ?: "Weather event"
        val eventColor = getWeatherEventColor(label)
        CommonUtils.showTitle("${eventColor}${BOLD}$label ${WHITE}ends soon")
        ChatUtils.sendLocalChat("${eventColor}${BOLD}$label ${WHITE}ends soon.", true)
        SoundUtils.playSound()
    }

    private fun getWeatherEventColor(eventName: String?): ColorCodes {
        return when (eventName) {
            "Tropical Rain",  "Rain" -> AQUA
            "Acid Rain" -> GREEN
            "Thunderstorm", "Thunder" -> YELLOW
            "Snowstorm" -> WHITE
            "Hellstorm" -> RED
            "Voidstorm" -> DARK_PURPLE
            "Wispfall" -> GRAY
            "Ashfall" -> DARK_GRAY
            "Moonfall" -> BLUE
            "Rockfall" -> GRAY
            "Blossoming" -> LIGHT_PURPLE
            "Blooming" -> LIGHT_PURPLE
            "Blizzard" -> WHITE
            "Breeze" -> AQUA
            "Mist" -> GRAY
            "Smog" -> GRAY
            else -> WHITE
        }
    }

    /**
     * Parses TabList weather time string (e.g. "02m 30s", "10s") to total seconds.
     * Returns -1 if parsing fails.
     */
    private fun parseWeatherTimeToSeconds(timeStr: String): Int {
        val match = TIMER_VALUE_REGEX.find(timeStr.trim()) ?: return -1
        val minutes = match.groups["minutes"]?.value?.toIntOrNull() ?: 0
        val seconds = match.groups["seconds"]?.value?.toIntOrNull()
            ?: match.groups["secondsOnly"]?.value?.toIntOrNull()
            ?: 0
        if (match.groups["minutes"] == null && match.groups["secondsOnly"] == null) return -1
        return minutes * 60 + seconds
    }
}
