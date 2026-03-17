package com.github.sleepypanda.feesh.features.items.background

import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.settings.categories.Items
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

object TrashBooksHighlighter : BaseBackgroundHighlighter() {

    private const val TRASH_BOOKS_HIGHLIGHT_COLOR = 0x80FF0000.toInt()
    override val highlightColor: Int = TRASH_BOOKS_HIGHLIGHT_COLOR

    private var trashSubtitles = emptyList<String>()

    fun init() {
        setSearchSubtitles()
    }

    override fun isEnabled(): Boolean {
        return WorldUtils.isInSkyblock() && Items.trashBooksHighlighter
    }

    override fun getItemStackBackgroundColor(stack: ItemStack, screen: HandledScreen<*>, slot: Slot): Int? {
        if (trashSubtitles.isEmpty()) return null

        val itemName = stack.name.string.removeFormatting()
        if (itemName != "Enchanted Book") return null

        val lore = stack.get(DataComponentTypes.LORE)?.lines?.map { it.string } ?: emptyList()
        val bookName = lore.firstOrNull()?.removeFormatting()?.trim() ?: return null
        if (bookName.isEmpty()) return null

        if (trashSubtitles.any { bookName.equals(it, ignoreCase = true) }) return highlightColor

        return null
    }

    fun setSearchSubtitles() {
        trashSubtitles = Items.trashBooksHighlighterSubtitles[0].split(",").map { it.trim() }
    }
}
