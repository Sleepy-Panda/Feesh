package com.github.sleepypanda.feesh.utils.gui

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.features.overlays.SeaCreaturesTracker
import com.github.sleepypanda.feesh.features.overlays.LegionBobbingTimeTracker
import com.github.sleepypanda.feesh.features.overlays.JerryWorkshopTracker
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.enums.Alignment
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Click
import net.minecraft.client.input.KeyInput
import net.minecraft.text.Text
import java.awt.Color

data class GuiMapping(
    val settingGetter: () -> Boolean,
    val gui: FeeshGui
)

object MoveGuis {
    const val COMMAND_NAME = "feeshMoveAllGuis"

    private val guiMappings = mutableListOf<GuiMapping>()

    fun init() {
        initializeGuiMappings()
        RegisterUtils.command(COMMAND_NAME) {
            moveAllGuis()
        }
    }

    fun moveAllGuis() {
        if (!WorldUtils.isInSkyblock()) return

        val mc = FeeshMod.mc
        mc.send {
            mc.setScreen(MoveGuisScreen())
        }
    }

    private fun initializeGuiMappings() {
        val registeredGuis = FeeshGui.getAllRegisteredGuis()
        registeredGuis.forEach { gui ->
            guiMappings.add(GuiMapping(
                settingGetter = gui.getSettingsKey() ?: { false },
                gui = gui
            ))
        }
    }
    
    fun getEnabledGuis(): List<GuiMapping> {
        return guiMappings.filter { it.settingGetter() }
    }
}

class MoveGuisScreen : Screen(Text.literal("Feesh Move Guis")) {
    private val enabledGuis: List<GuiMapping> by lazy { MoveGuis.getEnabledGuis() }
    private var isDraggingGui: FeeshGui? = null
    private var dragOffsetX = 0
    private var dragOffsetY = 0
    private var lastDraggedGui: FeeshGui? = null
    private val color = Color(255, 255, 255, 255).rgb
    
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {       
        val textRenderer = client?.textRenderer ?: return
        
        val hint1 = Text.literal("${RED}${BOLD}Enable GUIs in settings to see them here!")
        val x1 = client!!.window.scaledWidth / 2 - textRenderer.getWidth(hint1) / 2
        context.drawText(textRenderer, hint1, x1, 10, color, true)
        val hint2 = Text.literal("${YELLOW}Move them using your mouse. Press +/- or scroll to scale. Press 0 to change alignment. Press ESC to exit.")
        val x2 = client!!.window.scaledWidth / 2 - textRenderer.getWidth(hint2) / 2
        context.drawText(textRenderer, hint2, x2, 20, color, true)
        
        enabledGuis.forEach { mapping ->
            val gui = mapping.gui           
            gui.drawSample(context, textRenderer, client!!)
        }
        
        super.render(context, mouseX, mouseY, delta)
    }
    
    override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
        if (click.button() != 0) return super.mouseClicked(click, doubled)
        
        val mouseX = click.x()
        val mouseY = click.y()
        val textRenderer = client!!.textRenderer
        
        enabledGuis.forEach { mapping ->
            val gui = mapping.gui
            
            val isInSample = gui.isInSample(textRenderer, client!!, mouseX, mouseY)
            if (isInSample) {
                isDraggingGui = gui
                lastDraggedGui = gui
                
                // Calculate left edge for drag offset
                val linesToUse = if (gui.getSampleLines().isNotEmpty()) gui.getSampleLines() else gui.getLines()
                val maxWidth = linesToUse.maxOfOrNull { textRenderer.getWidth(Text.literal(it)) } ?: 0
                val leftEdge = when (gui.getAlignment()) {
                    Alignment.LEFT -> gui.getX()
                    Alignment.CENTER -> gui.getX() - maxWidth / 2
                    Alignment.RIGHT -> gui.getX() - maxWidth
                }
                
                dragOffsetX = (mouseX - leftEdge).toInt()
                dragOffsetY = (mouseY - gui.getY()).toInt()
                return true
            }
        }
        
        return super.mouseClicked(click, doubled)
    }
    
    override fun mouseDragged(click: Click, deltaX: Double, deltaY: Double): Boolean {
        if (click.button() == 0 && isDraggingGui != null) {
            val gui = isDraggingGui!!
            val textRenderer = client!!.textRenderer
            
            // Calculate the left edge of the overlay based on current alignment
            val linesToUse = if (gui.getSampleLines().isNotEmpty()) gui.getSampleLines() else gui.getLines()
            val maxWidth = linesToUse.maxOfOrNull { textRenderer.getWidth(Text.literal(it)) } ?: 0
            
            // Calculate new left edge from mouse position
            val newLeftEdge = (click.x() - dragOffsetX).toInt().coerceAtLeast(0)
            
            // Convert new left edge back to x coordinate based on alignment
            val newX = when (gui.getAlignment()) {
                Alignment.LEFT -> newLeftEdge
                Alignment.CENTER -> newLeftEdge + maxWidth / 2
                Alignment.RIGHT -> newLeftEdge + maxWidth
            }
            
            val newY = ((click.y() - dragOffsetY).toInt().coerceAtLeast(0))
            gui.setX(newX).setY(newY)
            return true
        }
        return super.mouseDragged(click, deltaX, deltaY)
    }
    
    override fun mouseReleased(click: Click): Boolean {
        if (click.button() == 0) {
            if (isDraggingGui != null) {
                saveGuiCoords(isDraggingGui!!)
            }
            isDraggingGui = null
        }
        return super.mouseReleased(click)
    }
    
    override fun close() {
        enabledGuis.forEach { mapping ->
            val gui = mapping.gui
            saveGuiCoords(gui)
        }
        super.close()
    }
    
    override fun keyPressed(input: KeyInput): Boolean {
        if (input.key() == 256) { // ESC
            client!!.setScreen(null)
            return true
        }
        
        if (lastDraggedGui != null) {
            when (input.key()) {
                61, 334 -> { // = or + on numpad
                    changeScale(lastDraggedGui!!, 0.1f)
                    return true
                }
                45, 333 -> { // - or - on numpad
                    changeScale(lastDraggedGui!!, -0.1f)
                    return true
                }
                48 -> { // 0
                    changeAlignment(lastDraggedGui!!)
                    return true
                }
            }
        }
        
        return super.keyPressed(input)
    }
    
    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (lastDraggedGui != null && verticalAmount != 0.0) {
            val delta = if (verticalAmount > 0) 0.1f else -0.1f
            changeScale(lastDraggedGui!!, delta)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    private fun changeScale(gui: FeeshGui, delta: Float) {
        val currentScale = gui.getScale()
        val newScale = (currentScale + delta).coerceAtLeast(0.2f)
        gui.setScale(newScale)
        saveGuiCoords(gui)
    }
    
    private fun changeAlignment(gui: FeeshGui) {
        val textRenderer = client!!.textRenderer
        val currentAlignment = gui.getAlignment()
        val newAlignment = when (currentAlignment) {
            Alignment.LEFT -> Alignment.CENTER
            Alignment.CENTER -> Alignment.RIGHT
            Alignment.RIGHT -> Alignment.LEFT
        }
        gui.recalculateXForAlignment(textRenderer, currentAlignment, newAlignment)
        gui.setAlignment(newAlignment)
        saveGuiCoords(gui)
    }
    
    private fun saveGuiCoords(gui: FeeshGui) {
        PersistentDataManager.updateOverlayCoordsData(gui.getCoordsDataKey(), gui.getX(), gui.getY(), gui.getScale(), gui.getAlignment())
    }
}
