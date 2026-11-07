package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.gizmos.CuboidGizmo
import net.minecraft.gizmos.GizmoStyle
import net.minecraft.gizmos.SimpleGizmoCollector
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.util.LightCoordsUtil
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

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
        val text: String?,
    )

    private val waypoints = mutableListOf<Waypoint>()
    private var tickCounter = 0

    private const val TICKS_PER_PROXIMITY_CHECK = 10
    private const val REMOVE_DISTANCE = 5.0
    private const val LABEL_BASE_SCALE = 0.05f
    private const val LABEL_DISTANCE_SCALING = 0.1f
    private const val LABEL_COLOR = -1

    fun init() {
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
    }

    /**
     * Submits the box and label while level features are still being collected.
     * Far labels are drawn larger and lifted so they stay above the box.
     */
    fun submit(collector: SubmitNodeCollector, gizmos: SimpleGizmoCollector, camera: CameraRenderState) {
        expire()
        if (waypoints.isEmpty()) return
        CommonUtils.runWithCatching("Failed to render waypoints") {
            for (waypoint in waypoints) {
                drawBox(gizmos, waypoint)
                val text = waypoint.text ?: continue
                val anchor = Vec3(waypoint.x, waypoint.y + waypoint.size, waypoint.z)
                drawDistanceScaledText(collector, camera, anchor, legacyText(text), waypoint.throughWalls)
            }
        }
    }

    /**
     * Adds a filled-box waypoint at the given world position.
     * @param colorRgb 0xRRGGBB box color.
     * @param durationMillis Lifetime from now. Use [Long.MAX_VALUE] for no expiry.
     * @param throughWalls If true, the box is drawn through terrain.
     * @param text Optional label drawn above the box. `§` formatting is kept.
     * Far labels are drawn larger and close ones smaller, so the text stays readable.
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
        text: String? = null,
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
                text = text?.takeIf { it.isNotBlank() },
            )
        )
    }

    fun remove(x: Double, y: Double, z: Double) {
        waypoints.removeAll { it.x == x && it.y == y && it.z == z }
    }

    private fun drawBox(gizmos: SimpleGizmoCollector, waypoint: Waypoint) {
        val half = waypoint.size / 2.0
        val box = AABB(
            waypoint.x - half,
            waypoint.y,
            waypoint.z - half,
            waypoint.x + half,
            waypoint.y + waypoint.size,
            waypoint.z + half,
        )
        val properties = gizmos.add(CuboidGizmo(box, GizmoStyle.fill(argb(waypoint)), false))
        if (waypoint.throughWalls) properties.setAlwaysOnTop()
    }

    private fun argb(waypoint: Waypoint): Int {
        val alpha = (waypoint.alpha * 255f).toInt().coerceIn(0, 255)
        val red = (waypoint.red * 255f).toInt().coerceIn(0, 255)
        val green = (waypoint.green * 255f).toInt().coerceIn(0, 255)
        val blue = (waypoint.blue * 255f).toInt().coerceIn(0, 255)
        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }

    private fun expire() {
        val now = System.currentTimeMillis()
        waypoints.removeAll { it.expiresAtMillis <= now }
    }

    private fun drawDistanceScaledText(
        collector: SubmitNodeCollector,
        camera: CameraRenderState,
        pos: Vec3,
        text: Component,
        throughWalls: Boolean,
    ) {
        val distance = camera.pos.distanceTo(pos)
        val scale = LABEL_BASE_SCALE * (1f + distance.toFloat() * LABEL_DISTANCE_SCALING)
        drawText(collector, camera, pos.add(0.0, distance * LABEL_BASE_SCALE, 0.0), text, scale, throughWalls)
    }

    private fun drawText(
        collector: SubmitNodeCollector,
        camera: CameraRenderState,
        pos: Vec3,
        text: Component,
        scale: Float,
        throughWalls: Boolean,
    ) {
        val cameraPos = camera.pos
        val matrices = PoseStack()
        matrices.translate((pos.x - cameraPos.x).toFloat(), (pos.y - cameraPos.y).toFloat(), (pos.z - cameraPos.z).toFloat())
        matrices.rotateAround(camera.orientation, 0f, 0f, 0f)
        matrices.scale(scale, -scale, scale)
        val mode = if (throughWalls) Font.DisplayMode.SEE_THROUGH else Font.DisplayMode.NORMAL
        collector.submitText(
            matrices,
            -FeeshMod.mc.font.width(text) / 2f,
            1f,
            text.visualOrderText,
            true,
            mode,
            LightCoordsUtil.FULL_BRIGHT,
            LABEL_COLOR,
            0,
            0,
        )
    }

    private fun ChatFormatting.isColorCode(): Boolean {
        return this == ChatFormatting.RESET || this.toString().last() in '0'..'9' || this.toString().last() in 'a'..'f'
    }

    private fun legacyText(text: String): Component {
        if (!text.contains('§')) return Component.literal(text)
        val root = Component.empty()
        var style = Style.EMPTY
        val part = StringBuilder()
        fun flush() {
            if (part.isNotEmpty()) {
                root.append(Component.literal(part.toString()).withStyle(style))
                part.clear()
            }
        }
        var index = 0
        while (index < text.length) {
            if (text[index] == '§' && index + 1 < text.length) {
                flush()
                val formatting = ChatFormatting.getByCode(text[index + 1])
                if (formatting != null) {
                    style = if (formatting.isColorCode()) {
                        Style.EMPTY.applyFormat(formatting)
                    } else {
                        style.applyFormat(formatting)
                    }
                }
                index += 2
                continue
            }
            part.append(text[index])
            index++
        }
        flush()
        return root
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
}
