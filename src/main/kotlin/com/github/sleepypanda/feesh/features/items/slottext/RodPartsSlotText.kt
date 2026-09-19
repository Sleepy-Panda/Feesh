package com.github.sleepypanda.feesh.features.items.slottext

import com.github.sleepypanda.feesh.features.items.RodPartUtils
import com.github.sleepypanda.feesh.settings.categories.Items
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.Slot

object RodPartsSlotText : BaseSlotTextRenderer() {

    private const val SLOT_COLOR = 0xff54fcfc.toInt()
    private const val SLOT_TEXT_SCALE = 0.55f

    fun init() {
        // Calling this ensures the object is initialized, and registered in base class.
    }

    override fun isEnabled(): Boolean = Items.showRodPartsSlotText && Items.showRodPartsSlotTextList.isNotEmpty()

    override fun getItemStackSlotText(stack: ItemStack, screen: AbstractContainerScreen<*>, slot: Slot): String? {
        if (stack.isEmpty) return null

        val enabledTypes = Items.showRodPartsSlotTextList.toSet()
        if (enabledTypes.isEmpty()) return null

        val abbreviations = RodPartUtils.getRodParts(stack)
            .filter { it.type in enabledTypes }
            .take(3)
            .map { RodPartUtils.abbreviate(it) }
            .filter { it.isNotEmpty() }

        if (abbreviations.isEmpty()) return null
        return abbreviations.joinToString("\n")
    }

    override fun getTextColor(): Int = SLOT_COLOR

    override fun getPosition(): SlotTextPosition = SlotTextPosition.TOP_LEFT

    override fun getTextScale(): Float = SLOT_TEXT_SCALE
}
