package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes

object ChumBucketAutoPickupAlert {
    const val PATTERN = "^Automatically picked up the Chum Bucket you left back there\\!$"

    fun init() {
        RegisterUtils.chat(Regex(PATTERN)) { _, _ -> onBucketPickedUp() }
    }

    private fun onBucketPickedUp() {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnChumBucketAutoPickup) return

        CommonUtils.showTitle("${ColorCodes.YELLOW}Chum Bucket is gone")
        SoundUtils.playSound()
    }
}
