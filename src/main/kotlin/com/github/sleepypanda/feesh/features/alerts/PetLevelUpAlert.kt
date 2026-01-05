package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting

object PetLevelUpAlert {
    // §r§aYour §r§5Ender Dragon §r§aleveled up to level §r§981§r§a!
    const val PATTERN = "^§r§aYour (.*?) §r§aleveled up to level (.*?)§r§a\\!$"

    fun init() {
        RegisterUtils.chat(Regex(PATTERN), false) { _, matchResult -> onPetLevelUp(matchResult) }
    }

    private fun onPetLevelUp(matchResult: MatchResult) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnPetLevelUp) return

        val petDisplayName = matchResult.groupValues[1]
        val petLevel = matchResult.groupValues[2].removeFormatting().toInt()

       // if (petLevel != 100 && petLevel != 200) return
        
        CommonUtils.showTitle("${petDisplayName} ${RESET}is maxed", "${WHITE}Level ${petLevel}")
        SoundUtils.playSound()
    }
}
