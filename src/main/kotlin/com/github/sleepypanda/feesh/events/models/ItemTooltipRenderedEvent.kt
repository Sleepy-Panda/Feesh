package com.github.sleepypanda.feesh.events.models

import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

/**
 * Fired when an item tooltip is built, after vanilla lines were appended. Runs every frame!
 * @param stack The item stack being hovered.
 * @param lines Mutable tooltip lines.
 */
data class ItemTooltipRenderedEvent(
    val stack: ItemStack,
    val lines: MutableList<Component>,
)
