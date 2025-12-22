package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes

object RareCatches {
    fun init() {
        // TODO: Add Vanquisher
        // TODO: Add party source
        // TODO: Add SH format
        SeaCreatures.allSeaCreatures.filter { it.isRare }.forEach { sc ->
            RegisterUtils.chat(Regex(sc.pattern)) { _, _ ->
                if (Alerts.alertOnRareSeaCreatures) {
                    val playerName = PlayerUtils.getName()
                    CommonUtils.showTitle("${sc.rarityColorCode}${FormattingCodes.BOLD.code}${sc.name}", playerName)
                    SoundUtils.playSound()
                }
            }
        }
    }
}
