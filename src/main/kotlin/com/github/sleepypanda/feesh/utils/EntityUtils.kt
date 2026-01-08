package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.entity.decoration.ArmorStandEntity
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

        val heldItem = player.mainHandStack
        if (!ItemUtils.isFishingRod(heldItem)) return false

        val fishingHook = getPlayersFishingHook()
        if (fishingHook == null) return false
        if (fishingHook.isInLava() || fishingHook.isTouchingWater()) return true

        val isDirtRod = heldItem.name.string.contains("Dirt Rod")
        if (isDirtRod) return true // For dirt rod, the player's hook can be in dirt

        return false
    }

    /**
     * Get the player's fishing hook if it is active.
     * @returns The player's fishing hook.
     */
    fun getPlayersFishingHook(): FishingBobberEntity? {
        val player = FeeshMod.mc.player ?: return null
        val world = FeeshMod.mc.world ?: return null
        return world.entities.filterIsInstance<FishingBobberEntity>().firstOrNull { it.owner == player }
    }

    /**
     * Get all ArmorStandEntities within the specified range from the specified entity.
     * @param entity The entity to search from.
     * @param distance The maximum distance to search.
     * @returns List of ArmorStandEntity
     */
    fun getArmorStandsInRange(entity: Entity, distance: Double): List<ArmorStandEntity> {
        val world = FeeshMod.mc.world ?: return emptyList()
        val armorStands = world.entities
            .filterIsInstance<ArmorStandEntity>()
            .filter { asEntity ->
                EntityUtils.getDistance(entity, asEntity) <= distance
            }

        return armorStands
    }

    /**
     * Get all ArmorStandEntities with the specified unformattedname within the specified range from the specified entity.
     * @param entity The entity to search from.
     * @param distance The maximum distance to search.
     * @param name The unformatted name of the ArmorStandEntity.
     * @returns List of ArmorStandEntity
     */
    fun getArmorStandsInRange(entity: Entity, distance: Double, name: String): List<ArmorStandEntity> {
        val armorStands = getArmorStandsInRange(entity, distance)
            .filter { asEntity ->
                asEntity.customName?.string == name
            }

        return armorStands
    }
}