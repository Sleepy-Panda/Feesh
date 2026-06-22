package com.github.sleepypanda.feesh.features.items.background

import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.settings.categories.Items
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents as DataComponentTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.Slot

object KatWrongPetsHighlighter : BaseBackgroundHighlighter() {

    private const val KAT_GUI_TITLE = "Pet Sitter"
    private const val PET_SLOT_INDEX = 13
    private const val CONFIRM_SLOT_INDEX = 22
    private const val KAT_HIGHLIGHT_COLOR = 0x80FF0000.toInt()
    private val MEGALODON_ITEM_NAME = "${EPIC}Megalodon"

    override val highlightColor: Int = KAT_HIGHLIGHT_COLOR

    fun init() {
        // Calling this ensures the object is initialized, and registered in base class.
    }

    override fun isEnabled(): Boolean = Items.katWrongPetsHighlighter

    override fun getItemStackBackgroundColor(stack: ItemStack, screen: AbstractContainerScreen<*>, slot: Slot): Int? {
        val title = screen.title.string.removeFormatting().trim()
        if (title != KAT_GUI_TITLE) return null

        if (slot.index != PET_SLOT_INDEX && slot.index != CONFIRM_SLOT_INDEX) return null

        if (slot.index == PET_SLOT_INDEX) {
            val itemName = stack.hoverName.getFormattedString()
            if (itemName.contains(MEGALODON_ITEM_NAME)) return highlightColor
            return null
        } else if (slot.index == CONFIRM_SLOT_INDEX) {
            if (itemColorCache.isEmpty()) return null
            val itemLore = stack.get(DataComponentTypes.LORE)?.lines()?.map { it?.getFormattedString() ?: "" } ?: emptyList()
            if (itemLore.any { it.contains(MEGALODON_ITEM_NAME) }) return highlightColor
            return null
        }

        return null
    }
}
