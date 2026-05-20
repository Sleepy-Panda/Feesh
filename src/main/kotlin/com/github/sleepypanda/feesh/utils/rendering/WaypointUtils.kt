package com.github.sleepypanda.feesh.utils.rendering

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.WorldUtils
//#if MC >= 26.1
//$$ import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents as WorldRenderEvents
//#else
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
//#endif

object WaypointUtils {
    private val waypoints = mutableListOf<TextWaypoint>()

    fun init() {
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)

        //#if MC >= 26.1
        //$$ WorldRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register { context ->
        //#else
        WorldRenderEvents.BEFORE_TRANSLUCENT.register { context ->
        //#endif
            if (!WorldUtils.isInSkyblock()) return@register
            if (waypoints.isEmpty()) return@register

            for (waypoint in waypoints) {
                waypoint.render(context)
            }
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        clear()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        // TODO once per second?
        // TODO check if player close
        waypoints.removeIf { it.isExpired() }
    }

    fun add(waypoint: TextWaypoint) {
        waypoints.add(waypoint)
    }

    fun clear() {
        waypoints.clear()
    }
}
