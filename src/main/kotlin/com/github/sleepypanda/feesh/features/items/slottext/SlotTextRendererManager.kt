package com.github.sleepypanda.feesh.features.items.slottext

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ScreenBeforeInitEvent
import com.github.sleepypanda.feesh.events.models.AfterSlotRenderedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.client.gui.Font
//#if MC >= 26.1
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor as GuiGraphics
//#else
import net.minecraft.client.gui.GuiGraphics
//#endif
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.Slot

/**
 * Coordinates all [BaseSlotTextRenderer] instances:
 * - subscribes to AfterSlotRenderedEvent and ScreenBeforeInitEvent;
 * - keeps all registered renderers;
 * - tracks enabled renderers (refreshed on init and settings change);
 * - clears per-screen caches;
 * - draws slot text;
 */
object SlotTextRendererManager {

    private const val DEFAULT_SLOT_SIZE = 16
    private const val SLOT_TEXT_SCALE = 0.7f

    private val renderers: MutableList<BaseSlotTextRenderer> = mutableListOf()
    private val enabledRenderers: MutableList<BaseSlotTextRenderer> = mutableListOf()

    private fun drawStringCompat(context: GuiGraphics, textRenderer: Font, text: String, x: Int, y: Int, color: Int, shadow: Boolean) {
        //#if MC >= 26.1
        //$$ context.text(textRenderer, text, x, y, color, shadow)
        //#else
        context.drawString(textRenderer, text, x, y, color, shadow)
        //#endif
    }

    fun init() {
        EventBus.subscribe(AfterSlotRenderedEvent::class, ::onSlotRendered)
        EventBus.subscribe(ScreenBeforeInitEvent::class, ::onScreenBeforeInit)
        refreshEnabledRenderers()
    }

    fun register(renderer: BaseSlotTextRenderer) {
        renderers.add(renderer)
    }

    fun refreshEnabledRenderers() {
        enabledRenderers.clear()
        enabledRenderers.addAll(renderers.filter { it.isEnabled() })
    }

    private fun onScreenBeforeInit(@Suppress("UNUSED_PARAMETER") event: ScreenBeforeInitEvent) {
        renderers.forEach { it.clearCache() }
    }

    private fun onSlotRendered(event: AfterSlotRenderedEvent) {
        if (!WorldUtils.isInSkyblock()) return
        if (enabledRenderers.isEmpty()) return

        val textRenderer = FeeshMod.mc.font
        val screen = event.screen
        val slot = event.slot
        val stack = slot.item
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
        drawStringCompat(context, textRenderer, text, (x / SLOT_TEXT_SCALE).toInt(), (y / SLOT_TEXT_SCALE).toInt(), color, shadow)
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
        drawStringCompat(context, textRenderer, text, (x / SLOT_TEXT_SCALE).toInt(), (y / SLOT_TEXT_SCALE).toInt(), color, shadow)
        context.pose().popMatrix()
    }

    private fun getStackIdentifier(stack: ItemStack): String {
        return stack.hoverName.getUnformattedString() + System.identityHashCode(stack)
    }
}
