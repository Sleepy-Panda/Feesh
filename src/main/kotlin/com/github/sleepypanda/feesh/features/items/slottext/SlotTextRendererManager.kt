package com.github.sleepypanda.feesh.features.items.slottext

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ScreenBeforeInitEvent
import com.github.sleepypanda.feesh.events.models.AfterSlotRenderedEvent
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.Slot

/**
 * Coordinates all [BaseSlotTextRenderer] instances:
 * - subscribes to AfterSlotRenderedEvent and ScreenBeforeInitEvent;
 * - keeps all registered renderers;
 * - clears per-screen caches;
 * - draws slot text;
 */
object SlotTextRendererManager {

    private const val DEFAULT_SLOT_SIZE = 16
    private const val SLOT_TEXT_SCALE = 0.7f

    private val renderers: MutableList<BaseSlotTextRenderer> = mutableListOf()

    fun init() {
        EventBus.subscribe(AfterSlotRenderedEvent::class, ::onSlotRendered)
        EventBus.subscribe(ScreenBeforeInitEvent::class, ::onScreenBeforeInit)
    }

    fun register(renderer: BaseSlotTextRenderer) {
        renderers.add(renderer)
    }

    private fun onScreenBeforeInit(@Suppress("UNUSED_PARAMETER") event: ScreenBeforeInitEvent) {
        renderers.forEach { it.clearCache() }
    }

    private fun onSlotRendered(event: AfterSlotRenderedEvent) {
        if (renderers.isEmpty()) return

        val enabledRenderers = renderers.filter { it.isEnabled() }
        if (enabledRenderers.isEmpty()) return

        val textRenderer = FeeshMod.mc.font ?: return
        val screen = event.screen
        val slot = event.slot
        val stack = slot.item ?: return
        if (stack.isEmpty) return

        val identifier = getStackIdentifier(stack)
        val context = event.drawContext

        for (renderer in enabledRenderers) {
            val text = renderer.itemTextCache.getOrPut(identifier) {
                renderer.getItemStackSlotText(stack, screen, slot)
            } ?: continue

            drawBottomLeftText(context, textRenderer, slot, text, renderer.getTextColor(), renderer.drawShadow())
        }
    }

    private fun drawBottomLeftText(
        context: GuiGraphics,
        textRenderer: Font,
        slot: Slot,
        text: String,
        color: Int,
        shadow: Boolean
    ) {
        val scaledTextHeight = textRenderer.lineHeight * SLOT_TEXT_SCALE
        val x = slot.x + 1
        val y = slot.y + DEFAULT_SLOT_SIZE - scaledTextHeight

        context.pose().pushMatrix()
        context.pose().scale(SLOT_TEXT_SCALE, SLOT_TEXT_SCALE)
        context.drawString(
            textRenderer,
            text,
            (x / SLOT_TEXT_SCALE).toInt(),
            (y / SLOT_TEXT_SCALE).toInt(),
            color,
            shadow
        )
        context.pose().popMatrix()
    }

    private fun drawBottomRightText(
        context: GuiGraphics,
        textRenderer: Font,
        slot: Slot,
        text: String,
        color: Int,
        shadow: Boolean
    ) {
        val scaledTextWidth = textRenderer.width(text) * SLOT_TEXT_SCALE
        val scaledTextHeight = textRenderer.lineHeight * SLOT_TEXT_SCALE
        val x = slot.x + DEFAULT_SLOT_SIZE - scaledTextWidth - 1
        val y = slot.y + DEFAULT_SLOT_SIZE - scaledTextHeight

        context.pose().pushMatrix()
        context.pose().scale(SLOT_TEXT_SCALE, SLOT_TEXT_SCALE)
        context.drawString(
            textRenderer,
            text,
            (x / SLOT_TEXT_SCALE).toInt(),
            (y / SLOT_TEXT_SCALE).toInt(),
            color,
            shadow
        )
        context.pose().popMatrix()
    }

    private fun getStackIdentifier(stack: ItemStack): String {
        return stack.hoverName.string + System.identityHashCode(stack)
    }
}
