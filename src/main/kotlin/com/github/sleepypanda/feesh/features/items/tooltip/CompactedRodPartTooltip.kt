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
        """^. (Hook NONE|Line NONE|Sinker NONE|.+ Hook|.+ Line|.+ Sinker)$"""
    )

    fun init() {
        // Calling this ensures the object is initialized and registered in the base class.
    }

    override fun isEnabled(): Boolean = Items.compactRodPartTooltip

    override fun modifyTooltip(stack: ItemStack, lines: MutableList<Component>) {
        try {
            if (!ItemUtils.isFishingRod(stack)) return
    
            var index = 0
            while (index < lines.size) {
                val text = lines[index].getUnformattedString().trim()
                if (!isRodPartName(text)) {
                    index++
                    continue
                }

                val nextIndex = index + 1
    
                if (!text.contains("NONE", ignoreCase = true)) {
                    // Rod part description line(s)
                    while (nextIndex < lines.size && isNotEmptyLine(lines[nextIndex].getUnformattedString())) {
                        lines.removeAt(nextIndex)
                    }
                }
                // Empty line between rod parts only
                while (nextIndex < lines.size && isEmptyLine(lines[nextIndex].getUnformattedString())) {
                    val nextNonEmptyText = lines.drop(nextIndex + 1)
                        .firstOrNull { isNotEmptyLine(it.getUnformattedString()) }
                        ?.getUnformattedString()
                        ?.trim() ?: ""
                    if (!isRodPartName(nextNonEmptyText)) break
                    lines.removeAt(nextIndex)
                }
                index++
            }
        } catch (e: Exception) { }
    }

    private fun isRodPartName(text: String): Boolean = ROD_PART_NAME_REGEX.matches(text)

    private fun isEmptyLine(text: String): Boolean = text.trim().isEmpty()

    private fun isNotEmptyLine(text: String): Boolean = text.trim().isNotEmpty()
}
