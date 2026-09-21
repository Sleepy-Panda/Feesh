package com.github.sleepypanda.feesh.features.items.slottext

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ScreenBeforeInitEvent
import com.github.sleepypanda.feesh.events.models.AfterSlotRenderedEvent
import com.github.sleepypanda.feesh.features.items.slottext.models.SlotTextLine
import com.github.sleepypanda.feesh.features.items.slottext.models.SlotTextPosition
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor as GuiGraphics
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

    private val renderers: MutableList<BaseSlotTextRenderer> = mutableListOf()
    private val enabledRenderers: MutableList<BaseSlotTextRenderer> = mutableListOf()

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
        clearAllCaches()
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
            val lines = renderer.itemTextCache.getOrPut(identifier) {
                renderer.getItemStackSlotLines(stack, screen, slot)
            } ?: continue

            drawSlotText(
                context,
                textRenderer,
                slot,
                lines,
                renderer.drawShadow(),
                renderer.getPosition(),
                renderer.getTextScale()
            )
        }
    }

    private fun clearAllCaches() {
        renderers.forEach { it.clearCache() }
    }

    private fun drawSlotText(
        context: GuiGraphics,
        textRenderer: Font,
        slot: Slot,
        lines: List<SlotTextLine>,
        shadow: Boolean,
        position: SlotTextPosition,
        scale: Float
    ) {
        if (lines.isEmpty()) return

        val scaledTextHeight = textRenderer.lineHeight * scale

        context.pose().pushMatrix()
        context.pose().scale(scale, scale)

        lines.forEachIndexed { index, line ->
            val x = slot.x + 1
            val y = when (position) {
                SlotTextPosition.TOP_LEFT -> slot.y + 1 + index * scaledTextHeight
                SlotTextPosition.BOTTOM_LEFT ->
                    slot.y + 1 + DEFAULT_SLOT_SIZE - scaledTextHeight * (lines.size - index)
            }

            drawStringCompat(
                context,
                textRenderer,
                line,
                (x / scale).toInt(),
                (y / scale).toInt(),
                shadow
            )
        }

        context.pose().popMatrix()
    }

    private fun drawStringCompat(
        context: GuiGraphics,
        textRenderer: Font,
        line: SlotTextLine,
        x: Int,
        y: Int,
        shadow: Boolean
    ) {
        context.text(textRenderer, line.component, x, y, line.color, shadow)
    }

    private fun getStackIdentifier(stack: ItemStack): String {
        return stack.hoverName.getUnformattedString() + System.identityHashCode(stack)
    }
}
