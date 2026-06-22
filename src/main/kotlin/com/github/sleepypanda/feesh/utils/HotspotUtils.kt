package com.github.sleepypanda.feesh.utils

import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.Entity
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import net.minecraft.world.phys.Vec3

object HotspotUtils {
    data class HotspotData(
        val entity: ArmorStand,
        val x: Double,
        val y: Double,
        val z: Double,
        val perk: String?
    )

    /**
     * Check if an armor stand is a perk for a hotspot armor stand.
     * A perk armor stand must be at the same X and Z, Y is below the HOTSPOT, within 1 block, and have the same pitch.
     * @param perkCandidate The armor stand to check if it's a perk.
     * @param hotspotStand The hotspot armor stand.
     * @return true if the candidate is a perk for the hotspot.
     */
    private fun isPerkForHotspot(perkCandidate: ArmorStand, hotspotStand: ArmorStand): Boolean {
        return perkCandidate.x == hotspotStand.x &&
                perkCandidate.y < hotspotStand.y &&
                hotspotStand.y - perkCandidate.y <= 1.0 &&
                perkCandidate.z == hotspotStand.z &&
                perkCandidate.xRot == hotspotStand.xRot
    }

    /**
     * Find the closest Hotspot in the specified range from the specified entity position.
     * @param entityPosition The position to search from.
     * @param distance The maximum distance to search.
     * @returns HotspotData in the format { entity, position, perk } or null if not found.
     */
    fun findClosestHotspotInRange(entityPosition: Vec3, distance: Double): HotspotData? {
        val armorStands = EntityUtils.getArmorStandsInRange(entityPosition, distance)
        if (armorStands.isEmpty()) return null

        val closestHotspotArmorStand = armorStands
            .filter { it.customName.getUnformattedString() == "HOTSPOT" }
            .minByOrNull { EntityUtils.getDistance(it, entityPosition.x, entityPosition.y, entityPosition.z) }

        if (closestHotspotArmorStand == null) return null

        // Find the perk armor stand (same X and Z, Y is below the HOTSPOT, within 1 block)
        val perkArmorStand = armorStands.find { e ->
            isPerkForHotspot(e, closestHotspotArmorStand)
        }

        val perk = perkArmorStand?.customName?.getFormattedString()

        return HotspotData(
            entity = closestHotspotArmorStand,
            x = closestHotspotArmorStand.x,
            y = closestHotspotArmorStand.y,
            z = closestHotspotArmorStand.z,
            perk = perk
        )
    }

    /**
     * Find all Hotspots within the specified range from the specified entity.
     * @param entity The entity to search from.
     * @param distance The maximum distance to search.
     * @returns List of HotspotData
     */
    fun findHotspotsInRange(entity: Entity, distance: Double): List<HotspotData> {
        val armorStands = EntityUtils.getArmorStandsInRange(Vec3(entity.x, entity.y, entity.z), distance)
        val closeHotspotArmorStands = armorStands
            .filter { it.customName.getUnformattedString() == "HOTSPOT" }
            .sortedBy { EntityUtils.getDistance(entity, it) }
            .map { asEntity ->
                // Find the perk armor stand (same X and Z, Y is below the HOTSPOT, within 1 block)
                val perkArmorStand = armorStands.find { e ->
                    isPerkForHotspot(e, asEntity)
                }

                val perk = perkArmorStand?.customName?.getFormattedString()

                HotspotData(
                    entity = asEntity,
                    x = asEntity.x,
                    y = asEntity.y,
                    z = asEntity.z,
                    perk = perk
                )
            }

        return closeHotspotArmorStands
    }
}

