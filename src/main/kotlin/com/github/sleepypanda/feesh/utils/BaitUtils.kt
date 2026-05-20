package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import net.minecraft.core.component.DataComponents

object BaitUtils {
    private const val TICKS_PER_UPDATE = 20
    private var tickCounter = 0

    private var baitName = ""
    private var baitDisplayName = ""
    private var baitRemaining = null as Int?

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) {
            reset()
            return
        }

        // Fishing bag has preview in slot #9 in hotbar
        val lastSlotItem = FeeshMod.mc.player?.inventory?.getItem(8) ?: return
        val itemName = lastSlotItem.hoverName?.string?.removeFormatting() ?: return

        if (!itemName.contains("Bait")) return

        // Check lore for line "Bait Remaining: 1,234"
        val lore = lastSlotItem.get(DataComponents.LORE)?.lines()?.map { it.string } ?: return
        val baitRemainingLine = lore.find { it.contains("Bait Remaining:") } ?: return
        val baitRemaining = baitRemainingLine.split(":")[1].trim().replace(",", "").toInt()

        if (baitRemaining > 0) {
            baitName = itemName
            baitDisplayName = lastSlotItem.hoverName.getFormattedString()
            this.baitRemaining = baitRemaining
        }
    }

    private fun reset() {
        baitName = ""
        baitDisplayName = ""
        baitRemaining = null
    }
}
