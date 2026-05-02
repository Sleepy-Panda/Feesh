package com.github.sleepypanda.feesh.events.models

import net.minecraft.client.gui.Font
//#if MC >= 26.1
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor as GuiGraphics
//#else
import net.minecraft.client.gui.GuiGraphics
//#endif
import net.minecraft.world.item.ItemStack

/**
 * Published after each hotbar item is drawn, including when no container screen is open.
 */
data class AfterHotbarSlotRenderedEvent(
    val drawContext: GuiGraphics,
    val textRenderer: Font,
    val hotbarSlotIndex: Int,
    val slotX: Int,
    val slotY: Int,
    val stack: ItemStack
)
