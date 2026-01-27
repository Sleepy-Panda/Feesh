package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.ChatEvent
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.PetLevelUpEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.WorldUtils

object PetLevelUpPublisher {
    // §aYour §5Ender Dragon §aleveled up to level §981§a!
    private val PATTERN = Regex("^§aYour (.*?) §aleveled up to level (.*?)§a\\!$")

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock()) return

        val formattedMessage = event.message.getFormattedString()
        if (formattedMessage.isNullOrEmpty()) return

        val matchResult = PATTERN.matchEntire(formattedMessage) ?: return
        val petDisplayName = matchResult.groupValues[1]
        if (petDisplayName.isNullOrBlank()) return
        
        val petLevel = matchResult.groupValues[2].removeFormatting().toIntOrNull() ?: return

        if (petLevel != 100 && petLevel != 200) return

        EventBus.publish(
            PetLevelUpEvent(
                petName = petDisplayName.removeFormatting(),
                petDisplayName = petDisplayName,
                level = petLevel
            )
        )
    }
}
