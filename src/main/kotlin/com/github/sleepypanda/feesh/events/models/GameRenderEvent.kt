package com.github.sleepypanda.feesh.events.models

import net.minecraft.client.gui.GuiGraphics
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
