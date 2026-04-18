package com.github.sleepypanda.feesh.features.items.slottext

import com.github.sleepypanda.feesh.features.items.slottext.SlotTextRendererManager
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.Slot

/**
 * Base class for slot text renderers that draw short text in slot corners.
 *
 * Responsibilities:
 * - Register once in [SlotTextRendererManager].
 * - Maintain a per-renderer cache of "item stack identifier" -> text (nullable).
 * - Clear caches when the active GUI (screen) changes.
 */
abstract class BaseSlotTextRenderer {

    /**
     * Cache: item stack identifier -> text or null (meaning "no text").
     * Cleared automatically when GUI (screen) changes.
     */
    val itemTextCache: MutableMap<String, String?> = mutableMapOf()

    init {
        SlotTextRendererManager.register(this)
    }

    /**
     * Should this renderer be active in the current context.
     */
    abstract fun isEnabled(): Boolean

    /**
     * Resolves text for a stack in current context.
     *
     * Return:
     * - String to draw in slot corner, or
     * - null if nothing should be drawn for this stack.
     *
     * The result is cached per item stack identifier until GUI changes.
     */
    abstract fun getItemStackSlotText(
        stack: ItemStack,
        screen: AbstractContainerScreen<*>,
        slot: Slot
    ): String?

    /**
     * Override to change text color for this renderer.
     */
    open fun getTextColor(): Int = 0xFFFFFF

    /**
     * Override to disable text shadow for this renderer.
     */
    open fun drawShadow(): Boolean = true

    fun clearCache() {
        if (itemTextCache.isNotEmpty()) itemTextCache.clear()
    }
}
