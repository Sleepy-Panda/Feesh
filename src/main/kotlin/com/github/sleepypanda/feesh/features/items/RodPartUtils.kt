package com.github.sleepypanda.feesh.features.items

import com.github.sleepypanda.feesh.settings.models.RodPartTypes
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.enums.HexColorCodes
import net.minecraft.world.item.ItemStack

object RodPartUtils {

    private val ROD_PART_NAME_REGEX = Regex(
        """^(ථ|ꨃ|࿉) (Hook NONE|Line NONE|Sinker NONE|.+ Hook|.+ Line|.+ Sinker)$"""
    )
    private val COLOR_CODE_REGEX = Regex("§[0-9a-fA-F]")
    private val LEADING_ICON_REGEX = Regex("^(?:§.)*\\s*(?:ථ|ꨃ|࿉)")

    data class RodPart(
        val type: RodPartTypes,
        val name: String,
        val isNone: Boolean,
        val color: Int
    )

    fun isRodPartName(text: String): Boolean {
        val cleanText = text.removeFormatting().trim()
        return cleanText.isNotEmpty() && ROD_PART_NAME_REGEX.matches(cleanText)
    }

    fun getRodParts(stack: ItemStack): List<RodPart> {
        if (stack.isEmpty || !ItemUtils.isFishingRod(stack)) return emptyList()
        return ItemUtils.getFormattedLoreLines(stack).mapNotNull { parseRodPart(it) }
    }

    fun abbreviate(part: RodPart): String {
        if (part.isNone) return ""
        return part.name
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString("") { it.first().uppercaseChar().toString() }
    }

    private fun parseRodPart(formattedText: String): RodPart? {
        val unformatted = formattedText.removeFormatting().trim()
        val match = ROD_PART_NAME_REGEX.matchEntire(unformatted) ?: return null
        val name = match.groupValues[2]
        val type = when {
            name == "Hook NONE" || name.endsWith(" Hook") -> RodPartTypes.HOOK
            name == "Line NONE" || name.endsWith(" Line") -> RodPartTypes.LINE
            name == "Sinker NONE" || name.endsWith(" Sinker") -> RodPartTypes.SINKER
            else -> return null
        }
        return RodPart(
            type = type,
            name = name,
            isNone = name.endsWith(" NONE"),
            color = extractNameColor(formattedText)
        )
    }

    private fun extractNameColor(formattedText: String): Int {
        val defaultColor = 0xFFFFFFFF.toInt()
        val textAfterIcon = formattedText.replaceFirst(LEADING_ICON_REGEX, "")
        val colorCode = COLOR_CODE_REGEX.find(textAfterIcon)?.value
            ?: COLOR_CODE_REGEX.findAll(formattedText).lastOrNull()?.value
            ?: return defaultColor
        return HexColorCodes.getHexColorForRarity(colorCode)?.argbColorCode?.toInt() ?: defaultColor
    }
}
