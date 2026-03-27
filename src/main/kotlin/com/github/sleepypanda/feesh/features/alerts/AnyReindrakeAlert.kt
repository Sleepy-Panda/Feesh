package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils

object AnyReindrakeAlert {
    // WOAH! [MVP+] MoonTheSadFisher summoned a Reindrake from the depths!
    // WOAH! [MVP+] MoonTheSadFisher summoned TWO Reindrakes from the depths!
    const val REINDRAKE_PATTERN = "^WOAH! (?<playerNameAndRank>.*) summoned (a|TWO|two) Reindrake(s)? from the depths!$"
    val reindrake = SeaCreatures.allSeaCreatures.find { it.name == "Reindrake" }!!

    fun init() {
        RegisterUtils.chat(Regex(REINDRAKE_PATTERN)) { _, matchResult -> onAnyReindrake(reindrake.boldDisplayName, reindrake.rarityColorCode, matchResult) }
    }

    private fun onAnyReindrake(boldDisplayName: String, rarityColorCode: String, matchResult: MatchResult) {
        if (boldDisplayName.isNullOrEmpty() || rarityColorCode.isNullOrEmpty()) return
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnAnyReindrake || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return

        val isDoubleHook = matchResult.groupValues[2].equals("two", ignoreCase = true)
        CommonUtils.showTitle(SeaCreatures.getTitle(reindrake.name, isDoubleHook))
        ChatUtils.sendLocalChatWithCommand("Click to warp to Jerry's Workshop spawn point!", "warp jerry", true)
        SoundUtils.playSound()
    }
}
