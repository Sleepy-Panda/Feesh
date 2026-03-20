package com.github.sleepypanda.feesh.features.items.slottext

import com.github.sleepypanda.feesh.settings.categories.Items
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import kotlin.math.truncate

object AutoRecombFlag : BaseSlotTextRenderer() {

    private const val SLOT_COLOR = 0xFF55FFFF.toInt()

    fun init() {
        // Calling this ensures the object is initialized, and registered in base class.
    }

    override fun isEnabled(): Boolean {
        return WorldUtils.isInSkyblock() && Items.showAutoRecombFlag
    }

    override fun getItemStackSlotText(stack: ItemStack, screen: HandledScreen<*>, slot: Slot): String? {
        if (stack.isEmpty()) return null
        val name = stack.name.string.removeFormatting() ?: return null

        val isFishingItem =
            name == "Slug Boots" ||
            name == "Moogma Leggings" ||
            name == "Flaming Chestplate" ||
            name == "Taurus Helmet" ||
            name == "Blade of the Volcano" ||
            name == "Staff of the Volcano" ||
            name == "Fairy's Polo" ||
            name == "Fairy's Fedora" ||
            name == "Fairy's Trousers" ||
            name == "Fairy's Galoshes" ||
            name == "Squid Boots" ||
            name == "Rabbit Hat" ||
            name == "Water Hydra Head" ||
            name == "Fish Affinity Talisman" ||
            name == "Lucky Hoof" ||
            name == "Tiki Mask"

        if (!isFishingItem) return null

        val nbt = ItemUtils.getCustomData(stack) ?: return null
        val obj = ItemUtils.customDataToJsonObject(nbt) ?: return null
        val hasUpgrade = obj.get("rarity_upgrades")?.asInt ?: false
        val slotText = if (hasUpgrade == 1) "R" else null
        return slotText
    }

    override fun getTextColor(): Int = SLOT_COLOR
}