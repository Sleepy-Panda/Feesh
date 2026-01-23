package com.github.sleepypanda.feesh.utils.gui

import com.github.sleepypanda.feesh.events.GameRenderEvent
import com.github.sleepypanda.feesh.events.ScreenAfterBackgroundRenderEvent
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.Alignment
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import net.minecraft.text.Text
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import java.awt.Color

/**
 * A class that represents a GUI for the Feesh mod. GUI is shown when in Skyblock and the condition is met.
 * @property key The unique key for this GUI, used for saving/loading configuration.
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

    private var coordsDataKey: String = ""
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
        EventBus.subscribe(ScreenAfterBackgroundRenderEvent::class, ::postDrawAfterBackground)
    }
    
    fun getCoordsDataKey(): String = coordsDataKey
    fun getX(): Int = x
    fun getY(): Int = y
    fun getScale(): Float = scale
    fun getAlignment(): Alignment = alignment
    fun getLines(): List<String> = lines
    fun getSampleLines(): List<String> = sampleLines
    fun getSettingsKey(): (() -> Boolean)? = settingsKey
    fun getCondition(): () -> Boolean = condition
    fun getIsClickable(): Boolean = isClickable

    fun setCoordsDataKey(coordsDataKey: String): FeeshGui {
        this.coordsDataKey = coordsDataKey
        val savedData = PersistentDataManager.getOverlayCoordsData(coordsDataKey)
        this.x = savedData.x
        this.y = savedData.y
        this.scale = savedData.scale
        this.alignment = savedData.alignment
        return this
    }

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
    
    /**
     * Recalculates x coordinate when alignment changes to keep overlay at the same visual position.
     * @param textRenderer TextRenderer to calculate text widths
     * @param oldAlignment Previous alignment
     * @param newAlignment New alignment
     */
    fun recalculateXForAlignment(textRenderer: TextRenderer, oldAlignment: Alignment, newAlignment: Alignment) {
        if (oldAlignment == newAlignment) return
        
        // Use sampleLines if available, otherwise use lines
        val linesToUse = if (sampleLines.isNotEmpty()) sampleLines else lines
        if (linesToUse.isEmpty()) return
        
        val maxWidth = linesToUse.maxOfOrNull { textRenderer.getWidth(Text.literal(it)) } ?: 0
        
        // Convert current x from old alignment reference point to actual left edge
        val leftEdge = when (oldAlignment) {
            Alignment.LEFT -> x
            Alignment.CENTER -> x - maxWidth / 2
            Alignment.RIGHT -> x - maxWidth
        }
        
        // Convert left edge to new alignment reference point
        val newX = when (newAlignment) {
            Alignment.LEFT -> leftEdge
            Alignment.CENTER -> leftEdge + maxWidth / 2
            Alignment.RIGHT -> leftEdge + maxWidth
        }
        
        this.x = newX
    }
    
    /**
     * Gets the maximum width of the overlay using sampleLines if available, otherwise lines.
     */
    private fun getMaxWidth(textRenderer: TextRenderer): Int {
        val linesToUse = if (sampleLines.isNotEmpty()) sampleLines else lines
        return linesToUse.maxOfOrNull { textRenderer.getWidth(Text.literal(it)) } ?: 0
    }
    
    /**
     * Converts x coordinate (which represents different points based on alignment) to actual left edge.
     */
    private fun getLeftEdge(textRenderer: TextRenderer): Int {
        val maxWidth = getMaxWidth(textRenderer)
        return when (alignment) {
            Alignment.LEFT -> x
            Alignment.CENTER -> x - maxWidth / 2
            Alignment.RIGHT -> x - maxWidth
        }
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

        val scaledY = (y / scale).toInt()
        
        // Use sampleLines for maxWidth calculation to match drawSample behavior
        val maxWidth = getMaxWidth(textRenderer)
        
        // Convert x (which represents different points based on alignment) to left edge
        val leftEdge = getLeftEdge(textRenderer)
        val scaledLeftEdge = (leftEdge / scale).toInt()

        var currentY = scaledY

        for (line in lines) {
            val text = Text.literal(line)
            val textWidth = textRenderer.getWidth(text)
            
            // Text is aligned within the overlay block based on alignment
            val actualX = when (alignment) {
                Alignment.LEFT -> scaledLeftEdge
                Alignment.RIGHT -> scaledLeftEdge + maxWidth - textWidth
                Alignment.CENTER -> scaledLeftEdge + (maxWidth - textWidth) / 2
            }

            drawContext.drawText(textRenderer, text, actualX, currentY, color, true)
            currentY += textRenderer.fontHeight + 2
        }

        drawContext.matrices.popMatrix()
    }

    fun postDrawAfterBackground(event: ScreenAfterBackgroundRenderEvent) { // Draw in front of background but under Inventory GUI
        if (!isClickable) return
        if (event.screen !is InventoryScreen) return

        draw(event.drawContext, event.textRenderer, event.mcClient)
    }
    
    fun drawSample(drawContext: DrawContext, textRenderer: TextRenderer, mcClient: MinecraftClient) {
        if (sampleLines.isEmpty()) return
        if (!WorldUtils.isInSkyblock()) return
        if (mcClient.currentScreen !is MoveGuisScreen) return

        drawContext.matrices.pushMatrix()
        drawContext.matrices.scale(scale, scale)

        val scaledY = (y / scale).toInt()
        
        val maxWidth = getMaxWidth(textRenderer)
        val height = (sampleLines.size * (textRenderer.fontHeight + 2))
        
        // Convert x (which represents different points based on alignment) to left edge
        val leftEdge = getLeftEdge(textRenderer)
        val scaledLeftEdge = (leftEdge / scale).toInt()
        
        // Background is always drawn from left edge
        val backgroundX = scaledLeftEdge
        drawContext.fill(backgroundX - 2, scaledY - 2, backgroundX + maxWidth.toInt() + 2, scaledY + height.toInt() + 2, Color(0, 0, 0, 80).rgb)

        var currentY = scaledY
        for (line in sampleLines) {
            val text = Text.literal(line)
            val textWidth = textRenderer.getWidth(text)
            
            // Text is aligned within the overlay block based on alignment
            val actualX = when (alignment) {
                Alignment.LEFT -> scaledLeftEdge
                Alignment.RIGHT -> scaledLeftEdge + maxWidth - textWidth
                Alignment.CENTER -> scaledLeftEdge + (maxWidth - textWidth) / 2
            }

            drawContext.drawText(textRenderer, text, actualX, currentY, color, true)
            currentY += textRenderer.fontHeight + 2
        }

        drawContext.matrices.popMatrix()
    }

    fun isInSample(textRenderer: TextRenderer, client: MinecraftClient, mouseX: Double, mouseY: Double): Boolean {
        if (sampleLines.isEmpty()) return false

        val maxWidth = getMaxWidth(textRenderer) * scale
        val height = (sampleLines.size * (textRenderer.fontHeight + 2)) * scale
        
        // Convert x to left edge for hitbox calculation
        val leftEdge = getLeftEdge(textRenderer).toFloat()
            
        if (mouseX >= leftEdge - 2 &&
            mouseX <= leftEdge + maxWidth + 2 &&
            mouseY >= y - 2 &&
            mouseY <= y + height + 2) {        
            return true
        }
        return false
    }
}
