package com.github.sleepypanda.feesh.utils.enums

// For gradients creation
// https://icolorpalette.com/color/

enum class HexColorCodes(val rgbColorCode: Int, val argbColorCode: Long, val gradientColorCodes: IntArray) {
    COMMON(0xFFFFFF, 0xFFFFFFFF, intArrayOf(0xFFFFFF, 0xFFF8C8, 0xFFD4E6, 0xD0EBFF)),
    UNCOMMON(0x54FC54, 0xFF54FC54, intArrayOf(0x54FC54, 0xa8fc54, 0x3ab03a)),
    RARE(0x449AFC, 0xFF449AFC, intArrayOf(0x449AFC, 0x3FE4B9, 0x6DBBEA)),
    EPIC(0xA234EB, 0xFFA234EB, intArrayOf(0xA234EB, 0xE68CAF, 0x873285)),
    LEGENDARY(0xFC8F00, 0xFFFC8F00, intArrayOf(0xFC8F00, 0xFFB137, 0xFCCD18)),
    MYTHIC(0xFC54FC, 0xFFFC54FC, intArrayOf(0xFC54FC, 0xFBAE52, 0xEB7195)),
    DIVINE(0x54FCFC, 0xFF54FCFC, intArrayOf(0x54FCFC, 0x46C0FC, 0x54FCFC)), // Not used so no good gradient
    SPECIAL(0xFC5454, 0xFFFC5454, intArrayOf(0xFC5454, 0xB20303, 0xFC5454)),
    VERY_SPECIAL(0xD13228, 0xFFD13228, intArrayOf(0xD13228, 0xD13228, 0xD13228));

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
            ColorCodes.VERY_SPECIAL.code -> VERY_SPECIAL
            else -> null
        }
    }
}
