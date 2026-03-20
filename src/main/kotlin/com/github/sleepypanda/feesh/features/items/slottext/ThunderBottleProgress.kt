package com.github.sleepypanda.feesh.features.items.slottext

import com.github.sleepypanda.feesh.settings.categories.Items
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import kotlin.math.truncate

object ThunderBottleProgress : BaseSlotTextRenderer() {

    private data class Bottle(val name: String, val maxCharge: Int)

    private val BOTTLES = listOf(
        Bottle("Empty Thunder Bottle", 50000),
        Bottle("Empty Storm Bottle", 500_000),
        Bottle("Empty Hurricane Bottle", 5_000_000)
    )

    private const val PROGRESS_COLOR = 0xFF55FFFF.toInt()

    fun init() {
        // Calling this ensures the object is initialized, and registered in base class.
    }

    override fun isEnabled(): Boolean {
        return WorldUtils.isInSkyblock() && Items.showThunderBottleProgress
    }

    override fun getItemStackSlotText(stack: ItemStack, screen: HandledScreen<*>, slot: Slot): String? {
        if (stack.isEmpty()) return null
        val name = stack.name.string.removeFormatting() ?: return null

        if (!BOTTLES.map { it.name }.contains(name)) return null

        val maxCharge = BOTTLES.find { it.name == name }?.maxCharge ?: return null
        val nbt = ItemUtils.getCustomData(stack) ?: return null
        val obj = ItemUtils.customDataToJsonObject(nbt) ?: return null
        val currentCharge = obj.get("thunder_charge")?.asDouble ?: 0.0
        val percent = truncate(currentCharge / maxCharge * 100).toInt()
        val percentSafe = percent.coerceIn(0, 100)
        val slotText = "${percentSafe}%"
        return slotText
    }

    override fun getTextColor(): Int = PROGRESS_COLOR
}