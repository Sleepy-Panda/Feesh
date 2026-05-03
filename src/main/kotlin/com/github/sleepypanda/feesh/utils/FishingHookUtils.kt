package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.events.models.SoundPlayedEvent
import java.util.Date
import net.minecraft.world.phys.Vec3

/** 
 * Information about the casted fishing hook. It can be casted into any blocks, air or fluid.
 */
data class FishingHookInfo(
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
    private var fishingHook: FishingHookInfo? = null
    private var submergedFishingHook: SubmergedFishingHookInfo? = null
    private var submergedFishingHookSeenAt: Date? = null
    private var submergedFishingHookInHotspotSeenAt: Date? = null
    private var lastSoundPlayedAt: Date? = null

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(SoundPlayedEvent::class, ::onSoundPlayed)
    }

    /**
     * Check if the player's fishing hook is active (submerged into water/lava, or casted into the ground for dirt rod).
     * @returns True if the player's fishing hook is active.
     */
    fun isFishingHookActive(): Boolean {
        return submergedFishingHook != null
    }

    /**
     * Get the information about the active fishing hook casted into a water/lava.
     * @returns The information about the active fishing hook.
     */
    fun getActiveFishingHook(): SubmergedFishingHookInfo? {
        return submergedFishingHook
    }

    /**
     * Get the information about the fishing hook, even if it is not in a water/lava.
     * @returns The information about the casted fishing hook.
     */
    fun getFishingHook(): FishingHookInfo? {
        return fishingHook
    }

    fun lastActiveFishingHookSeenAt(): Date? {
        return submergedFishingHookSeenAt
    }

    fun lastActiveFishingHookInHotspotSeenAt(): Date? {
        return submergedFishingHookInHotspotSeenAt
    }

    fun wasFishingHookActiveMinutesAgo(minutes: Int): Boolean {
        val lastFishingHookSeenAt = lastActiveFishingHookSeenAt() ?: return false
        val now = Date()

        return now.time - lastFishingHookSeenAt.time <= minutes * 60 * 1000
    }

    fun wasFishingHookActiveSecondsAgo(seconds: Int): Boolean {
        val lastFishingHookSeenAt = lastActiveFishingHookSeenAt() ?: return false
        val now = Date()

        return now.time - lastFishingHookSeenAt.time <= seconds * 1000
    }

    fun wasFishingHookActiveInHotspotSecondsAgo(seconds: Int): Boolean {
        val lastFishingHookSeenAt = lastActiveFishingHookInHotspotSeenAt() ?: return false
        val now = Date()

        return now.time - lastFishingHookSeenAt.time <= seconds * 1000
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld() ||
            !PlayerUtils.hasFishingRodInHotbar()
        ) {
            fishingHook = null
            submergedFishingHook = null
            return
        }

        val isPrevSubmerged = submergedFishingHook != null

        val fishingHookEntity = EntityUtils.getPlayersFishingHookEntity() ?: run {
            fishingHook = null
            submergedFishingHook = null
            return@onClientTick
        }

        fishingHook = FishingHookInfo(
            x = fishingHookEntity.x,
            y = fishingHookEntity.y,
            z = fishingHookEntity.z,
            age = fishingHookEntity.tickCount,
        )

        if (isOwnFishingHookActive()) {
            submergedFishingHookSeenAt = Date()

            val closestHotspot = if (WorldUtils.isInHotspotFishingWorld()) HotspotUtils.findClosestHotspotInRange(Vec3(fishingHook!!.x, fishingHook!!.y, fishingHook!!.z), 5.0) else null
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

// Double announce
        if (submergedFishingHook != null && lastSoundPlayedAt != null && Date().time - lastSoundPlayedAt!!.time <= 100) {
            ChatUtils.sendLocalChat("Fishing hook caught a fish", true)
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        fishingHook = null
        submergedFishingHook = null
        submergedFishingHookSeenAt = null
        submergedFishingHookInHotspotSeenAt = null
    }

    private fun onSoundPlayed(@Suppress("UNUSED_PARAMETER") event: SoundPlayedEvent) {
        // Check for fishing rod and is fishing
        // Is in proper world
        // Requires Players sounds be enabled
        // Check for reel in click event?
        // Played when someone kills my sc
        if (event.soundName == "entity.experience_orb.pickup" && event.volume == 0.5F) {
            lastSoundPlayedAt = Date()
            ChatUtils.sendLocalChat("Sound played; is submerged ${submergedFishingHook == null}; last seen ${Date().time - (lastActiveFishingHookSeenAt()?.time ?: 0) < 100}", true)
        }
    }

    private fun isOwnFishingHookActive(): Boolean {
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
