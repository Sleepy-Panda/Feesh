package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ReindrakeSummonedEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.AlertUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
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
            ChatUtils.sendLocalChatWithCommand("Click to warp to Jerry's Workshop spawn point!", "warp jerry", true)

            val playerName = PlayerUtils.getFormattedPlayerNameFromPartyChat(event.playerNameAndRank) ?: ""
            if (isOwnReindrake(playerName) && isRareCatchAlertEnabledForReindrake()) return@runWithCatching

            AlertUtils.alert(
                title = AlertUtils.TitleConfig(SeaCreatures.getTitle(reindrake.name, event.isDoubleHook), playerName),
                sound = AlertUtils.SoundConfig.McSound(),
                delayNext = false, // Annoying delay when party member summons it, and 2 alerts played. Needs party members detection logic
            )
        }
    }

    private fun isOwnReindrake(formattedPlayerName: String): Boolean {
        val me = PlayerUtils.getUnformattedName()
        val playerName = formattedPlayerName.removeFormatting()
        if (me.isNullOrEmpty() || playerName.isEmpty()) return false
        return playerName.contains(me)
    }

    private fun isRareCatchAlertEnabledForReindrake(): Boolean {
        if (!Alerts.alertOnRareSeaCreatures) return false
        return Alerts.alertOnSeaCreaturesList.any { it.displayName.equals(reindrake.name, ignoreCase = true) }
    }
}
