package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.InteractActionType
import com.github.sleepypanda.feesh.events.models.PlayerInteractEvent
import com.github.sleepypanda.feesh.events.models.ShurikenUsedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object ShurikenUsedPublisher {
    private const val SHURIKEN_ITEM_NAME = "Extremely Real Shuriken"
    private const val SHURIKEN_ITEM_ID = "FAKE_SHURIKEN"
    private const val TICKS_PER_SCAN = 10
    private const val RIGHT_CLICK_WINDOW_MS = 1000L

    private var tickCounter = 0
    private var lastShurikensCount: Int? = null
    private var lastRightClickTimeMs = 0L

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(PlayerInteractEvent::class, ::onPlayerInteract)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        reset()
    }

    private fun onPlayerInteract(event: PlayerInteractEvent) {
        CommonUtils.runWithCatching("Failed to handle Shuriken item interaction") {
            if (!WorldUtils.isInSkyblock()) return
            if (!event.isMainHand || (event.actionType != InteractActionType.USE_ITEM && event.actionType != InteractActionType.USE_BLOCK)) return

            val heldItem = FeeshMod.mc.player?.mainHandItem
            if (heldItem == null || heldItem.isEmpty) return

            val heldItemName = heldItem.hoverName.getUnformattedString()
            if (heldItemName != SHURIKEN_ITEM_NAME) return

            lastRightClickTimeMs = System.currentTimeMillis()
        }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_SCAN) return
        tickCounter = 0

        CommonUtils.runWithCatching("Failed to scan hotbar for Shuriken item") {
            scanHotbar()
        }
    }

    private fun scanHotbar() {
        if (!WorldUtils.isInSkyblock()) {
            reset()
            return
        }

        val player = FeeshMod.mc.player ?: return
        var currentCount = 0

        // Exclude last slot 8 used for SB menu or bait bag preview
        for (i in 0..7) {
            val stack = player.inventory.getItem(i)
            if (stack.isEmpty) continue
            val name = stack.hoverName.getUnformattedString()
            if (name != SHURIKEN_ITEM_NAME) continue

            currentCount += stack.count
        }

        if (lastShurikensCount != null && currentCount == lastShurikensCount!! - 1) {
            val clickedRecently = System.currentTimeMillis() - lastRightClickTimeMs <= RIGHT_CLICK_WINDOW_MS
            if (clickedRecently) {
                EventBus.publish(
                    ShurikenUsedEvent(
                        itemId = SHURIKEN_ITEM_ID,
                        itemName = SHURIKEN_ITEM_NAME,
                    )
                )
            }
        }

        lastShurikensCount = currentCount
    }

    private fun reset() {
        tickCounter = 0
        lastShurikensCount = null
        lastRightClickTimeMs = 0L
    }
}
