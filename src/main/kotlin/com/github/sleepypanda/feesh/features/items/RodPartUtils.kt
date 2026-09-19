package com.github.sleepypanda.feesh.features.items

import com.github.sleepypanda.feesh.settings.models.RodPartTypes
import com.github.sleepypanda.feesh.utils.ItemUtils
import net.minecraft.world.item.ItemStack

object RodPartUtils {

    private val ROD_PART_NAME_REGEX = Regex(
        """^(ථ|ꨃ|࿉) (Hook NONE|Line NONE|Sinker NONE|.+ Hook|.+ Line|.+ Sinker)$"""
    )

    data class RodPart(
        val type: RodPartTypes,
        val name: String,
        val isNone: Boolean
    )

    fun isRodPartName(text: String): Boolean {
        val cleanText = text.trim()
        return cleanText.isNotEmpty() && ROD_PART_NAME_REGEX.matches(cleanText)
    }

    fun getRodParts(stack: ItemStack): List<RodPart> {
        if (stack.isEmpty || !ItemUtils.isFishingRod(stack)) return emptyList()
        return ItemUtils.getUnformattedLoreLines(stack).mapNotNull { parseRodPart(it) }
    }

    fun abbreviate(part: RodPart): String {
        if (part.isNone) return ""
        return part.name
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString("") { it.first().uppercaseChar().toString() }
    }

    private fun parseRodPart(text: String): RodPart? {
        val match = ROD_PART_NAME_REGEX.matchEntire(text.trim()) ?: return null
        val name = match.groupValues[2]
        val type = when {
            name == "Hook NONE" || name.endsWith(" Hook") -> RodPartTypes.HOOK
            name == "Line NONE" || name.endsWith(" Line") -> RodPartTypes.LINE
            name == "Sinker NONE" || name.endsWith(" Sinker") -> RodPartTypes.SINKER
            else -> return null
        }
        return RodPart(type = type, name = name, isNone = name.endsWith(" NONE"))
    }
}
