package com.github.sleepypanda.feesh.events

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer

/*
 * This event is triggered after a Screen is rendered.
 * It is used to render overlays while a GUI is opened. Custom drawings are rendered on top of the background.
 */
data class ScreenPostRenderEvent(
    val drawContext: DrawContext,
    val textRenderer: TextRenderer,
    val mcClient: MinecraftClient,
    val screen: Screen,
    val mouseX: Int,
    val mouseY: Int,
    val delta: Float
)
