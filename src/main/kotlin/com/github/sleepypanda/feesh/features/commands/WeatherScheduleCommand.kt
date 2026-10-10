package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object WeatherScheduleCommand {
    const val COMMAND_NAME = "feeshWeatherSchedule"

    private const val CLEAR_DURATION = 2400L
    private const val WEATHER_DURATION = 1200L
    private const val CYCLE_DURATION = CLEAR_DURATION + WEATHER_DURATION
    private const val EXTREME_FREQUENCY = 3L
    private const val SKYBLOCK_EPOCH_START_MS = 1560275700000L
    private const val SKYBLOCK_EPOCH_START_SECONDS = SKYBLOCK_EPOCH_START_MS / 1000

    fun init() {
        RegisterUtils.command(COMMAND_NAME) {
            showWeatherSchedule()
        }
    }

    private fun formatElapsedTime(seconds: Long): String {
        val hours = TimeUnit.SECONDS.toHours(seconds)
        val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
        val secs = seconds % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m ${secs}s"
            minutes > 0 -> "${minutes}m ${secs}s"
            else -> "${secs}s"
        }
    }

    private fun formatDate(date: java.util.Date): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault())
        return formatter.format(date.toInstant())
    }

    private fun formatTimeElapsedBetweenDates(start: java.util.Date, end: java.util.Date): String {
        val diffSeconds = (end.time - start.time) / 1000
        return formatElapsedTime(diffSeconds)
    }

    private fun secondsToDate(seconds: Long): java.util.Date {
        return java.util.Date(seconds * 1000)
    }

    private fun showWeatherSchedule() {
        if (!WorldUtils.isInSkyblock()) {
            ChatUtils.sendLocalChat("${RED}You must be on Hypixel Skyblock to use this command!", true)
            return
        }

        CommonUtils.runWithCatching("Failed to show weather schedule") {
            val nowSeconds = System.currentTimeMillis() / 1000
            val skyblockAge = nowSeconds - SKYBLOCK_EPOCH_START_SECONDS

            val sinceLastWeatherFinished = skyblockAge % CYCLE_DURATION
            val extremeCycle = CYCLE_DURATION * EXTREME_FREQUENCY
            // Extreme is the last weather of every 3-hour cycle
            val extremeStart = CLEAR_DURATION + 2 * CYCLE_DURATION
            val sinceLastExtremeFinished = skyblockAge % extremeCycle

            val isWeatherActive = sinceLastWeatherFinished >= CLEAR_DURATION
            val isExtreme = sinceLastExtremeFinished >= extremeStart
            val weatherTimeLeft = if (isWeatherActive) CYCLE_DURATION - sinceLastWeatherFinished else 0L

            val nextWeather = if (isWeatherActive) weatherTimeLeft + CLEAR_DURATION else CLEAR_DURATION - sinceLastWeatherFinished
            val nextEvents = listOf(
                nextWeather,
                nextWeather + CYCLE_DURATION,
                nextWeather + 2 * CYCLE_DURATION
            )

            val chatBreak = "${GRAY}${ChatUtils.getChatBreak("-")}"
            ChatUtils.sendLocalChat(chatBreak)
            ChatUtils.sendLocalChat("${GREEN}${BOLD}Weather schedule${RESET}", true)

            if (isWeatherActive) {
                val weatherType = if (isExtreme) "${RED}${BOLD}Extreme" else "${YELLOW}Mild"
                ChatUtils.sendLocalChat("${WHITE}Now: ${AQUA}$weatherType ${RESET}weather (${formatElapsedTime(weatherTimeLeft)} left)")
            } else {
                ChatUtils.sendLocalChat("${WHITE}Now: Clear weather")
            }

            nextEvents.forEach { startsIn ->
                val eventTime = nowSeconds + startsIn
                val isNextEventExtreme = (skyblockAge + startsIn) % extremeCycle == extremeStart
                val weatherType = if (isNextEventExtreme) "${RED}${BOLD}Extreme" else "${YELLOW}Mild"
                val startsAtStr = formatDate(secondsToDate(eventTime))
                val startsInStr = formatTimeElapsedBetweenDates(secondsToDate(nowSeconds), secondsToDate(eventTime))
                ChatUtils.sendLocalChat("${GRAY}- $weatherType ${WHITE}starts at $startsAtStr (in $startsInStr)")
            }
        }
    }
}
