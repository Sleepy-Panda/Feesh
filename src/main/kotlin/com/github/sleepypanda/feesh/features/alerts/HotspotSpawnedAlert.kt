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

// Hotspot gone overlaps with hotspot found
// Remove debug logging of cleanup in HotspotUtils
// What if perk is loaded after hotspot armor stand?
object HotspotSpawnedAlert {
    private val handledHotspotIds = mutableSetOf<String>() // To avoid duplicated alerts when same hotspot armor stand is loaded/unloaded multiple times
    private val renderedWaypointIds = mutableMapOf<Int, Long>() // Hotspot armor stand id -> waypoint id

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
            val waypointId = WaypointUtils.add(
                x = event.position.x,
                y = event.position.y,
                z = event.position.z,
                boxColorArgb = 0x6654FCFC,
                durationMilliseconds = 30_000L,
                throughWalls = true,
                text = "${event.perkFormatted} ${RESET}${LIGHT_PURPLE}Hotspot",
                renderMode = WaypointUtils.RenderMode.BOX_AND_TEXT,
            )
            renderedWaypointIds[event.hotspotArmorStandId] = waypointId
        }
    }

    private fun onArmorStandDespawned(event: ArmorStandDespawnedEvent) {
        if (!Alerts.alertOnHotspotSeen || !WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return
        if (!renderedWaypointIds.containsKey(event.armorStand.id)) return

        val hotspot = event.armorStand
        val player = FeeshMod.mc.player ?: return
        val distanceSqr = EntityUtils.getDistanceSqr(player.x, player.y, player.z, hotspot.x, hotspot.y, hotspot.z)
        if (distanceSqr > 30.0 * 30.0) return // Do not remove waypoint if hotspot nametag was unloaded because player went too far away

        removeRenderedWaypoint(hotspot.id)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        renderedWaypointIds.clear()
        handledHotspotIds.clear()
    }

    private fun removeRenderedWaypoint(armorStandId: Int) {
        val waypointId = renderedWaypointIds.remove(armorStandId) ?: return
        WaypointUtils.remove(waypointId)
    }
}
