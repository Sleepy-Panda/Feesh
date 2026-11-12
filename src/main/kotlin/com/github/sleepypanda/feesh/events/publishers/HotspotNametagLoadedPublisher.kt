package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ArmorStandCustomNameChangedEvent
import com.github.sleepypanda.feesh.events.models.HotspotNametagLoadedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.HotspotUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.world.entity.decoration.ArmorStand

object HotspotNametagLoadedPublisher {
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

        CommonUtils.runWithCatching("Failed to publish hotspot nametag loaded") {
            if (event.customName.unformatted == "HOTSPOT") {
                pendingHotspotIds.add(event.entityId)
                return@runWithCatching
            }

            val hotspotId = event.entityId - 1
            if (hotspotId !in pendingHotspotIds) return@runWithCatching

            val hotspotStand = EntityUtils.getMcEntityById(hotspotId) as? ArmorStand ?: return@runWithCatching
            val perkStand = EntityUtils.getMcEntityById(event.entityId) as? ArmorStand ?: return@runWithCatching
            if (!HotspotUtils.isPerkForHotspot(perkStand, hotspotStand)) return@runWithCatching

            val perk = perkStand.customName.getFormattedString()
            if (perk.isEmpty()) return@runWithCatching

            pendingHotspotIds.remove(hotspotId)
            EventBus.publish(
                HotspotNametagLoadedEvent(
                    id = HotspotNametagLoadedEvent.getHotspotId(hotspotStand.x, hotspotStand.y, hotspotStand.z, perk.removeFormatting()),
                    position = HotspotNametagLoadedEvent.Position(
                        x = hotspotStand.x,
                        y = hotspotStand.y,
                        z = hotspotStand.z,
                    ),
                    perkFormatted = perk,
                    perkUnformatted = perk.removeFormatting(),
                    hotspotArmorStandId = hotspotId,
                )
            )
        }
    }
}
