package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.FishingProfitDrops
import com.github.sleepypanda.feesh.constants.Sounds
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.FishingProfitItemPickupEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.GOLD
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.GRAY
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.BOLD
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.RESET

object RareDropChatAlert {
    fun init() {
        EventBus.subscribe(FishingProfitItemPickupEvent::class, ::onFishingProfitItemPickup)
    }

    private fun onFishingProfitItemPickup(event: FishingProfitItemPickupEvent) {
        if (!Alerts.alertOnRareDropsChatWhenPickup) return

        val dropInfo = FishingProfitDrops.items.find { it.itemId == event.itemId } ?: return
        if (!dropInfo.shouldAnnounceRareDrop) return

        val diffText = if (event.difference > 1) " ${RESET}${GRAY}${event.difference}x" else ""
        ChatUtils.sendLocalChat("${GOLD}${BOLD}RARE DROP! ${RESET}${dropInfo.itemDisplayName}${diffText}", true)
        
        if (General.soundMode != SoundMode.OFF) SoundUtils.playCustomSound(Sounds.FEESH_RARE_DROP)
    }
}
