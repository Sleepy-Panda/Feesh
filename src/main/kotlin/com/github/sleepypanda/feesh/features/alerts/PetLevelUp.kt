package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes

object PetLevelUp {
    fun init() {    
        RegisterUtils.chat(Regex("Your (.*?) leveled up to level (.*?)!")) { _, matchResult ->
            if (Alerts.alertOnPetLevelUp) {
                val petDisplayName = matchResult.groupValues[1]
                val petLevel = matchResult.groupValues[2]
                CommonUtils.showTitle("${petDisplayName} ${FormattingCodes.RESET}is maxed", petLevel)
                SoundUtils.playSound()
            }
        }
    }
}
