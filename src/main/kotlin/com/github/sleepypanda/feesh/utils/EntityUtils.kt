package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import kotlin.math.sqrt

object EntityUtils {
    /*
     * Get the distance between two entities.
     * @param entityA The first entity.
     * @param entityB The second entity.
     * @returns {Double} The distance between the two entities.
     */
    fun getDistance(entityA: Entity, entityB: Entity): Double {
        val dx = entityB.x - entityA.x
        val dy = entityB.y - entityA.y
        val dz = entityB.z - entityA.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun isFishingHookActive(player: PlayerEntity): Boolean {
        if (!WorldUtils.isInSkyblock()) return false
        val world = FeeshMod.mc.world ?: return false

        val heldItem = player.mainHandStack
        if (!ItemUtils.isFishingRod(heldItem)) return false

        val fishingHook = world.entities.filterIsInstance<FishingBobberEntity>().firstOrNull { it.owner == player }
        if (fishingHook == null) return false
        if (fishingHook.isInLava() || fishingHook.isTouchingWater()) return true

        val isDirtRod = heldItem.name.string.contains("Dirt Rod")
        if (isDirtRod) return true // For dirt rod, the player's hook can be in dirt

        return false
    }
}