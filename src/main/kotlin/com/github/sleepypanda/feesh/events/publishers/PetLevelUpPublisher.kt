package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.PetLevelUpEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
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

        CommonUtils.runWithCatching("Failed to handle pet level up.") {
            val matchResult = PATTERN.matchEntire(event.formattedText) ?: return@onChat
            val petDisplayName = matchResult.groupValues[1]
            if (petDisplayName.isNullOrBlank()) return@onChat
   
            val petLevel = matchResult.groupValues[2].removeFormatting().toIntOrNull() ?: return@onChat

            if (petLevel != 100 && petLevel != 200) return@onChat

            EventBus.publish(
                PetLevelUpEvent(
                    petName = petDisplayName.removeFormatting(),
                    petDisplayName = petDisplayName,
                    level = petLevel
                )
            )
        }
    }
}
