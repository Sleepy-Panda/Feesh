package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import java.util.Timer
import kotlin.concurrent.timerTask

object ThunderBottleChargedAlert {
    private const val THUNDER_BOTTLE_CHARGED_MESSAGE = "^> Your bottle of thunder has fully charged\\!$"
    private const val STORM_BOTTLE_CHARGED_MESSAGE = "^> Your Storm in a Bottle has fully charged\\!$"
    private const val HURRICANE_BOTTLE_CHARGED_MESSAGE = "^> Your Hurricane in a Bottle has fully charged\\!$"

    data class BottleChargedTrigger(val trigger: String, val bottleName: String)

    private val BOTTLE_CHARGED_TRIGGERS = listOf(
        BottleChargedTrigger(THUNDER_BOTTLE_CHARGED_MESSAGE, "Thunder bottle"),
        BottleChargedTrigger(STORM_BOTTLE_CHARGED_MESSAGE, "Storm bottle"),
        BottleChargedTrigger(HURRICANE_BOTTLE_CHARGED_MESSAGE, "Hurricane bottle")
    )

    private var timer: Timer? = null

    fun init() {
        BOTTLE_CHARGED_TRIGGERS.forEach { entry ->
            RegisterUtils.chat(Regex(entry.trigger)) { _, _ -> onThunderBottleCharged(entry) }
        }
    }

    private fun onThunderBottleCharged(entry: BottleChargedTrigger) {
        if (!Alerts.alertOnThunderBottleCharged || !WorldUtils.isInSkyblock()) return

        resetTimer()

        val task = timerTask {
            resetTimer()
            playAlertOnBottleCharged(entry.bottleName)
        }
        timer?.schedule(task, 2000) // Little delay to not override the Thunder spawn alert
    }

    private fun playAlertOnBottleCharged(bottleName: String) {
        CommonUtils.showTitle("${AQUA}${bottleName} is full", "", 1, 30, 1)
        SoundUtils.playSound()
    }

    private fun resetTimer() {
        timer?.cancel()
        timer = Timer()
    }
}
