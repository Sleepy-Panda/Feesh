package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.events.models.SeaCreatureCocoonedByYouEvent
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.utils.WorldUtils

object SeaCreaturesCocoonPublisher {
    private val COCOONED_PATTERN = Regex("^CAUGHT! You cocooned (a|an) (?<mobName>.+)!$")

    fun init() {
        EventBus.subscribe(ChatCancellableEvent::class, ::onChat)
    }

    private fun onChat(event: ChatCancellableEvent) {
        if (!WorldUtils.isInSkyblock()) return
        
        var chatMessage = event.unformattedText

        val cocoonMatch = COCOONED_PATTERN.matchEntire(chatMessage) ?: return
        val mobName = cocoonMatch.groups["mobName"]?.value ?: return
        SeaCreatures.allSeaCreatures
            .find { sc -> sc.name == mobName }
            ?.let { sc ->
                EventBus.publish(SeaCreatureCocoonedByYouEvent(sc.name, chatMessage, sc))
            }
    }
}

