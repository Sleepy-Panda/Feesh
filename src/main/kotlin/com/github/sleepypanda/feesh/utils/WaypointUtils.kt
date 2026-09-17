package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.gizmos.GizmoStyle
import net.minecraft.gizmos.Gizmos
import net.minecraft.util.ARGB
import net.minecraft.world.phys.AABB

/**
 * World-space waypoints drawn as filled boxes.
 * Inspired by [SBO RenderUtils3D](https://github.com/SkyblockOverhaul/SBO/blob/main/src/main/kotlin/net/sbo/mod/utils/render/RenderUtils3D.kt).
 */
object WaypointUtils {
    data class Waypoint(
        val x: Double,
        val y: Double,
        val z: Double,
        val red: Float,
        val green: Float,
        val blue: Float,
        val alpha: Float,
        val expiresAtMillis: Long,
        val size: Double,
        val throughWalls: Boolean,
    )

    private val waypoints = mutableListOf<Waypoint>()
    private var tickCounter = 0

    private const val TICKS_PER_PROXIMITY_CHECK = 10
    private const val REMOVE_DISTANCE = 5.0

    fun init() {
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        LevelRenderEvents.COLLECT_SUBMITS.register(::onCollectSubmits)
    }

    /**
     * Adds a filled-box waypoint at the given world position.
     * @param colorRgb 0xRRGGBB box color.
     * @param durationMillis Lifetime from now. Use [Long.MAX_VALUE] for no expiry.
     * @param throughWalls If true, the box is drawn through terrain.
     */
    fun add(
        x: Double,
        y: Double,
        z: Double,
        colorRgb: Int = 0xFC54FC,
        alpha: Float = 0.4f,
        durationMillis: Long = 30_000L,
        size: Double = 1.0,
        throughWalls: Boolean = false,
    ) {
        val red = ((colorRgb shr 16) and 0xFF) / 255f
        val green = ((colorRgb shr 8) and 0xFF) / 255f
        val blue = (colorRgb and 0xFF) / 255f
        val expiresAt = if (durationMillis == Long.MAX_VALUE) Long.MAX_VALUE else System.currentTimeMillis() + durationMillis
        waypoints.add(
            Waypoint(
                x = x,
                y = y,
                z = z,
                red = red,
                green = green,
                blue = blue,
                alpha = alpha,
                expiresAtMillis = expiresAt,
                size = size,
                throughWalls = throughWalls,
            )
        )
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        waypoints.clear()
        tickCounter = 0
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (waypoints.isEmpty()) return

        tickCounter++
        if (tickCounter < TICKS_PER_PROXIMITY_CHECK) return
        tickCounter = 0

        val player = FeeshMod.mc.player ?: return
        waypoints.removeAll {
            EntityUtils.getDistance(player.x, player.y, player.z, it.x, it.y, it.z) <= REMOVE_DISTANCE
        }
    }

    private fun onCollectSubmits(@Suppress("UNUSED_PARAMETER") context: LevelRenderContext) {
        val now = System.currentTimeMillis()
        waypoints.removeAll { it.expiresAtMillis <= now }
        if (waypoints.isEmpty()) return

        CommonUtils.runWithCatching("Failed to render waypoints") {
            for (waypoint in waypoints) {
                val half = waypoint.size / 2.0
                val box = AABB(
                    waypoint.x - half,
                    waypoint.y,
                    waypoint.z - half,
                    waypoint.x + half,
                    waypoint.y + waypoint.size,
                    waypoint.z + half,
                )
                val color = ARGB.colorFromFloat(waypoint.alpha, waypoint.red, waypoint.green, waypoint.blue)
                val gizmo = Gizmos.cuboid(box, GizmoStyle.fill(color))
                if (waypoint.throughWalls) {
                    gizmo.setAlwaysOnTop()
                }
            }
        }
    }
}
