package com.github.sleepypanda.feesh.events.models

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.RenderTickCounter

/*
 * This event is triggered during in-game HUD render, right before TabList (Players list) is drawn.
 * Use it to draw custom content UNDER TabList and the rest of the HUD.
 */
data class GameRenderEvent(
    val drawContext: DrawContext,
    val textRenderer: TextRenderer,
    val mcClient: MinecraftClient,
    val renderTickCounter: RenderTickCounter
)
