package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.PetLevelUpEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.PriceUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ItemUtils

object PetLevelUpAlert {
    fun init() {
        EventBus.subscribe(PetLevelUpEvent::class, ::onPetLevelUp)
    }

    private fun onPetLevelUp(event: PetLevelUpEvent) {
        if (!WorldUtils.isInSkyblock() || (!Alerts.alertOnPetLevelUp && !Alerts.showPetLevelUpPrice)) return

        if (Alerts.alertOnPetLevelUp) {
            CommonUtils.showTitle("${event.petDisplayName} ${RESET}${WHITE}is maxed", "${WHITE}Level ${event.level}")
            SoundUtils.playSound()    
        }

        if (Alerts.showPetLevelUpPrice) {
            val itemIdMaxLevel = ItemUtils.getMaxedPetId(event.petDisplayName, event.level)
            val itemId1level = ItemUtils.getLevel1PetId(event.petDisplayName)
            val basePrice = PriceUtils.getAuctionItemPrice(itemId1level)?.lbin ?: 0.0
            val priceMaxLevel = PriceUtils.getAuctionItemPrice(itemIdMaxLevel)?.lbin ?: 0.0
            if (priceMaxLevel <= 0.0) return
    
            val priceStr = "${GOLD}${CommonUtils.toShortNumber(priceMaxLevel)}"
            val profitStr = "${GOLD}${CommonUtils.toShortNumber(priceMaxLevel - basePrice)}"
            ChatUtils.sendLocalChat("Estimated cost for this pet is ${priceStr}${RESET}, profit for leveling up is ${profitStr}${RESET}.", true)    
        }
    }
}
