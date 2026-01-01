package com.github.sleepypanda.feesh.utils.gui

import com.github.sleepypanda.feesh.events.GameRenderEvent
import com.github.sleepypanda.feesh.events.ScreenPostRenderEvent
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.text.Text
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import java.awt.Color

enum class Alignment {
    LEFT,
    RIGHT,
    CENTER
}

class FeeshGui {
    private var x: Int = 0
    private var y: Int = 0
    private var scale: Float = 1.0f
    private var alignment: Alignment = Alignment.LEFT
    private var condition: () -> Boolean = { true }
    private var isClickable: Boolean = false
    private var lines: List<String> = emptyList()
    private var sampleLines: List<String> = emptyList()

    private var color: Int = Color(255, 255, 255, 255).rgb

    constructor() {
        EventBus.subscribe(GameRenderEvent::class, { event -> draw(event.drawContext, event.textRenderer, event.mcClient) })
        EventBus.subscribe(ScreenPostRenderEvent::class, ::postDraw)
    }

    fun setX(x: Int): FeeshGui {
        this.x = x
        return this
    }

    fun setY(y: Int): FeeshGui {
        this.y = y
        return this
    }

    fun setScale(scale: Float): FeeshGui {
        this.scale = scale
        return this
    }

    fun setAlignment(alignment: Alignment): FeeshGui {
        this.alignment = alignment
        return this
    }

    fun setCondition(condition: () -> Boolean): FeeshGui {
        this.condition = condition
        return this
    }

    fun setClickable(isClickable: Boolean): FeeshGui {
        this.isClickable = isClickable
        return this
    }

    fun setLines(lines: List<String>): FeeshGui {
        this.lines = lines
        return this
    }

    fun clearLines(): FeeshGui {
        this.lines = emptyList()
        return this
    }

    fun setSampleLines(sampleLines: List<String>): FeeshGui {
        this.sampleLines = sampleLines
        return this
    }

    fun draw(drawContext: DrawContext, textRenderer: TextRenderer, mcClient: MinecraftClient) {
        if (lines.isEmpty()) return
        if (!WorldUtils.isInSkyblock()) return
        if (!condition()) return

        drawContext.matrices.pushMatrix()
        drawContext.matrices.scale(scale, scale)

        val screenWidth = mcClient.window.scaledWidth
        val scaledScreenWidth = (screenWidth / scale).toInt()

        var currentY = y

        for (line in lines) {
            val text = Text.literal(line)
            val textWidth = textRenderer.getWidth(text)
            
            val actualX = when (alignment) {
                Alignment.LEFT -> x
                Alignment.RIGHT -> scaledScreenWidth - textWidth - x
                Alignment.CENTER -> (scaledScreenWidth - textWidth) / 2 + x
            }

            drawContext.drawText(textRenderer, text, actualX, currentY, color, true)
            currentY += textRenderer.fontHeight + 2
        }

        drawContext.matrices.popMatrix()
    }

    fun postDraw(event: ScreenPostRenderEvent) { // Draw in front of background when Inventory GUI is opened
        if (!isClickable) return
        if (event.screen !is InventoryScreen) return

        draw(event.drawContext, event.textRenderer, event.mcClient)
    }
}
