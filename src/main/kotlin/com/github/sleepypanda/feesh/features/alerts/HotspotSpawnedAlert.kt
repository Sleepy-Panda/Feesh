package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ArmorStandDespawnedEvent
import com.github.sleepypanda.feesh.events.models.HotspotNametagLoadedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.AlertUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.WaypointUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils

// Hotspot gone overlaps with hotspot found
// What if perk is loaded after hotspot armor stand?
// No box, half missing nametags in 26.2
object HotspotSpawnedAlert {
    private val handledHotspotIds = mutableSetOf<Int>() // Hotspot armor stand ID
    private val renderedWaypointIds = mutableMapOf<Int, Long>() // Hotspot armor stand ID -> waypoint ID

    fun init() {
        EventBus.subscribe(HotspotNametagLoadedEvent::class, ::onHotspotNametagLoaded)
        EventBus.subscribe(ArmorStandDespawnedEvent::class, ::onArmorStandDespawned)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onHotspotNametagLoaded(event: HotspotNametagLoadedEvent) {
        if (!Alerts.alertOnHotspotSeen || !WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return
        if (handledHotspotIds.contains(event.hotspotArmorStandId)) return
        handledHotspotIds.add(event.hotspotArmorStandId)

        // Maybe add a cooldown to avoid spamming if same hotspot gets loaded/unloaded? Based on perk+coords maybe

        CommonUtils.runWithCatching("Failed to announce Hotspot spawned") {
            AlertUtils.alert(
                title = AlertUtils.TitleConfig("New ${LIGHT_PURPLE}Hotspot", "${event.perkFormatted}"),
                sound = AlertUtils.SoundConfig.McSound(),
                delayNext = false,
            )
            ChatUtils.sendLocalChat("${event.perkFormatted} ${RESET}${LIGHT_PURPLE}Hotspot ${WHITE}spawned!", true)
        }

        CommonUtils.runWithCatching("Failed to render Hotspot waypoint") {
            removeRenderedWaypoint(event.hotspotArmorStandId)
            val waypointId = WaypointUtils.add(
                x = event.position.x,
                y = event.position.y - 0.5,
                z = event.position.z,
                colorArgb = 0x6654FCFC,
                durationMilliseconds = 30_000L,
                throughWalls = true,
                text = "${event.perkFormatted} ${RESET}${LIGHT_PURPLE}Hotspot",
                renderModes = setOf(WaypointUtils.RenderMode.BOX, WaypointUtils.RenderMode.BEAM, WaypointUtils.RenderMode.TEXT),
            )

            WaypointUtils.add( // TODO remove debug
                x = event.position.x,
                y = event.position.y + 5,
                z = event.position.z,
                colorArgb = 0x6654FCFC,
                durationMilliseconds = 30_000L,
                throughWalls = false,
                text = "${event.perkFormatted} ${RESET}${LIGHT_PURPLE}Hotspot",
                renderModes = setOf(WaypointUtils.RenderMode.BOX, WaypointUtils.RenderMode.BEAM, WaypointUtils.RenderMode.TEXT),
            )
            renderedWaypointIds[event.hotspotArmorStandId] = waypointId
        }
    }

    private fun onArmorStandDespawned(event: ArmorStandDespawnedEvent) {
        if (!Alerts.alertOnHotspotSeen || !WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return
        if (!handledHotspotIds.contains(event.armorStand.id)) return

        val hotspot = event.armorStand
        handledHotspotIds.remove(hotspot.id)
        val player = FeeshMod.mc.player ?: return
        val distanceSqr = EntityUtils.getDistanceSqr(player.x, player.y, player.z, hotspot.x, hotspot.y, hotspot.z)

        if (distanceSqr > 30.0 * 30.0) {
            return // Do not remove waypoint if hotspot nametag was unloaded because player went too far away
        }

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
