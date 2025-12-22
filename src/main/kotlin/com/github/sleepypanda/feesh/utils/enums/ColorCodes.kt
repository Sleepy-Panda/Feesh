package com.github.sleepypanda.feesh.utils.enums

import java.awt.Color

enum class ColorCodes(val code: String, val color: Color, val displayName: String) {
    BLACK("§0", Color.BLACK, "Black"),
    DARK_BLUE("§1", Color(0, 0, 170), "Dark Blue"),
    DARK_GREEN("§2", Color(0, 170, 0), "Dark Green"),
    DARK_AQUA("§3", Color(0, 170, 170), "Dark Aqua"),
    DARK_RED("§4", Color(170, 0, 0), "Dark Red"),
    DARK_PURPLE("§5", Color(170, 0, 170), "Dark Purple"),
    GOLD("§6", Color(255, 170, 0), "Gold"),
    GRAY("§7", Color.GRAY, "Gray"),
    DARK_GRAY("§8", Color.DARK_GRAY, "Dark Gray"),
    BLUE("§9", Color.BLUE, "Blue"),
    GREEN("§a", Color.GREEN, "Green"),
    AQUA("§b", Color(85, 255, 255), "Aqua"),
    RED("§c", Color.RED, "Red"),
    LIGHT_PURPLE("§d", Color.MAGENTA, "Light Purple"),
    YELLOW("§e", Color.YELLOW, "Yellow"),
    WHITE("§f", Color.WHITE, "White"),

    COMMON("§f", Color.WHITE, "White"),
    UNCOMMON("§a", Color.GREEN, "Green"),
    RARE("§9", Color.BLUE, "Blue"),
    EPIC("§5", Color(170, 0, 170), "Dark Purple"),
    LEGENDARY("§6", Color(255, 170, 0), "Gold"),
    MYTHIC("§d", Color.MAGENTA, "Light Purple")
}