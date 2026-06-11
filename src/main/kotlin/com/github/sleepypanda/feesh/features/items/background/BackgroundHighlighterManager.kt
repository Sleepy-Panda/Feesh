package com.github.sleepypanda.feesh.features.items.background

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ScreenBeforeInitEvent
import com.github.sleepypanda.feesh.events.models.BeforeSlotRenderedEvent
import com.github.sleepypanda.feesh.utils.WorldUtils
//#if MC >= 26.1
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor as GuiGraphics
//#else
import net.minecraft.client.gui.GuiGraphics
//#endif
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.Slot

/**
 * Coordinates all [BaseBackgroundHighlighter] instances:
 * - subscribes to BeforeSlotRenderedEvent and ScreenBeforeInitEvent;
 * - keeps all registered highlighters;
 * - tracks enabled highlighters (refreshed on init and settings change);
 * - clears per-screen caches and draws backgrounds under item icons.
 */
object BackgroundHighlighterManager {

    private const val DEFAULT_SLOT_SIZE = 16

    private val highlighters: MutableList<BaseBackgroundHighlighter> = mutableListOf()
    private val enabledHighlighters: MutableList<BaseBackgroundHighlighter> = mutableListOf()

    fun init() {
        EventBus.subscribe(BeforeSlotRenderedEvent::class, ::onSlotRendered)
        EventBus.subscribe(ScreenBeforeInitEvent::class, ::onScreenBeforeInit)
        refreshEnabledHighlighters()
    }

    /**
     * Register a new background highlighter.
     * @param highlighter The highlighter to register.
     */
    fun register(highlighter: BaseBackgroundHighlighter) {
        highlighters.add(highlighter)
    }

    fun refreshEnabledHighlighters() {
        enabledHighlighters.clear()
        enabledHighlighters.addAll(highlighters.filter { it.isEnabled() })
    }

    private fun onScreenBeforeInit(@Suppress("UNUSED_PARAMETER") event: ScreenBeforeInitEvent) {
        highlighters.forEach { it.clearCache() }
    }

    private fun onSlotRendered(event: BeforeSlotRenderedEvent) {
        if (!WorldUtils.isInSkyblock()) return
        if (enabledHighlighters.isEmpty()) return

        val screen = event.screen
        val slot = event.slot
        val stack = slot.item
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

    private fun drawBackground(context: GuiGraphics, slot: Slot, color: Int) {
        val x = slot.x
        val y = slot.y
        context.fill(x, y, x + DEFAULT_SLOT_SIZE, y + DEFAULT_SLOT_SIZE, color)
    }

    /**
     * Calculates an item stack identifier used for caching.
     */
    private fun getStackIdentifier(stack: ItemStack): String {
        return stack.hoverName.string + System.identityHashCode(stack)
    }
}
