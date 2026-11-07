package com.github.sleepypanda.feesh.features.items.slottext

import com.github.sleepypanda.feesh.features.items.slottext.models.SlotTextLine
import com.github.sleepypanda.feesh.features.items.slottext.models.SlotTextPosition
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.Slot

/**
 * Base class for slot text renderers that draw short text in slot corners.
 *
 * Responsibilities:
 * - Register once in [SlotTextRendererManager].
 * - Maintain a per-renderer cache of "item stack identifier" -> lines (nullable).
 * - Clear caches when the active GUI (screen) changes.
 */
abstract class BaseSlotTextRenderer {

    /**
     * Cache: item stack identifier -> slot text lines or null (meaning "no text").
     * Cleared automatically when GUI (screen) changes.
     */
    val itemTextCache: MutableMap<String, List<SlotTextLine>?> = mutableMapOf()

    init {
        SlotTextRendererManager.register(this)
    }

    /**
     * Should this renderer be active in the current context.
     */
    abstract fun isEnabled(): Boolean

    /**
     * Resolves one or more slot text lines for a stack in current context.
     *
     * Return:
     * - Lines to draw in the slot, or
     * - null if nothing should be drawn for this stack.
     *
     * The result is cached per item stack identifier until GUI changes.
     */
    abstract fun getItemStackSlotLines(
        stack: ItemStack,
        screen: AbstractContainerScreen<*>,
        slot: Slot
    ): List<SlotTextLine>?

    /**
     * Override to disable text shadow for this renderer.
     */
    open fun drawShadow(): Boolean = true

    /**
     * Override to change where the text is positioned inside the slot.
     */
    open fun getPosition(): SlotTextPosition = SlotTextPosition.BOTTOM_LEFT

    /**
     * Override to change the text scale for this renderer.
     */
    open fun getTextScale(): Float = 0.7f

    fun clearCache() {
        if (itemTextCache.isNotEmpty()) itemTextCache.clear()
    }
}
