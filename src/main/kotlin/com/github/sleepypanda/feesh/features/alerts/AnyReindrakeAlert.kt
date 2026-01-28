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
import com.github.sleepypanda.feesh.utils.data.CustomSoundsManager
import com.github.sleepypanda.feesh.constants.Sounds
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.SoundMode

object AnyReindrakeAlert {
    const val REINDRAKE_PATTERN = "^WOAH\\! A Reindrake was summoned from the depths\\!$"
    val reindrake = SeaCreatures.allSeaCreatures.find { it.name == "Reindrake" }!!

    fun init() {
        RegisterUtils.chat(Regex(REINDRAKE_PATTERN)) { _, _ -> onAnyReindrake(reindrake.boldDisplayName, reindrake.rarityColorCode) }
    }

    private fun onAnyReindrake(boldDisplayName: String, rarityColorCode: String) {
        if (boldDisplayName.isNullOrEmpty() || rarityColorCode.isNullOrEmpty()) return
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnAnyReindrake || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return

        CommonUtils.showTitle(SeaCreatures.getTitle(reindrake.name, false))
        ChatUtils.sendLocalChatWithCommand("Click to warp to Jerry's Workshop spawn point!", "warp jerry", true)

        val soundData = CustomSoundsManager.getCatchSoundData(reindrake.name)
        val soundFileName = soundData?.source

        if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(soundFileName)
        else SoundUtils.playSound()
    }
}
