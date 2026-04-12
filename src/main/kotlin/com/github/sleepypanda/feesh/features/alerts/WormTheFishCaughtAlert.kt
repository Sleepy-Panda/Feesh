package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ItemEntityDetailsLoadedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import net.minecraft.sound.SoundEvents
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap

object WormTheFishCaughtAlert {
    private const val WORM_THE_FISH_NAME = "Worm the Fish"
    private const val CLEANUP_INTERVAL_MS = 10_000L // How often to run cleanup
    private const val CLEANUP_DELAY_MS = 30_000L // Remove processed entries after 30 seconds

    private val processedEntityIds = ConcurrentHashMap<Int, Long>()
    private var cleanupTimer: Timer? = null

    fun init() {
        EventBus.subscribe(ItemEntityDetailsLoadedEvent::class, ::onItemEntityDetailsLoaded)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        startCleanupTimer()
    }

    private fun startCleanupTimer() {
        cleanupTimer?.cancel()
        cleanupTimer = Timer("WormTheFishEntitiesCleanup", true)

        val cleanupTask = object : TimerTask() {
            override fun run() {
                cleanupOutdatedEntries()
            }
        }
        cleanupTimer?.scheduleAtFixedRate(cleanupTask, CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        processedEntityIds.clear()
    }

    private fun onItemEntityDetailsLoaded(event: ItemEntityDetailsLoadedEvent) {
        if (!Alerts.alertOnWormTheFishCaught || !WorldUtils.isInSkyblock() || !PlayerUtils.hasDirtRodInHand()) return

        val player = FeeshMod.mc.player ?: return
        val itemEntity = event.entity
        if (!itemEntity.isInRange(player, 10.0)) return

        val entityId = event.entityId
        if (processedEntityIds.containsKey(entityId)) return

        val itemName = event.itemNameUnformatted
        if (itemName != WORM_THE_FISH_NAME) return

        processedEntityIds[entityId] = System.currentTimeMillis()

        CommonUtils.showTitle("${WHITE}Pickup ${RED}Worm the Fish")
        SoundUtils.playSound(SoundEvents.ENTITY_GENERIC_SPLASH)
    }

    private fun cleanupOutdatedEntries() {
        val now = System.currentTimeMillis()

        if (processedEntityIds.isNotEmpty()) {
            val expiredProcessedIds = processedEntityIds.filter { (_, timestamp) ->
                now - timestamp > CLEANUP_DELAY_MS
            }.keys

            expiredProcessedIds.forEach { entityId ->
                processedEntityIds.remove(entityId)
            }
        }
    }
}
