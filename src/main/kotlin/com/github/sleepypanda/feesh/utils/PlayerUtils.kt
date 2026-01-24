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
    private var cachedLastFishingHookInHotspotSeenAt: Date? = null
    private var cachedHasFishingRodInHotbar: Boolean = false
    private var cachedHasDirtRodInHand: Boolean = false
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
            setHasDirtRodInHand()
        }
        timer?.scheduleAtFixedRate(task, 0, 500)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        cachedLastFishingHookSeenAt = null
        cachedLastFishingHookInHotspotSeenAt = null
        cachedHasFishingRodInHotbar = false
        cachedHasDirtRodInHand = false
    }

    /*
     * Get the player's name without formatting.
     * @returns {String} The player's name.
     */
    fun getName() : String? {      
        val mc = FeeshMod.mc
        val nameText = mc.player?.name?.string ?: return null
        return nameText
    }

    /*
     * Get the player's formatted name.
     * @returns {String} The player's formatted name.
     */
    fun getFormattedName() : String? {      
        val mc = FeeshMod.mc
        val nameText = mc.player?.getCustomName() ?: mc.player?.displayName ?: return null
        val displayName = nameText.getFormattedString()
        return displayName
    }

    fun hasFishingRodInHotbar(): Boolean {
        return cachedHasFishingRodInHotbar
    }

    fun hasDirtRodInHand(): Boolean {
        return cachedHasDirtRodInHand
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

    private fun setHasDirtRodInHand() {
        val player = FeeshMod.mc.player ?: run {
            cachedHasDirtRodInHand = false
            return
        }
        val heldItem = player.mainHandStack ?: run {
            cachedHasDirtRodInHand = false
            return
        }
        cachedHasDirtRodInHand = ItemUtils.isDirtRod(heldItem)
    }

    fun lastFishingHookSeenAt(): Date? {
        return cachedLastFishingHookSeenAt
    }

    fun lastFishingHookInHotspotSeenAt(): Date? {
        return cachedLastFishingHookInHotspotSeenAt
    }

    fun isFishingHookSeenMinutesAgo(minutes: Int): Boolean {
        val now = Date()
        val lastFishingHookSeenAt = lastFishingHookSeenAt()
        if (lastFishingHookSeenAt == null) return false

        return now.time - lastFishingHookSeenAt.time <= minutes * 60 * 1000
    }

    fun isFishingHookInHotspotSeenMinutesAgo(minutes: Int): Boolean {
        val now = Date()
        val lastFishingHookSeenAt = lastFishingHookInHotspotSeenAt()
        if (lastFishingHookSeenAt == null) return false

        return now.time - lastFishingHookSeenAt.time <= minutes * 60 * 1000
    }

    private fun setLastFishingHookSeenAt() {
        if (!WorldUtils.isInSkyblock()) return
        val player = FeeshMod.mc.player ?: return
    
        val isHookActive = EntityUtils.isFishingHookActive(player)
        if (isHookActive) {
            cachedLastFishingHookSeenAt = Date()

            val playerHook = EntityUtils.getPlayersFishingHook() ?: return
	        HotspotUtils.findClosestHotspotInRange(playerHook) ?: return
	        cachedLastFishingHookInHotspotSeenAt = Date()
        }
    }
}