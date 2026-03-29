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
import com.github.sleepypanda.feesh.events.models.PartyChatEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.sound.SoundEvents

object PlayerDeathAlert {
    const val YOU_DIED_PATTERN = "^ ☠ You were killed by (Ragnarok|Thunder|Lord Jawbus|Jawbus Follower|Wiki Tiki|Wiki Tiki Laser Totem|Titanoboa|Nessie)\\.$"
    const val PARTY_MEMBER_DIED_PATTERN = "^--> I was killed, please wait for me until I come back <--$"

    fun init() {
        RegisterUtils.chat(Regex(YOU_DIED_PATTERN)) { _, matchResult -> onOwnDeath(matchResult) }
        EventBus.subscribe(PartyChatEvent::class, ::onPartyChatDeath)
    }

    // TODO Sound is not played on death :c
    private fun onOwnDeath(matchResult: MatchResult) {
        if (!Alerts.alertOnPlayerDeath || !WorldUtils.isInSkyblock()) return

        CommonUtils.showTitle("${RED}You were killed ☠")
        SoundUtils.playSound(SoundEvents.ENTITY_VILLAGER_DEATH)

        if (matchResult.groups[1]?.value == "Nessie") {
            ChatUtils.sendLocalChatWithCommand("Click to warp to Murkwater Loch!", "warp murk", true)
        }
    }

    private fun onPartyChatDeath(event: PartyChatEvent) {
        if (!Alerts.alertOnPlayerDeath || !WorldUtils.isInSkyblock()) return
        if (!Regex(PARTY_MEMBER_DIED_PATTERN).containsMatchIn(event.messagePayload)) return

        val me = PlayerUtils.getName() ?: return
        val playerName = PlayerUtils.getFormattedPlayerNameFromPartyChat(event.rankAndPlayer) ?: return
        if (!playerName.isNullOrEmpty() && !me.isNullOrEmpty() && playerName.removeFormatting().contains(me)) return

        val title = "${playerName} ${RED}was killed ☠";
		CommonUtils.showTitle(title, "Wait for them to come back");
		SoundUtils.playSound(SoundEvents.ENTITY_VILLAGER_DEATH)
    }
}
