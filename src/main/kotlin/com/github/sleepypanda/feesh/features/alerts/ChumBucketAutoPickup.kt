package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes

object ChumBucketAutoPickup {
    fun init() {    
        RegisterUtils.chat(Regex("Automatically picked up the Chum Bucket you left back there\\!")) { _, _ ->
            if (Alerts.alertOnChumBucketAutoPickup) {
                CommonUtils.showTitle("${ColorCodes.YELLOW.code}Chum Bucket is gone", null)
                SoundUtils.playSound()
            }
        }
    }
}
