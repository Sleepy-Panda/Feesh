package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.PartyChatEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import net.minecraft.sounds.SoundEvents

object PlayerDeathAlert {
    val YOU_DIED_PATTERN = Regex("^ ☠ You were killed by (Ragnarok|Thunder|Lord Jawbus|Jawbus Follower|Wiki Tiki|Wiki Tiki Laser Totem|Titanoboa|Nessie)\\.$")
    const val PARTY_MEMBER_DIED_PATTERN = "^--> I was killed, please wait for me until I come back <--$"

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onOwnDeath)
        EventBus.subscribe(PartyChatEvent::class, ::onPartyChatDeath)
    }

    private fun onOwnDeath(event: ChatEvent) {
        if (!Alerts.alertOnPlayerDeath || !WorldUtils.isInSkyblock()) return

        val matchResult = YOU_DIED_PATTERN.matchEntire(event.unformattedText) ?: return

        CommonUtils.showTitle("${RED}You were killed ☠")
        SoundUtils.playSound(SoundEvents.VILLAGER_DEATH)

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
		SoundUtils.playSound(SoundEvents.VILLAGER_DEATH)
    }
}
