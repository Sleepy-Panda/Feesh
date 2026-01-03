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
    private val guiMappings = mutableListOf<GuiMapping>()

    fun init() {
        initializeGuiMappings()
        RegisterUtils.command("feeshMoveAllGuis") {
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
        
        val hint = Text.literal("${YELLOW}${BOLD}Move / scale the GUIs using your mouse. Press +/- or scroll to scale. Press ESC to exit.")
        val x = client!!.window.scaledWidth / 2 - textRenderer.getWidth(hint) / 2
        context.drawText(textRenderer, hint, x, 20, color, true)
        
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
            val x = gui.getX()
            val y = gui.getY()
            val scale = gui.getScale()
            
            val sampleLines = gui.getSampleLines()
            val maxWidth = (sampleLines.maxOfOrNull { textRenderer.getWidth(Text.literal(it)) } ?: 175) * scale
            val height = (sampleLines.size * (textRenderer.fontHeight + 2)) * scale
            
            if (mouseX >= x - 2 && mouseX <= x + maxWidth + 2 &&
                mouseY >= y - 2 && mouseY <= y + height + 2) {
                isDraggingGui = gui
                lastDraggedGui = gui
                dragOffsetX = (mouseX - x).toInt()
                dragOffsetY = (mouseY - y).toInt()
                return true
            }
        }
        
        return super.mouseClicked(click, doubled)
    }
    
    override fun mouseDragged(click: Click, deltaX: Double, deltaY: Double): Boolean {
        if (click.button() == 0 && isDraggingGui != null) {
            val newX = ((click.x() - dragOffsetX).toInt().coerceAtLeast(0))
            val newY = ((click.y() - dragOffsetY).toInt().coerceAtLeast(0))
            isDraggingGui!!.setX(newX).setY(newY)
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
    
    private fun saveGuiCoords(gui: FeeshGui) {
        PersistentDataManager.updateOverlayCoordsData(gui.getCoordsDataKey(), gui.getX(), gui.getY(), gui.getScale(), gui.getAlignment())
    }
}
