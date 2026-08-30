package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.events.models.ReindrakeSummonedEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object ReindrakeSummonedPublisher {
    // WOAH! [MVP+] MoonTheSadFisher summoned a Reindrake from the depths!
    // WOAH! [MVP+] MoonTheSadFisher summoned TWO Reindrakes from the depths!
    // §c§lWOAH! §6[MVP§0++§6] Dulkir§f §csummoned a §4Reindrake §cfrom the depths!
    // §c§lWOAH! §b[MVP§d+§b] MoonTheSadFisher§f §csummoned a §4Reindrake §cfrom the depths!
    // §c§lWOAH! §b[MVP§d+§b] MoonTheSadFisher§f §csummoned §4TWO Reindrakes §cfrom the depths!
    private val PATTERN = Regex("^§c§lWOAH! (?<playerNameAndRank>.+?) §csummoned (?:§4)?(?<count>a|(?i:two)) (?:§4)?Reindrakes? §cfrom the depths!$")

    fun init() {
        EventBus.subscribe(ChatCancellableEvent::class, ::onChat)
    }

    private fun onChat(event: ChatCancellableEvent) {
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return

        CommonUtils.runWithCatching("Failed to handle Reindrake summoned chat") {
            val matchResult = PATTERN.matchEntire(event.formattedText) ?: return@onChat
            val count = matchResult.groups["count"]?.value ?: "a"
            val isDoubleHook = count.equals("two", ignoreCase = true)
            val playerNameAndRank = matchResult.groups["playerNameAndRank"]?.value ?: ""

            EventBus.publish(
                ReindrakeSummonedEvent(
                    playerNameAndRank = playerNameAndRank,
                    isDoubleHook = isDoubleHook
                )
            )
        }
    }
}
