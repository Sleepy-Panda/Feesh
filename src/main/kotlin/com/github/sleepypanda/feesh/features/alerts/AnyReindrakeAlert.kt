package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ReindrakeSummonedEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.AlertUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object AnyReindrakeAlert {
    val reindrake = SeaCreatures.allSeaCreatures.find { it.name == "Reindrake" }!!

    fun init() {
        EventBus.subscribe(ReindrakeSummonedEvent::class, ::onReindrakeSummoned)
    }

    private fun onReindrakeSummoned(event: ReindrakeSummonedEvent) {
        if (!Alerts.alertOnAnyReindrake) return
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return

        CommonUtils.runWithCatching("Failed to show Any Reindrake alert") {
            val name = PlayerUtils.getFormattedPlayerNameFromPartyChat(event.playerNameAndRank) ?: ""

            AlertUtils.alert(
                title = AlertUtils.TitleConfig(SeaCreatures.getTitle(reindrake.name, event.isDoubleHook), name),
                sound = AlertUtils.SoundConfig.McSound(),
            )
            ChatUtils.sendLocalChatWithCommand("Click to warp to Jerry's Workshop spawn point!", "warp jerry", true)
       }
    }
}
