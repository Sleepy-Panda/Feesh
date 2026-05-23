package com.github.sleepypanda.feesh.features.alerts

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

            ChatUtils.sendLocalChat("${WHITE}Bait changed from ${event.oldBaitDisplayName} ${WHITE}to ${event.newBaitDisplayName}${WHITE}.", true)
            CommonUtils.showTitle("${YELLOW}Bait changed")
            SoundUtils.playSound()
        }
    }

    private fun onBaitRunningOut(event: BaitRunningOutEvent) {
        CommonUtils.runWithCatching("Failed to alert on bait running out") {
            if (!Alerts.alertOnBaitRunningOut || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
            if (!FishingHookUtils.wasFishingHookActiveMinutesAgo(5)) return

            ChatUtils.sendLocalChat("You are almost out of ${event.baitDisplayName}${WHITE}.", true)
            CommonUtils.showTitle("${YELLOW}Out of bait soon")
            SoundUtils.playSound()
        }
    }
}
