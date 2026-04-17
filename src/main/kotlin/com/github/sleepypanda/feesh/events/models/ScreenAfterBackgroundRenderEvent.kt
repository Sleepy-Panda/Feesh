package com.github.sleepypanda.feesh.events.models

import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font

/*
 * This event is triggered after the background is rendered but before GUI elements.
 * It is used to render custom text in front of background but under Inventory GUI.
 */
data class ScreenAfterBackgroundRenderEvent(
    val drawContext: GuiGraphics,
    val textRenderer: Font,
    val mcClient: Minecraft,
    val screen: Screen,
    val mouseX: Int,
    val mouseY: Int,
    val delta: Float
)
