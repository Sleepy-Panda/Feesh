package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.ItemEntityDetailsLoadedEvent
import com.github.sleepypanda.feesh.events.models.ItemEntityLoadedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.entity.ItemEntity

object ItemEntityPublisher {
    private val loadedThisTick = mutableListOf<ItemEntity>()
    private var checkOnNextClientTick = listOf<ItemEntity>()

    fun init() {
        EventBus.subscribe(ItemEntityLoadedEvent::class, ::onItemEntityLoaded)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onItemEntityLoaded(event: ItemEntityLoadedEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        loadedThisTick.add(event.itemEntity)
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        for (itemEntity in checkOnNextClientTick) {
            CommonUtils.runWithCatching("Failed to publish item entity details") {
                val stack = itemEntity.stack
                if (stack.isEmpty) return@runWithCatching
                val nameText = stack.customName ?: stack.name
                val formatted = nameText.getFormattedString()
                val unformatted = nameText.string.removeFormatting()
                EventBus.publish(ItemEntityDetailsLoadedEvent(itemEntity, itemEntity.id, formatted, unformatted))
            }
        }
        checkOnNextClientTick = loadedThisTick.toList()
        loadedThisTick.clear()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        loadedThisTick.clear()
        checkOnNextClientTick = emptyList()
    }
}
