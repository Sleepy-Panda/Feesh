package com.github.sleepypanda.feesh.utils.gui

import com.github.sleepypanda.feesh.events.models.GameRenderEvent
import com.github.sleepypanda.feesh.events.models.ScreenAfterBackgroundRenderEvent
import com.github.sleepypanda.feesh.events.models.AfterMouseClickEvent
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.settings.categories.Overlays
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
 * An inline action button for a specific overlay line. Defined by the overlay.
 * @param text The formatted display text (e.g. "[+]", "[-]", "[x]" with color codes).
 * @param onClick The callback to execute when the button is clicked.
 */
data class LineAction(
    val text: String,
    val onClick: () -> Unit
)

/**
 * A class that represents a GUI for the Feesh mod. GUI is shown when in Skyblock and the condition is met.
 * @property coordsDataKey The unique key for this GUI, used for saving/loading coords/scale configuration.
 * @property x The x position of the GUI.
 * @property y The y position of the GUI.
 * @property scale The scale of the GUI.
 * @property alignment The alignment of the GUI.
 * @property condition The condition that must be met for the GUI to be displayed.
 * @property settingsKey The settings key that must be enabled for the GUI to be displayed.
 * @property applyCustomStyleKey The settings key that must be enabled for the GUI to have custom style applied. When false, custom Overlays style (background, border, colors) is not applied to this GUI.
 * @property isClickable Whether the GUI is clickable, and will be rendered in front of background when Inventory GUI is opened.
 * @property lines The lines of the GUI.
 * @property sampleLines The sample lines of the GUI for MoveGuis screen.
 */
class FeeshGui {
    companion object {
        private val registeredGuis = mutableListOf<FeeshGui>()

        fun getAllRegisteredGuis(): List<FeeshGui> {
            return registeredGuis.toList()
        }

        /** Applies saved scale/position/alignment to all registered GUIs, after they are loaded from file. */
        fun applyOverlayCoordsToAllGuis() {
            registeredGuis.forEach { it.applyCoordsFromStorage() }
        }
    }

    private var coordsDataKey: String = ""
    private var x: Int = 10
    private var y: Int = 10
    private var scale: Float = 1.0f
    private var alignment: Alignment = Alignment.LEFT
    private var condition: () -> Boolean = { true }
    private var settingsKey: (() -> Boolean)? = null
    private var applyCustomStyleKey: () -> Boolean = { true }
    private var isClickable: Boolean = false
    private var lines: List<String> = emptyList()
    private var sampleLines: List<String> = emptyList()

    private var color: Int = Color(255, 255, 255, 255).rgb
    // Buttons are shown when in inventory/chat on top of the overlay (e.g. Reset, Pause, switch mode).
    private var buttons: List<GuiButton> = emptyList()
    // Inline actions are shown when hovering over a line (e.g. [+], [-], [x]).
    // Index starts from the first overlay line including buttons.
    private var lineIndexToActions: Map<Int, List<LineAction>> = emptyMap()

    constructor() {
        registeredGuis.add(this)
        EventBus.subscribe(GameRenderEvent::class, { event -> onDraw(event.drawContext, event.textRenderer, event.mcClient) })
        EventBus.subscribe(ScreenAfterBackgroundRenderEvent::class, ::onPostDrawAfterBackground)
        EventBus.subscribe(AfterMouseClickEvent::class, ::onMouseClick)
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
        applyCoordsFromStorage()
        return this
    }

    /** Applies x, y, scale, alignment from overlayCoordsData. No-op if coordsDataKey is empty. */
    fun applyCoordsFromStorage(): FeeshGui {
        if (coordsDataKey.isEmpty()) return this
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
        
    
    fun setCondition(condition: () -> Boolean): FeeshGui {
        this.condition = condition
        return this
    }

    fun setSettingsKey(settingsKey: () -> Boolean): FeeshGui {
        this.settingsKey = settingsKey
        return this
    }

    fun setApplyCustomStyleKey(applyCustomStyleKey: () -> Boolean): FeeshGui {
        this.applyCustomStyleKey = applyCustomStyleKey
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

    /**
     * Sets the mapping of line index to inline actions. Each overlay defines its own actions
     * (e.g. [+], [-], [x]) and callbacks. Buttons are shown when in inventory/chat and hovering the line.
     */
    fun setLineActions(lineIndexToActions: Map<Int, List<LineAction>>): FeeshGui {
        this.lineIndexToActions = lineIndexToActions
        return this
    }

    fun clearLines(): FeeshGui {
        this.lines = emptyList()
        this.buttons = emptyList()
        this.lineIndexToActions = emptyMap()
        return this
    }

    fun setSampleLines(sampleLines: List<String>): FeeshGui {
        this.sampleLines = sampleLines
        return this
    }

    private fun onDraw(drawContext: DrawContext, textRenderer: TextRenderer, mcClient: MinecraftClient) {
        if (mcClient.currentScreen is InventoryScreen && isClickable) return
        draw(drawContext, textRenderer, mcClient, null, null)
    }

    private fun onPostDrawAfterBackground(event: ScreenAfterBackgroundRenderEvent) { // Draw in front of dark background when Inventory is opened
        if (!isClickable) return
        if (event.screen !is InventoryScreen) return

        draw(event.drawContext, event.textRenderer, event.mcClient, event.mouseX, event.mouseY)
    }

    private fun onMouseClick(event: AfterMouseClickEvent) {
        if (lines.isEmpty() && buttons.isEmpty()) return
        if (!isClickable) return
        if (!WorldUtils.isInSkyblock()) return
        if (!GuiUtils.isInInventoryOrChat()) return
        if (event.button != 0) return
        if (event.screen !is InventoryScreen && event.screen !is ChatScreen) return

        val textRenderer = event.screen.textRenderer ?: return

        if (buttons.isNotEmpty()) {
            val clickedButton = getClickedButton(textRenderer, event.mouseX, event.mouseY)
            if (clickedButton != null) {
                clickedButton.onClick()
                return
            }
        }
   
        if (lineIndexToActions.isNotEmpty() && event.screen is InventoryScreen) { // TODO: Inline buttons are not rendered when in chat, due to missing mouseX/mouseY for hover.
            val clickedLineAction = getClickedLineAction(textRenderer, event.mouseX, event.mouseY)
            if (clickedLineAction != null) {
                clickedLineAction.onClick()
                return
            }
        }
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

    private fun draw(drawContext: DrawContext, textRenderer: TextRenderer, mcClient: MinecraftClient, mouseX: Int? = null, mouseY: Int? = null) {
        if (lines.isEmpty()) return
        if (!WorldUtils.isInSkyblock()) return
        if (settingsKey != null && !settingsKey!!()) return
        if (!condition()) return
        if (mcClient.currentScreen is MoveGuisScreen) return

        drawContext.matrices.pushMatrix()
        drawContext.matrices.scale(scale, scale)

        val allLines = getDisplayLinesForRender()
        val scaledY = (y / scale).toInt()
        val fontHeight = textRenderer.fontHeight
        val lineHeightPx = fontHeight + 2

        // Build lines with buttons for all lines that have actions - used for hover bounds so hovering over buttons counts
        val linesWithAllButtons = allLines.mapIndexed { index, line ->
            val actions = lineIndexToActions[index]
            if (actions != null && actions.isNotEmpty()) line + " " + actions.joinToString("") { it.text } else line
        }

        val hoveredLineIndex = if (mouseX != null && mouseY != null && lineIndexToActions.isNotEmpty()) {
            getHoveredLineIndex(textRenderer, linesWithAllButtons, mouseX.toDouble(), mouseY.toDouble())
        } else null

        val hoveredActions = hoveredLineIndex?.let { lineIndexToActions[it] }?.takeIf { it.isNotEmpty() }

        // Use linesWithAllButtons for width when we have actions, so overlay bounds include button area for hover detection
        val linesForWidth = if (lineIndexToActions.isNotEmpty()) linesWithAllButtons else allLines
        val maxWidth = getMaxWidth(textRenderer, linesForWidth)
        val leftEdge = getLeftEdge(textRenderer, linesForWidth)
        val scaledLeftEdge = (leftEdge / scale).toInt()
        val height = (allLines.size * lineHeightPx)
        var currentY = scaledY

        val useCustomStyle = applyCustomStyleKey()
        if (useCustomStyle) {
            drawOverlayBackground(drawContext, scaledLeftEdge, scaledY, maxWidth, height)
            drawOverlayBorder(drawContext, scaledLeftEdge, scaledY, maxWidth, height)
        }

        for ((index, line) in allLines.withIndex()) {
            val actions = if (index == hoveredLineIndex) hoveredActions else null
            if (!actions.isNullOrEmpty()) {
                val buttonsStr = actions.joinToString("") { it.text }
                val lineWidth = textRenderer.getWidth(Text.literal(line))
                val buttonsWidth = textRenderer.getWidth(Text.literal(buttonsStr))
                val spaceWidth = textRenderer.getWidth(Text.literal(" "))
                val fullWidth = buttonsWidth + spaceWidth + lineWidth

                when (alignment) {
                    Alignment.LEFT -> {
                        val buttonsX = scaledLeftEdge
                        val lineX = scaledLeftEdge + buttonsWidth + spaceWidth
                        drawContext.drawText(textRenderer, Text.literal(buttonsStr), buttonsX, currentY, color, true)
                        drawContext.drawText(textRenderer, Text.literal(line), lineX, currentY, color, true)
                    }
                    Alignment.RIGHT -> {
                        val lineX = scaledLeftEdge + maxWidth - fullWidth
                        val buttonsX = scaledLeftEdge + maxWidth - buttonsWidth
                        drawContext.drawText(textRenderer, Text.literal(line), lineX, currentY, color, true)
                        drawContext.drawText(textRenderer, Text.literal(buttonsStr), buttonsX, currentY, color, true)
                    }
                    Alignment.CENTER -> {
                        val contentLeft = scaledLeftEdge + (maxWidth - fullWidth) / 2
                        val buttonsX = contentLeft
                        val lineX = contentLeft + buttonsWidth + spaceWidth
                        drawContext.drawText(textRenderer, Text.literal(buttonsStr), buttonsX, currentY, color, true)
                        drawContext.drawText(textRenderer, Text.literal(line), lineX, currentY, color, true)
                    }
                }
            } else {
                val text = Text.literal(line)
                val textWidth = textRenderer.getWidth(text)
                val actualX = when (alignment) {
                    Alignment.LEFT -> scaledLeftEdge
                    Alignment.RIGHT -> scaledLeftEdge + maxWidth - textWidth
                    Alignment.CENTER -> scaledLeftEdge + (maxWidth - textWidth) / 2
                }
                drawContext.drawText(textRenderer, text, actualX, currentY, color, true)
            }
            currentY += lineHeightPx
        }

        drawContext.matrices.popMatrix()
    }

    private data class OverlayBackgroundCoords(val left: Int, val top: Int, val right: Int, val bottom: Int)

    /**
     * Gets the scaled coordinates of the rectangle around overlay text, with some padding.
     */
    private fun getOverlayBackgroundCoords(scaledLeftEdge: Int, scaledY: Int, maxWidth: Int, height: Int): OverlayBackgroundCoords {
        return OverlayBackgroundCoords(
            scaledLeftEdge - 4,
            scaledY - 4,
            scaledLeftEdge + maxWidth + 4,
            scaledY + height + 2,
        )
    }

    private fun drawOverlayBackground(
        drawContext: DrawContext,
        scaledLeftEdge: Int,
        scaledY: Int,
        maxWidth: Int,
        height: Int,
    ) {
        if (!Overlays.overlaysBackground) return

        val backgroundTopColor = Color(Overlays.overlaysBackgroundColor1, true).rgb
        val backgroundBottomColor = Color(Overlays.overlaysBackgroundColor2, true).rgb

        val backgroundCoords = getOverlayBackgroundCoords(scaledLeftEdge, scaledY, maxWidth, height)
        drawContext.fillGradient(
            backgroundCoords.left,
            backgroundCoords.top,
            backgroundCoords.right,
            backgroundCoords.bottom,
            backgroundTopColor,
            backgroundBottomColor
        )
    }

    private fun drawOverlayBorder(
        drawContext: DrawContext,
        scaledLeftEdge: Int,
        scaledY: Int,
        maxWidth: Int,
        height: Int,
    ) {
        if (!Overlays.overlaysBorder) return

        val borderColor = Color(Overlays.overlaysBorderColor, true).rgb
        val borderWidth = Overlays.overlaysBorderWidth.coerceIn(1..5)

        val backgroundCoords = getOverlayBackgroundCoords(scaledLeftEdge, scaledY, maxWidth, height)
        val left = backgroundCoords.left
        val top = backgroundCoords.top
        val right = backgroundCoords.right
        val bottom = backgroundCoords.bottom

        // Draw border borderWidth pixels outside the overlay, so it does not overlap the background.
        // Also made lines not overlap in the corners.
        drawContext.fill(left - borderWidth, top - borderWidth, right + borderWidth, top, borderColor) // top
        drawContext.fill(left - borderWidth, bottom, right + borderWidth, bottom + borderWidth, borderColor) // bottom
        drawContext.fill(left - borderWidth, top, left, bottom, borderColor) // left
        drawContext.fill(right, top, right + borderWidth, bottom, borderColor) // right
    }

    private fun getHoveredLineIndex(textRenderer: TextRenderer, displayLines: List<String>, mouseX: Double, mouseY: Double): Int? {
        val maxWidth = getMaxWidth(textRenderer, displayLines) * scale
        val leftEdge = getLeftEdge(textRenderer, displayLines).toFloat()
        val fontHeight = textRenderer.fontHeight
        val lineHeightPx = (fontHeight + 2) * scale

        if (mouseX < leftEdge - 2 || mouseX > leftEdge + maxWidth + 2) return null
        if (mouseY < y - 2 || mouseY > y + displayLines.size * lineHeightPx + 2) return null

        val lineIndex = ((mouseY - y) / lineHeightPx).toInt()
        return if (lineIndex in 0 until displayLines.size) lineIndex else null
    }
    
    /**
     * Draws the sample overlay from sampleLines, for move GUIs screen.
     */
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

        val backgroundCoords = getOverlayBackgroundCoords(scaledLeftEdge, scaledY, maxWidth, height)
        drawContext.fill(
            backgroundCoords.left,
            backgroundCoords.top,
            backgroundCoords.right,
            backgroundCoords.bottom,
            Color(0, 0, 0, 80).rgb
        )

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

    /**
     * Checks if the mouse is over the sample overlay.
     */
    fun isInSample(textRenderer: TextRenderer, @Suppress("UNUSED_PARAMETER") client: MinecraftClient, mouseX: Double, mouseY: Double): Boolean {
        if (sampleLines.isEmpty()) return false

        val maxWidth = getMaxSampleWidth(textRenderer) * scale
        val height = (sampleLines.size * (textRenderer.fontHeight + 2)) * scale
        
        // Convert x to left edge for hitbox calculation
        val leftEdge = getLeftSampleEdge(textRenderer).toFloat()
            
        if (mouseX >= leftEdge - 4 &&
            mouseX <= leftEdge + maxWidth + 4 &&
            mouseY >= y - 4 &&
            mouseY <= y + height + 2) {        
            return true
        }
        return false
    }

    /**
     * Returns the LineAction if the click hits an inline action button, null otherwise.
     * Button positions: LEFT/CENTER = buttons on left before line; RIGHT = buttons on right after line.
     */
    private fun getClickedLineAction(textRenderer: TextRenderer, mouseX: Double, mouseY: Double): LineAction? {
        if (lines.isEmpty() || lineIndexToActions.isEmpty()) return null

        val allLines = getDisplayLinesForRender()
        val linesWithAllButtons = allLines.mapIndexed { index, line ->
            val actions = lineIndexToActions[index]
            if (!actions.isNullOrEmpty()) line + " " + actions.joinToString("") { it.text } else line
        }
        val hoveredLineIndex = getHoveredLineIndex(textRenderer, linesWithAllButtons, mouseX, mouseY) ?: return null
        val actions = lineIndexToActions[hoveredLineIndex] ?: return null
        if (actions.isEmpty()) return null

        val scaleDouble = scale.toDouble()
        val lineStr = allLines[hoveredLineIndex]
        val buttonsStr = actions.joinToString("") { it.text }
        val lineWidth = textRenderer.getWidth(Text.literal(lineStr))
        val buttonsWidth = textRenderer.getWidth(Text.literal(buttonsStr))
        val spaceWidth = textRenderer.getWidth(Text.literal(" "))
        val fullWidth = buttonsWidth + spaceWidth + lineWidth

        val maxWidthScreen = getMaxWidth(textRenderer, linesWithAllButtons) * scaleDouble
        val leftEdge = getLeftEdge(textRenderer, linesWithAllButtons).toDouble()
        val fontHeight = textRenderer.fontHeight
        val lineHeightPx = (fontHeight + 2) * scale
        val lineTop = y + hoveredLineIndex * lineHeightPx
        val lineBottom = lineTop + lineHeightPx

        if (mouseY < lineTop || mouseY >= lineBottom) return null
        val buttonsWidthScreen = buttonsWidth * scaleDouble

        val buttonStartX = when (alignment) {
            Alignment.LEFT -> leftEdge
            Alignment.RIGHT -> leftEdge + maxWidthScreen - buttonsWidthScreen
            Alignment.CENTER -> {
                val fullWidthScreen = fullWidth * scaleDouble
                leftEdge + (maxWidthScreen - fullWidthScreen) / 2
            }
        }

        if (mouseX < buttonStartX || mouseX >= buttonStartX + buttonsWidthScreen) return null

        var relX = mouseX - buttonStartX
        for (action in actions) {
            val actionWidth = textRenderer.getWidth(Text.literal(action.text)) * scaleDouble
            if (relX < actionWidth) return action
            relX -= actionWidth
        }
        return null
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
