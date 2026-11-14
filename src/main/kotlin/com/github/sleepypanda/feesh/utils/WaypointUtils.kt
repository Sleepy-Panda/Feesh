package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.LevelCollectSubmitsEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.Font
//#if MC >= 26.2
//$$ import net.fabricmc.fabric.api.client.rendering.v1.FabricOrderedSubmitNodeCollector
//$$ import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases
//$$ import net.minecraft.client.renderer.feature.TextFeatureRenderer
//$$ import org.joml.Matrix4f
//#endif
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BeaconRenderer
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.gizmos.GizmoStyle
import net.minecraft.gizmos.Gizmos
import net.minecraft.network.chat.Style
import net.minecraft.util.ARGB
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.LightCoordsUtil
import net.minecraft.util.StringDecomposer
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object WaypointUtils {
    enum class RenderMode {
        BOX,
        TEXT,
        BEAM,
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
        val renderModes: Set<RenderMode>,
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
    private const val BEAM_HEIGHT = 320
    private const val BEAM_RADIUS = 0.25f
    private const val BEAM_GLOW_RADIUS = 0.25f

    fun init() {
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(LevelCollectSubmitsEvent::class, ::onLevelCollectSubmits)
    }

    private fun onLevelCollectSubmits(event: LevelCollectSubmitsEvent) {
        val context = event.context
        render(context.submitNodeCollector(), context.levelState().cameraRenderState)
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
     * @param colorArgb 0xAARRGGBB color for the box and the beacon beam. The beam uses the same RGB at full alpha.
     * @param durationMilliseconds How long the waypoint should be visible (in milliseconds). Long.MAX_VALUE for no expiry.
     * @param text Formatted text drawn when [renderModes] includes [RenderMode.TEXT].
     * @param throughWalls If true, the box and text are drawn through blocks. The beam is not.
     * @param renderModes Any of [RenderMode.BOX], [RenderMode.TEXT], and [RenderMode.BEAM].
     * @param removeDistance The waypoint is removed when the player gets close. Use 0 to disable that.
     * @return ID which can be used to remove the waypoint.
     */
    fun add(
        x: Double,
        y: Double,
        z: Double,
        colorArgb: Long = 0x66FC54FC,
        durationMilliseconds: Long = 30_000L,
        size: Double = 1.0,
        throughWalls: Boolean = false,
        text: String? = null,
        renderModes: Set<RenderMode>,
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
                colorArgb = colorArgb.toInt(),
                expiresAtMillis = expiresAt,
                size = size,
                throughWalls = throughWalls,
                text = text?.takeIf { it.isNotBlank() },
                renderModes = renderModes.toSet(),
                removeDistance = removeDistance,
            )
        )
        return id
    }

    fun remove(id: Long) {
        waypoints.removeAll { it.id == id }
    }

    private fun render(collector: SubmitNodeCollector, camera: CameraRenderState) {
        if (waypoints.isEmpty()) return
        CommonUtils.runWithCatching("Failed to render waypoints") {
            for (waypoint in waypoints) {
                val box = getWaypointBox(waypoint)
                if (RenderMode.BOX in waypoint.renderModes) {
                    renderBox(waypoint, box)
                }
                if (RenderMode.BEAM in waypoint.renderModes) {
                    renderBeam(collector, camera, waypoint, box)
                }
                if (RenderMode.TEXT in waypoint.renderModes) {
                    val text = waypoint.text ?: continue
                    val anchorY = if (RenderMode.BOX in waypoint.renderModes) waypoint.y + waypoint.size + 0.5 else waypoint.y
                    val anchor = Vec3(waypoint.x, anchorY, waypoint.z)
                    renderScaledText(collector, camera, anchor, text, waypoint.throughWalls)    
                }
            }
        }
    }

    private fun getWaypointBox(waypoint: Waypoint): AABB {
        val half = waypoint.size / 2.0
        val minY = waypoint.y - 1.0
        return AABB(
            waypoint.x - half,
            minY,
            waypoint.z - half,
            waypoint.x + half,
            minY + waypoint.size,
            waypoint.z + half,
        )
    }

    private fun renderBox(waypoint: Waypoint, box: AABB) {
        val properties = Gizmos.cuboid(box, GizmoStyle.fill(waypoint.colorArgb))
        // Commented out for 26.3 because it breaks with some shaders
        //#if MC <= 26.2
        //$$ if (waypoint.throughWalls) properties.setAlwaysOnTop()
        //#endif
    }

    private fun renderBeam(
        collector: SubmitNodeCollector,
        camera: CameraRenderState,
        waypoint: Waypoint,
        box: AABB,
    ) {
        val level = FeeshMod.mc.level ?: return
        val partialTick = FeeshMod.mc.deltaTracker.getGameTimeDeltaPartialTick(true)
        val animationTime = Math.floorMod(level.gameTime, 40) + partialTick
        val cameraPos = camera.pos
        val center = box.center
        val matrices = PoseStack()
        // Vanilla beam recenters with +(0.5, 0, 0.5), so start half a block back, on the top face.
        matrices.translate(center.x - 0.5 - cameraPos.x, box.maxY - cameraPos.y, center.z - 0.5 - cameraPos.z)
        BeaconRenderer.submitBeaconBeam(
            matrices,
            collector,
            BeaconRenderer.BEAM_LOCATION,
            1f,
            animationTime,
            0,
            BEAM_HEIGHT,
            ARGB.opaque(waypoint.colorArgb),
            BEAM_RADIUS,
            BEAM_GLOW_RADIUS,
        )
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
        val formatted = FormattedCharSequence { sink -> StringDecomposer.iterateFormatted(text, Style.EMPTY, sink) }
        val x = -FeeshMod.mc.font.width(text) / 2f
        //#if MC >= 26.2
        //$$ val mode = if (throughWalls) Font.DisplayMode.SEE_THROUGH else Font.DisplayMode.POLYGON_OFFSET
        //$$ if (throughWalls) {
        //$$     (collector as FabricOrderedSubmitNodeCollector).submitCustom(
        //$$         SubmitRenderPhases.AFTER_TERRAIN,
        //#if MC >= 26.3
        //$$         TextFeatureRenderer.Submit(
        //$$             Matrix4f(matrices.last().pose()),
        //$$             mode,
        //$$             LightCoordsUtil.FULL_BRIGHT,
        //$$             TextFeatureRenderer.Content.Text(x, 1f, formatted, shadow, TEXT_COLOR, 0, 0),
        //$$         ),
        //#else
        //$$         TextFeatureRenderer.Submit(
        //$$             Matrix4f(matrices.last().pose()),
        //$$             x,
        //$$             1f,
        //$$             formatted,
        //$$             shadow,
        //$$             mode,
        //$$             LightCoordsUtil.FULL_BRIGHT,
        //$$             TEXT_COLOR,
        //$$             0,
        //$$             0,
        //$$         ),
        //#endif
        //$$     )
        //$$ } else {
        //$$     collector.submitText(matrices, x, 1f, formatted, shadow, mode, LightCoordsUtil.FULL_BRIGHT, TEXT_COLOR, 0, 0)
        //$$ }
        //#else
        val mode = if (throughWalls) Font.DisplayMode.SEE_THROUGH else Font.DisplayMode.NORMAL
        collector.submitText(
            matrices,
            x,
            1f,
            formatted,
            shadow,
            mode,
            LightCoordsUtil.FULL_BRIGHT,
            TEXT_COLOR,
            0,
            0,
        )
        //#endif
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
