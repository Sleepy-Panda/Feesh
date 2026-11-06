package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.PetLevelUpEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.WorldUtils

object PetLevelUpPublisher {
    // §aYour §5Ender Dragon §aleveled up to level §981§a!
    // Your Rose Dragon leveled up to level 100!
    private val PATTERN = Regex("^§aYour (.*?) §aleveled up to level (.*?)§a\\!$")
    private val PET_WITH_MAX_LEVEL_200 = listOf("Golden Dragon", "Rose Dragon", "Jade Dragon") // Exception: Ender Dragon

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
            val petName = petDisplayName.removeFormatting()
            val isPetWithMaxLevel200 = PET_WITH_MAX_LEVEL_200.any { petName.contains(it) }

            if (isPetWithMaxLevel200 && petLevel != 200) return@onChat
            if (!isPetWithMaxLevel200 && petLevel != 100) return@onChat

            EventBus.publish(
                PetLevelUpEvent(
                    petName = petName,
                    petDisplayName = petDisplayName,
                    level = petLevel
                )
            )
        }
    }
}
