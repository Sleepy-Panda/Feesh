package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object PetLevelUpAlert {
    const val PATTERN = "^Your (.*?) leveled up to level (.*?)!$"

    fun init() {
        RegisterUtils.chat(Regex(PATTERN)) { _, matchResult -> onPetLevelUp(matchResult) }
    }

    // TODO: Process formatted message to get pet display name and level
    private fun onPetLevelUp(matchResult: MatchResult) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnPetLevelUp) return

        val petDisplayName = matchResult.groupValues[1]
        val petLevel = matchResult.groupValues[2]

        if (petLevel != "100" && petLevel != "200") return
        
        CommonUtils.showTitle("${petDisplayName} ${RESET}is maxed", petLevel)
        SoundUtils.playSound()
    }
}
