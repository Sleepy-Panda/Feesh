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
    private const val BAIT_RUNNING_OUT_THRESHOLD = 16
    private const val BAIT_RUNNING_OUT_CACHE_MS = 120_000L
    private const val BAIT_CHANGED_CACHE_MS = 60_000L

    private const val TICKS_PER_UPDATE = 20
    private var tickCounter = 0

    private val baitChangedPublishCache = mutableMapOf<String, Long>()
    private val baitRunningOutPublishCache = mutableMapOf<String, Long>()

    private var lastBaitName = ""
    private var lastBaitDisplayName = ""
    private var lastBaitRemaining = null as Int?

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

        CommonUtils.runWithCatching("Failed to update bait info") {
            updateBaitInfo()
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        reset()
    }

    private fun updateBaitInfo() {
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
            baitRunningOutPublishCache.remove(currentBaitName)
        }

        if (lastBaitName.isNotEmpty() && lastBaitName != currentBaitName && shouldPublishBaitChanged(lastBaitName, currentBaitName)) {
            EventBus.publish(BaitChangedEvent(lastBaitName, lastBaitDisplayName, currentBaitName, currentBaitDisplayName))
        }

        if (currentBaitRemaining <= BAIT_RUNNING_OUT_THRESHOLD && shouldPublishBaitRunningOut(currentBaitName)) {
            EventBus.publish(BaitRunningOutEvent(lastBaitName, lastBaitDisplayName))
        }

        lastBaitName = currentBaitName
        lastBaitDisplayName = currentBaitDisplayName
        lastBaitRemaining = currentBaitRemaining
    }

    private fun shouldPublishBaitChanged(oldBaitName: String, newBaitName: String): Boolean {
        /** Order-independent key - SB can repeat bait change A<->B a few times due to "bait refund". */
        fun baitPairCacheKey(baitA: String, baitB: String): String =
            if (baitA <= baitB) "$baitA|$baitB" else "$baitB|$baitA"

        val key = baitPairCacheKey(oldBaitName, newBaitName)
        val now = System.currentTimeMillis()
        val lastPublishedAt = baitChangedPublishCache[key]
        if (lastPublishedAt != null && now - lastPublishedAt < BAIT_CHANGED_CACHE_MS) return false

        baitChangedPublishCache.entries.removeIf { now - it.value >= BAIT_CHANGED_CACHE_MS }
        baitChangedPublishCache[key] = now
        return true
    }

    private fun shouldPublishBaitRunningOut(baitName: String): Boolean {
        val key = baitName
        val now = System.currentTimeMillis()
        val lastPublishedAt = baitRunningOutPublishCache[key]
        if (lastPublishedAt != null && now - lastPublishedAt < BAIT_RUNNING_OUT_CACHE_MS) return false

        baitRunningOutPublishCache.entries.removeIf { now - it.value >= BAIT_RUNNING_OUT_CACHE_MS }
        baitRunningOutPublishCache[key] = now
        return true
    }

    private fun reset() {
        lastBaitName = ""
        lastBaitDisplayName = ""
        lastBaitRemaining = null
        baitChangedPublishCache.clear()
        baitRunningOutPublishCache.clear()
    }
}
