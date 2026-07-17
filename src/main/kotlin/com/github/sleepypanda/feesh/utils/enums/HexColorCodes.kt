package com.github.sleepypanda.feesh.utils.enums

// For gradients creation
// https://icolorpalette.com/color/

enum class HexColorCodes(val colorCode: Int, val gradientColorCode1: Int, val gradientColorCode2: Int) {
    COMMON(0xFFFFFF, 0xFFFFFF, 0xA8BFD2),
    UNCOMMON(0x54FC54, 0x54FC54, 0x01C401),
    RARE(0x449AFC, 0x449AFC, 0x0BE9FE),
    EPIC(0xA234EB, 0xA234EB, 0xFC8ECE),
    LEGENDARY(0xFC8F00, 0xFC8F00, 0xFF887F),
    MYTHIC(0xFC54FC, 0xFC54FC, 0xFFC0CB),
    DIVINE(0x54FCFC, 0x54FCFC, 0x46C0FC),
    SPECIAL(0xFC5454, 0xFC5454, 0xFE9CA7);

    companion object {
        fun getHexColorForRarity(rarityColorCode: String): HexColorCodes? = when (rarityColorCode) {
            ColorCodes.COMMON.code -> COMMON
            ColorCodes.UNCOMMON.code -> UNCOMMON
            ColorCodes.RARE.code -> RARE
            ColorCodes.EPIC.code -> EPIC
            ColorCodes.LEGENDARY.code -> LEGENDARY
            ColorCodes.MYTHIC.code -> MYTHIC
            ColorCodes.DIVINE.code -> DIVINE
            ColorCodes.SPECIAL.code -> SPECIAL
            else -> null
        }
    }
}
