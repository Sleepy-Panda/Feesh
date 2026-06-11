package com.github.sleepypanda.feesh.features.items.tooltip

import com.github.sleepypanda.feesh.settings.categories.Items
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

object ExpertiseTooltip : BaseTooltip() {

    private const val MAX_EXPERTISE_KILLS = 15_000

    fun init() {
        // Calling this ensures the object is initialized and registered in the base class.
    }

    override fun isEnabled(): Boolean = Items.showExpertiseKillsTooltip

    override fun modifyTooltip(stack: ItemStack, lines: MutableList<Component>) {
        if (!ItemUtils.isFishingRod(stack)) return

        val kills = getCachedExpertiseKills(stack) ?: return
        val killsFormatted = CommonUtils.formatNumberWithSpaces(kills)
        val maxKillsFormatted = CommonUtils.formatNumberWithSpaces(MAX_EXPERTISE_KILLS)
        val line = if (kills >= MAX_EXPERTISE_KILLS) "${GRAY}Expertise Kills: ${AQUA}${killsFormatted} ${DARK_GRAY}(Maxed)"
            else "${GRAY}Expertise Kills: ${WHITE}${killsFormatted} ${GRAY}/ ${AQUA}${maxKillsFormatted}"
        CommonUtils.appendTooltipLine(lines, line)
    }

    private fun getCachedExpertiseKills(stack: ItemStack): Int? {
        return getCachedValue(stack, ::getExpertiseKills)
    }

    private fun getExpertiseKills(stack: ItemStack): Int? {
        val customData = ItemUtils.getCustomData(stack) ?: return null
        val obj = ItemUtils.customDataToJsonObject(customData) ?: return null
        val enchantments = obj.get("enchantments")
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject
            ?: return null
        if (!enchantments.has("expertise")) return null

        return obj.get("expertise_kills")
            ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }
            ?.asInt
            ?: 0
    }
}
