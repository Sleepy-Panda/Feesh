package com.github.sleepypanda.feesh.features.items.tooltip

import com.github.sleepypanda.feesh.settings.categories.Items
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.ItemUtils
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

/**
 * This feature is used to remove the description lines of the tooltip for fishing rod parts (Hook, Line, Sinker).
*/
object CompactedRodPartTooltip : BaseTooltip() {

    private val ROD_PART_NAME_REGEX = Regex(
        """^(ථ|ꨃ|࿉) (Hook NONE|Line NONE|Sinker NONE|.+ Hook|.+ Line|.+ Sinker)$"""
    )

    fun init() {
        // Calling this ensures the object is initialized and registered in the base class.
    }

    override fun isEnabled(): Boolean = Items.compactRodPartTooltip

    override fun modifyTooltip(stack: ItemStack, lines: MutableList<Component>) {
        try {
            if (!ItemUtils.isFishingRod(stack)) return
            compactRodPartLines(lines)
        } catch (e: Exception) {}
    }

    private fun compactRodPartLines(lines: MutableList<Component>) {
        if (lines.isEmpty()) return
        val rodPartIndexes = lines.indices.filter { isRodPartName(lines[it]) }
        if (rodPartIndexes.isEmpty()) return

        val firstRodPartIndex = rodPartIndexes.first()
        val lastRodPartIndex = rodPartIndexes.last()
        val indexesToRemove = mutableSetOf<Int>()

        for (index in firstRodPartIndex..lastRodPartIndex) {
            if (!isRodPartName(lines[index])) {
                indexesToRemove.add(index)
            }
        }

        val firstLineIndexAfterLastRodPart = lines.indices.drop(lastRodPartIndex + 1).firstOrNull { index ->
            lines[index].getUnformattedString().trim().isEmpty()
        }

        if (firstLineIndexAfterLastRodPart != null) {
            for (index in (lastRodPartIndex + 1) until firstLineIndexAfterLastRodPart) {
                indexesToRemove.add(index)
            }
        }

        for (index in indexesToRemove.sortedDescending()) {
            lines.removeAt(index)
        }
    }

    private fun isRodPartName(text: Component): Boolean {
        val cleanText = text.getUnformattedString().trim()
        return !cleanText.isEmpty() && ROD_PART_NAME_REGEX.matches(cleanText)
    }
}
