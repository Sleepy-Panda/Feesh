package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ArmorStandDespawnedEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.HotspotNametagLoadedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.Entity
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import net.minecraft.world.phys.Vec3
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import java.util.UUID

object HotspotUtils {
    data class HotspotData(
        val armorStandId: Int,
        val armorStandUuid: UUID,
        val x: Double,
        val y: Double,
        val z: Double,
        val perk: String?,
        var isLastFished: Boolean = false
    )

    private val knownHotspots = mutableListOf<HotspotData>() // All armor stands loaded in the world currently
    private var lastFishedHotspot: HotspotData? = null // Last fished hotspot, kept after its armor stand is removed from [knownHotspots]
    private var tickCounter = 0

    private const val TICKS_PER_CHECK = 10
    private const val NEAREST_HOTSPOT_RANGE_FROM_HOOK = 5.0

    fun init() {
        EventBus.subscribe(HotspotNametagLoadedEvent::class, ::onHotspotSpawned)
        EventBus.subscribe(ArmorStandDespawnedEvent::class, ::onArmorStandDespawned)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    fun getKnownHotspots(): List<HotspotData> = knownHotspots

    fun getLastFishedHotspot(): HotspotData? = knownHotspots.find { it.isLastFished }

    fun getRetainedLastFishedHotspot(): HotspotData? = lastFishedHotspot

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        ChatUtils.sendLocalChat("${RED}World changed, clearing ${knownHotspots.size} hotspots") // TODO
        knownHotspots.clear()
        lastFishedHotspot = null
        tickCounter = 0
    }

    private fun onHotspotSpawned(event: HotspotNametagLoadedEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld()) return

        CommonUtils.runWithCatching("Failed to handle Hotspot spawned event") {
            val entity = EntityUtils.getMcEntityById(event.hotspotArmorStandId) as? ArmorStand ?: return@runWithCatching
            knownHotspots.removeAll { it.armorStandId == event.hotspotArmorStandId }
            knownHotspots.add(
                HotspotData(
                    armorStandId = entity.id,
                    armorStandUuid = entity.uuid,
                    x = event.position.x,
                    y = event.position.y,
                    z = event.position.z,
                    perk = event.perkFormatted,
                )
            )
        }
    }

    private fun onArmorStandDespawned(event: ArmorStandDespawnedEvent) {
        knownHotspots.removeAll { it.armorStandId == event.armorStand.id }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return

        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        updateIsLastFished()
    }

    private fun updateIsLastFished() {
        CommonUtils.runWithCatching("Failed to track fishing in hotspot") {
            if (!FishingHookUtils.isFishingHookSubmerged()) return
            val playerHook = FishingHookUtils.getSubmergedFishingHook() ?: return
            val sqrDistance = NEAREST_HOTSPOT_RANGE_FROM_HOOK * NEAREST_HOTSPOT_RANGE_FROM_HOOK
            val closestHotspotInRange = knownHotspots
                .filter {
                    EntityUtils.getDistanceSqr(it.x, it.y, it.z, playerHook.x, playerHook.y, playerHook.z) <= sqrDistance
                }
                .minByOrNull {
                    EntityUtils.getDistanceSqr(it.x, it.y, it.z, playerHook.x, playerHook.y, playerHook.z)
                } ?: return

            knownHotspots.forEach { it.isLastFished = it.armorStandId == closestHotspotInRange.armorStandId }
            lastFishedHotspot = closestHotspotInRange
        }
    }

    /**
     * Check if an armor stand is a perk for a hotspot armor stand.
     * A perk armor stand must be at the same X and Z, Y is below the HOTSPOT, within 1 block, and have the same pitch.
     * @param perkCandidate The armor stand to check if it's a perk.
     * @param hotspotStand The hotspot armor stand.
     * @return true if the candidate is a perk for the hotspot.
     */
    fun isPerkForHotspot(perkCandidate: ArmorStand, hotspotStand: ArmorStand): Boolean {
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
     * @returns HotspotData, or null if not found.
     */
    fun findClosestHotspotInRange(entityPosition: Vec3, distance: Double): HotspotData? {
        val sqrDistance = distance * distance
        val closestHotspotInRange = knownHotspots
            .filter { EntityUtils.getDistanceSqr(it.x, it.y, it.z, entityPosition.x, entityPosition.y, entityPosition.z) <= sqrDistance }
            .minByOrNull { EntityUtils.getDistanceSqr(it.x, it.y, it.z, entityPosition.x, entityPosition.y, entityPosition.z) }
        if (closestHotspotInRange != null) return closestHotspotInRange

        return findClosestHotspotArmorStandInRange(entityPosition, distance)
    }

    /**
     * Find all Hotspots within the specified range from the specified entity.
     * @param entity The entity to search from.
     * @param distance The maximum distance to search.
     * @returns List of HotspotData
     */
    fun findHotspotsInRange(entity: Entity, distance: Double): List<HotspotData> {
        val sqrDistance = distance * distance
        val hotspotsInRange = knownHotspots
            .filter { EntityUtils.getDistanceSqr(entity.x, entity.y, entity.z, it.x, it.y, it.z) <= sqrDistance }
            .sortedBy { EntityUtils.getDistanceSqr(entity.x, entity.y, entity.z, it.x, it.y, it.z) }
        if (hotspotsInRange.isNotEmpty()) return hotspotsInRange

        return findHotspotArmorStandsInRange(entity, distance)
    }

    private fun findClosestHotspotArmorStandInRange(entityPosition: Vec3, distance: Double): HotspotData? {
        val armorStands = EntityUtils.getArmorStandsInRange(entityPosition, distance)
        if (armorStands.isEmpty()) return null

        val closestHotspotArmorStand = armorStands
            .filter { it.customName.getUnformattedString() == "HOTSPOT" }
            .minByOrNull { EntityUtils.getDistanceSqr(it.x, it.y, it.z, entityPosition.x, entityPosition.y, entityPosition.z) }

        if (closestHotspotArmorStand == null) return null

        val perkArmorStand = armorStands.find { e ->
            isPerkForHotspot(e, closestHotspotArmorStand)
        }

        return HotspotData(
            armorStandId = closestHotspotArmorStand.id,
            armorStandUuid = closestHotspotArmorStand.uuid,
            x = closestHotspotArmorStand.x,
            y = closestHotspotArmorStand.y,
            z = closestHotspotArmorStand.z,
            perk = perkArmorStand?.customName?.getFormattedString()
        )
    }

    private fun findHotspotArmorStandsInRange(entity: Entity, distance: Double): List<HotspotData> {
        val armorStands = EntityUtils.getArmorStandsInRange(Vec3(entity.x, entity.y, entity.z), distance)
        return armorStands
            .filter { it.customName.getUnformattedString() == "HOTSPOT" }
            .sortedBy { EntityUtils.getDistanceSqr(entity.x, entity.y, entity.z, it.x, it.y, it.z) }
            .map { asEntity ->
                val perkArmorStand = armorStands.find { e ->
                    isPerkForHotspot(e, asEntity)
                }

                HotspotData(
                    armorStandId = asEntity.id,
                    armorStandUuid = asEntity.uuid,
                    x = asEntity.x,
                    y = asEntity.y,
                    z = asEntity.z,
                    perk = perkArmorStand?.customName?.getFormattedString()
                )
            }
    }
}
