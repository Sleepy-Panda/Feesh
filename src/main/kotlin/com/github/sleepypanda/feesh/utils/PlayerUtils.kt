package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormatted
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.WorldChangedEvent
import net.minecraft.text.Text
import net.minecraft.item.ItemStack
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.projectile.FishingBobberEntity
import java.util.Date
import java.util.Timer
import kotlin.concurrent.timerTask

object PlayerUtils {
    private var cachedLastFishingHookSeenAt: Date? = null
    private var timer: Timer? = null

    fun init() {
        startTimer()
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun startTimer() {
        timer?.cancel()
        timer = Timer()
        
        val task = timerTask {
            setLastFishingHookSeenAt()
        }
        timer?.scheduleAtFixedRate(task, 0, 500)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        cachedLastFishingHookSeenAt = null
    }

    fun getName() : String {      
        val mc = FeeshMod.mc
        val displayNameText = mc.player?.displayName ?: return ""
        val displayName = displayNameText.getFormatted()
        //FeeshMod.LOGGER.info("FEESH DISPLAY NAME: ${displayName}")
        return displayName
    }

    fun hasFishingRodInHotbar(): Boolean {
        val player = FeeshMod.mc.player ?: return false
        for (i in 0..8) {
            val stack = player.inventory.getStack(i)
            if (isFishingRod(stack)) return true
        }
        return false
    }

    fun lastFishingHookSeenAt(): Date? {
        return cachedLastFishingHookSeenAt
    }

    fun isFishingHookSeenMinutesAgo(minutes: Int): Boolean {
        val now = Date()
        val lastFishingHookSeenAt = lastFishingHookSeenAt()
        if (lastFishingHookSeenAt == null) return false

        return now.time - lastFishingHookSeenAt.time <= minutes * 60 * 1000
    }

    private fun setLastFishingHookSeenAt() {
        if (!WorldUtils.isInSkyblock()) return
    
        val isHookActive = isFishingHookActive()
        if (isHookActive) {
            cachedLastFishingHookSeenAt = Date()
        }
    }

    private fun isFishingHookActive(): Boolean {
        if (!WorldUtils.isInSkyblock()) return false
        val player = FeeshMod.mc.player ?: return false
        val world = FeeshMod.mc.world ?: return false

        val heldItem = player.mainHandStack
        if (!isFishingRod(heldItem)) return false

        val fishingHook = world.entities.filterIsInstance<FishingBobberEntity>().firstOrNull { it.owner == player }
        if (fishingHook == null) return false
        if (fishingHook.isInLava() || fishingHook.isTouchingWater()) return true

        val isDirtRod = heldItem.name.string.contains("Dirt Rod")
        if (isDirtRod) return true // For dirt rod, the player's hook can be in dirt

        return false
    }

    private fun isFishingRod(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty) return false

        val name = item.name.string      
        if (name.contains("Carnival Rod")) return false

        val loreLines = item.get(DataComponentTypes.LORE)?.lines?.map { it.string } ?: listOf()
        return loreLines.any { it.contains("FISHING ROD", ignoreCase = true) || it.contains("FISHING WEAPON", ignoreCase = true) }
    }
}