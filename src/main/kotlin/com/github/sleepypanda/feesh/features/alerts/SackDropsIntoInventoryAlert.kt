package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.FishingProfitDrops
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.InventoryProfitItemPickupEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.ClickEvent.RunCommand
import net.minecraft.network.chat.HoverEvent.ShowText
import net.minecraft.sounds.SoundEvents

object SackDropsIntoInventoryAlert {
    private const val ALERT_COOLDOWN_MS = 10 * 60 * 1000L
    private val lastAlertAtByItemId = mutableMapOf<String, Long>()

    fun init() {
        EventBus.subscribe(InventoryProfitItemPickupEvent::class, ::onInventoryProfitItemPickup)
    }

    private fun onInventoryProfitItemPickup(event: InventoryProfitItemPickupEvent) {
        CommonUtils.runWithCatching("Failed to alert on sack drops going into inventory") {
            if (!Alerts.alertOnSackDropsIntoInventory) return
            if (!WorldUtils.isInSkyblock() || WorldUtils.isOnBingo()) return
            if (!WorldUtils.isInFishingWorld()) return
            if (event.amount <= 0) return

            val dropInfo = FishingProfitDrops.items.find { it.itemId == event.itemId } ?: return
            val compactedItemName = dropInfo.compactedItemName ?: return

            val now = System.currentTimeMillis()
            val lastAlertAt = lastAlertAtByItemId[dropInfo.itemId]
            if (lastAlertAt != null && now - lastAlertAt < ALERT_COOLDOWN_MS) return
            lastAlertAtByItemId[dropInfo.itemId] = now

            ChatUtils.sendLocalChat("${YELLOW}Your sacks are full of ${dropInfo.itemDisplayName}${YELLOW}!", true)

            val compactText = Component.literal("${GRAY}[${WHITE}Craft $compactedItemName]")
                .setStyle(
                    Style.EMPTY
                        .withClickEvent(RunCommand("/recipe $compactedItemName"))
                        .withHoverEvent(ShowText(Component.literal("Click to open Supercraft menu for $compactedItemName")))
                )
            val orText = Component.literal(" ${RESET}${GRAY}or ")
            val bazaarText = Component.literal("${GRAY}[${GOLD}Sell on BZ${GRAY}]")
                .setStyle(
                    Style.EMPTY
                        .withClickEvent(RunCommand("/bz ${dropInfo.itemName}"))
                        .withHoverEvent(ShowText(Component.literal("Click to open Bazaar for ${dropInfo.itemName}")))
                )
            ChatUtils.sendLocalChat(compactText.append(orText).append(bazaarText))

            SoundUtils.playSound(SoundEvents.CHEST_OPEN, 2.0f, 0.5f)
        }
    }
}
