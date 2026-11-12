package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.utils.AlertUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*

object WormholeGoneAlert {
    val PATTERN = Regex("^Your Wormhole closed up\\.\\.\\.$")

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock() || (WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL && WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) || !Alerts.alertOnWormholeGone) return
        if (!PATTERN.matches(event.unformattedText)) return

        AlertUtils.alert(
            title = AlertUtils.TitleConfig("${LIGHT_PURPLE}Wormhole ${RED}is gone"),
            sound = AlertUtils.SoundConfig.McSound(),
        )
    }
}
