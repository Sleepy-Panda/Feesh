package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object AnyReindrakeAlert {
    const val REINDRAKE_PATTERN = "^WOAH\\! A Reindrake was summoned from the depths\\!$"

    fun init() {
        val reindrake = SeaCreatures.allSeaCreatures.find { it.name == "Reindrake" }!!
        RegisterUtils.chat(Regex(REINDRAKE_PATTERN)) { _, _ -> onAnyReindrake(reindrake.boldDisplayName, reindrake.rarityColorCode) }
    }

    private fun onAnyReindrake(boldDisplayName: String, rarityColorCode: String) {
        if (boldDisplayName.isNullOrEmpty() || rarityColorCode.isNullOrEmpty()) return
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnAnyReindrake) return

        CommonUtils.showTitle(getSeaCreatureDisplayName(boldDisplayName, rarityColorCode))
        SoundUtils.playSound()
    }

    private fun getSeaCreatureDisplayName(boldDisplayName: String, rarityColorCode: String): String {
        return if (rarityColorCode == MYTHIC.code) "${YELLOW}${OBFUSCATED}x${RESET} ${boldDisplayName}${RESET} ${YELLOW}${OBFUSCATED}x${RESET}" 
        else boldDisplayName
    }
}
