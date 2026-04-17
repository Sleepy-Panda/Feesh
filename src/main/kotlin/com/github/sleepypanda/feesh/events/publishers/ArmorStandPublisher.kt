package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.events.models.ArmorStandDetailsLoadedEvent
import com.github.sleepypanda.feesh.events.models.ArmorStandLoadedEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import net.minecraft.world.entity.decoration.ArmorStand

object ArmorStandPublisher {
    private val loadedThisTick = mutableListOf<ArmorStand>()
    private var checkOnNextClientTick = listOf<ArmorStand>()

    fun init() {
        EventBus.subscribe(ArmorStandLoadedEvent::class, ::onArmorStandLoaded)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onArmorStandLoaded(event: ArmorStandLoadedEvent) {
        if (!event.entity.isAlive) return
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        loadedThisTick.add(event.entity)
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        for (armorStand in checkOnNextClientTick) {
            CommonUtils.runWithCatching("Failed to publish armor stand details") {
                if (!armorStand.isAlive) return@runWithCatching
                val name = armorStand.customName ?: return@runWithCatching
                val formatted = name.getFormattedString()
                val unformatted = name.string.removeFormatting()
                EventBus.publish(ArmorStandDetailsLoadedEvent(armorStand, armorStand.id, formatted, unformatted))
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
