package com.github.sleepypanda.feesh.utils.gui

import com.github.sleepypanda.feesh.events.GameRenderEvent
import com.github.sleepypanda.feesh.events.ScreenPostRenderEvent
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.text.Text
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import java.awt.Color

enum class Alignment {
    LEFT,
    RIGHT,
    CENTER
}

/**
 * A class that represents a GUI for the Feesh mod. GUI is shown when in Skyblock and the condition is met.
 * @property x The x position of the GUI.
 * @property y The y position of the GUI.
 * @property scale The scale of the GUI.
 * @property alignment The alignment of the GUI.
 * @property condition The condition that must be met for the GUI to be displayed.
 * @property settingsKey The settings key that must be enabled for the GUI to be displayed.
 * @property isClickable Whether the GUI is clickable, and will be rendered in front of background when Inventory GUI is opened.
 * @property lines The lines of the GUI.
 * @property sampleLines The sample lines of the GUI.
 */
class FeeshGui {
    companion object {
        private val registeredGuis = mutableListOf<FeeshGui>()
        
        fun getAllRegisteredGuis(): List<FeeshGui> {
            return registeredGuis.toList()
        }
    }

    private var x: Int = 10
    private var y: Int = 10
    private var scale: Float = 1.0f
    private var alignment: Alignment = Alignment.LEFT
    private var condition: () -> Boolean = { true }
    private var settingsKey: (() -> Boolean)? = null
    private var isClickable: Boolean = false
    private var lines: List<String> = emptyList()
    private var sampleLines: List<String> = emptyList()

    private var color: Int = Color(255, 255, 255, 255).rgb

    constructor() {
        registeredGuis.add(this)
        EventBus.subscribe(GameRenderEvent::class, { event -> draw(event.drawContext, event.textRenderer, event.mcClient) })
        EventBus.subscribe(ScreenPostRenderEvent::class, ::postDraw)
    }
    
    fun getX(): Int = x
    fun getY(): Int = y
    fun getScale(): Float = scale
    fun getSampleLines(): List<String> = sampleLines
    fun getLines(): List<String> = lines
    fun getSettingsKey(): (() -> Boolean)? = settingsKey
    fun getCondition(): () -> Boolean = condition
    fun getIsClickable(): Boolean = isClickable

    fun setX(x: Int): FeeshGui {
        this.x = x
        return this
    }

    fun setY(y: Int): FeeshGui {
        this.y = y
        return this
    }

    fun setScale(scale: Float): FeeshGui {
        this.scale = scale
        return this
    }

    fun setAlignment(alignment: Alignment): FeeshGui {
        this.alignment = alignment
        return this
    }

    fun setCondition(condition: () -> Boolean): FeeshGui {
        this.condition = condition
        return this
    }

    fun setSettingsKey(settingsKey: () -> Boolean): FeeshGui {
        this.settingsKey = settingsKey
        return this
    }

    fun setClickable(isClickable: Boolean): FeeshGui {
        this.isClickable = isClickable
        return this
    }

    fun setLines(lines: List<String>): FeeshGui {
        this.lines = lines
        return this
    }

    fun clearLines(): FeeshGui {
        this.lines = emptyList()
        return this
    }

    fun setSampleLines(sampleLines: List<String>): FeeshGui {
        this.sampleLines = sampleLines
        return this
    }

    fun draw(drawContext: DrawContext, textRenderer: TextRenderer, mcClient: MinecraftClient) {
        if (lines.isEmpty()) return
        if (!WorldUtils.isInSkyblock()) return
        if (!condition()) return
        if (settingsKey != null && !settingsKey!!()) return
        if (mcClient.currentScreen is MoveGuisScreen) return

        drawContext.matrices.pushMatrix()
        drawContext.matrices.scale(scale, scale)

        val screenWidth = mcClient.window.scaledWidth
        val scaledScreenWidth = (screenWidth / scale).toInt()

        var currentY = y

        for (line in lines) {
            val text = Text.literal(line)
            val textWidth = textRenderer.getWidth(text)
            
            val actualX = when (alignment) {
                Alignment.LEFT -> x
                Alignment.RIGHT -> scaledScreenWidth - textWidth - x
                Alignment.CENTER -> (scaledScreenWidth - textWidth) / 2 + x
            }

            drawContext.drawText(textRenderer, text, actualX, currentY, color, true)
            currentY += textRenderer.fontHeight + 2
        }

        drawContext.matrices.popMatrix()
    }

    fun postDraw(event: ScreenPostRenderEvent) { // Draw in front of background when Inventory GUI is opened
        if (!isClickable) return
        if (event.screen !is InventoryScreen) return

        draw(event.drawContext, event.textRenderer, event.mcClient)
    }
    
    fun drawSample(drawContext: DrawContext, textRenderer: TextRenderer, mcClient: MinecraftClient, x: Int, y: Int) {
        if (sampleLines.isEmpty()) return
        if (!WorldUtils.isInSkyblock()) return

        drawContext.matrices.pushMatrix()
        drawContext.matrices.scale(scale, scale)

        val screenWidth = mcClient.window.scaledWidth
        val scaledScreenWidth = (screenWidth / scale).toInt()
        val scaledX = (x / scale).toInt()
        val scaledY = (y / scale).toInt()

        var currentY = scaledY

        for (line in sampleLines) {
            val text = Text.literal(line)
            val textWidth = textRenderer.getWidth(text)
            
            val actualX = when (alignment) {
                Alignment.LEFT -> scaledX
                Alignment.RIGHT -> scaledScreenWidth - textWidth - scaledX
                Alignment.CENTER -> (scaledScreenWidth - textWidth) / 2 + scaledX
            }

            drawContext.drawText(textRenderer, text, actualX, currentY, color, true)
            currentY += textRenderer.fontHeight + 2
        }

        drawContext.matrices.popMatrix()
    }
}
