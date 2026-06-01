package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import java.util.Date
import net.minecraft.world.phys.Vec3

/** 
 * Information about the active fishing hook. It can be casted into any blocks, air or fluid.
 */
data class ActiveFishingHookInfo(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    val age: Int = 0,
)

/** 
 * Information about the active fishing hook submerged into water/lava while in a fishing world (or into dirt for dirt rod).
 * It is relevant to detect if the player is fishing (not just casting their rod for other goals such as autopet etc).
 */
data class SubmergedFishingHookInfo(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var age: Int = 0,
    var isInHotspot: Boolean = false,
)

object FishingHookUtils {
    private var activeFishingHook: ActiveFishingHookInfo? = null
    private var activeFishingHookSeenAt: Date? = null
    private var lastActiveFishingHook: ActiveFishingHookInfo? = null // Used to store the last known fishing hook even if it is not present anymore

    private var submergedFishingHook: SubmergedFishingHookInfo? = null
    private var submergedFishingHookSeenAt: Date? = null
    private var submergedFishingHookInHotspotSeenAt: Date? = null

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    /**
     * Get the information about the casted fishing hook, even if it is not submerged in a water/lava.
     * @returns The information about the casted fishing hook.
     */
    fun getActiveFishingHook(): ActiveFishingHookInfo? {
        return activeFishingHook
    }

    fun getLastActiveFishingHook(): ActiveFishingHookInfo? {
        return activeFishingHook ?: lastActiveFishingHook
    }

    fun lastActiveFishingHookSeenAt(): Date? {
        return activeFishingHookSeenAt
    }

    /**
     * Checks if the casted fishing hook (not necessarily submerged) was present during the last given seconds.
     */
    fun wasFishingHookActiveSecondsAgo(seconds: Int): Boolean {
        val lastSeen = activeFishingHookSeenAt ?: return false
        val now = Date()
        return now.time - lastSeen.time <= seconds * 1000
    }

    /**
     * Check if the player's fishing hook is submerged (into water/lava, or casted into the ground for dirt rod).
     * @returns True if the player's fishing hook is active.
     */
    fun isFishingHookSubmerged(): Boolean {
        return submergedFishingHook != null
    }

    /**
     * Get the information about the active fishing hook casted into a water/lava.
     * @returns The information about the active fishing hook.
     */
    fun getSubmergedFishingHook(): SubmergedFishingHookInfo? {
        return submergedFishingHook
    }

    fun lastSubmergedFishingHookSeenAt(): Date? {
        return submergedFishingHookSeenAt
    }

    fun lastSubmergedFishingHookInHotspotSeenAt(): Date? {
        return submergedFishingHookInHotspotSeenAt
    }

    fun wasFishingHookSubmergedMinutesAgo(minutes: Int): Boolean {
        val lastSeen = lastSubmergedFishingHookSeenAt() ?: return false
        val now = Date()
        return now.time - lastSeen.time <= minutes * 60 * 1000
    }

    fun wasFishingHookSubmergedSecondsAgo(seconds: Int): Boolean {
        val lastSeen = lastSubmergedFishingHookSeenAt() ?: return false
        val now = Date()
        return now.time - lastSeen.time <= seconds * 1000
    }

    fun wasFishingHookSubmergedInHotspotSecondsAgo(seconds: Int): Boolean {
        val lastSeen = lastSubmergedFishingHookInHotspotSeenAt() ?: return false
        val now = Date()
        return now.time - lastSeen.time <= seconds * 1000
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld() ||
            !PlayerUtils.hasFishingRodInHotbar()
        ) {
            activeFishingHook = null
            lastActiveFishingHook = null
            submergedFishingHook = null
            return
        }

        val fishingHookEntity = EntityUtils.getPlayersFishingHookEntity() ?: run {
            activeFishingHook = null
            submergedFishingHook = null
            return@onClientTick
        }

        activeFishingHook = ActiveFishingHookInfo(
            x = fishingHookEntity.x,
            y = fishingHookEntity.y,
            z = fishingHookEntity.z,
            age = fishingHookEntity.tickCount,
        )
        lastActiveFishingHook = activeFishingHook
        activeFishingHookSeenAt = Date()

        if (isOwnFishingHookSubmerged()) {
            submergedFishingHookSeenAt = Date()

            val closestHotspot = if (WorldUtils.isInHotspotFishingWorld()) HotspotUtils.findClosestHotspotInRange(Vec3(activeFishingHook!!.x, activeFishingHook!!.y, activeFishingHook!!.z), 5.0) else null
            if (closestHotspot != null) submergedFishingHookInHotspotSeenAt = Date()

            submergedFishingHook = SubmergedFishingHookInfo(
                x = fishingHookEntity.x,
                y = fishingHookEntity.y,
                z = fishingHookEntity.z,
                age = fishingHookEntity.tickCount,
                isInHotspot = closestHotspot != null,
            )
        } else {
            submergedFishingHook = null
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        activeFishingHook = null
        activeFishingHookSeenAt = null
        lastActiveFishingHook = null
        submergedFishingHook = null
        submergedFishingHookSeenAt = null
        submergedFishingHookInHotspotSeenAt = null
    }

    private fun isOwnFishingHookSubmerged(): Boolean {
        if (!WorldUtils.isInSkyblock()) return false
        val player = FeeshMod.mc.player ?: return false

        val heldItem = player.mainHandItem
        if (!ItemUtils.isFishingRod(heldItem)) return false

        val fishingHook = EntityUtils.getPlayersFishingHookEntity() ?: return false
        if (fishingHook.isInLava || fishingHook.isInWater) return true

        val isDirtRod = heldItem.hoverName.string.contains("Dirt Rod")
        if (isDirtRod) return true // For dirt rod, the player's hook can be in dirt

        return false
    }
}
