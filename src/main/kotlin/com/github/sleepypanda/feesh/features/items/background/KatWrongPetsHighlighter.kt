package com.github.sleepypanda.feesh.features.items.background

import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.settings.categories.Items
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

object KatWrongPetsHighlighter : BaseBackgroundHighlighter() {

    private const val KAT_GUI_TITLE = "Pet Sitter"
    private const val PET_SLOT_INDEX = 13
    private const val CONFIRM_SLOT_INDEX = 22
    private const val KAT_HIGHLIGHT_COLOR = 0x80FF0000.toInt()
    private val MEGALODON_ITEM_NAME = "${EPIC}Megalodon"
    private val BABY_YETI_ITEM_NAME = "${EPIC}Baby Yeti"

    override val highlightColor: Int = KAT_HIGHLIGHT_COLOR

    fun init() {
        // Calling this ensures the object is initialized, and registered in base class.
    }

    override fun isEnabled(): Boolean {
        return WorldUtils.isInSkyblock() && Items.katWrongPetsHighlighter
    }

    override fun getItemStackBackgroundColor(stack: ItemStack, screen: HandledScreen<*>, slot: Slot): Int? {
        val title = screen.title?.string?.removeFormatting()?.trim() ?: ""
        if (title != KAT_GUI_TITLE) return null

        if (slot.index != PET_SLOT_INDEX && slot.index != CONFIRM_SLOT_INDEX) return null

        if (slot.index == PET_SLOT_INDEX) {
            val itemName = stack.name.getFormattedString()
            if (itemName.contains(MEGALODON_ITEM_NAME) || itemName.contains(BABY_YETI_ITEM_NAME)) return highlightColor
            return null
        } else if (slot.index == CONFIRM_SLOT_INDEX) {
            if (itemColorCache.isEmpty()) return null
            val itemLore = stack.get(DataComponentTypes.LORE)?.lines?.map { it.getFormattedString() } ?: emptyList()
            if (itemLore.any { it.contains(BABY_YETI_ITEM_NAME) || it.contains(MEGALODON_ITEM_NAME) }) return highlightColor
            return null
        }
        // TODO enabled highlighters to update dynamically instead of check every render event

        return null
    }
}
