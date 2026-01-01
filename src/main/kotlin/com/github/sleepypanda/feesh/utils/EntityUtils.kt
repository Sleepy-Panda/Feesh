package com.github.sleepypanda.feesh.utils

import net.minecraft.entity.Entity
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
}