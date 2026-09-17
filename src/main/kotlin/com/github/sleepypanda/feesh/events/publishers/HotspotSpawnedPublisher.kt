package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ArmorStandCustomNameChangedEvent
import com.github.sleepypanda.feesh.events.models.HotspotSpawnedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.world.entity.decoration.ArmorStand

object HotspotSpawnedPublisher {
    private val pendingHotspotIds = mutableSetOf<Int>()

    fun init() {
        EventBus.subscribe(ArmorStandCustomNameChangedEvent::class, ::onArmorStandCustomNameChanged)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        pendingHotspotIds.clear()
    }

    private fun onArmorStandCustomNameChanged(event: ArmorStandCustomNameChangedEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld()) return
        if (!event.isFirstLoaded) return

        CommonUtils.runWithCatching("Failed to publish hotspot spawned") {
            if (event.customName.unformatted == "HOTSPOT") {
                pendingHotspotIds.add(event.entityId)
                return@runWithCatching
            }

            val hotspotId = event.entityId - 1
            if (hotspotId !in pendingHotspotIds) return@runWithCatching

            val hotspotStand = EntityUtils.getMcEntityById(hotspotId) as? ArmorStand ?: return@runWithCatching
            val perkStand = EntityUtils.getMcEntityById(event.entityId) as? ArmorStand ?: return@runWithCatching
            if (!isPerkForHotspot(perkStand, hotspotStand)) return@runWithCatching

            val perk = perkStand.customName.getFormattedString()
            if (perk.isEmpty()) return@runWithCatching

            pendingHotspotIds.remove(hotspotId)
            EventBus.publish(
                HotspotSpawnedEvent(
                    position = HotspotSpawnedEvent.Position(
                        x = hotspotStand.x,
                        y = hotspotStand.y,
                        z = hotspotStand.z,
                    ),
                    perk = perk,
                    hotspotArmorStandId = hotspotId,
                )
            )
        }
    }

    private fun isPerkForHotspot(perkCandidate: ArmorStand, hotspotStand: ArmorStand): Boolean {
        return perkCandidate.x == hotspotStand.x &&
                perkCandidate.y < hotspotStand.y &&
                hotspotStand.y - perkCandidate.y <= 1.0 &&
                perkCandidate.z == hotspotStand.z &&
                perkCandidate.xRot == hotspotStand.xRot
    }
}
