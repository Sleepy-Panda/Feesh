package com.github.sleepypanda.feesh.events.models

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

/**
 * Published when a slot is after drawn in a [AbstractContainerScreen] (container GUI).
 * Fired at the end of renderSlot, after the slot background and item are rendered,
 * so listeners can draw on top of the item (e.g. slot text).
 */
data class AfterSlotRenderedEvent(
    val drawContext: GuiGraphics,
    val slot: Slot,
    val screen: AbstractContainerScreen<*>
)
