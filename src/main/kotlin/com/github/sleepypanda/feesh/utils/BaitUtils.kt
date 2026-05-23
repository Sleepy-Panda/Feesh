package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.BaitChangedEvent
import com.github.sleepypanda.feesh.events.models.BaitRunningOutEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import net.minecraft.core.component.DataComponents

object BaitUtils {
    private const val BAIT_RUNNING_OUT_THRESHOLD = 10
    private const val TICKS_PER_UPDATE = 20
    private var tickCounter = 0

    private var lastBaitName = ""
    private var lastBaitDisplayName = ""
    private var lastBaitRemaining = null as Int?
    private var isBaitRunningOutPublished = false

    fun getBaitDisplayName(): String = lastBaitDisplayName

    fun getBaitRemaining(): Int? = lastBaitRemaining

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) {
            reset()
            return
        }

        // Fishing bag has preview in slot #9 in hotbar
        // It appears with some delay when holding a fishing rod
        val lastSlotItem = FeeshMod.mc.player?.inventory?.getItem(8) ?: return
        val currentBaitName = lastSlotItem.hoverName?.string?.removeFormatting() ?: return
        if (!currentBaitName.contains("Bait") && !currentBaitName.contains("Obfuscated")) return

        val currentBaitDisplayName = lastSlotItem.hoverName.getFormattedString()
        val lore = lastSlotItem.get(DataComponents.LORE)?.lines()?.map { it.string } ?: return
        val baitRemainingLine = lore.find { it.contains("Bait Remaining:") } ?: return
        val currentBaitRemaining = baitRemainingLine.split(":")[1].trim().replace(",", "").toInt()
        if (currentBaitRemaining <= 0) return

        if (currentBaitName == lastBaitName && lastBaitRemaining != null && currentBaitRemaining == lastBaitRemaining!! + 1) return // SB does some kind of "+1 bait refund" when not spending a bait
        if (currentBaitName == lastBaitName && lastBaitRemaining != null && currentBaitRemaining > lastBaitRemaining!! + 1) { // User added more bait
            isBaitRunningOutPublished = false
        }

        if (lastBaitName.isNotEmpty() && lastBaitName != currentBaitName) {
            EventBus.publish(BaitChangedEvent(lastBaitName, lastBaitDisplayName, currentBaitName, currentBaitDisplayName))
            isBaitRunningOutPublished = false
        }

        if (currentBaitRemaining <= BAIT_RUNNING_OUT_THRESHOLD && !isBaitRunningOutPublished) {
            isBaitRunningOutPublished = true
            EventBus.publish(BaitRunningOutEvent(lastBaitName, lastBaitDisplayName))
        }

        //if (currentBaitRemaining == 1) {
        //    return // To avoid false triggers when last bait is consumed and then refunded a few times
        //}
        //How to cleanup when bait is over?

        lastBaitName = currentBaitName
        lastBaitDisplayName = currentBaitDisplayName
        lastBaitRemaining = currentBaitRemaining
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        reset()
    }

    private fun reset() {
        lastBaitName = ""
        lastBaitDisplayName = ""
        lastBaitRemaining = null
        isBaitRunningOutPublished = false
    }
}
