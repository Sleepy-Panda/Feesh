package com.github.sleepypanda.feesh.events.models

//#if MC >= 26.1
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor as GuiGraphics
//#else
import net.minecraft.client.gui.GuiGraphics
//#endif
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.DeltaTracker

/*
 * This event is triggered during in-game HUD render, right before TabList (Players list) is drawn.
 * Use it to draw custom content UNDER TabList and the rest of the HUD.
 */
data class GameRenderEvent(
    val drawContext: GuiGraphics,
    val textRenderer: Font,
    val mcClient: Minecraft,
    val renderTickCounter: DeltaTracker
)
