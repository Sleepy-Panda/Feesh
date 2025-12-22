package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes
import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

object RareCatches {
    fun init() {
        val mc = FeeshMod.mc
        val player = mc.player
        val playerName = PlayerUtils.getName()

        // TODO: Add Vanquisher
        // TODO: Add party source
        // TODO: Add SH format
        SeaCreatures.allSeaCreatures.filter { it.isRare }.forEach { sc ->
            RegisterUtils.chat(Regex(sc.pattern)) { _, _ ->
                if (Alerts.alertOnRareSeaCreatures) {
                    CommonUtils.showTitle("${sc.rarityColorCode}${FormattingCodes.BOLD.code}${sc.name}", playerName)
                    mc.world?.playSound(player, player?.blockPos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f)
                }
            }
        }
    }
}
