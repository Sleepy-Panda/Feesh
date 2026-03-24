package com.github.sleepypanda.feesh.features.items.background

import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.settings.categories.Items
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

object TrashBooksHighlighter : BaseBackgroundHighlighter() {

    private const val TRASH_BOOKS_HIGHLIGHT_COLOR = 0x80FF0000.toInt()
    override val highlightColor: Int = TRASH_BOOKS_HIGHLIGHT_COLOR

    private var trashBookNames = emptyList<String>()

    fun init() {
        setSearchBookNames()
    }

    override fun isEnabled(): Boolean {
        return WorldUtils.isInSkyblock() && Items.trashBooksHighlighter
    }

    override fun getItemStackBackgroundColor(stack: ItemStack, screen: HandledScreen<*>, slot: Slot): Int? {
        if (trashBookNames.isEmpty()) return null

        val itemName = stack.name.string.removeFormatting()
        if (itemName != "Enchanted Book") return null

        val bookName = ItemUtils.getEnchantedBookName(stack) ?: return null
        if (bookName.isEmpty()) return null

        if (trashBookNames.any { bookName.equals(it, ignoreCase = true) }) return highlightColor

        return null
    }

    fun setSearchBookNames() {
        if (Items.trashBooksHighlighterNames.isEmpty()) {
            trashBookNames = emptyList()
            return
        }
        
        trashBookNames = Items.trashBooksHighlighterNames[0].split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
