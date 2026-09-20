package com.github.sleepypanda.feesh.features.items.slottext

import com.github.sleepypanda.feesh.features.items.RodPartUtils
import com.github.sleepypanda.feesh.features.items.slottext.models.SlotTextLine
import com.github.sleepypanda.feesh.features.items.slottext.models.SlotTextPosition
import com.github.sleepypanda.feesh.settings.categories.Items
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.Slot

object FishingRodPartsAbbreviations : BaseSlotTextRenderer() {

    private const val SLOT_TEXT_SCALE = 0.6f

    fun init() {
        // Calling this ensures the object is initialized, and registered in base class.
    }

    override fun isEnabled(): Boolean = Items.showRodPartsSlotText && Items.showRodPartsSlotTextList.isNotEmpty()

    override fun getItemStackSlotLines(stack: ItemStack, screen: AbstractContainerScreen<*>, slot: Slot): List<SlotTextLine>? {
        if (stack.isEmpty) return null

        val enabledTypes = Items.showRodPartsSlotTextList.toSet()
        if (enabledTypes.isEmpty()) return null

        val lines = RodPartUtils.getRodParts(stack)
            .filter { it.type in enabledTypes }
            .take(3)
            .mapNotNull { part ->
                val abbreviation = RodPartUtils.abbreviate(part)
                if (abbreviation.isEmpty()) return@mapNotNull null
                return@mapNotNull SlotTextLine(abbreviation, part.color)
            }

        if (lines.isEmpty()) return null
        return lines
    }

    override fun getPosition(): SlotTextPosition = SlotTextPosition.TOP_LEFT

    override fun getTextScale(): Float = SLOT_TEXT_SCALE
}
