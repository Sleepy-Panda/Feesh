package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import java.util.Timer
import kotlin.concurrent.timerTask

object ThunderBottleChargedAlert {
    private val THUNDER_BOTTLE_CHARGED_MESSAGE = Regex("^> Your bottle of thunder has fully charged\\!$")
    private val STORM_BOTTLE_CHARGED_MESSAGE = Regex("^> Your Storm in a Bottle has fully charged\\!$")
    private val HURRICANE_BOTTLE_CHARGED_MESSAGE = Regex("^> Your Hurricane in a Bottle has fully charged\\!$")

    data class BottleChargedTrigger(val trigger: Regex, val bottleName: String, val rarityColorCode: ColorCodes)

    private val BOTTLE_CHARGED_TRIGGERS = listOf(
        BottleChargedTrigger(THUNDER_BOTTLE_CHARGED_MESSAGE, "Thunder Bottle", ColorCodes.EPIC),
        BottleChargedTrigger(STORM_BOTTLE_CHARGED_MESSAGE, "Storm Bottle", ColorCodes.EPIC),
        BottleChargedTrigger(HURRICANE_BOTTLE_CHARGED_MESSAGE, "Hurricane Bottle", ColorCodes.LEGENDARY),
    )

    private var timer: Timer? = null

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!Alerts.alertOnThunderBottleCharged || !WorldUtils.isInSkyblock()) return

        BOTTLE_CHARGED_TRIGGERS.forEach { entry ->
            if (entry.trigger.matches(event.unformattedText)) {
                onThunderBottleCharged(entry)
            }
        }
    }

    private fun onThunderBottleCharged(entry: BottleChargedTrigger) {
        if (!Alerts.alertOnThunderBottleCharged || !WorldUtils.isInSkyblock()) return

        resetTimer()

        val task = timerTask {
            resetTimer()
            playAlertOnBottleCharged(entry)
        }
        timer?.schedule(task, 2000) // Little delay to not override the Thunder spawn alert
    }

    private fun playAlertOnBottleCharged(entry: BottleChargedTrigger) {
        CommonUtils.showTitle("${entry.rarityColorCode}${entry.bottleName} ${WHITE}is full")
        SoundUtils.playSound()
    }

    private fun resetTimer() {
        timer?.cancel()
        timer = Timer()
    }
}
