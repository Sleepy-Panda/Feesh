package com.github.sleepypanda.feesh.utils.rendering

import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.client.gui.Font
//#if MC >= 26.1
import net.minecraft.util.Brightness
//#else
import net.minecraft.client.renderer.LightTexture
//#endif
//#if MC >= 26.1
//$$ import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext as WorldRenderContext
//#else
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
//#endif

class TextWaypoint {
    private var x: Int
    private var y: Int
    private var z: Int
    private var durationSeconds: Int
    private var expiresAtMs: Long
    private var color: Int
    private var text: String
    private var seeThroughWalls: Boolean

    constructor(x: Int = 0, y: Int = 0, z: Int = 0, durationSeconds: Int = 30, color: Int = 0xFFFFFF, text: String = "", seeThroughWalls: Boolean = false) {
        this.x = x
        this.y = y
        this.z = z
        this.durationSeconds = durationSeconds
        this.expiresAtMs = System.currentTimeMillis() + durationSeconds * 1000L
        this.color = color
        this.text = text
        this.seeThroughWalls = seeThroughWalls
    }

    fun setX(x: Int): TextWaypoint {
        this.x = x
        return this
    }

    fun setY(y: Int): TextWaypoint {
        this.y = y
        return this
    }

    fun setZ(z: Int): TextWaypoint {
        this.z = z
        return this
    }

    fun setDurationSeconds(durationSeconds: Int): TextWaypoint {
        this.durationSeconds = durationSeconds
        this.expiresAtMs = System.currentTimeMillis() + durationSeconds * 1000L
        return this
    }

    fun setColor(color: Int): TextWaypoint {
        this.color = color
        return this
    }

    fun setText(text: String): TextWaypoint {
        this.text = text
        return this
    }

    fun setSeeThroughWalls(seeThroughWalls: Boolean): TextWaypoint {
        this.seeThroughWalls = seeThroughWalls
        return this
    }

    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expiresAtMs
    }

    fun render(context: WorldRenderContext) {
        val label = displayText()
        if (label.isEmpty()) return

        //#if MC >= 26.1
        //$$ val matrices = context.poseStack() ?: return
        //$$ val consumers = context.bufferSource() ?: return
        //$$ val camera = context.levelState().cameraRenderState
        //#else
        val matrices = context.matrices() ?: return
        val consumers = context.consumers() ?: return
        val camera = context.worldState().cameraRenderState
        //#endif

        val font = FeeshMod.mc.font
        val layer = if (seeThroughWalls) Font.DisplayMode.SEE_THROUGH else Font.DisplayMode.NORMAL
        val textColor = argbColor()
        val halfWidth = font.width(label) / 2f

        matrices.pushPose()
        matrices.translate(
            x + 0.5 - camera.pos.x,
            y + 1.0 - camera.pos.y,
            z + 0.5 - camera.pos.z,
        )
        matrices.mulPose(camera.orientation)
        matrices.scale(-0.025f, -0.025f, 0.025f)

        //#if MC >= 26.1
        //$$ val light = Brightness.FULL_BRIGHT.pack()
        //#else
        val light = LightTexture.FULL_BRIGHT
        //#endif

        font.drawInBatch(
            label,
            -halfWidth,
            0f,
            textColor,
            false,
            matrices.last().pose(),
            consumers,
            layer,
            0,
            light,
        )

        matrices.popPose()
    }

    private fun displayText(): String {
        return text
    }

    private fun argbColor(): Int {
        if ((color ushr 24) != 0) return color
        return (color and 0xFFFFFF) or (0xCC shl 24)
    }
}
