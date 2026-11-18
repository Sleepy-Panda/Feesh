package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.BottleFullyChargedEvent
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.EPIC
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.LEGENDARY

object BottleFullyChargedPublisher {
    private data class BottleFullyChargedInfo(
        val messageRegex: Regex,
        val itemId: String,
        val itemName: String,
        val itemDisplayName: String,
    )

    private val THUNDER_BOTTLE_CHARGED_MESSAGE = Regex("^> Your bottle of thunder has fully charged\\!$")
    private val STORM_BOTTLE_CHARGED_MESSAGE = Regex("^> Your Storm in a Bottle has fully charged\\!$")
    private val HURRICANE_BOTTLE_CHARGED_MESSAGE = Regex("^> Your Hurricane in a Bottle has fully charged\\!$")

    private val BOTTLE_CHARGED_TRIGGERS = listOf(
        BottleFullyChargedInfo(
            THUNDER_BOTTLE_CHARGED_MESSAGE,
            "THUNDER_IN_A_BOTTLE",
            "Filled Thunder in a Bottle",
            "${EPIC}Filled Thunder in a Bottle",
        ),
        BottleFullyChargedInfo(
            STORM_BOTTLE_CHARGED_MESSAGE,
            "STORM_IN_A_BOTTLE",
            "Storm in a Bottle",
            "${EPIC}Storm in a Bottle",
        ),
        BottleFullyChargedInfo(
            HURRICANE_BOTTLE_CHARGED_MESSAGE,
            "HURRICANE_IN_A_BOTTLE",
            "Hurricane in a Bottle",
            "${LEGENDARY}Hurricane in a Bottle",
        ),
    )

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock()) return

        CommonUtils.runWithCatching("Failed to handle bottle fully charged.") {
            val entry = BOTTLE_CHARGED_TRIGGERS.find { it.messageRegex.matches(event.unformattedText) } ?: return@onChat
            EventBus.publish(
                BottleFullyChargedEvent(
                    itemId = entry.itemId,
                    itemName = entry.itemName,
                    itemDisplayName = entry.itemDisplayName,
                )
            )
        }
    }
}
