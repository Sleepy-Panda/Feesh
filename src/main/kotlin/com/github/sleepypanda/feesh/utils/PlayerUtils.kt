package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.WorldChangedEvent
import net.minecraft.text.Text
import java.util.Date
import java.util.Timer
import kotlin.concurrent.timerTask

object PlayerUtils {
    private var cachedLastFishingHookSeenAt: Date? = null
    private var cachedHasFishingRodInHotbar: Boolean = false
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
            setHasFishingRodInHotbar()
        }
        timer?.scheduleAtFixedRate(task, 0, 500)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        cachedLastFishingHookSeenAt = null
        cachedHasFishingRodInHotbar = false
    }

    fun getName() : String {      
        val mc = FeeshMod.mc
        val nameText = mc.player?.getCustomName() ?: mc.player?.displayName ?: return ""
        val displayName = nameText.getFormattedString()
        return displayName
    }

    fun hasFishingRodInHotbar(): Boolean {
        return cachedHasFishingRodInHotbar
    }

    private fun setHasFishingRodInHotbar() {
        val player = FeeshMod.mc.player ?: run {
            cachedHasFishingRodInHotbar = false
            return
        }
        for (i in 0..8) {
            val stack = player.inventory.getStack(i)
            if (ItemUtils.isFishingRod(stack)) {
                cachedHasFishingRodInHotbar = true
                return
            }
        }
        cachedHasFishingRodInHotbar = false
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
        val player = FeeshMod.mc.player ?: return
    
        val isHookActive = EntityUtils.isFishingHookActive(player)
        if (isHookActive) {
            cachedLastFishingHookSeenAt = Date()
        }
    }
}