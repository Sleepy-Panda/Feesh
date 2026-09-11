package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.TabListAndScoreboardUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object DayNightStartedAlert {
    private const val TICKS_PER_CHECK = 20
    private const val DAY_PREFIX = "6:00am"
    private const val NIGHT_PREFIX = "7:00pm"

    private var tickCounter = 0
    private var lastAlertedPhase: SkyblockTimePhase? = null

    private val TIME_LINE_REGEX = Regex("^\\d{1,2}:\\d{2}(?:am|pm)\\b.*", RegexOption.IGNORE_CASE)

    private enum class SkyblockTimePhase { DAY, NIGHT }

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!Alerts.alertOnDayNight || !WorldUtils.isInSkyblock()) return
        if (Alerts.alertOnDayNightOnlyWhenFishing && (!WorldUtils.isInFishingWorld() || !FishingHookUtils.wasFishingHookSubmergedMinutesAgo(5))) return

        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        CommonUtils.runWithCatching("Failed to check day/night alert") {
            val timeLine = TabListAndScoreboardUtils.getUnformattedScoreboardLines()
                .map { it.trim() }
                .firstOrNull { line -> TIME_LINE_REGEX.matches(line) }
                ?: return@runWithCatching

            val phase = when {
                timeLine.startsWith(DAY_PREFIX, ignoreCase = true) -> SkyblockTimePhase.DAY
                timeLine.startsWith(NIGHT_PREFIX, ignoreCase = true) -> SkyblockTimePhase.NIGHT
                else -> {
                    lastAlertedPhase = null
                    return@runWithCatching
                }
            }

            if (phase == lastAlertedPhase) return@runWithCatching
            lastAlertedPhase = phase
            playAlert(phase)
        }
    }

    private fun playAlert(phase: SkyblockTimePhase) {
        const dayIcon = "☀"
        const nightIcon = ".✦⋆⁺"
        when (phase) {
            SkyblockTimePhase.DAY -> {
                CommonUtils.showTitle("${GOLD}${dayIcon} ${YELLOW}${BOLD}Day started")
                ChatUtils.sendLocalChat("${GOLD}${dayIcon} ${YELLOW}Daytime started!", true)
            }
            SkyblockTimePhase.NIGHT -> {
                CommonUtils.showTitle("${YELLOW}${nightIcon} ${DARK_BLUE}${BOLD}Night started")
                ChatUtils.sendLocalChat("${YELLOW}${nightIcon} ${DARK_BLUE}Nighttime started!", true)
            }
        }
        SoundUtils.playSound()
    }
}
