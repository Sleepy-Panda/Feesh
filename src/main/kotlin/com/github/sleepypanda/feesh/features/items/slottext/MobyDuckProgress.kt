package com.github.sleepypanda.feesh.features.items.slottext

import com.github.sleepypanda.feesh.settings.categories.Items
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import kotlin.math.truncate

object MobyDuckProgress : BaseSlotTextRenderer() {

    private const val MAX_PROGRESS_SECONDS = 300 * 60 * 60
    private const val PROGRESS_COLOR = 0xFF55FFFF.toInt()

    fun init() {
        // Calling this ensures the object is initialized, and registered in base class.
    }

    override fun isEnabled(): Boolean {
        return WorldUtils.isInSkyblock() && Items.showMobyDuckProgress
    }

    override fun getItemStackSlotText(stack: ItemStack, screen: HandledScreen<*>, slot: Slot): String? {
        if (stack.isEmpty()) return null
        if (stack.name.string.removeFormatting() != "Moby-Duck") return null

        val nbt = ItemUtils.getCustomData(stack) ?: return null
        val obj = ItemUtils.customDataToJsonObject(nbt) ?: return null
        val secondsHeld = obj.get("seconds_held")?.asInt ?: 0
        val percent = truncate(secondsHeld.toDouble() / MAX_PROGRESS_SECONDS.toDouble() * 100.0).toInt()
        val percentSafe = percent.coerceIn(0, 100)
        val slotText = "${percentSafe}%"
        return slotText
    }

    override fun getTextColor(): Int = PROGRESS_COLOR
}