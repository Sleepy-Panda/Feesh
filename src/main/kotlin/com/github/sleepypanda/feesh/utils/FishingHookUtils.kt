package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import net.minecraft.entity.projectile.FishingBobberEntity

import java.util.Date

/** 
 * Information about the active fishing hook casted into water/lava (or into dirt for dirt rod).
 */
data class ActiveFishingHookInfo(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    val age: Int = 0,
)

object FishingHookUtils {
    var activeFishingHook: ActiveFishingHookInfo? = null
    private var lastFishingHookSeenAt: Date? = null

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    /**
     * Check if the player's fishing hook is active (casted into water/lava, or into the ground for dirt rod).
     * @returns True if the player's fishing hook is active.
     */
    fun isFishingHookActive(): Boolean {
        return activeFishingHook != null
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld() ||
            !PlayerUtils.hasFishingRodInHotbar()
        ) {
            activeFishingHook = null
            return
        }

        if (isOwnFishingHookActive()) {
            lastFishingHookSeenAt = Date()
            val fishingHook = EntityUtils.getPlayersFishingHook() ?: return
            activeFishingHook = ActiveFishingHookInfo(
                x = fishingHook.x,
                y = fishingHook.y,
                z = fishingHook.z,
                age = fishingHook.age,
            )
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        activeFishingHook = null
        lastFishingHookSeenAt = null
    }

    private fun isOwnFishingHookActive(): Boolean {
        if (!WorldUtils.isInSkyblock()) return false
        val player = FeeshMod.mc.player ?: return false

        val heldItem = player.mainHandStack
        if (!ItemUtils.isFishingRod(heldItem)) return false

        val fishingHook = EntityUtils.getPlayersFishingHook() ?: return false
        if (fishingHook.isInLava() || fishingHook.isTouchingWater()) return true

        val isDirtRod = heldItem.name.string.contains("Dirt Rod")
        if (isDirtRod) return true // For dirt rod, the player's hook can be in dirt

        return false
    }
}
