package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.PartyChatEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.sound.SoundEvents

object PlayerDeathAlert {
    const val YOU_DIED_PATTERN = "^ ☠ You were killed by (Ragnarok|Thunder|Lord Jawbus|Wiki Tiki|Titanoboa)\\.$"
    const val PARTY_MEMBER_DIED_PATTERN = "^--> I was killed, please wait for me until I come back <--$"

    fun init() {
        RegisterUtils.chat(Regex(YOU_DIED_PATTERN)) { _, _ -> onOwnDeath() }
        EventBus.subscribe(PartyChatEvent::class, ::onPartyChatDeath)
    }

    // TODO Sound is not played on death :c
    private fun onOwnDeath() {
        if (!Alerts.alertOnPlayerDeath || !WorldUtils.isInSkyblock()) return

        CommonUtils.showTitle("${RED}You were killed ☠")
        SoundUtils.playSound(SoundEvents.ENTITY_VILLAGER_DEATH)
    }

    private fun onPartyChatDeath(event: PartyChatEvent) {
        if (!Alerts.alertOnPlayerDeath || !WorldUtils.isInSkyblock()) return
        if (!event.messagePayload.contains(PARTY_MEMBER_DIED_PATTERN)) return

        val playerName = PlayerUtils.getName() ?: return
        if (event.rankAndPlayer.removeFormatting().contains(playerName)) return

        val title = "${event.rankAndPlayer} ${RED}was killed ☠";
		CommonUtils.showTitle(title, "Wait for them to come back");
		SoundUtils.playSound(SoundEvents.ENTITY_VILLAGER_DEATH)
    }
}
