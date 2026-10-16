package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.PartyChatEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.features.overlays.FishingProfitTrackerPartyCommands
import com.github.sleepypanda.feesh.features.overlays.base.TrackerViewMode
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*

object PartyChatCommands {
    private const val COMMAND_PREFIX = "!feesh"
    private const val RESPONSE_COOLDOWN_MS = 4_000L
    private const val OWN_COMMAND_RESPONSE_DELAY_MS = 1_000L

    private val COMMAND_PATTERN = Regex("^${COMMAND_PREFIX}\\s+(?<args>.+)$", RegexOption.IGNORE_CASE)

    private var cooldownUntil = 0L
    private var scheduledMessage: String? = null
    private var scheduledMessageSendAt = 0L

    fun init() {
        EventBus.subscribe(PartyChatEvent::class, ::onPartyChat)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onPartyChat(event: PartyChatEvent) {
        CommonUtils.runWithCatching("Failed to process party chat command") {
            if (!WorldUtils.isInSkyblock()) return
            if (!Chat.enablePartyCommands) return
            if (!event.messagePayload.startsWith(COMMAND_PREFIX, ignoreCase = true)) return
    
            val now = System.currentTimeMillis()
            if (now < cooldownUntil) {
                ChatUtils.sendLocalChat("${RED}Responding to the party chat command is on cooldown.")
                return
            }
    
            val payload = event.messagePayload.removeFormatting().trim()
            val args = COMMAND_PATTERN.matchEntire(payload)?.groups?.get("args")?.value?.trim() ?: return
            val commandWithArgs = args.lowercase()
    
            val message = when {
                commandWithArgs == "profit" -> FishingProfitTrackerPartyCommands.getProfitChatMessage(TrackerViewMode.SESSION)
                commandWithArgs == "profittotal" -> FishingProfitTrackerPartyCommands.getProfitChatMessage(TrackerViewMode.TOTAL)
                commandWithArgs == "profit top1" -> FishingProfitTrackerPartyCommands.getTopProfitItemChatMessage(TrackerViewMode.SESSION)
                commandWithArgs == "profittotal top1" -> FishingProfitTrackerPartyCommands.getTopProfitItemChatMessage(TrackerViewMode.TOTAL)
                commandWithArgs.startsWith("profittotal ") -> FishingProfitTrackerPartyCommands.getProfitItemChatMessage(
                    TrackerViewMode.TOTAL,
                    args.substringAfter(' ').trim()
                )
                commandWithArgs.startsWith("profit ") -> FishingProfitTrackerPartyCommands.getProfitItemChatMessage(
                    TrackerViewMode.SESSION,
                    args.substringAfter(' ').trim()
                )
                else -> null
            } ?: return
    
            cooldownUntil = now + RESPONSE_COOLDOWN_MS // Prevent spamming back to the pchat if someone sends a lot of commands
            if (isOwnPartyMessage(event.rankAndPlayer)) { // !feesh command message is just sent and player can't send another message right away
            scheduledMessage = message
            scheduledMessageSendAt = now + OWN_COMMAND_RESPONSE_DELAY_MS
                return
            }
    
            ChatUtils.sendPartyChat(message)    
        }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        val message = scheduledMessage ?: return
        if (System.currentTimeMillis() < scheduledMessageSendAt) return

        scheduledMessage = null
        if (!WorldUtils.isInSkyblock()) return
        ChatUtils.sendPartyChat(message)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        cooldownUntil = 0L
        scheduledMessage = null
        scheduledMessageSendAt = 0L
    }

    private fun isOwnPartyMessage(rankAndPlayer: String): Boolean {
        val me = PlayerUtils.getUnformattedName()
        if (me.isNullOrEmpty()) return false
        return rankAndPlayer.removeFormatting().contains(me)
    }
}
