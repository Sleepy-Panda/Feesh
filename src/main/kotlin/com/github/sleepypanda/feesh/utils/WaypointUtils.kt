package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.gizmos.CuboidGizmo
import net.minecraft.gizmos.GizmoStyle
import net.minecraft.gizmos.SimpleGizmoCollector
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.LightCoordsUtil
import net.minecraft.util.StringDecomposer
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object WaypointUtils {
    enum class RenderMode {
        BOX,
        BOX_AND_TEXT,
        TEXT,
    }

    data class Waypoint(
        val id: Long,
        val x: Double,
        val y: Double,
        val z: Double,
        val colorArgb: Int,
        val expiresAtMillis: Long,
        val size: Double,
        val throughWalls: Boolean,
        val text: String?,
        val renderMode: RenderMode,
        val removeDistance: Double,
    )

    private val waypoints = mutableListOf<Waypoint>()
    private var nextWaypointId = 1L
    private var tickCounter = 0

    private const val TICKS_PER_CHECK = 10
    private const val DEFAULT_REMOVE_DISTANCE = 5.0
    private const val TEXT_SCALE_PER_BLOCK = 0.008f
    private const val TEXT_MIN_SCALE = 0.012f
    private const val TEXT_MAX_SCALE = 1.5f
    private const val TEXT_COLOR = -1

    fun init() {
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
    }
  
    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        waypoints.clear()
        tickCounter = 0
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (waypoints.isEmpty()) return
        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        CommonUtils.runWithCatching("Failed to cleanup waypoints") {
            removeCloseWaypoints()
            removeExpiredWaypoints()    
        }
    }

    /**
     * Adds a waypoint at the given world position.
     * @param boxColorArgb 0xAARRGGBB box color. Used when [renderMode] includes a box.
     * @param durationMilliseconds How long the waypoint should be visible (in milliseconds). Long.MAX_VALUE for no expiry.
     * @param text Formatted text drawn when [renderMode] includes text.
     * @param throughWalls If true, the box and text are drawn through blocks.
     * @param renderMode [RenderMode.BOX], [RenderMode.BOX_AND_TEXT], or [RenderMode.TEXT].
     * @param removeDistance The waypoint is removed when the player gets close. Use 0 to disable that.
     * @return ID which can be used to remove the waypoint.
     */
    fun add(
        x: Double,
        y: Double,
        z: Double,
        boxColorArgb: Long = 0x66FC54FC,
        durationMilliseconds: Long = 30_000L,
        size: Double = 1.0,
        throughWalls: Boolean = false,
        text: String? = null,
        renderMode: RenderMode,
        removeDistance: Double = DEFAULT_REMOVE_DISTANCE,
    ): Long {
        val id = nextWaypointId++
        val expiresAt = if (durationMilliseconds == Long.MAX_VALUE) Long.MAX_VALUE else System.currentTimeMillis() + durationMilliseconds
        waypoints.add(
            Waypoint(
                id = id,
                x = x,
                y = y,
                z = z,
                colorArgb = boxColorArgb.toInt(),
                expiresAtMillis = expiresAt,
                size = size,
                throughWalls = throughWalls,
                text = text?.takeIf { it.isNotBlank() },
                renderMode = renderMode,
                removeDistance = removeDistance,
            )
        )
        return id
    }

    fun remove(id: Long) {
        waypoints.removeAll { it.id == id }
    }

    fun render(collector: SubmitNodeCollector, gizmos: SimpleGizmoCollector, camera: CameraRenderState) {
        if (waypoints.isEmpty()) return
        CommonUtils.runWithCatching("Failed to render waypoints") {
            for (waypoint in waypoints) {
                if (waypoint.renderMode != RenderMode.TEXT) {
                    renderBox(gizmos, waypoint)
                }
                if (waypoint.renderMode == RenderMode.BOX) continue
                val text = waypoint.text ?: continue
                val anchorY = if (waypoint.renderMode == RenderMode.TEXT) waypoint.y else waypoint.y + waypoint.size
                val anchor = Vec3(waypoint.x, anchorY, waypoint.z)
                renderScaledText(collector, camera, anchor, text, waypoint.throughWalls)
            }
        }
    }

    private fun renderBox(gizmos: SimpleGizmoCollector, waypoint: Waypoint) {
        val half = waypoint.size / 2.0
        val box = AABB(
            waypoint.x - half,
            waypoint.y,
            waypoint.z - half,
            waypoint.x + half,
            waypoint.y + waypoint.size,
            waypoint.z + half,
        )
        val properties = gizmos.add(CuboidGizmo(box, GizmoStyle.fill(waypoint.colorArgb), false))
        if (waypoint.throughWalls) properties.setAlwaysOnTop()
    }

    private fun renderScaledText(
        collector: SubmitNodeCollector,
        camera: CameraRenderState,
        pos: Vec3,
        text: String,
        throughWalls: Boolean,
    ) {
        val distance = camera.pos.distanceTo(pos).toFloat()
        val scale = (distance * TEXT_SCALE_PER_BLOCK).coerceIn(TEXT_MIN_SCALE, TEXT_MAX_SCALE)
        drawText(collector, camera, pos.add(0.0, scale * 9.0, 0.0), text, scale, throughWalls)
    }

    private fun drawText(
        collector: SubmitNodeCollector,
        camera: CameraRenderState,
        pos: Vec3,
        text: String,
        scale: Float,
        throughWalls: Boolean,
        shadow: Boolean = true,
    ) {
        val cameraPos = camera.pos
        val matrices = PoseStack()
        matrices.translate((pos.x - cameraPos.x).toFloat(), (pos.y - cameraPos.y).toFloat(), (pos.z - cameraPos.z).toFloat())
        matrices.rotateAround(camera.orientation, 0f, 0f, 0f)
        matrices.scale(scale, -scale, scale)
        val mode = if (throughWalls) Font.DisplayMode.SEE_THROUGH else Font.DisplayMode.NORMAL
        val formatted = FormattedCharSequence { sink -> StringDecomposer.iterateFormatted(text, Style.EMPTY, sink) }
        collector.submitText(
            matrices,
            -FeeshMod.mc.font.width(text) / 2f,
            1f,
            formatted,
            shadow,
            mode,
            LightCoordsUtil.FULL_BRIGHT,
            TEXT_COLOR,
            0,
            0,
        )
    }

    private fun removeCloseWaypoints() {
        val player = FeeshMod.mc.player ?: return
        waypoints.removeAll {
            it.removeDistance > 0.0 &&
                EntityUtils.getDistanceSqr(player.x, player.y, player.z, it.x, it.y, it.z) <= it.removeDistance * it.removeDistance
        }
    }
    
    private fun removeExpiredWaypoints() {
        val now = System.currentTimeMillis()
        waypoints.removeAll { it.expiresAtMillis <= now }
    }
}
