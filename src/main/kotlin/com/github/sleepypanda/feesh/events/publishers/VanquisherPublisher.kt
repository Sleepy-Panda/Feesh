package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.events.models.VanquisherCocoonedByYouEvent
import com.github.sleepypanda.feesh.events.models.VanquisherSpawnedEvent
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.WorldUtils

object VanquisherPublisher {
    private val COCOONED_PATTERN = Regex("^CAUGHT! You cocooned (a|an) (?<mobName>.+)!$")

    fun init() {
        EventBus.subscribe(ChatCancellableEvent::class, ::onChat)
    }

    private fun onChat(event: ChatCancellableEvent) {
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return

        val chatMessage = event.unformattedText
        val vanquisher = SeaCreatures.vanquisher

        if (vanquisher.pattern.containsMatchIn(chatMessage)) {
            EventBus.publish(VanquisherSpawnedEvent(chatMessage, vanquisher))
            return
        }

        val cocoonMatch = COCOONED_PATTERN.matchEntire(chatMessage) ?: return
        val mobName = cocoonMatch.groups["mobName"]?.value ?: return
        if (mobName != vanquisher.name) return
        EventBus.publish(VanquisherCocoonedByYouEvent(chatMessage, vanquisher))
    }
}
