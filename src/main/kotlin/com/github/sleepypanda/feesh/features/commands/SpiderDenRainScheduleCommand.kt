package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.FeeshMod
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object SpiderDenRainScheduleCommand {
    const val COMMAND_NAME = "feeshSpiderDenRainSchedule"

    private const val RAIN_COOLDOWN = 2400L
    private const val RAIN_DURATION = 1200L
    private const val CYCLE_DURATION = RAIN_COOLDOWN + RAIN_DURATION
    private const val THUNDERSTORM_FREQUENCY = 3L
    private const val SKYBLOCK_EPOCH_START_MS = 1560275700000L
    private const val SKYBLOCK_EPOCH_START_SECONDS = SKYBLOCK_EPOCH_START_MS / 1000

    fun init() {
        RegisterUtils.command(COMMAND_NAME) {
            showSpidersDenRainSchedule()
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

    private fun showSpidersDenRainSchedule() {
        if (!WorldUtils.isInSkyblock()) {
            ChatUtils.sendLocalChat("${RED}You must be on Hypixel Skyblock to use this command!", true)
            return
        }

        try {
            val nowSeconds = System.currentTimeMillis() / 1000
            val skyblockAge = nowSeconds - SKYBLOCK_EPOCH_START_SECONDS

            val sinceLastRainFinished = skyblockAge % CYCLE_DURATION
            val sinceLastThunderstormFinished = skyblockAge % (CYCLE_DURATION * THUNDERSTORM_FREQUENCY)

            val isRaining = sinceLastRainFinished >= RAIN_COOLDOWN
            val isThunderstorm = sinceLastThunderstormFinished >= RAIN_COOLDOWN && sinceLastThunderstormFinished < CYCLE_DURATION
            val rainTimeLeft = if (isRaining) CYCLE_DURATION - sinceLastRainFinished else 0L

            val nextRain = if (isRaining) rainTimeLeft + RAIN_COOLDOWN else RAIN_COOLDOWN - sinceLastRainFinished
            val nextEvents = listOf(
                nextRain,
                nextRain + CYCLE_DURATION,
                nextRain + 2 * CYCLE_DURATION
            )

            val chatBreak = "${GRAY}${ChatUtils.getChatBreak("-")}"
            ChatUtils.sendLocalChat(chatBreak)
            ChatUtils.sendLocalChat("${GREEN}${BOLD}Spider's Den rain schedule${RESET}", true)
           
            val message = StringBuilder()
            if (isRaining) {
                val weatherType = if (isThunderstorm) "Thunderstorm" else "Rain"
                message.append("${WHITE}Now: ${AQUA}$weatherType ${RESET}(${formatElapsedTime(rainTimeLeft)} left)\n\n")
            } else {
                message.append("${WHITE}Now: ${YELLOW}Sunny\n\n")
            }

            nextEvents.forEach { startsIn ->
                val eventTime = nowSeconds + startsIn
                val isNextEventThunderstorm = (skyblockAge + startsIn) % (CYCLE_DURATION * THUNDERSTORM_FREQUENCY) == RAIN_COOLDOWN
                val weatherType = if (isNextEventThunderstorm) "Thunderstorm" else "Rain"
                val startsAtStr = formatDate(secondsToDate(eventTime))
                val startsInStr = formatTimeElapsedBetweenDates(secondsToDate(nowSeconds), secondsToDate(eventTime))
                message.append("${GRAY}- ${AQUA}$weatherType ${WHITE}starts at $startsAtStr (in $startsInStr)\n")
            }

            message.append("\n${DARK_GRAY}Gain +50☂ Fishing Speed during Rain, and +3α Sea Creature Chance during Thunderstorm.\n")

            ChatUtils.sendLocalChat(message.toString())
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to show Spider's Den rain schedule.", e)
        }
    }
}

