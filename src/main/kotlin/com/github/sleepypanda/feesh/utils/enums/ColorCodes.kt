package com.github.sleepypanda.feesh.utils.enums

import java.awt.Color

enum class ColorCodes(val code: String) {
    BLACK("§0"),
    DARK_BLUE("§1"),
    DARK_GREEN("§2"),
    DARK_AQUA("§3"),
    DARK_RED("§4"),
    DARK_PURPLE("§5"),
    GOLD("§6"),
    GRAY("§7"),
    DARK_GRAY("§8"),
    BLUE("§9"),
    GREEN("§a"),
    AQUA("§b"),
    RED("§c"),
    LIGHT_PURPLE("§d"),
    YELLOW("§e"),
    WHITE("§f"),

    COMMON(WHITE.code),
    UNCOMMON(GREEN.code),
    RARE(BLUE.code),
    EPIC(DARK_PURPLE.code),
    LEGENDARY(GOLD.code),
    MYTHIC(LIGHT_PURPLE.code),
    DIVINE(AQUA.code),
    SPECIAL(RED.code);

    override fun toString(): String = code
}
