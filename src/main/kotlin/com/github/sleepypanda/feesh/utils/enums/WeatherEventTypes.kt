package com.github.sleepypanda.feesh.utils.enums

enum class WeatherEventTypes(val displayName: String) {
    TROPICAL_RAIN("Tropical Rain"),
    ACID_RAIN("Acid Rain"),
    THUNDERSTORM("Thunderstorm"),
    THUNDER("Thunder"),
    SNOWSTORM("Snowstorm"),
    HELLSTORM("Hellstorm"),
    VOIDSTORM("Voidstorm"),
    WISPFALL("Wispfall"),
    ASHFALL("Ashfall"),
    MOONFALL("Moonfall"),
    ROCKFALL("Rockfall"),
    BLOSSOMING("Blossoming"),
    BLOOMING("Blooming"),
    BLIZZARD("Blizzard"),
    BREEZE("Breeze"),
    MIST("Mist"),
    SMOG("Smog"),
    RAIN("Rain");

    override fun toString(): String = displayName

    companion object {
        fun fromDisplayName(name: String): WeatherEventTypes? = values().find { it.displayName == name }

        val lineRegex: Regex = Regex(
            "^(${values().joinToString("|") { Regex.escape(it.displayName) }}):\\s(.+)"
        )
    }
}
