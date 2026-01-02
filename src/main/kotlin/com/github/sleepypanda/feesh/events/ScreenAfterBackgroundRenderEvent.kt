package com.github.sleepypanda.feesh.events

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer

/*
 * This event is triggered after the background is rendered but before GUI elements.
 * It is used to render custom text in front of background but under Inventory GUI.
 */
data class ScreenAfterBackgroundRenderEvent(
    val drawContext: DrawContext,
    val textRenderer: TextRenderer,
    val mcClient: MinecraftClient,
    val screen: Screen,
    val mouseX: Int,
    val mouseY: Int,
    val delta: Float
)

