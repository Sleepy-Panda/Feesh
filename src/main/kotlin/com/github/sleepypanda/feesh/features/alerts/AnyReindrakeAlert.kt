package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils

object AnyReindrakeAlert {
    // WOAH! [MVP+] MoonTheSadFisher summoned a Reindrake from the depths!
    // WOAH! [MVP+] MoonTheSadFisher summoned TWO Reindrakes from the depths!
    // §c§lWOAH! §6[MVP§0++§6] Dulkir§f §csummoned a §4Reindrake §cfrom the depths!
    // §c§lWOAH! §b[MVP§d+§b] MoonTheSadFisher§f §csummoned a §4Reindrake §cfrom the depths!
    // §c§lWOAH! §b[MVP§d+§b] MoonTheSadFisher§f §csummoned §4TWO Reindrakes §cfrom the depths!
    const val REINDRAKE_PATTERN = "^§c§lWOAH! (?<playerNameAndRank>.+?) §csummoned (?:§4)?(?<count>a|(?i:two)) (?:§4)?Reindrakes? §cfrom the depths!$"
    val reindrake = SeaCreatures.allSeaCreatures.find { it.name == "Reindrake" }!!

    fun init() {
        RegisterUtils.chat(Regex(REINDRAKE_PATTERN), noFormatting = false) { _, matchResult -> onAnyReindrake(reindrake.boldDisplayName, reindrake.rarityColorCode, matchResult) }
    }

    private fun onAnyReindrake(boldDisplayName: String, rarityColorCode: String, matchResult: MatchResult) {
        if (boldDisplayName.isEmpty() || rarityColorCode.isEmpty()) return
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnAnyReindrake || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return

       CommonUtils.runWithCatching("Failed to show Any Reindrake alert") {
            val count = matchResult.groups["count"]?.value ?: "a"
            val isDoubleHook = count.equals("two", ignoreCase = true)
            val playerNameAndRank = matchResult.groups["playerNameAndRank"]?.value ?: ""
            val name = PlayerUtils.getFormattedPlayerNameFromPartyChat(playerNameAndRank) ?: ""
    
            CommonUtils.showTitle(SeaCreatures.getTitle(reindrake.name, isDoubleHook), name)
            ChatUtils.sendLocalChatWithCommand("Click to warp to Jerry's Workshop spawn point!", "warp jerry", true)
            SoundUtils.playSound()
       }
    }
}
