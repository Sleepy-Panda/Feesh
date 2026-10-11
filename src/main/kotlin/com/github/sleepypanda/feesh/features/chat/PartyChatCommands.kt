package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.PartyChatEvent
import com.github.sleepypanda.feesh.features.overlays.base.TrackerViewMode
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting

object PartyChatCommands {
    private const val RESPONSE_COOLDOWN_MS = 5_000L

    private val COMMAND_PATTERN = Regex("^!feesh\\s+(?<args>.+)$", RegexOption.IGNORE_CASE)
    private val TOTAL_PROFIT_ITEM_PATTERN = Regex("^totalprofit\\s+(?<query>.+)$", RegexOption.IGNORE_CASE)
    private val PROFIT_ITEM_PATTERN = Regex("^profit\\s+(?<query>.+)$", RegexOption.IGNORE_CASE)

    private var cooldownUntil = 0L

    fun init() {
        EventBus.subscribe(PartyChatEvent::class, ::onPartyChat)
    }

    private fun onPartyChat(event: PartyChatEvent) {
        if (!Chat.enablePartyCommands) return

        val now = System.currentTimeMillis()
        if (now < cooldownUntil) return

        val payload = event.messagePayload.removeFormatting().trim()
        val args = COMMAND_PATTERN.matchEntire(payload)?.groups?.get("args")?.value?.trim() ?: return
        val lower = args.lowercase()

        val message = when {
            lower == "profit" -> FishingProfitTrackerPartyCommands.profitMessage(TrackerViewMode.SESSION)
            lower == "totalprofit" -> FishingProfitTrackerPartyCommands.profitMessage(TrackerViewMode.TOTAL)
            else -> {
                TOTAL_PROFIT_ITEM_PATTERN.matchEntire(args)?.let {
                    FishingProfitTrackerPartyCommands.profitItemMessage(
                        TrackerViewMode.TOTAL,
                        it.groups["query"]?.value?.trim().orEmpty()
                    )
                } ?: PROFIT_ITEM_PATTERN.matchEntire(args)?.let {
                    FishingProfitTrackerPartyCommands.profitItemMessage(
                        TrackerViewMode.SESSION,
                        it.groups["query"]?.value?.trim().orEmpty()
                    )
                }
            }
        } ?: return

        cooldownUntil = now + RESPONSE_COOLDOWN_MS
        ChatUtils.sendPartyChat(message)
    }
}
