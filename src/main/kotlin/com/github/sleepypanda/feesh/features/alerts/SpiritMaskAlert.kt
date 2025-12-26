package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import java.util.Timer
import kotlin.concurrent.timerTask
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents

object SpiritMaskAlert {
    const val SPIRIT_MASK_USED_PATTERN = "^Second Wind Activated\\! Your Spirit Mask saved your life\\!$"
    private var timer: Timer? = null

    fun init() {
        RegisterUtils.chat(Regex(SPIRIT_MASK_USED_PATTERN)) { _, _ -> onSpiritMaskUsed() }

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { _, _ ->
            resetTimer()
        }
    }

    private fun onSpiritMaskUsed() {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnSpiritMaskUsed) return

        CommonUtils.showTitle("${YELLOW}Spirit Mask used")
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
        CommonUtils.showTitle("${GREEN}Spirit Mask is ready")
        SoundUtils.playSound()
    }

    private fun resetTimer() {
        timer?.cancel()
        timer = Timer()
    }
}
