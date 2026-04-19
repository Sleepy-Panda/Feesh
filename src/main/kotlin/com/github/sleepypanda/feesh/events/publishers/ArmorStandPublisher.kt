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
    private const val MAX_ATTEMPTS = 2

    private data class PendingNameCheck(val armorStand: ArmorStand, val attempt: Int)

    private val loadedThisTick = mutableListOf<ArmorStand>()
    private var queueForNextClientTick = listOf<PendingNameCheck>()

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
        if (queueForNextClientTick.isEmpty() && loadedThisTick.isEmpty()) return

        val toProcess = queueForNextClientTick
        val retryNextTick = mutableListOf<PendingNameCheck>()

        for (pending in toProcess) {
            val armorStand = pending.armorStand

            CommonUtils.runWithCatching("Failed to publish armor stand details") {
                if (!armorStand.isAlive) return@runWithCatching
                val name = armorStand.customName
                if (name != null) {
                    val formatted = name.getFormattedString()
                    val unformatted = name.string.removeFormatting()
                    EventBus.publish(ArmorStandDetailsLoadedEvent(armorStand, armorStand.id, formatted, unformatted))
                    return@runWithCatching
                }

                if (pending.attempt < MAX_ATTEMPTS) {
                    retryNextTick.add(PendingNameCheck(armorStand, pending.attempt + 1))
                }
            }
        }

        queueForNextClientTick = loadedThisTick.map { PendingNameCheck(it, 1) } + retryNextTick
        loadedThisTick.clear()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        loadedThisTick.clear()
        queueForNextClientTick = emptyList()
    }
}
