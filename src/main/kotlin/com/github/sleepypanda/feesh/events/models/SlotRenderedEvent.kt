package com.github.sleepypanda.feesh.events.models

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.slot.Slot

/**
 * Published when a slot is about to be drawn in a [HandledScreen] (container GUI).
 * Fired at the start of drawSlot, before the slot background and item are rendered,
 * so listeners can draw under the item (e.g. highlight background).
 */
data class SlotRenderedEvent(
    val drawContext: DrawContext,
    val slot: Slot,
    val screen: HandledScreen<*>
)
