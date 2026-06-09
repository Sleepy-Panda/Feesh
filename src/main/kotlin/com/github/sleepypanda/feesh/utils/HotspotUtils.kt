package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ArmorStandDespawnedEvent
import com.github.sleepypanda.feesh.events.models.ArmorStandDetailsLoadedEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.Vec3

object HotspotUtils {
    private const val HOTSPOT_ARMOR_STAND_NAME = "HOTSPOT"
    private const val EXPIRATION_MS = 7 * 60 * 1000L // 7 minutes
    private const val TICKS_PER_CLEANUP = 20

    data class HotspotData(
        val entity: ArmorStand,
        val x: Double,
        val y: Double,
        val z: Double,
        val perk: String?
    )

    data class VisibleHotspotData(
        val hotspotPos: Vec3,
        val perkId: Int?,
        val perkPos: Vec3?,
        val perk: String?,
        val firstSeenAt: Long
    )

    data class KnownHotspotData(
        val hotspotId: Int,
        val hotspotPos: Vec3,
        val perkId: Int?,
        val perkPos: Vec3?,
        val perk: String?,
        val firstLoadedAt: Long,
        val lastLoadedAt: Long
    )

    // Hotspots that are currently visible in the world
    private val visibleHotspots = mutableMapOf<Int, VisibleHotspotData>() // Hotspot ArmorStand ID -> hotspot information

    // Hotspots that are already known to the player, but may be not currently visible in the world (far)
    private val knownHotspots = mutableMapOf<String, KnownHotspotData>() // perk|x,y,z -> hotspot information

    private var cleanupTickCounter = 0

    fun init() {
        EventBus.subscribe(ArmorStandDetailsLoadedEvent::class, ::onArmorStandDetailsLoaded)
        EventBus.subscribe(ArmorStandDespawnedEvent::class, ::onArmorStandDespawned)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
    }

    fun getVisibleHotspot(hotspotId: Int): VisibleHotspotData? = visibleHotspots[hotspotId]

    fun getVisibleHotspots(): Map<Int, VisibleHotspotData> = visibleHotspots.toMap()

    fun getKnownHotspots(): Map<String, KnownHotspotData> = knownHotspots.toMap()

    private fun onArmorStandDetailsLoaded(event: ArmorStandDetailsLoadedEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld()) return

        val entity = event.entity
        val now = System.currentTimeMillis()

        // TODO: Sometimes perk armor stand spawns later?
        if (event.customNameUnformatted == HOTSPOT_ARMOR_STAND_NAME) {
            val perkEntity = findHotspotPerkArmorStand(entity, event.entityId - 1)

            ChatUtils.sendLocalChat("Found hotspot at ${entity.x},${entity.y},${entity.z}", true)
            ChatUtils.sendLocalChat("Found perk: ${perkEntity?.third}", true)

            visibleHotspots[event.entityId] = VisibleHotspotData(
                hotspotPos = Vec3(entity.x, entity.y, entity.z),
                perkId = perkEntity?.first,
                perkPos = perkEntity?.second,
                perk = perkEntity?.third,
                firstSeenAt = now
            )

            val knownHotspotKey = "${perkEntity?.third}|${entity.x},${entity.y},${entity.z}"
            val firstLoadedAt = knownHotspots[knownHotspotKey]?.firstLoadedAt ?: now
            knownHotspots[knownHotspotKey] = KnownHotspotData(
                hotspotId = event.entityId,
                hotspotPos = Vec3(entity.x, entity.y, entity.z),
                perkId = perkEntity?.first,
                perkPos = perkEntity?.second,
                perk = perkEntity?.third,
                firstLoadedAt = firstLoadedAt,
                lastLoadedAt = now
            )
            return
        }

        //val hotspotId = event.entityId + 1
        //val cached = knownHotspots[hotspotId] ?: return
//
        //knownHotspots[hotspotId] = cached.copy(
        //    perkId = event.entityId,
        //    perkPos = Vec3(entity.x, entity.y, entity.z),
        //    perk = event.customNameFormatted,
        //    firstSeenAt = now
        //)
    }

    private fun onArmorStandDespawned(event: ArmorStandDespawnedEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld()) return

        if (event.armorStand.customName?.string != HOTSPOT_ARMOR_STAND_NAME) return

        val id = event.armorStand.id
        visibleHotspots.remove(id)

        val player = FeeshMod.mc.player ?: return
        val hotspotPos = Vec3(event.armorStand.x, event.armorStand.y, event.armorStand.z)
        val playerPos = Vec3(player.x, player.y, player.z)
        val distance = EntityUtils.getDistance(hotspotPos.x, hotspotPos.y, hotspotPos.z, playerPos.x, playerPos.y, playerPos.z)
        if (distance <= 30.0) {
            knownHotspots.entries.removeIf { (_, cached) -> cached.hotspotPos == hotspotPos }

            ChatUtils.sendLocalChat("Hotspot gone at ${event.armorStand.x},${event.armorStand.y},${event.armorStand.z}", true)
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        clearHotspots()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        cleanupTickCounter++
        if (cleanupTickCounter < TICKS_PER_CLEANUP) return
        cleanupTickCounter = 0

        cleanupExpiredHotspots()
    }

    private fun clearHotspots() {
        visibleHotspots.clear()
        knownHotspots.clear()
    }

    private fun cleanupExpiredHotspots() {
        if (visibleHotspots.isEmpty() && knownHotspots.isEmpty()) return

        val now = System.currentTimeMillis()
        visibleHotspots.entries.removeIf { (_, cached) -> now - cached.firstSeenAt > EXPIRATION_MS }
        knownHotspots.entries.removeIf { (_, cached) -> now - cached.lastLoadedAt > EXPIRATION_MS }
    }

    private fun findHotspotPerkArmorStand(hotspotStand: ArmorStand, perkEntityId: Int): Triple<Int, Vec3, String>? {
        val perkEntity = hotspotStand.level().getEntity(perkEntityId) as? ArmorStand ?: return null
        val perkName = perkEntity.customName?.getFormattedString() ?: return null
        return Triple(perkEntity.id, Vec3(perkEntity.x, perkEntity.y, perkEntity.z), perkName)
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
        val armorStands = EntityUtils.getArmorStandsInRange(entityPosition, distance)
        if (armorStands.isEmpty()) return null

        val closestHotspotArmorStand = armorStands
            .filter { it.customName?.string == HOTSPOT_ARMOR_STAND_NAME }
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
            .filter { it.customName?.string == HOTSPOT_ARMOR_STAND_NAME }
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
