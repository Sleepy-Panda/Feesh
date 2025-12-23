package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes

object SpiritMaskAlert {
    const val SPIRIT_MASK_USED_PATTERN = "^Second Wind Activated\\! Your Spirit Mask saved your life\\!$"

    fun init() {
        RegisterUtils.chat(Regex(SPIRIT_MASK_USED_PATTERN)) { _, _ -> onSpiritMaskUsed() }
    }

    private fun onSpiritMaskUsed() {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnSpiritMaskUsed) return

        CommonUtils.showTitle("${ColorCodes.YELLOW}Spirit Mask used")
        SoundUtils.playSound()

        // TODO: Add logic to alert on Spirit Mask is back after 30 seconds
    }

    private fun onSpiritMaskback() {
        if (!WorldUtils.isInSkyblock()) return
        ChatUtils.sendLocalChat("${ColorCodes.DARK_PURPLE}Spirit Mask ${ColorCodes.WHITE}is back")

        if (!Alerts.alertOnSpiritMaskBack) return
        CommonUtils.showTitle("${ColorCodes.GREEN}Spirit Mask is back")
        SoundUtils.playSound()
    }
}
