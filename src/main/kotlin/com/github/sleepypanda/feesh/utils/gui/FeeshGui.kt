package com.github.sleepypanda.feesh.utils.gui

import com.github.sleepypanda.feesh.events.models.GameRenderEvent
import com.github.sleepypanda.feesh.events.models.ScreenAfterBackgroundRenderEvent
import com.github.sleepypanda.feesh.events.models.AfterMouseClickEvent
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.utils.GuiUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.Alignment
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import net.minecraft.text.Text
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.gui.Click
import java.awt.Color

/**
 * A button within an overlay line. Contains the display text and click callback.
 * Buttons are rendered on top of all other content in the overlay.
 * @param lineIndex 0-based index of the line in the overlay. Used for hit detection.
 * @param text The text with formatting to display on the button.
 * @param onClick The callback to execute when the button is clicked.
 */
data class GuiButton(
    val lineIndex: Int,
    val text: String,
    val onClick: () -> Unit
)

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
    private var buttons: List<GuiButton> = emptyList()

    constructor() {
        registeredGuis.add(this)
        EventBus.subscribe(GameRenderEvent::class, { event -> onDraw(event.drawContext, event.textRenderer, event.mcClient) })
        EventBus.subscribe(ScreenAfterBackgroundRenderEvent::class, ::onPostDrawAfterBackground)
        EventBus.subscribe(AfterMouseClickEvent::class, ::onMouseClick)
    }
    
    private fun onMouseClick(event: AfterMouseClickEvent) {
        if (buttons.isEmpty()) return
        if (!isClickable) return
        if (!WorldUtils.isInSkyblock()) return
        if (!GuiUtils.isInInventoryOrChat()) return
        if (event.button != 0) return
        if (event.screen !is InventoryScreen && event.screen !is ChatScreen) return

        val textRenderer = event.screen.textRenderer ?: return
        val clickedButton = getClickedButton(textRenderer, event.mouseX, event.mouseY)
        if (clickedButton != null) {
            clickedButton.onClick()
        }
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
        
        val linesToUse = when {
            sampleLines.isNotEmpty() -> sampleLines
            else -> emptyList()
        }
        if (linesToUse.isEmpty()) return
        
        val maxWidth = linesToUse.maxOfOrNull { textRenderer.getWidth(Text.literal(it)) } ?: 0
        val renderedWidth = (maxWidth * scale).toInt()
        
        // Convert current x from old alignment reference point to actual left edge (screen space)
        val leftEdge = when (oldAlignment) {
            Alignment.LEFT -> x
            Alignment.CENTER -> x - renderedWidth / 2
            Alignment.RIGHT -> x - renderedWidth
        }
        
        // Convert left edge to new alignment reference point
        val newX = when (newAlignment) {
            Alignment.LEFT -> leftEdge
            Alignment.CENTER -> leftEdge + renderedWidth / 2
            Alignment.RIGHT -> leftEdge + renderedWidth
        }
        
        this.x = newX
    }
    
    /**
     * Gets all display lines in order: buttons first (at their lineIndex), then lines.
     */
    private fun getAllDisplayLines(): List<String> {
        val buttonLines = buttons.sortedBy { it.lineIndex }.map { it.text }
        return buttonLines + lines
    }

    /**
     * Gets display lines for rendering. Buttons are included only when in inventory or chat.
     */
    private fun getDisplayLinesForRender(): List<String> {
        return if (GuiUtils.isInInventoryOrChat()) getAllDisplayLines() else lines
    }

    /**
     * Gets the maximum width of the overlay for the given display lines.
     */
    private fun getMaxWidth(textRenderer: TextRenderer, displayLines: List<String>): Int {
        return displayLines.maxOfOrNull { textRenderer.getWidth(Text.literal(it)) } ?: 0
    }
    
    /**
     * Gets the maximum width of the sample overlay.
     */
    private fun getMaxSampleWidth(textRenderer: TextRenderer): Int {
        val linesToUse = when {
            sampleLines.isNotEmpty() -> sampleLines
            else -> emptyList()
        }
        return linesToUse.maxOfOrNull { textRenderer.getWidth(Text.literal(it)) } ?: 0
    }

    /**
     * Converts x coordinate (which represents different points based on alignment) to actual left edge.
     * Uses scaled width for CENTER/RIGHT so that the reference point stays correct after matrices.scale(scale).
     */
    private fun getLeftEdge(textRenderer: TextRenderer, displayLines: List<String>): Int {
        val maxWidth = getMaxWidth(textRenderer, displayLines)
        val renderedWidth = (maxWidth * scale).toInt()
        return when (alignment) {
            Alignment.LEFT -> x
            Alignment.CENTER -> x - renderedWidth / 2
            Alignment.RIGHT -> x - renderedWidth
        }
    }

    /**
     * Converts x coordinate (which represents different points based on alignment) to actual left edge.
     * Uses scaled width for CENTER/RIGHT so that the reference point stays correct after matrices.scale(scale).
     */
    private fun getLeftSampleEdge(textRenderer: TextRenderer): Int {
        val maxWidth = getMaxSampleWidth(textRenderer)
        val renderedWidth = (maxWidth * scale).toInt()
        return when (alignment) {
            Alignment.LEFT -> x
            Alignment.CENTER -> x - renderedWidth / 2
            Alignment.RIGHT -> x - renderedWidth
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

    /**
     * Sets the list of buttons. Each button has lineIndex, text, and onClick callback.
     */
    fun setButtons(buttons: List<GuiButton>): FeeshGui {
        this.buttons = buttons
        return this
    }

    fun getButtons(): List<GuiButton> = buttons

    fun clearLines(): FeeshGui {
        this.lines = emptyList()
        this.buttons = emptyList()
        return this
    }

    fun setSampleLines(sampleLines: List<String>): FeeshGui {
        this.sampleLines = sampleLines
        return this
    }

    fun onDraw(drawContext: DrawContext, textRenderer: TextRenderer, mcClient: MinecraftClient) {
        if (mcClient.currentScreen is InventoryScreen && isClickable) return
        draw(drawContext, textRenderer, mcClient)
    }

    fun draw(drawContext: DrawContext, textRenderer: TextRenderer, mcClient: MinecraftClient) {
        if (lines.isEmpty()) return
        if (!WorldUtils.isInSkyblock()) return
        if (settingsKey != null && !settingsKey!!()) return
        if (!condition()) return
        if (mcClient.currentScreen is MoveGuisScreen) return

        drawContext.matrices.pushMatrix()
        drawContext.matrices.scale(scale, scale)

        val allLines = getDisplayLinesForRender()
        val scaledY = (y / scale).toInt()
        val maxWidth = getMaxWidth(textRenderer, allLines)
        val leftEdge = getLeftEdge(textRenderer, allLines)
        val scaledLeftEdge = (leftEdge / scale).toInt()
        var currentY = scaledY

        for (line in allLines) {
            val text = Text.literal(line)
            val textWidth = textRenderer.getWidth(text)

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

    fun onPostDrawAfterBackground(event: ScreenAfterBackgroundRenderEvent) { // Draw in front of background but under Inventory GUI
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
        
        val maxWidth = getMaxSampleWidth(textRenderer)
        val height = (sampleLines.size * (textRenderer.fontHeight + 2))
        
        // Convert x (which represents different points based on alignment) to left edge
        val leftEdge = getLeftSampleEdge(textRenderer)
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

        val alignmentLabel = when (alignment) {
            Alignment.LEFT -> "[L]"
            Alignment.RIGHT -> "[R]"
            Alignment.CENTER -> "[C]"
        }
        val labelText = Text.literal("${GRAY}$alignmentLabel")
        val labelX = leftEdge - 2
        val labelY = y - 2 - textRenderer.fontHeight
        drawContext.drawText(textRenderer, labelText, labelX, labelY, color, true)
    }

    fun isInSample(textRenderer: TextRenderer, @Suppress("UNUSED_PARAMETER") client: MinecraftClient, mouseX: Double, mouseY: Double): Boolean {
        if (sampleLines.isEmpty()) return false

        val maxWidth = getMaxSampleWidth(textRenderer) * scale
        val height = (sampleLines.size * (textRenderer.fontHeight + 2)) * scale
        
        // Convert x to left edge for hitbox calculation
        val leftEdge = getLeftSampleEdge(textRenderer).toFloat()
            
        if (mouseX >= leftEdge - 2 &&
            mouseX <= leftEdge + maxWidth + 2 &&
            mouseY >= y - 2 &&
            mouseY <= y + height + 2) {        
            return true
        }
        return false
    }

    /**
     * Returns the button if (mouseX, mouseY) hits a button line, null otherwise.
     * Uses exclusive line boundaries so a click between two lines hits neither.
     */
    private fun getClickedButton(textRenderer: TextRenderer, mouseX: Double, mouseY: Double): GuiButton? {
        if (lines.isEmpty() || buttons.isEmpty()) return null

        val displayLines = getAllDisplayLines()
        val maxWidth = getMaxWidth(textRenderer, displayLines) * scale
        val leftEdge = getLeftEdge(textRenderer, displayLines).toFloat()

        if (mouseX < leftEdge - 2 || mouseX > leftEdge + maxWidth + 2) return null

        val fontHeight = textRenderer.fontHeight
        val lineHeightPx = (fontHeight + 2) * scale

        val totalLines = buttons.sortedBy { it.lineIndex }.size + lines.size
        for (button in buttons) {
            val i = button.lineIndex
            if (i < 0 || i >= totalLines) continue

            val lineTop = y + i * lineHeightPx
            val lineBottom = y + (i + 1) * lineHeightPx - 2 // -2 to make sure line has less space to be not clickable under text in free space

            if (mouseY >= lineTop && mouseY < lineBottom) {
                return button
            }
        }
        return null
    }
}
