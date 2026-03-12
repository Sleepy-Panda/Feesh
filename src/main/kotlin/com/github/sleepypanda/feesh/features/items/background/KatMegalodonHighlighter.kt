package com.github.sleepypanda.feesh.features.items

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.SlotRenderedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.slot.Slot

private const val KAT_GUI_TITLE = "Pet Sitter"
private val MEGALODON_ITEM_NAME = "${EPIC}Megalodon"
private const val SLOT_INDEX = 13
private const val SLOT_SIZE = 16
private const val HIGHLIGHT_COLOR = 0x80FF0000.toInt() // 50% opacity red (ARGB)

object KatMegalodonHighlighter {
    fun init() {
        EventBus.subscribe(SlotRenderedEvent::class, ::onSlotRendered)
    }

    private fun onSlotRendered(event: SlotRenderedEvent) {
        if (!WorldUtils.isInSkyblock()) return
        // TODO setting
        val screen = event.screen
        val drawContext = event.drawContext
        val slot = event.slot
        renderHighlight(screen, drawContext, slot)
    }

    private fun renderHighlight(screen: HandledScreen<*>, drawContext: DrawContext, slot: Slot) {
        // TODO Also highlight button under this one
        // TODO caching to not explode fps
        val title = screen.title.string.removeFormatting().trim()
        if (title != KAT_GUI_TITLE) return
        if (slot.index != SLOT_INDEX) return

        val stack = slot.stack
        if (stack.isEmpty) return

        val itemName = stack.name.getFormattedString()
        if (!itemName.contains(MEGALODON_ITEM_NAME)) return

        val x = slot.x
        val y = slot.y
        drawContext.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, HIGHLIGHT_COLOR)
    }
}
