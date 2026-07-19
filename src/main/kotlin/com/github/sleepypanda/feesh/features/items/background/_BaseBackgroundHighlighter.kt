package com.github.sleepypanda.feesh.features.items.background

import com.github.sleepypanda.feesh.features.items.background.BackgroundHighlighterManager
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.Slot

/**
 * Base class for all background highlighters that draw backgrounds under item icons in container GUIs.
 *
 * Responsibilities:
 * - Subscribe once to [BeforeSlotRenderedEvent] and fan out to all registered highlighters.
 * - Maintain a per-highlighter cache of "item stack identifier" -> highlight color (nullable).
 * - Clear caches when the active GUI (screen) changes.
 */
abstract class BaseBackgroundHighlighter {

    protected abstract val highlightColor: Int

    /**
     * Cache: item stack identifier -> resolved color or null (meaning "no highlight").
     * Cleared automatically on GUI (screen) change.
     */
    val itemColorCache: MutableMap<String, Int?> = mutableMapOf()

    init {
        BackgroundHighlighterManager.register(this)
    }

    /**
     * Should this highlighter be active (e.g. world / settings).
     */
    abstract fun isEnabled(): Boolean

    /**
     * Assigns a highlight color for the given stack in the current context.
     *
     * Return:
     * - Int color to highlight with, or
     * - null if this highlighter should not highlight this stack.
     *
     * The result is cached per item stack identifier until GUI changes.
     */
    abstract fun getItemStackBackgroundColor(
        stack: ItemStack,
        screen: AbstractContainerScreen<*>,
        slot: Slot
    ): Int?

    fun clearCache() {
        if (itemColorCache.isNotEmpty()) itemColorCache.clear()
    }
}
