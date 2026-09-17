package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ArmorStandDespawnedEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.HotspotSpawnedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
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
        val perk: String?,
        var fishedInRecently: Boolean = false
    )

    private val knownHotspots = mutableListOf<HotspotData>()
    private var tickCounter = 0

    private const val TICKS_PER_CHECK = 10
    private const val NEAREST_HOTSPOT_RANGE_FROM_HOOK = 5.0

    fun init() {
        EventBus.subscribe(HotspotSpawnedEvent::class, ::onHotspotSpawned)
        EventBus.subscribe(ArmorStandDespawnedEvent::class, ::onArmorStandDespawned)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    fun getKnownHotspots(): List<HotspotData> = knownHotspots

    fun getLastFishedHotspot(): HotspotData? = knownHotspots.find { it.fishedInRecently }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        knownHotspots.clear()
        tickCounter = 0
    }

    private fun onHotspotSpawned(event: HotspotSpawnedEvent) {
        CommonUtils.runWithCatching("Failed to remember spawned hotspot") {
            val entity = EntityUtils.getMcEntityById(event.hotspotArmorStandId) as? ArmorStand ?: return@runWithCatching
            knownHotspots.removeAll { it.entity.id == event.hotspotArmorStandId }
            knownHotspots.add(
                HotspotData(
                    entity = entity,
                    x = event.position.x,
                    y = event.position.y,
                    z = event.position.z,
                    perk = event.perk,
                )
            )
            WaypointUtils.add(
                x = event.position.x,
                y = event.position.y - 1,
                z = event.position.z,
                colorRgb = 0xFC54FC,
                alpha = 0.4f,
                durationMillis = 30_000L,
                throughWalls = true,
            )
        }
    }

    private fun onArmorStandDespawned(event: ArmorStandDespawnedEvent) {
        knownHotspots.removeAll { it.entity.id == event.armorStand.id }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return

        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        updateFishedInRecently()
    }

    private fun updateFishedInRecently() {
        CommonUtils.runWithCatching("Failed to track fishing in hotspot") {
            if (!FishingHookUtils.isFishingHookSubmerged()) return
            val playerHook = FishingHookUtils.getSubmergedFishingHook() ?: return
            val closestKnown = knownHotspots
                .filter {
                    EntityUtils.getDistance(it.x, it.y, it.z, playerHook.x, playerHook.y, playerHook.z) <= NEAREST_HOTSPOT_RANGE_FROM_HOOK
                }
                .minByOrNull {
                    EntityUtils.getDistance(it.x, it.y, it.z, playerHook.x, playerHook.y, playerHook.z)
                } ?: return

            knownHotspots.forEach { it.fishedInRecently = it.entity.id == closestKnown.entity.id }
        }
    }

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
        val closestKnown = knownHotspots
            .filter { EntityUtils.getDistance(it.x, it.y, it.z, entityPosition.x, entityPosition.y, entityPosition.z) <= distance }
            .minByOrNull { EntityUtils.getDistance(it.x, it.y, it.z, entityPosition.x, entityPosition.y, entityPosition.z) }
        if (closestKnown != null) return closestKnown

        return findClosestHotspotInRangeFromWorld(entityPosition, distance)
    }

    /**
     * Find all Hotspots within the specified range from the specified entity.
     * @param entity The entity to search from.
     * @param distance The maximum distance to search.
     * @returns List of HotspotData
     */
    fun findHotspotsInRange(entity: Entity, distance: Double): List<HotspotData> {
        val closeKnown = knownHotspots
            .filter { EntityUtils.getDistance(entity, it.x, it.y, it.z) <= distance }
            .sortedBy { EntityUtils.getDistance(entity, it.x, it.y, it.z) }
        if (closeKnown.isNotEmpty()) return closeKnown

        return findHotspotsInRangeFromWorld(entity, distance)
    }

    private fun findClosestHotspotInRangeFromWorld(entityPosition: Vec3, distance: Double): HotspotData? {
        val armorStands = EntityUtils.getArmorStandsInRange(entityPosition, distance)
        if (armorStands.isEmpty()) return null

        val closestHotspotArmorStand = armorStands
            .filter { it.customName.getUnformattedString() == "HOTSPOT" }
            .minByOrNull { EntityUtils.getDistance(it, entityPosition.x, entityPosition.y, entityPosition.z) }

        if (closestHotspotArmorStand == null) return null

        val perkArmorStand = armorStands.find { e ->
            isPerkForHotspot(e, closestHotspotArmorStand)
        }

        return HotspotData(
            entity = closestHotspotArmorStand,
            x = closestHotspotArmorStand.x,
            y = closestHotspotArmorStand.y,
            z = closestHotspotArmorStand.z,
            perk = perkArmorStand?.customName?.getFormattedString()
        )
    }

    private fun findHotspotsInRangeFromWorld(entity: Entity, distance: Double): List<HotspotData> {
        val armorStands = EntityUtils.getArmorStandsInRange(Vec3(entity.x, entity.y, entity.z), distance)
        return armorStands
            .filter { it.customName.getUnformattedString() == "HOTSPOT" }
            .sortedBy { EntityUtils.getDistance(entity, it) }
            .map { asEntity ->
                val perkArmorStand = armorStands.find { e ->
                    isPerkForHotspot(e, asEntity)
                }

                HotspotData(
                    entity = asEntity,
                    x = asEntity.x,
                    y = asEntity.y,
                    z = asEntity.z,
                    perk = perkArmorStand?.customName?.getFormattedString()
                )
            }
    }
}
