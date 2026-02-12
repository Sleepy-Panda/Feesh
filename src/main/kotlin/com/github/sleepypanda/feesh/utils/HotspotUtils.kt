package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.Entity
import net.minecraft.text.Text
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting

object HotspotUtils {
    data class HotspotData(
        val entity: ArmorStandEntity,
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
    private fun isPerkForHotspot(perkCandidate: ArmorStandEntity, hotspotStand: ArmorStandEntity): Boolean {
        return perkCandidate.x == hotspotStand.x &&
                perkCandidate.y < hotspotStand.y &&
                hotspotStand.y - perkCandidate.y <= 1.0 &&
                perkCandidate.z == hotspotStand.z &&
                perkCandidate.pitch == hotspotStand.pitch
    }

    /**
     * Find the closest Hotspot in the specified range from the specified entity.
     * @param entity The entity to search from.
     * @param distance The maximum distance to search.
     * @returns HotspotData in the format { entity, position, perk } or null if not found.
     */
    fun findClosestHotspotInRange(entity: Entity, distance: Double): HotspotData? {
        val armorStands = EntityUtils.getArmorStandsInRange(entity, distance)
        if (armorStands.isEmpty()) return null

        val closestHotspotArmorStand = armorStands
            .filter { it.customName?.string == "HOTSPOT" }
            .minByOrNull { EntityUtils.getDistance(entity, it) }

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
        val armorStands = EntityUtils.getArmorStandsInRange(entity, distance)
        val closeHotspotArmorStands = armorStands
            .filter { it.customName?.string == "HOTSPOT" }
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

