package com.github.sleepypanda.feesh.utils.gui

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.enums.Alignment
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import net.minecraft.client.gui.screens.Screen
//#if MC >= 26.1
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor as GuiGraphics
//#else
import net.minecraft.client.gui.GuiGraphics
//#endif
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import java.awt.Color
import java.util.Date

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
        if (!WorldUtils.isInSkyblock()) {
            ChatUtils.sendLocalChat("${RED}You must be on Hypixel Skyblock to use this command!", true)
            return
        }

        val mc = FeeshMod.mc
        FeeshMod.mc.schedule {
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

class MoveGuisScreen : Screen(Component.literal("Feesh Move Guis")) {
    private val enabledGuis: List<GuiMapping> by lazy { MoveGuis.getEnabledGuis() }
    private var isDraggingGui: FeeshGui? = null
    private var dragOffsetX = 0
    private var dragOffsetY = 0
    private var lastDraggedGui: FeeshGui? = null
    private val color = Color(255, 255, 255, 255).rgb
    
    //#if MC >= 26.1
    //$$ override fun extractRenderState(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
    //#else
    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
    //#endif
        val mc = minecraft ?: return
        val textRenderer = mc.font
        
        val hint1 = Component.literal("${RED}${BOLD}Enable GUIs in settings to see them here!")
        val x1 = mc.window.guiScaledWidth / 2 - textRenderer.width(hint1) / 2
        drawStringCompat(context, textRenderer, hint1, x1, 10, color, true)
        val hint2 = Component.literal("${YELLOW}Move them using your mouse. Press +/- or scroll to scale. Press 0 to change alignment. Press ESC to exit.")
        val x2 = mc.window.guiScaledWidth / 2 - textRenderer.width(hint2) / 2
        drawStringCompat(context, textRenderer, hint2, x2, 20, color, true)
        
        enabledGuis.forEach { mapping ->
            val gui = mapping.gui           
            gui.drawSample(context, textRenderer, mc)
        }
        
        //#if MC < 26.1
        super.render(context, mouseX, mouseY, delta)
        //#endif
    }

    private fun drawStringCompat(context: GuiGraphics, textRenderer: net.minecraft.client.gui.Font, text: Component, x: Int, y: Int, color: Int, shadow: Boolean) {
        //#if MC >= 26.1
        //$$ context.text(textRenderer, text, x, y, color, shadow)
        //#else
        context.drawString(textRenderer, text, x, y, color, shadow)
        //#endif
    }
    
    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, doubled: Boolean): Boolean {
        if (mouseButtonEvent.button() != 0) return super.mouseClicked(mouseButtonEvent, doubled)
        val mc = minecraft ?: return super.mouseClicked(mouseButtonEvent, doubled)
        val textRenderer = mc.font
        val mouseX = mouseButtonEvent.x()
        val mouseY = mouseButtonEvent.y()
        
        enabledGuis.forEach { mapping ->
            val gui = mapping.gui
            
            val isInSample = gui.isInSample(textRenderer, mc, mouseX, mouseY)
            if (isInSample) {
                isDraggingGui = gui
                lastDraggedGui = gui
                
                // Calculate left edge for drag offset
                val linesToUse = gui.getSampleLines()
                val maxWidth = linesToUse.maxOfOrNull { textRenderer.width(Component.literal(it)) } ?: 0
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
        
        return super.mouseClicked(mouseButtonEvent, doubled)
    }
    
    override fun mouseDragged(mouseButtonEvent: MouseButtonEvent, deltaX: Double, deltaY: Double): Boolean {
        if (mouseButtonEvent.button() == 0 && isDraggingGui != null) {
            val gui = isDraggingGui!!
            val mc = minecraft ?: return super.mouseDragged(mouseButtonEvent, deltaX, deltaY)
            val textRenderer = mc.font
            val mouseX = mouseButtonEvent.x()
            val mouseY = mouseButtonEvent.y()
            
            // Calculate the left edge of the overlay based on current alignment
            val linesToUse = gui.getSampleLines()
            val maxWidth = linesToUse.maxOfOrNull { textRenderer.width(Component.literal(it)) } ?: 0
            
            // Calculate new left edge from mouse position
            val newLeftEdge = (mouseX - dragOffsetX).toInt().coerceAtLeast(0)
            
            // Convert new left edge back to x coordinate based on alignment
            val newX = when (gui.getAlignment()) {
                Alignment.LEFT -> newLeftEdge
                Alignment.CENTER -> newLeftEdge + maxWidth / 2
                Alignment.RIGHT -> newLeftEdge + maxWidth
            }
            
            val newY = ((mouseY - dragOffsetY).toInt().coerceAtLeast(0))
            gui.setX(newX).setY(newY)
            return true
        }
        return super.mouseDragged(mouseButtonEvent, deltaX, deltaY)
    }
    
    override fun mouseReleased(mouseButtonEvent: MouseButtonEvent): Boolean {
        if (mouseButtonEvent.button() == 0) {
            if (isDraggingGui != null) {
                saveGuiCoords(isDraggingGui!!)
            }
            isDraggingGui = null
        }
        return super.mouseReleased(mouseButtonEvent)
    }
    
    override fun onClose() {
        enabledGuis.forEach { mapping ->
            val gui = mapping.gui
            saveGuiCoords(gui)
        }
        super.onClose()
    }
    
    override fun keyPressed(keyEvent: KeyEvent): Boolean {
        val mc = minecraft ?: return super.keyPressed(keyEvent)
        val keyCode = keyEvent.key()
        if (keyCode == 256) { // ESC
            mc.setScreen(null)
            return true
        }
        
        if (lastDraggedGui != null) {
            when (keyCode) {
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
        
        return super.keyPressed(keyEvent)
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
        val mc = minecraft ?: return
        val textRenderer = mc.font
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
