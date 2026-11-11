package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.mixin.RenderPipelinesInvoker
import com.github.sleepypanda.feesh.mixin.RenderTypeInvoker
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.Font
//#if MC >= 26.3
//$$ import com.mojang.renderpearl.api.pipeline.PrimitiveTopology
//$$ import com.mojang.renderpearl.api.pipeline.RenderPipeline
//#elseif MC >= 26.2
//$$ import com.mojang.blaze3d.PrimitiveTopology
//$$ import com.mojang.blaze3d.pipeline.RenderPipeline
//#endif
//#if MC >= 26.2
//$$ import com.mojang.blaze3d.vertex.VertexConsumer
//$$ import net.fabricmc.fabric.api.client.rendering.v1.FabricOrderedSubmitNodeCollector
//$$ import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases
//$$ import net.minecraft.client.renderer.feature.TextFeatureRenderer
//$$ import net.minecraft.client.renderer.rendertype.RenderSetup
//$$ import net.minecraft.client.renderer.rendertype.RenderType
//$$ import net.minecraft.resources.Identifier
//$$ import org.joml.Matrix4f
//$$ import java.util.Optional
//#endif
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

    //#if MC >= 26.2
    //$$ private val xrayFill: RenderType by lazy { createFill(throughWalls = true) }
    //$$ private val depthFill: RenderType by lazy { createFill(throughWalls = false) }
    //#endif

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
                    renderBox(collector, gizmos, camera, waypoint)
                }
                if (waypoint.renderMode == RenderMode.BOX) continue
                val text = waypoint.text ?: continue
                val anchorY = if (waypoint.renderMode == RenderMode.TEXT) waypoint.y else waypoint.y + waypoint.size + 0.5
                val anchor = Vec3(waypoint.x, anchorY, waypoint.z)
                renderScaledText(collector, camera, anchor, text, waypoint.throughWalls)
            }
        }
    }

    private fun renderBox(
        collector: SubmitNodeCollector,
        gizmos: SimpleGizmoCollector,
        camera: CameraRenderState,
        waypoint: Waypoint,
    ) {
        val half = waypoint.size / 2.0
        val box = AABB(
            waypoint.x - half,
            waypoint.y - 1.0,
            waypoint.z - half,
            waypoint.x + half,
            waypoint.y - 1.0 + waypoint.size,
            waypoint.z + half,
        )
        //#if MC >= 26.2
        //$$ renderFillBox(collector, camera, box, waypoint.colorArgb, waypoint.throughWalls)
        //#else
        val properties = gizmos.add(CuboidGizmo(box, GizmoStyle.fill(waypoint.colorArgb), false))
        if (waypoint.throughWalls) properties.setAlwaysOnTop()
        //#endif
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

    //#if MC >= 26.2
    //$$ private fun renderFillBox(
    //$$     collector: SubmitNodeCollector,
    //$$     camera: CameraRenderState,
    //$$     box: AABB,
    //$$     color: Int,
    //$$     throughWalls: Boolean,
    //$$ ) {
    //$$     val cameraPos = camera.pos
    //$$     val layer = if (throughWalls) xrayFill else depthFill
    //$$     collector.submitCustomGeometry(PoseStack(), layer) { pose, consumer ->
    //$$         addFilledBoxVertices(
    //$$             pose,
    //$$             consumer,
    //$$             (box.minX - cameraPos.x).toFloat(),
    //$$             (box.minY - cameraPos.y).toFloat(),
    //$$             (box.minZ - cameraPos.z).toFloat(),
    //$$             (box.maxX - cameraPos.x).toFloat(),
    //$$             (box.maxY - cameraPos.y).toFloat(),
    //$$             (box.maxZ - cameraPos.z).toFloat(),
    //$$             color,
    //$$         )
    //$$     }
    //$$ }
    //$$
    //$$ private fun createFill(throughWalls: Boolean): RenderType {
    //$$     val name = if (throughWalls) "waypoint_fill_xray" else "waypoint_fill"
    //$$     val pipeline = RenderPipelinesInvoker.feeshRegister(
    //$$         RenderPipeline.builder(RenderPipelinesInvoker.feeshDebugFilledSnippet())
    //$$             .withLocation(Identifier.fromNamespaceAndPath("feesh", name))
    //$$             .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
    //$$             .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
    //$$             .withCull(false)
    //$$             .apply { if (throughWalls) withDepthStencilState(Optional.empty()) }
    //$$             .build(),
    //$$     )
    //$$     assignIrisPipeline(pipeline)
    //$$     return RenderTypeInvoker.feeshCreate("feesh_$name", RenderSetup.builder(pipeline).createRenderSetup())
    //$$ }
    //$$
    //$$ private fun assignIrisPipeline(pipeline: RenderPipeline) {
    //$$     val api = runCatching {
    //$$         Class.forName("net.irisshaders.iris.api.v0.IrisApi").getMethod("getInstance").invoke(null)
    //$$     }.getOrNull() ?: return
    //$$     val programClass = Class.forName("net.irisshaders.iris.api.v0.IrisProgram")
    //$$     val basic = java.lang.Enum.valueOf(programClass.asSubclass(Enum::class.java), "BASIC")
    //$$     val assign = api.javaClass.methods.firstOrNull { method ->
    //$$         method.name == "assignPipeline" &&
    //$$             method.parameterCount == 2 &&
    //$$             method.parameterTypes[0].isAssignableFrom(pipeline.javaClass)
    //$$     } ?: return
    //$$     assign.invoke(api, pipeline, basic)
    //$$ }
    //$$
    //$$ private fun addFilledBoxVertices(
    //$$     pose: PoseStack.Pose,
    //$$     consumer: VertexConsumer,
    //$$     minX: Float,
    //$$     minY: Float,
    //$$     minZ: Float,
    //$$     maxX: Float,
    //$$     maxY: Float,
    //$$     maxZ: Float,
    //$$     color: Int,
    //$$ ) {
    //$$     fun vertex(x: Float, y: Float, z: Float) {
    //$$         consumer.addVertex(pose, x, y, z).setColor(color)
    //$$     }
    //$$     vertex(minX, minY, minZ)
    //$$     vertex(minX, minY, minZ)
    //$$     vertex(minX, minY, minZ)
    //$$     vertex(minX, minY, maxZ)
    //$$     vertex(minX, maxY, minZ)
    //$$     vertex(minX, maxY, maxZ)
    //$$     vertex(minX, maxY, maxZ)
    //$$     vertex(minX, minY, maxZ)
    //$$     vertex(maxX, maxY, maxZ)
    //$$     vertex(maxX, minY, maxZ)
    //$$     vertex(maxX, minY, maxZ)
    //$$     vertex(maxX, minY, minZ)
    //$$     vertex(maxX, maxY, maxZ)
    //$$     vertex(maxX, maxY, minZ)
    //$$     vertex(maxX, maxY, minZ)
    //$$     vertex(maxX, minY, minZ)
    //$$     vertex(minX, maxY, minZ)
    //$$     vertex(minX, minY, minZ)
    //$$     vertex(minX, minY, minZ)
    //$$     vertex(maxX, minY, minZ)
    //$$     vertex(minX, minY, maxZ)
    //$$     vertex(maxX, minY, maxZ)
    //$$     vertex(maxX, minY, maxZ)
    //$$     vertex(minX, maxY, minZ)
    //$$     vertex(minX, maxY, minZ)
    //$$     vertex(minX, maxY, maxZ)
    //$$     vertex(maxX, maxY, minZ)
    //$$     vertex(maxX, maxY, maxZ)
    //$$     vertex(maxX, maxY, maxZ)
    //$$     vertex(maxX, maxY, maxZ)
    //$$ }
    //#endif

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
