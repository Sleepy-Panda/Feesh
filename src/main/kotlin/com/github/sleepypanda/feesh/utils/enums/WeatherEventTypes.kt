package com.github.sleepypanda.feesh.utils.enums

enum class WeatherEventTypes(val displayName: String) {
    ACID_RAIN("Acid Rain"),
    ASHFALL("Ashfall"),
    BLIZZARD("Blizzard"),
    BLOOMING("Blooming"),
    BLOSSOMING("Blossoming"),
    BREEZE("Breeze"),
    HELLSTORM("Hellstorm"),
    MIST("Mist"),
    MOONFALL("Moonfall"),
    RAIN("Rain"),
    ROCKFALL("Rockfall"),
    SMOG("Smog"),
    SNOWSTORM("Snowstorm"),
    THUNDER("Thunder"),
    THUNDERSTORM("Thunderstorm"),
    TROPICAL_RAIN("Tropical Rain"),
    VOIDSTORM("Voidstorm"),
    WISPFALL("Wispfall");

    override fun toString(): String = displayName

    companion object {
        fun getByName(name: String): WeatherEventTypes? = values().find { it.displayName == name }

        private val eventNames = values().joinToString("|") { Regex.escape(it.displayName) }
        val weatherTablistLineRegex = Regex("^($eventNames):\\s(.+)")
    }
}
