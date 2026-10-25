package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.TrophyFrogCaughtEvent
import com.github.sleepypanda.feesh.events.models.TrophyFishCaughtEvent
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.settings.models.TrophyRarityTypes
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object HideTrophyCatchMessages {
    fun init() {
        EventBus.subscribe(TrophyFrogCaughtEvent::class, ::onTrophyFrogCaught)
        EventBus.subscribe(TrophyFishCaughtEvent::class, ::onTrophyFishCaught)
    }

    private fun onTrophyFrogCaught(event: TrophyFrogCaughtEvent) {
        if (!Chat.hideTrophyFrogCatches) return
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL) return

        CommonUtils.runWithCatching("Failed to hide trophy frog catch chat message.") {
            val rarity = TrophyRarityTypes.entries.find { it.name.equals(event.rarity, true) } ?: return
            if (Chat.hideTrophyCatchRarities.contains(rarity)) {
                event.chatEvent.isCancelled = true
            }
        }
    }

    private fun onTrophyFishCaught(event: TrophyFishCaughtEvent) {
        if (!Chat.hideTrophyFishCatches) return
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return

        CommonUtils.runWithCatching("Failed to hide trophy fish catch chat message.") {
            val rarity = TrophyRarityTypes.entries.find { it.name.equals(event.rarity, true) } ?: return
            if (Chat.hideTrophyCatchRarities.contains(rarity)) {
                event.chatEvent.isCancelled = true
            }
        }   
    }
}
