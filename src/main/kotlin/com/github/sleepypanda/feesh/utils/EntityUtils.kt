package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.ArmorStandEntity
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
        return getDistance(entityA.x, entityA.y, entityA.z, entityB.x, entityB.y, entityB.z)
    }

    /**
     * Get the distance between an entity and a point in the world.
     * @param entityA The entity.
     * @param x The x coordinate of the point.
     * @param y The y coordinate of the point.
     * @param z The z coordinate of the point.
     * @returns {Double} The distance between the entity and the point.
     */
    fun getDistance(entityA: Entity, x: Double, y: Double, z: Double): Double {
        return getDistance(entityA.x, entityA.y, entityA.z, x, y, z)
    }

    /**
     * Get the distance between two points in the world.
     * @param xa The x coordinate of the first point.
     * @param ya The y coordinate of the first point.
     * @param za The z coordinate of the first point.
     * @param xb The x coordinate of the second point.
     * @param yb The y coordinate of the second point.
     * @param zb The z coordinate of the second point.
     * @returns {Double} The distance between the two points.
     */
    fun getDistance(xa: Double, ya: Double, za: Double, xb: Double, yb: Double, zb: Double): Double {
        val dx = xb - xa
        val dy = yb - ya
        val dz = zb - za
        return sqrt(dx * dx + dy * dy + dz * dz)
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

    /**
     * Get an entity by its numeric ID from the world.
     * @param entityId The numeric ID of the entity.
     * @return The entity if found, null otherwise.
     */
    fun getMcEntityById(entityId: Int): Entity? {
        val world = FeeshMod.mc.world ?: return null
        return world.getEntityById(entityId)
    }
}