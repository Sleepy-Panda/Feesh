package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.ItemEntitySpawnedEvent
import com.github.sleepypanda.feesh.events.WorldChangedEvent
import net.minecraft.entity.ItemEntity
import net.minecraft.sound.SoundEvents
import java.util.concurrent.ConcurrentHashMap
import java.util.Timer
import java.util.TimerTask

object WormTheFishCaughtAlert {
    private const val WORM_THE_FISH_NAME = "Worm the Fish"
    private const val CHECK_DELAY_MS = 100L // Wait 100ms before checking item stack name as it's not available immediately
    private const val CLEANUP_INTERVAL_MS = 10_000L // How often to run cleanup
    private const val CLEANUP_DELAY_MS = 30_000L // Remove processed entries after 30 seconds

    private val processedEntityIds = ConcurrentHashMap<Int, Long>()
    private val activeTimers = ConcurrentHashMap<Int, TimerTask>() // entityId -> timer task
    private var checkTimer: Timer? = null
    private var cleanupTimer: Timer? = null

    fun init() {
        EventBus.subscribe(ItemEntitySpawnedEvent::class, ::onItemEntitySpawned)
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

    private fun getOrCreateCheckTimer(): Timer {
        if (checkTimer == null) {
            checkTimer = Timer("WormTheFishEntitiesCheck", true)
        }
        return checkTimer!!
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        activeTimers.values.forEach { it.cancel() }
        activeTimers.clear()
        processedEntityIds.clear()
        checkTimer?.cancel()
        checkTimer = null
    }

    private fun onItemEntitySpawned(event: ItemEntitySpawnedEvent) {
        if (!Alerts.alertOnWormTheFishCaught || !WorldUtils.isInSkyblock() || !PlayerUtils.hasDirtRodInHand()) return

        val player = FeeshMod.mc.player ?: return
        if (!event.itemEntity.isInRange(player, 10.0)) return

        val itemEntity = event.itemEntity
        val entityId = itemEntity.id

        // Skip if already processed recently
        val processedTime = processedEntityIds[entityId]
        if (processedTime != null) return

        // Cancel existing timer for this entity if any
        activeTimers[entityId]?.cancel()

        // Schedule check after delay using shared timer
        val timer = getOrCreateCheckTimer()
        val task = object : TimerTask() {
            override fun run() {
                activeTimers.remove(entityId)
                checkItemEntity(entityId)
            }
        }
        timer.schedule(task, CHECK_DELAY_MS)
        activeTimers[entityId] = task
    }

    private fun checkItemEntity(entityId: Int) {
        if (!Alerts.alertOnWormTheFishCaught || !WorldUtils.isInSkyblock() || !PlayerUtils.hasDirtRodInHand()) return

        val world = FeeshMod.mc.world ?: return
        val itemEntity = world.getEntityById(entityId) as? ItemEntity ?: return
        val itemStack = itemEntity.stack

        if (itemStack == null || itemStack.isEmpty) return

        val itemName = itemStack.customName?.string ?: itemStack.name.string ?: return
        if (itemName != WORM_THE_FISH_NAME) return

        val now = System.currentTimeMillis()
        processedEntityIds[entityId] = now

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
