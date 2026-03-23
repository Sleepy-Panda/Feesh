package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.ChatUtils

object AnyReindrakeAlert {
    // WOAH! [MVP+] MoonTheSadFisher summoned a Reindrake from the depths!
    const val REINDRAKE_PATTERN = "^WOAH! (?<playerNameAndRank>.*) summoned a Reindrake from the depths!$"
    val reindrake = SeaCreatures.allSeaCreatures.find { it.name == "Reindrake" }!!

    fun init() {
        RegisterUtils.chat(Regex(REINDRAKE_PATTERN)) { _, _ -> onAnyReindrake(reindrake.boldDisplayName, reindrake.rarityColorCode) }
    }

    private fun onAnyReindrake(boldDisplayName: String, rarityColorCode: String) {
        if (boldDisplayName.isNullOrEmpty() || rarityColorCode.isNullOrEmpty()) return
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnAnyReindrake || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return

        CommonUtils.showTitle(SeaCreatures.getTitle(reindrake.name, false))
        ChatUtils.sendLocalChatWithCommand("Click to warp to Jerry's Workshop spawn point!", "warp jerry", true)
        SoundUtils.playSound()
    }
}
