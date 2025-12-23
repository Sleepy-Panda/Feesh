package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes

object GoldenFishSpawnAlert {
    const val PATTERN = "^You spot a Golden Fish surface from beneath the lava\\!$"

    fun init() {
        RegisterUtils.chat(Regex(PATTERN)) { _, _ -> onGoldenFishSpawn() }
    }

    private fun onGoldenFishSpawn() {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnGoldenFishSpawn) return

        CommonUtils.showTitle("${ColorCodes.WHITE}Catch the ${ColorCodes.GOLD}Golden Fish")
        SoundUtils.playSound()
    }
}
