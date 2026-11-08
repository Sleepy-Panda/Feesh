package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ArmorStandDespawnedEvent
import com.github.sleepypanda.feesh.events.models.HotspotSpawnedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WaypointUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils

object HotspotSpawnedAlert {
    private data class RenderedHotspotWaypoint(val armorStandId: Int, val x: Double, val y: Double, val z: Double)

    private val handledHotspotIds = mutableSetOf<String>() // To avoid duplicated alerts when same hotspot armor stand is loaded/unloaded multiple times
    private val renderedHotspotWaypoints = mutableListOf<RenderedHotspotWaypoint>()

    fun init() {
        EventBus.subscribe(HotspotSpawnedEvent::class, ::onHotspotSpawned)
        EventBus.subscribe(ArmorStandDespawnedEvent::class, ::onArmorStandDespawned)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onHotspotSpawned(event: HotspotSpawnedEvent) {
        if (!Alerts.alertOnHotspotSeen || !WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return
        if (!handledHotspotIds.add(event.id)) return

        CommonUtils.runWithCatching("Failed to announce Hotspot spawned") {
            CommonUtils.showTitle("New ${LIGHT_PURPLE}Hotspot", "${event.perkFormatted}")
            ChatUtils.sendLocalChat("${event.perkFormatted} ${RESET}${LIGHT_PURPLE}Hotspot ${WHITE}spawned!", true)
            SoundUtils.playMcSound()
        }

        CommonUtils.runWithCatching("Failed to render Hotspot waypoint") {
            removeRenderedWaypoint(event.hotspotArmorStandId)
            val x = event.position.x
            val y = event.position.y + 0.5
            val z = event.position.z
            WaypointUtils.add(
                x = x,
                y = y,
                z = z,
                boxColorArgb = 0x6654FCFC,
                durationMilliseconds = 30_000L,
                throughWalls = true,
                text = "${event.perkFormatted} ${RESET}${LIGHT_PURPLE}Hotspot",
                renderMode = WaypointUtils.RenderMode.BOX_AND_TEXT,
            )
            renderedHotspotWaypoints.add(RenderedHotspotWaypoint(event.hotspotArmorStandId, x, y, z))
        }
    }

    private fun onArmorStandDespawned(event: ArmorStandDespawnedEvent) {
        removeRenderedWaypoint(event.armorStand.id)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        renderedHotspotWaypoints.clear()
        handledHotspotIds.clear()
    }

    private fun removeRenderedWaypoint(armorStandId: Int) {
        val rendered = renderedHotspotWaypoints.filter { it.armorStandId == armorStandId }
        if (rendered.isEmpty()) return
        renderedHotspotWaypoints.removeAll { it.armorStandId == armorStandId }
        for (hotspot in rendered) {
            WaypointUtils.remove(hotspot.x, hotspot.y, hotspot.z)
        }
    }
}
