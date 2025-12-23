package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypes
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes

object RareCatchAlert {
    const val REINDRAKE_PATTERN = "^WOAH\\! A Reindrake was summoned from the depths\\!$"

    fun init() {
        // TODO: DOUBLE HOOK
        // TODO: Add Vanquisher
        // TODO: Add party source
        // TODO: Add SH format
        // TODO: Check ANY reindrake logic
        // TODO: 
        SeaCreatures.allSeaCreatures
            .filter { it.isRare }
            .forEach { sc -> RegisterUtils.chat(Regex(sc.pattern)) { _, _ -> onSeaCreature(sc.name, sc.rarityColorCode) }
        }

        val reindrake = SeaCreatures.allSeaCreatures.find { it.name == "Reindrake" }!!
        RegisterUtils.chat(Regex(REINDRAKE_PATTERN)) { _, _ -> onAnyReindrake(reindrake.name, reindrake.rarityColorCode) }
    }

    private fun onSeaCreature(seaCreatureName: String, rarityColorCode: String) {
        if (seaCreatureName.isNullOrEmpty() || rarityColorCode.isNullOrEmpty()) return
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareSeaCreatures) return

        val type = try {
            RareSeaCreatureTypes.valueOf(seaCreatureName.uppercase().replace(" ", "_"))
        } catch (_: IllegalArgumentException) {
            return
        }

        if (!Alerts.alertOnSeaCreaturesTypes.contains(type)) return

        val playerName = PlayerUtils.getName()
        CommonUtils.showTitle("${rarityColorCode}${FormattingCodes.BOLD}${seaCreatureName}", playerName)
        SoundUtils.playSound()
    }

    private fun onAnyReindrake(seaCreatureName: String, rarityColorCode: String) {
        if (seaCreatureName.isNullOrEmpty() || rarityColorCode.isNullOrEmpty()) return
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnAnyReindrake) return

        CommonUtils.showTitle("${rarityColorCode}${FormattingCodes.BOLD}${seaCreatureName}")
        SoundUtils.playSound()
    }
}
