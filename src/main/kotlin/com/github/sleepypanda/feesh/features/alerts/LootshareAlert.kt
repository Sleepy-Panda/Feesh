package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.PartyChatEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.Sounds
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.SoundMode

object LootshareAlert {
    const val LOOTSHARE_PATTERN = "^Lootshare!$"

    fun init() {
        EventBus.subscribe(PartyChatEvent::class, ::onPartyChatLootshare)
    }

    private fun onPartyChatLootshare(event: PartyChatEvent) {
        if (!Alerts.alertOnLootshareMessage || !WorldUtils.isInSkyblock()) return
        if (!event.messagePayload.contains(LOOTSHARE_PATTERN)) return

        val playerName = PlayerUtils.getName()
        if (!playerName.isNullOrEmpty() && event.rankAndPlayer.removeFormatting().contains(playerName)) return

        CommonUtils.showTitle("${GREEN}${BOLD}Lootshare!")
        if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(Sounds.FEESH_NOTIFICATION_BELL)
        else SoundUtils.playSound()
    }
}
