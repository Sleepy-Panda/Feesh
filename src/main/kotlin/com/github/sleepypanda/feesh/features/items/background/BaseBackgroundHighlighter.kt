package com.github.sleepypanda.feesh.features.items.background

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.SlotRenderedEvent
import com.github.sleepypanda.feesh.events.models.ScreenBeforeInitEvent
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

/**
 * Base class for all background highlighters that draw backgrounds under item icons in container GUIs.
 *
 * Responsibilities:
 * - Subscribe once to [SlotRenderedEvent] and fan out to all registered highlighters.
 * - Maintain a per-highlighter cache of "item stack identifier" -> highlight color (nullable).
 * - Clear caches when the active GUI (screen) changes.
 */
abstract class BaseBackgroundHighlighter {

    protected abstract val highlightColor: Int

    /**
     * Cache: item stack identifier -> resolved color or null (meaning "no highlight").
     * Cleared automatically on GUI (screen) change.
     */
    protected val itemColorCache: MutableMap<String, Int?> = mutableMapOf()

    constructor() {
        highlighters.add(this)
    }

    /**
     * Should this highlighter be active (e.g. world / settings).
     */
    protected abstract fun isEnabled(): Boolean

    /**
     * Assigns a highlight color for the given stack in the current context.
     *
     * Return:
     * - Int color to highlight with, or
     * - null if this highlighter should not highlight this stack.
     *
     * The result is cached per item stack identifier until GUI changes.
     */
    protected abstract fun getItemStackBackgroundColor(
        stack: ItemStack,
        screen: HandledScreen<*>,
        slot: Slot
    ): Int?

    private fun clearCache() {
        if (itemColorCache.isNotEmpty()) itemColorCache.clear()
    }

    companion object {
        private const val DEFAULT_SLOT_SIZE = 16

        // List of all specific background highlighters.
        private val highlighters: MutableList<BaseBackgroundHighlighter> = mutableListOf()

        init {
            EventBus.subscribe(SlotRenderedEvent::class, ::onSlotRendered)
            EventBus.subscribe(ScreenBeforeInitEvent::class, ::onScreenBeforeInit)
        }

        private fun onScreenBeforeInit(@Suppress("UNUSED_PARAMETER") event: ScreenBeforeInitEvent) {
            highlighters.forEach { it.clearCache() }
        }

        private fun onSlotRendered(event: SlotRenderedEvent) {
            if (highlighters.isEmpty()) return

            val enabledHighlighters = highlighters.filter { it.isEnabled() }
            if (enabledHighlighters.isEmpty()) return

            val screen = event.screen
            val slot = event.slot
            val stack = slot.stack ?: return
            if (stack.isEmpty) return

            val identifier = getStackIdentifier(stack)
            val context = event.drawContext

            for (highlighter in enabledHighlighters) {
                val cachedColor = highlighter.itemColorCache.getOrPut(identifier) {
                    highlighter.getItemStackBackgroundColor(stack, screen, slot)
                } ?: continue

                drawBackground(context, slot, cachedColor)
            }
        }

        private fun drawBackground(context: DrawContext, slot: Slot, color: Int) {
            val x = slot.x
            val y = slot.y
            context.fill(x, y, x + DEFAULT_SLOT_SIZE, y + DEFAULT_SLOT_SIZE, color)
        }

        /**
         * Calculates an item stack identifier used for caching.
         */
        private fun getStackIdentifier(stack: ItemStack): String {
            return stack.name.string + System.identityHashCode(stack)
        }
    }
}
