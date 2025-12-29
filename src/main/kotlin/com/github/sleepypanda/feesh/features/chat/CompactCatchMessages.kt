package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.settings.categories.Chat as ChatSettings
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.OwnSeaCreatureCaughtEvent

import net.minecraft.text.Text

object CompactCatchMessages {
    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!WorldUtils.isInSkyblock() || !ChatSettings.compactSeaCreaturesMessages) return

        var seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name == event.seaCreatureName } ?: return
        val isDoubleHook = event.isDoubleHook
        val dhMessage = if (isDoubleHook) "${BLUE}${FormattingCodes.BOLD}DOUBLE HOOK! " else ""
        ChatUtils.sendLocalChat("${dhMessage}${seaCreatureInfo.boldDisplayName} ${GRAY}has spawned!")
    }
}