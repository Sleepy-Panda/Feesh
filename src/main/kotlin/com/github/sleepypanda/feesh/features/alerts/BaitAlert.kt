package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.BaitChangedEvent
import com.github.sleepypanda.feesh.events.models.BaitRunningOutEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.ClickEvent.RunCommand
import net.minecraft.network.chat.HoverEvent.ShowText
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

object BaitAlert {
    private var tickCounter = 0
    private const val TICKS_PER_CHECK = 20

    fun init() {
        EventBus.subscribe(BaitChangedEvent::class, ::onBaitChanged)
        EventBus.subscribe(BaitRunningOutEvent::class, ::onBaitRunningOut)
    }

    private fun onBaitChanged(event: BaitChangedEvent) {
        CommonUtils.runWithCatching("Failed to alert on bait change") {
            if (!Alerts.alertOnBaitChanged || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
            if (!FishingHookUtils.wasFishingHookActiveMinutesAgo(5)) return
            if (isInFishingBag()) return

            ChatUtils.sendLocalChat("${WHITE}Bait changed from ${event.oldBaitDisplayName} ${WHITE}to ${event.newBaitDisplayName}${WHITE}.", true)
            CommonUtils.showTitle("${YELLOW}Bait changed")
            SoundUtils.playSound()
        }
    }

    private fun onBaitRunningOut(event: BaitRunningOutEvent) {
        CommonUtils.runWithCatching("Failed to alert on bait running out") {
            if (!Alerts.alertOnBaitRunningOut || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
            if (!FishingHookUtils.wasFishingHookActiveMinutesAgo(5)) return
            if (isInFishingBag()) return

            ChatUtils.sendLocalChat("You are almost out of ${event.baitDisplayName}${WHITE}.", true)

            val baitName = event.baitName
            val isCraftableOrBuyable = !baitName.contains("Obfuscated")
            if (isCraftableOrBuyable) {
                val supercraftText = Component.literal("${GRAY}[${YELLOW}${BOLD}Supercraft${GRAY}]")
                    .setStyle(
                        Style.EMPTY
                            .withClickEvent(RunCommand("/recipe $baitName"))
                            .withHoverEvent(ShowText(Component.literal("Click to open Supercraft menu for $baitName")))
                    )
                val orText = Component.literal(" ${RESET}${GRAY}or ")
                val bazaarText = Component.literal("${GRAY}[${GOLD}${BOLD}Buy on BZ${GRAY}]")
                    .setStyle(
                        Style.EMPTY
                            .withClickEvent(RunCommand("/bz $baitName"))
                            .withHoverEvent(ShowText(Component.literal("Click to open Bazaar for $baitName")))
                    )
                ChatUtils.sendLocalChat(supercraftText.append(orText).append(bazaarText))
            }

            CommonUtils.showTitle("${YELLOW}Out of bait soon")
            SoundUtils.playSound()
        }
    }

    private fun isInFishingBag(): Boolean {
        val screen = FeeshMod.mc.screen ?: return false
        return screen is AbstractContainerScreen<*> && screen.title?.string?.contains("Fishing Bag") == true
    }
}
