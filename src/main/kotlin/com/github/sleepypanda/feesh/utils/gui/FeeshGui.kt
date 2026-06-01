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
import net.minecraft.network.chat.Component
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.client.gui.screens.ChatScreen
//#if MC >= 26.1
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor as GuiGraphics
//#else
import net.minecraft.client.gui.GuiGraphics
//#endif
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import java.awt.Color
import kotlin.math.max
import kotlin.math.roundToInt

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
 * A segment of a line used to display column-based text with columns alignment.
 * @param text The text to display in the segment.
 * @param xOffset The x offset within the segment table — set by [SegmentTable.layout].
 * @param columnWidth The width of the segment column — set by [SegmentTable.layout].
 */
data class LineSegment(
    val text: String,
    val xOffset: Int = 0,
    val columnWidth: Int = 0,
)

data class LineInfo(
    val text: String = "",
    val segments: List<LineSegment>? = null, // Text is not needed if Segments defined
    internal val layoutTableWidth: Int? = null,
    val tooltip: List<Component>? = null,
    val actions: List<LineAction> = emptyList(),
) {
    /** Full table width; from [SegmentTable.layout] or max segment extent. */
    val segmentTableWidth: Int?
        get() {
            layoutTableWidth?.let { return it }
            val segs = segments ?: return null
            if (segs.isEmpty()) return null
            return segs.maxOf { segment ->
                segment.xOffset + (segment.columnWidth.takeIf { it > 0 } ?: 0)
            }
        }

    companion object {
        fun withSegments(
            segments: List<LineSegment>,
            tableWidth: Int,
            text: String = "",
            tooltip: List<Component>? = null,
            actions: List<LineAction> = emptyList(),
        ): LineInfo = LineInfo(
            text = text,
            segments = segments,
            layoutTableWidth = tableWidth,
            tooltip = tooltip,
            actions = actions,
        )
    }
}

object SegmentTable {
    data class LayoutResult(
        val tableWidth: Int,
        val rows: List<List<LineSegment>>,
    )

    /**
     * Lays out columnar rows with shared column widths and x offsets.
     * @param rows Each row has the same number of cells; empty string skips cell content but keeps column slots.
     * @param separator Text drawn between enabled columns (column max width > 0).
     */
    fun layout(font: Font, rows: List<List<String>>, separator: String): LayoutResult {
        if (rows.isEmpty()) return LayoutResult(0, emptyList())

        val columnCount = rows.maxOf { it.size }
        val normalizedRows = rows.map { row ->
            if (row.size >= columnCount) row else row + List(columnCount - row.size) { "" }
        }

        val separatorWidth = font.width(Component.literal(separator))
        val columnWidths = (0 until columnCount).map { col ->
            normalizedRows.maxOf { font.width(Component.literal(it[col])) }
        }
        val enabled = columnWidths.map { it > 0 }

        val columnX = IntArray(columnCount) { -1 }
        val separatorX = mutableListOf<Int>()
        var x = 0
        var firstColumn = true
        for (col in 0 until columnCount) {
            if (!enabled[col]) continue
            if (!firstColumn) {
                separatorX.add(x)
                x += separatorWidth
            }
            columnX[col] = x
            x += columnWidths[col]
            firstColumn = false
        }

        val tableWidth = x
        val builtRows = normalizedRows.map { row ->
            val segments = mutableListOf<LineSegment>()
            var separatorIndex = 0
            var firstColumnInRow = true
            for (col in 0 until columnCount) {
                if (!enabled[col]) continue
                if (!firstColumnInRow) {
                    val sepX = separatorX[separatorIndex++]
                    segments.add(LineSegment(separator, sepX, separatorWidth))
                }
                if (row[col].isNotEmpty()) {
                    segments.add(LineSegment(row[col], columnX[col], columnWidths[col]))
                }
                firstColumnInRow = false
            }
            segments
        }

        return LayoutResult(tableWidth, builtRows)
    }
}

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
 * @property lines The lines of the GUI (text, optional tooltips, optional inline actions).
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
    private var lines: List<LineInfo> = emptyList()
    private var sampleLines: List<String> = emptyList()

    private var color: Int = Color(255, 255, 255, 255).rgb
    // Buttons are shown when in inventory/chat on top of the overlay (e.g. Reset, Pause, switch mode).
    private var buttons: List<GuiButton> = emptyList()

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
    fun getLines(): List<LineInfo> = lines
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

    fun setLines(lines: List<LineInfo>): FeeshGui {
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

    private fun onDraw(drawContext: GuiGraphics, textRenderer: Font, mcClient: Minecraft) {
        if (mcClient.screen is InventoryScreen && isClickable) return
        draw(drawContext, textRenderer, mcClient)
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
        if (Overlays.overlayButtonsRequireCtrlClick && !event.hasControlDown) return

        val textRenderer = Minecraft.getInstance().font

        if (buttons.isNotEmpty()) {
            val clickedButton = getClickedButton(textRenderer, event.mouseX, event.mouseY)
            if (clickedButton != null) {
                clickedButton.onClick()
                return
            }
        }
   
        if (lines.any { it.actions.isNotEmpty() } && event.screen is InventoryScreen) { // TODO: Inline buttons are not rendered when in chat, due to missing mouseX/mouseY for hover.
            val clickedLineAction = getClickedLineAction(textRenderer, event.mouseX, event.mouseY)
            if (clickedLineAction != null) {
                clickedLineAction.onClick()
                return
            }
        }
    }

    /**
     * Recalculates x coordinate when alignment changes to keep overlay at the same visual position.
     * @param textRenderer Font to calculate text widths
     * @param oldAlignment Previous alignment
     * @param newAlignment New alignment
     */
    fun recalculateXForAlignment(textRenderer: Font, oldAlignment: Alignment, newAlignment: Alignment) {
        if (oldAlignment == newAlignment) return
        
        val linesToUse = when {
            sampleLines.isNotEmpty() -> sampleLines
            else -> emptyList()
        }
        if (linesToUse.isEmpty()) return
        
        val maxWidth = linesToUse.maxOfOrNull { textRenderer.width(Component.literal(it)) } ?: 0
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
        return buttonLines + lines.map { getLineDisplayText(it) }
    }

    private fun getLineDisplayText(lineInfo: LineInfo): String {
        if (!lineInfo.segments.isNullOrEmpty()) {
            return lineInfo.segments.joinToString("") { it.text }
        }
        return lineInfo.text
    }

    private fun getLineWidth(textRenderer: Font, lineInfo: LineInfo): Int {
        lineInfo.segmentTableWidth?.let { return it }
        return textRenderer.width(Component.literal(lineInfo.text))
    }

    private fun getOverlayMaxWidth(
        textRenderer: Font,
        lineInfos: List<LineInfo>,
        buttonLineTexts: List<String>,
    ): Int {
        val widths = lineInfos.map { getLineWidth(textRenderer, it) } +
            buttonLineTexts.map { textRenderer.width(Component.literal(it)) }
        return widths.maxOrNull() ?: 0
    }

    /**
     * Gets the LineInfo for the given line index, skipping the buttons.
     */
    private fun getLineInfoByDisplayIndex(displayIndex: Int): LineInfo? {
        val offset = if (GuiUtils.isInInventoryOrChat()) buttons.size else 0
        if (displayIndex < offset) return null
        return lines.getOrNull(displayIndex - offset)
    }

    /**
     * Gets display lines for rendering. Buttons are included only when in inventory or chat.
     */
    private fun getDisplayLinesForRender(): List<String> {
        return if (GuiUtils.isInInventoryOrChat()) getAllDisplayLines() else lines.map { getLineDisplayText(it) }
    }

    private fun getRenderLineInfos(): List<LineInfo> {
        return if (GuiUtils.isInInventoryOrChat()) {
            val buttonLineInfos = buttons.sortedBy { it.lineIndex }.map { LineInfo(text = it.text) }
            buttonLineInfos + lines
        } else {
            lines
        }
    }

    /**
     * Gets the maximum width of the sample overlay.
     */
    private fun getMaxSampleWidth(textRenderer: Font): Int {
        val linesToUse = when {
            sampleLines.isNotEmpty() -> sampleLines
            else -> emptyList()
        }
        return linesToUse.maxOfOrNull { textRenderer.width(Component.literal(it)) } ?: 0
    }

    /**
     * Converts x coordinate (which represents different points based on alignment) to actual left edge.
     * Uses scaled width for CENTER/RIGHT so that the reference point stays correct after matrices.scale(scale).
     */
    private fun getLeftEdge(
        textRenderer: Font,
        lineInfos: List<LineInfo>,
        buttonLineTexts: List<String>,
    ): Int {
        val maxWidth = getOverlayMaxWidth(textRenderer, lineInfos, buttonLineTexts)
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
    private fun getLeftSampleEdge(textRenderer: Font): Int {
        val maxWidth = getMaxSampleWidth(textRenderer)
        val renderedWidth = (maxWidth * scale).toInt()
        return when (alignment) {
            Alignment.LEFT -> x
            Alignment.CENTER -> x - renderedWidth / 2
            Alignment.RIGHT -> x - renderedWidth
        }
    }

    private fun draw(drawContext: GuiGraphics, textRenderer: Font, mcClient: Minecraft, mouseX: Int? = null, mouseY: Int? = null) {
        if (lines.isEmpty()) return
        if (!WorldUtils.isInSkyblock()) return
        if (settingsKey != null && !settingsKey!!()) return
        if (!condition()) return
        if (mcClient.screen is MoveGuisScreen) return

        val allLines = getDisplayLinesForRender()
        val renderLineInfos = getRenderLineInfos()
        val buttonLineTexts = if (GuiUtils.isInInventoryOrChat()) buttons.sortedBy { it.lineIndex }.map { it.text } else emptyList()
        val scaledY = (y / scale).toInt()
        val fontHeight = textRenderer.lineHeight
        val lineHeightPx = fontHeight + 2

        val hasHoverableLines = lines.any { it.actions.isNotEmpty() || !it.tooltip.isNullOrEmpty() }
        val hoveredLineIndex = if (mouseX != null && mouseY != null && hasHoverableLines) {
            getHoveredLineIndex(textRenderer, allLines, mouseX.toDouble(), mouseY.toDouble())
        } else null
        val hoveredLineInfo = hoveredLineIndex?.let { getLineInfoByDisplayIndex(it) }

        val hoveredActions = hoveredLineInfo?.actions?.takeIf { it.isNotEmpty() }
        val hoveredTooltip = hoveredLineInfo?.tooltip

        val maxWidth = getOverlayMaxWidth(textRenderer, lines, buttonLineTexts)
        val leftEdge = getLeftEdge(textRenderer, lines, buttonLineTexts)
        val scaledLeftEdge = (leftEdge / scale).toInt()
        val height = (allLines.size * lineHeightPx)
        var currentY = scaledY

        val useCustomStyle = applyCustomStyleKey()
        val overlayBackgroundCoords = getOverlayBackgroundCoords(scaledLeftEdge, scaledY, maxWidth, height)
        if (useCustomStyle) {
            val overlayScreenCoords = toScreenCoords(overlayBackgroundCoords)
            drawOverlayBackgroundScreenSpace(drawContext, overlayScreenCoords)
            drawOverlayBorderScreenSpace(drawContext, overlayScreenCoords) // I had to draw it before scaling, so the border is not scaled, and aligned with background.
        }

        drawContext.pose().pushMatrix()
        drawContext.pose().scale(scale, scale)

        for ((index, line) in allLines.withIndex()) {
            val lineInfo = renderLineInfos.getOrNull(index)
            val actions = if (index == hoveredLineIndex) hoveredActions else null
            if (!actions.isNullOrEmpty()) {
                val buttonsStr = actions.joinToString("") { it.text }
                val buttonsWidth = textRenderer.width(Component.literal(buttonsStr))
                val spaceWidth = textRenderer.width(Component.literal(" "))
                val reservedWidth = buttonsWidth + spaceWidth
                val usesSegmentColumns = lineInfo.usesSegmentColumns()
                val lineAvailableWidth = (maxWidth - reservedWidth).coerceAtLeast(0)
                val clippedLine = if (usesSegmentColumns) {
                    line
                } else {
                    trimTextToWidth(textRenderer, line, lineAvailableWidth)
                }
                val clippedLineWidth = if (usesSegmentColumns) {
                    lineInfo?.segmentTableWidth ?: lineInfo?.let { getLineWidth(textRenderer, it) }
                        ?: textRenderer.width(Component.literal(clippedLine))
                } else {
                    textRenderer.width(Component.literal(clippedLine))
                }

                when (alignment) {
                    Alignment.LEFT -> {
                        val buttonsX = scaledLeftEdge
                        val lineX = scaledLeftEdge + reservedWidth
                        drawStringCompat(drawContext, textRenderer, Component.literal(buttonsStr), buttonsX, currentY, color, true)
                        drawLineContent(drawContext, textRenderer, lineInfo, clippedLine, lineX, lineAvailableWidth, currentY)
                    }
                    Alignment.RIGHT -> {
                        val buttonsX = scaledLeftEdge + maxWidth - buttonsWidth
                        if (usesSegmentColumns) {
                            drawLineContent(
                                drawContext,
                                textRenderer,
                                lineInfo,
                                clippedLine,
                                scaledLeftEdge,
                                maxWidth,
                                currentY,
                                clipRightOverride = scaledLeftEdge + lineAvailableWidth,
                            )
                        } else {
                            val lineX = scaledLeftEdge + lineAvailableWidth - clippedLineWidth
                            drawLineContent(drawContext, textRenderer, lineInfo, clippedLine, lineX, lineAvailableWidth, currentY)
                        }
                        drawStringCompat(drawContext, textRenderer, Component.literal(buttonsStr), buttonsX, currentY, color, true)
                    }
                    Alignment.CENTER -> {
                        val tableWidth = if (usesSegmentColumns) {
                            lineInfo?.segmentTableWidth ?: clippedLineWidth
                        } else {
                            clippedLineWidth
                        }
                        val textWidth = minOf(tableWidth, lineAvailableWidth)
                        val blockWidth = reservedWidth + textWidth
                        val contentLeft = scaledLeftEdge + (maxWidth - blockWidth) / 2
                        val buttonsX = contentLeft
                        val lineX = contentLeft + reservedWidth
                        drawStringCompat(drawContext, textRenderer, Component.literal(buttonsStr), buttonsX, currentY, color, true)
                        drawLineContent(drawContext, textRenderer, lineInfo, clippedLine, lineX, lineAvailableWidth, currentY)
                    }
                }
            } else {
                val textWidth = lineInfo?.let { getLineWidth(textRenderer, it) }
                    ?: textRenderer.width(Component.literal(line))
                val tableWidth = lineInfo?.segmentTableWidth
                val actualX = when (alignment) {
                    Alignment.LEFT -> scaledLeftEdge
                    Alignment.RIGHT -> if (lineInfo.usesSegmentColumns()) {
                        scaledLeftEdge
                    } else {
                        scaledLeftEdge + maxWidth - textWidth
                    }
                    Alignment.CENTER -> if (lineInfo.usesSegmentColumns() && tableWidth != null) {
                        scaledLeftEdge + (maxWidth - tableWidth) / 2
                    } else {
                        scaledLeftEdge + (maxWidth - textWidth) / 2
                    }
                }
                val contentWidth = when (alignment) {
                    Alignment.RIGHT -> maxWidth
                    else -> tableWidth ?: maxWidth
                }
                drawLineContent(drawContext, textRenderer, lineInfo, line, actualX, contentWidth, currentY)
            }
            currentY += lineHeightPx
        }

        drawContext.pose().popMatrix()

        if (!hoveredTooltip.isNullOrEmpty() && mouseX != null && mouseY != null) {
            drawContext.setComponentTooltipForNextFrame(textRenderer, hoveredTooltip, mouseX, mouseY)
        }
    }

    private fun LineInfo?.usesSegmentColumns(): Boolean = this != null && !this.segments.isNullOrEmpty()

    private fun drawLineContent(
        drawContext: GuiGraphics,
        textRenderer: Font,
        lineInfo: LineInfo?,
        fallbackText: String,
        lineStartX: Int,
        contentWidth: Int,
        currentY: Int,
        clipLeftOverride: Int? = null,
        clipRightOverride: Int? = null,
    ) {
        if (lineInfo != null && !lineInfo.segments.isNullOrEmpty()) {
            val tableWidth = lineInfo.segmentTableWidth
                ?: lineInfo.segments.maxOf { it.xOffset + textRenderer.width(Component.literal(it.text)) }
            val clipLeft = clipLeftOverride ?: lineStartX
            val clipRight = clipRightOverride ?: (lineStartX + contentWidth)
            val segmentOriginX = when (alignment) {
                Alignment.RIGHT -> lineStartX + contentWidth - tableWidth
                else -> lineStartX
            }
            for (segment in lineInfo.segments) {
                val segmentTextWidth = textRenderer.width(Component.literal(segment.text))
                val columnWidth = segment.columnWidth.takeIf { it > 0 } ?: segmentTextWidth
                val columnStart = segmentOriginX + segment.xOffset
                val columnEnd = columnStart + columnWidth
                if (columnEnd <= clipLeft || columnStart >= clipRight) continue

                val drawLeft = maxOf(columnStart, clipLeft)
                val drawRight = minOf(columnEnd, clipRight)
                val drawWidth = drawRight - drawLeft
                if (drawWidth <= 0) continue

                var segmentText = segment.text
                val leadingSkip = drawLeft - columnStart
                if (leadingSkip > 0) {
                    segmentText = trimFormattedTextLeading(textRenderer, segmentText, leadingSkip)
                }
                if (segmentText.isEmpty()) continue

                val displayText = trimTextToWidth(textRenderer, segmentText, drawWidth)
                if (displayText.isEmpty()) continue
                val displayTextWidth = textRenderer.width(Component.literal(displayText))

                val segmentX = when (alignment) {
                    Alignment.RIGHT -> drawRight - displayTextWidth
                    Alignment.CENTER -> drawLeft + (drawWidth - displayTextWidth) / 2
                    else -> drawLeft
                }

                drawStringCompat(
                    drawContext,
                    textRenderer,
                    Component.literal(displayText),
                    segmentX,
                    currentY,
                    color,
                    true,
                )
            }
        } else {
            val displayText = trimTextToWidth(textRenderer, fallbackText, contentWidth)
            drawStringCompat(drawContext, textRenderer, Component.literal(displayText), lineStartX, currentY, color, true)
        }
    }

    private fun drawStringCompat(drawContext: GuiGraphics, textRenderer: Font, text: Component, x: Int, y: Int, color: Int, shadow: Boolean) {
        //#if MC >= 26.1
        //$$ drawContext.text(textRenderer, text, x, y, color, shadow)
        //#else
        drawContext.drawString(textRenderer, text, x, y, color, shadow)
        //#endif
    }

    private fun trimTextToWidth(textRenderer: Font, text: String, maxWidth: Int): String {
        if (maxWidth <= 0 || text.isEmpty()) return ""
        if (textRenderer.width(Component.literal(text)) <= maxWidth) return text

        val result = StringBuilder()
        var i = 0
        while (i < text.length) {
            val ch = text[i]
            if (ch == '§' && i + 1 < text.length) { // formatting code
                result.append(ch)
                result.append(text[i + 1])
                i += 2
                continue
            }

            val next = result.toString() + ch
            if (textRenderer.width(Component.literal(next)) > maxWidth) break
            result.append(ch)
            i++
        }

        return result.toString()
    }

    private fun trimFormattedTextLeading(textRenderer: Font, text: String, skipWidth: Int): String {
        if (skipWidth <= 0 || text.isEmpty()) return text

        var index = 0
        var removedWidth = 0
        while (index < text.length && removedWidth < skipWidth) {
            val nextIndex = if (text[index] == '§' && index + 1 < text.length) index + 2 else index + 1
            removedWidth = textRenderer.width(Component.literal(text.substring(0, nextIndex)))
            index = nextIndex
        }

        return text.substring(index)
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

    private data class OverlayScreenCoords(val left: Int, val top: Int, val right: Int, val bottom: Int)

    private fun toScreenCoords(backgroundCoords: OverlayBackgroundCoords): OverlayScreenCoords {
        return OverlayScreenCoords(
            (backgroundCoords.left * scale).roundToInt(),
            (backgroundCoords.top * scale).roundToInt(),
            (backgroundCoords.right * scale).roundToInt(),
            (backgroundCoords.bottom * scale).roundToInt(),
        )
    }

    private fun drawOverlayBackgroundScreenSpace(drawContext: GuiGraphics, screenCoords: OverlayScreenCoords) {
        if (!Overlays.overlaysBackground) return

        val backgroundTopColor = Color(Overlays.overlaysBackgroundColor1, true).rgb
        val backgroundBottomColor = Color(Overlays.overlaysBackgroundColor2, true).rgb

        drawContext.fillGradient(
            screenCoords.left,
            screenCoords.top,
            screenCoords.right,
            screenCoords.bottom,
            backgroundTopColor,
            backgroundBottomColor
        )
    }

    private fun drawOverlayBorderScreenSpace(drawContext: GuiGraphics, screenCoords: OverlayScreenCoords) {
        if (!Overlays.overlaysBorder) return

        val borderColor = Color(Overlays.overlaysBorderColor, true).rgb
        val borderWidth = Overlays.overlaysBorderWidth.coerceIn(1..5)
        val left = screenCoords.left
        val top = screenCoords.top
        val right = screenCoords.right
        val bottom = screenCoords.bottom

        // Draw border borderWidth pixels outside the overlay, so it does not overlap the background.
        // Also made lines not overlap in the corners.
        drawContext.fill(left - borderWidth, top - borderWidth, right + borderWidth, top, borderColor) // top
        drawContext.fill(left - borderWidth, bottom, right + borderWidth, bottom + borderWidth, borderColor) // bottom
        drawContext.fill(left - borderWidth, top, left, bottom, borderColor) // left
        drawContext.fill(right, top, right + borderWidth, bottom, borderColor) // right
    }

    private fun getHoveredLineIndex(textRenderer: Font, displayLines: List<String>, mouseX: Double, mouseY: Double): Int? {
        val buttonLineTexts = if (GuiUtils.isInInventoryOrChat()) buttons.sortedBy { it.lineIndex }.map { it.text } else emptyList()
        val maxWidth = getOverlayMaxWidth(textRenderer, lines, buttonLineTexts) * scale
        val leftEdge = getLeftEdge(textRenderer, lines, buttonLineTexts).toFloat()
        val fontHeight = textRenderer.lineHeight
        val lineHeightPx = (fontHeight + 2) * scale

        if (mouseX < leftEdge - 2 || mouseX > leftEdge + maxWidth + 2) return null
        if (mouseY < y - 2 || mouseY > y + displayLines.size * lineHeightPx + 2) return null

        val lineIndex = ((mouseY - y) / lineHeightPx).toInt()
        return if (lineIndex in 0 until displayLines.size) lineIndex else null
    }
    
    /**
     * Draws the sample overlay from sampleLines, for move GUIs screen.
     */
    fun drawSample(drawContext: GuiGraphics, textRenderer: Font, mcClient: Minecraft) {
        if (sampleLines.isEmpty()) return
        if (!WorldUtils.isInSkyblock()) return
        if (mcClient.screen !is MoveGuisScreen) return

        drawContext.pose().pushMatrix()
        drawContext.pose().scale(scale, scale)

        val scaledY = (y / scale).toInt()
        
        val maxWidth = getMaxSampleWidth(textRenderer)
        val height = (sampleLines.size * (textRenderer.lineHeight + 2))
        
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
            val text = Component.literal(line)
            val textWidth = textRenderer.width(text)
            
            // Text is aligned within the overlay block based on alignment
            val actualX = when (alignment) {
                Alignment.LEFT -> scaledLeftEdge
                Alignment.RIGHT -> scaledLeftEdge + maxWidth - textWidth
                Alignment.CENTER -> scaledLeftEdge + (maxWidth - textWidth) / 2
            }

            drawStringCompat(drawContext, textRenderer, text, actualX, currentY, color, true)
            currentY += textRenderer.lineHeight + 2
        }

        drawContext.pose().popMatrix()

        val alignmentLabel = when (alignment) {
            Alignment.LEFT -> "[L]"
            Alignment.RIGHT -> "[R]"
            Alignment.CENTER -> "[C]"
        }
        val labelText = Component.literal("${GRAY}$alignmentLabel")
        val labelX = leftEdge - 2
        val labelY = y - 2 - textRenderer.lineHeight
        drawStringCompat(drawContext, textRenderer, labelText, labelX, labelY, color, true)
    }

    /**
     * Checks if the mouse is over the sample overlay.
     */
    fun isInSample(textRenderer: Font, @Suppress("UNUSED_PARAMETER") client: Minecraft, mouseX: Double, mouseY: Double): Boolean {
        if (sampleLines.isEmpty()) return false

        val maxWidth = getMaxSampleWidth(textRenderer) * scale
        val height = (sampleLines.size * (textRenderer.lineHeight + 2)) * scale
        
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
    private fun getClickedLineAction(textRenderer: Font, mouseX: Double, mouseY: Double): LineAction? {
        if (lines.none { it.actions.isNotEmpty() }) return null

        val allLines = getDisplayLinesForRender()
        val buttonLineTexts = if (GuiUtils.isInInventoryOrChat()) buttons.sortedBy { it.lineIndex }.map { it.text } else emptyList()
        val hoveredLineIndex = getHoveredLineIndex(textRenderer, allLines, mouseX, mouseY) ?: return null
        val lineInfo = getLineInfoByDisplayIndex(hoveredLineIndex) ?: return null
        val actions = lineInfo.actions
        if (actions.isEmpty()) return null

        val scaleDouble = scale.toDouble()
        val buttonsStr = actions.joinToString("") { it.text }
        val buttonsWidth = textRenderer.width(Component.literal(buttonsStr))
        val spaceWidth = textRenderer.width(Component.literal(" "))
        val reservedWidth = buttonsWidth + spaceWidth
        val maxWidth = getOverlayMaxWidth(textRenderer, lines, buttonLineTexts)
        val lineWidth = getLineWidth(textRenderer, lineInfo)
        val lineAvailableWidth = (maxWidth - reservedWidth).coerceAtLeast(0)
        val clippedLineWidth = if (lineInfo.usesSegmentColumns()) {
            minOf(lineInfo.segmentTableWidth ?: lineWidth, lineAvailableWidth)
        } else {
            minOf(lineWidth, lineAvailableWidth)
        }

        val maxWidthScreen = maxWidth * scaleDouble
        val leftEdge = getLeftEdge(textRenderer, lines, buttonLineTexts).toDouble()
        val fontHeight = textRenderer.lineHeight
        val lineHeightPx = (fontHeight + 2) * scale
        val lineTop = y + hoveredLineIndex * lineHeightPx
        val lineBottom = lineTop + lineHeightPx

        if (mouseY < lineTop || mouseY >= lineBottom) return null
        val buttonsWidthScreen = buttonsWidth * scaleDouble

        val buttonStartX = when (alignment) {
            Alignment.LEFT -> leftEdge
            Alignment.RIGHT -> leftEdge + maxWidthScreen - buttonsWidthScreen
            Alignment.CENTER -> {
                val tableWidth = if (lineInfo.usesSegmentColumns()) {
                    lineInfo.segmentTableWidth ?: clippedLineWidth
                } else {
                    clippedLineWidth
                }
                val textWidth = minOf(tableWidth, lineAvailableWidth)
                val blockWidth = reservedWidth + textWidth
                val contentWidthScreen = blockWidth * scaleDouble
                leftEdge + (maxWidthScreen - contentWidthScreen) / 2
            }
        }

        if (mouseX < buttonStartX || mouseX >= buttonStartX + buttonsWidthScreen) return null

        var relX = mouseX - buttonStartX
        for (action in actions) {
            val actionWidth = textRenderer.width(Component.literal(action.text)) * scaleDouble
            if (relX < actionWidth) return action
            relX -= actionWidth
        }
        return null
    }

    /**
     * Returns the button if (mouseX, mouseY) hits a button line, null otherwise.
     * Uses exclusive line boundaries so a click between two lines hits neither.
     */
    private fun getClickedButton(textRenderer: Font, mouseX: Double, mouseY: Double): GuiButton? {
        if (lines.isEmpty() || buttons.isEmpty()) return null

        val displayLines = getAllDisplayLines()
        val buttonLineTexts = if (GuiUtils.isInInventoryOrChat()) buttons.sortedBy { it.lineIndex }.map { it.text } else emptyList()
        val maxWidth = getOverlayMaxWidth(textRenderer, lines, buttonLineTexts) * scale
        val leftEdge = getLeftEdge(textRenderer, lines, buttonLineTexts).toFloat()

        if (mouseX < leftEdge - 2 || mouseX > leftEdge + maxWidth + 2) return null

        val fontHeight = textRenderer.lineHeight
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
