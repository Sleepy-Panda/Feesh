package com.github.sleepypanda.feesh.features.items.background

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ScreenBeforeInitEvent
import com.github.sleepypanda.feesh.events.models.BeforeSlotRenderedEvent
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

/**
 * Coordinates all [BaseBackgroundHighlighter] instances:
 * - subscribes to BeforeSlotRenderedEvent and ScreenBeforeInitEvent;
 * - keeps the list of active highlighters;
 * - clears per-screen caches and draws backgrounds under item icons.
 */
object BackgroundHighlighterManager {

    private const val DEFAULT_SLOT_SIZE = 16

    // List of all specific background highlighters.
    private val highlighters: MutableList<BaseBackgroundHighlighter> = mutableListOf()

    fun init() {
        EventBus.subscribe(BeforeSlotRenderedEvent::class, ::onSlotRendered)
        EventBus.subscribe(ScreenBeforeInitEvent::class, ::onScreenBeforeInit)
    }

    /**
     * Register a new background highlighter.
     * @param highlighter The highlighter to register.
     */
    fun register(highlighter: BaseBackgroundHighlighter) {
        highlighters.add(highlighter)
    }

    private fun onScreenBeforeInit(@Suppress("UNUSED_PARAMETER") event: ScreenBeforeInitEvent) {
        highlighters.forEach { it.clearCache() }
    }

    private fun onSlotRendered(event: BeforeSlotRenderedEvent) {
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
