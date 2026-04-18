package com.github.sleepypanda.feesh.events.models

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

/**
 * Published when a slot is about to be drawn in a [AbstractContainerScreen] (container GUI).
 * Fired at the start of renderSlot, before the slot background and item are rendered,
 * so listeners can draw under the item (e.g. highlight background).
 */
data class BeforeSlotRenderedEvent(
    val drawContext: GuiGraphics,
    val slot: Slot,
    val screen: AbstractContainerScreen<*>
)
