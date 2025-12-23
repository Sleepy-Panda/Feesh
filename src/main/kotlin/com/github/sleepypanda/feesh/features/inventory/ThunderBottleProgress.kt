package com.github.sleepypanda.feesh.features.inventory

import com.github.sleepypanda.feesh.FeeshMod
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

object ThunderBottleProgress {
    fun init() {
        ScreenEvents.AFTER_INIT.register { client, screen, scaledWidth, scaledHeight ->
            if (screen is HandledScreen<*>) {
                ScreenEvents.afterRender(screen).register { screen, drawContext, mouseX, mouseY, tickDelta ->
                    renderBottleIndicators(screen as HandledScreen<*>, drawContext)
                }
            }
        }
    }

    private fun renderBottleIndicators(screen: HandledScreen<*>, drawContext: DrawContext) {
        val handler = screen.getScreenHandler() ?: return
        val textRenderer = FeeshMod.mc.textRenderer ?: return

        for (slot in handler.slots) {
            val stack = slot.stack
            if (!stack.isEmpty && stack.name.string.contains("bottle", ignoreCase = true)) {
                val x = slot.x // TODO screen.x + 
                val y = slot.y
                drawContext.drawText(textRenderer, "B", x + 10, y + 2, 0xFFFFFFF, true)
            }
        }
    }
}