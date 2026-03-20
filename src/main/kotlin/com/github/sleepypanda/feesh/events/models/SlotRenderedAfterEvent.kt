package com.github.sleepypanda.feesh.events.models

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.slot.Slot

/**
 * Published when a slot is after drawn in a [HandledScreen] (container GUI).
 * Fired at the end of drawSlot, after the slot background and item are rendered,
 * so listeners can draw on top of the item (e.g. slot text).
 */
data class AfterSlotRenderedEvent(
    val drawContext: DrawContext,
    val slot: Slot,
    val screen: HandledScreen<*>
)
