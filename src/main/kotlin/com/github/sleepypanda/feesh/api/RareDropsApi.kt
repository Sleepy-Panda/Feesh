package com.github.sleepypanda.feesh.api

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.ChatEvent
import com.github.sleepypanda.feesh.events.RareDropEvent
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.WorldUtils

object RareDropsApi {
    val rareDropPattern = Regex("^RARE DROP! (?<item>(.+)) \\([+](?<mf>\\d+) ✯ Magic Find\\)$")
    val petDropPattern = Regex("^PET DROP! (?<pet>(.+))$")
    // TODO: PET DROP
    // TODO: DYE DROP
    // TODO: Phoenix pet drop
    // TODO: Squid pet catch

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock()) return
        
        var chatMessage = event.message.string
        //var formattedMessage = event.formattedMessage TODO
        
        val match = rareDropPattern.matchEntire(chatMessage) ?: return
        val itemName = match.groups.get("item")?.value ?: return
        val magicFind = match.groups.get("mf")?.value?.toIntOrNull()

        EventBus.publish(RareDropEvent(itemName, magicFind))
    }
}