package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import java.util.Timer
import kotlin.concurrent.timerTask

object SpiritMaskAlert {
    val SPIRIT_MASK_USED_PATTERN = Regex("^Second Wind Activated\\! Your Spirit Mask saved your life\\!$")
    private var timer: Timer? = null

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnSpiritMaskUsed) return
        if (!SPIRIT_MASK_USED_PATTERN.matches(event.unformattedText)) return

        CommonUtils.showTitle("${DARK_PURPLE}Spirit Mask ${YELLOW}used")
        SoundUtils.playSound()

        resetTimer()

        val task = timerTask {
            resetTimer()
            onSpiritMaskBack()
        }
        timer?.schedule(task, 30000)
    }

    private fun onSpiritMaskBack() {
        if (!WorldUtils.isInSkyblock()) return
        ChatUtils.sendLocalChat("${DARK_PURPLE}Spirit Mask ${WHITE}is ready!", true)

        if (!Alerts.alertOnSpiritMaskBack) return
        CommonUtils.showTitle("${DARK_PURPLE}Spirit Mask ${GREEN}is ready")
        SoundUtils.playSound()
    }

    private fun onWorldChanged(event: WorldChangedEvent) {
        resetTimer()
    }

    private fun resetTimer() {
        timer?.cancel()
        timer = Timer("Feesh-SpiritMaskAlert", true)
    }
}
