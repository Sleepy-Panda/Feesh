package com.github.sleepypanda.feesh.settings.models

enum class TrophyRarityTypes(val displayName: String) {
    BRONZE("Bronze"),
    SILVER("Silver"),
    GOLD("Gold"),
    DIAMOND("Diamond");

    override fun toString(): String = displayName
}
