package com.github.sleepypanda.feesh.events

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.RenderTickCounter

/*
 * This event is triggered when the in-game HUD is rendered.
 * Custom drawings are rendered under the background when a GUI (e.g. Inventory) is opened.
 */
data class GameRenderEvent(
    val drawContext: DrawContext,
    val textRenderer: TextRenderer,
    val mcClient: MinecraftClient,
    val renderTickCounter: RenderTickCounter
)
