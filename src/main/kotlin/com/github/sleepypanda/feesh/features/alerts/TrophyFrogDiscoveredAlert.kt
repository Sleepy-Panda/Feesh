package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object TrophyFrogDiscoveredAlert {
    // NEW DISCOVERY: Blessed Frog BRONZE
    private val NEW_DISCOVERY_PATTERN = Regex("^NEW DISCOVERY: (?<details>.+?)$")

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        CommonUtils.runWithCatching("Failed to play alert on Trophy Frog discovered") {
            if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL) return
            if (!Alerts.alertOnTrophyFrogDiscovered && !Alerts.autoShareTrophyFrogDiscovered) return
    
            val match = NEW_DISCOVERY_PATTERN.matchEntire(event.unformattedText) ?: return
            val details = event.formattedText.split(": ").last()
            if (details.isEmpty()) return

            if (Alerts.alertOnTrophyFrogDiscovered) {
                CommonUtils.showTitle(details, "${GREEN}${BOLD}FROG DISCOVERED!")
                // It has sound played by SB so no need to play our own
            }
    
            if (Alerts.autoShareTrophyFrogDiscovered) {
                ChatUtils.sendPartyChat("FROG DISCOVERED! ${details.removeFormatting()}")
            }
        }
    }
}
