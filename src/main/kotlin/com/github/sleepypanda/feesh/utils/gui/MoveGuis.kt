package com.github.sleepypanda.feesh.utils.gui

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.features.overlays.SeaCreaturesTracker
import com.github.sleepypanda.feesh.features.overlays.LegionBobbingTimeTracker
import com.github.sleepypanda.feesh.features.overlays.JerryWorkshopTracker
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Click
import net.minecraft.client.input.KeyInput
import net.minecraft.text.Text
import java.awt.Color

data class GuiMapping(
    val settingGetter: () -> Boolean,
    val guiGetter: () -> FeeshGui?
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
        // SeaCreaturesTracker
        try {
            val seaCreaturesTrackerClass = SeaCreaturesTracker::class.java
            val guiField = seaCreaturesTrackerClass.getDeclaredField("gui")
            guiField.isAccessible = true
            val guiInstance = guiField.get(null) as? FeeshGui
            if (guiInstance != null) {
                guiMappings.add(GuiMapping(
                    settingGetter = guiInstance.getSettingsKey() ?: { false },
                    guiGetter = { guiInstance }
                ))
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to register SeaCreaturesTracker gui", e)
        }
        
        // LegionBobbingTimeTracker
        try {
            val legionTrackerClass = LegionBobbingTimeTracker::class.java
            val guiField = legionTrackerClass.getDeclaredField("gui")
            guiField.isAccessible = true
            val guiInstance = guiField.get(null) as? FeeshGui
            if (guiInstance != null) {
                guiMappings.add(GuiMapping(
                    settingGetter = guiInstance.getSettingsKey() ?: { false },
                    guiGetter = { guiInstance }
                ))
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to register LegionBobbingTimeTracker gui", e)
        }
        
        // JerryWorkshopTracker
        try {
            val jerryTrackerClass = JerryWorkshopTracker::class.java
            val guiField = jerryTrackerClass.getDeclaredField("gui")
            guiField.isAccessible = true
            val guiInstance = guiField.get(null) as? FeeshGui
            if (guiInstance != null) {
                guiMappings.add(GuiMapping(
                    settingGetter = guiInstance.getSettingsKey() ?: { false },
                    guiGetter = { guiInstance }
                ))
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to register JerryWorkshopTracker gui", e)
        }
        
        FeeshGui.getAllRegisteredGuis().forEach { gui ->
            if (gui.getX() == 0 && gui.getY() == 0) {
                gui.setX(10).setY(10)
            }
            gui.setScale(1.0f)
            gui.setAlignment(Alignment.LEFT)
        }
    }
    
    fun getEnabledGuis(): List<GuiMapping> {
        return guiMappings.filter { it.settingGetter() }
    }
}

class MoveGuisScreen : Screen(Text.literal("Feesh Move Guis")) {
    private val enabledGuis: List<GuiMapping> by lazy { MoveGuis.getEnabledGuis() }
    private var draggedGui: FeeshGui? = null
    private var dragOffsetX = 0
    private var dragOffsetY = 0
    private val color = Color(255, 255, 255, 255).rgb
    
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {       
        val textRenderer = client!!.textRenderer
        
        context.drawText(textRenderer, Text.literal("${YELLOW}Move / scale the GUIs using your mouse. Press ESC to exit."), 10, 20, color, true)
        
        enabledGuis.forEach { mapping ->
            val gui = mapping.guiGetter() ?: return@forEach
            val x = gui.getX()
            val y = gui.getY()
            
            val sampleLines = if (gui.getSampleLines().isNotEmpty()) gui.getSampleLines() else if (gui.getLines().isNotEmpty()) gui.getLines() else listOf("Sample Text")
            val maxWidth = sampleLines.maxOfOrNull { textRenderer.getWidth(Text.literal(it)) } ?: 100
            val height = sampleLines.size * (textRenderer.fontHeight + 2)
            
            // Рисуем полупрозрачный фон
            //context.fill(x - 2, y - 2, x + maxWidth + 2, y + height + 2, 0x40000000)
            
            gui.drawSample(context, textRenderer, client!!, x, y)
        }
        
        super.render(context, mouseX, mouseY, delta)
    }
    
    override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
        if (click.button() != 0) return super.mouseClicked(click, doubled)
        
        val mouseX = click.x()
        val mouseY = click.y()
        val textRenderer = client!!.textRenderer
        
        enabledGuis.forEach { mapping ->
            val gui = mapping.guiGetter() ?: return@forEach
            val x = gui.getX()
            val y = gui.getY()
            
            val sampleLines = if (gui.getSampleLines().isNotEmpty()) gui.getSampleLines() else if (gui.getLines().isNotEmpty()) gui.getLines() else listOf("Sample Text")
            val maxWidth = sampleLines.maxOfOrNull { textRenderer.getWidth(Text.literal(it)) } ?: 100
            val height = sampleLines.size * (textRenderer.fontHeight + 2)
            
            if (mouseX >= x - 2 && mouseX <= x + maxWidth + 2 &&
                mouseY >= y - 2 && mouseY <= y + height + 2) {
                draggedGui = gui
                dragOffsetX = (mouseX - x).toInt()
                dragOffsetY = (mouseY - y).toInt()
                return true
            }
        }
        
        return super.mouseClicked(click, doubled)
    }
    
    override fun mouseDragged(click: Click, deltaX: Double, deltaY: Double): Boolean {
        if (click.button() == 0 && draggedGui != null) {
            val newX = ((click.x() - dragOffsetX).toInt().coerceAtLeast(0))
            val newY = ((click.y() - dragOffsetY).toInt().coerceAtLeast(0))
            draggedGui!!.setX(newX).setY(newY)
            return true
        }
        return super.mouseDragged(click, deltaX, deltaY)
    }
    
    override fun mouseReleased(click: Click): Boolean {
        if (click.button() == 0) {
            draggedGui = null
        }
        return super.mouseReleased(click)
    }
    
    override fun keyPressed(input: KeyInput): Boolean {
        if (input.key() == 256) { // ESC
            client!!.setScreen(null)
            return true
        }
        return super.keyPressed(input)
    }
}
