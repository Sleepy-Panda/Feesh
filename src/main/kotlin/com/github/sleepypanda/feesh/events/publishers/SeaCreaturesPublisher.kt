package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.WorldUtils

object SeaCreaturesPublisher {
    private val DOUBLE_HOOK_MESSAGES = arrayOf(
        "It's a Double Hook!",
        "It's a Double Hook! Woot woot!",
    )

    var isDoubleHook = false

    fun init() {
        EventBus.subscribe(ChatCancellableEvent::class, ::onChat)
    }

    private fun onChat(event: ChatCancellableEvent) {
        if (!WorldUtils.isInSkyblock()) return
        
        var chatMessage = event.unformattedText

        if (chatMessage in DOUBLE_HOOK_MESSAGES) {
            isDoubleHook = true
            if (Chat.compactSeaCreaturesMessages) {
                event.isCancelled = true
            }
            return
        }

        SeaCreatures.allSeaCreatures
            .find { sc -> sc.message == chatMessage }
            ?.let { sc ->
                val doubleHooked = if (!sc.canBeDoubleHooked) false else isDoubleHook
                EventBus.publish(OwnSeaCreatureCaughtEvent(sc.name, doubleHooked, chatMessage, sc))
                isDoubleHook = false
                if (Chat.compactSeaCreaturesMessages) {
                    event.isCancelled = true
                }
            }
    }
}

