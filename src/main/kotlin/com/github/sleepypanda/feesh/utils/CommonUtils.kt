package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import java.util.Date
import java.util.concurrent.TimeUnit

object CommonUtils {
    fun showTitle(title: String, subtitle: String? = null, fadeIn: Int = 0, stay: Int = 20, fadeOut: Int = 10) {      
        var mc = FeeshMod.mc

        mc.inGameHud.apply {
            setTitleTicks(fadeIn, stay, fadeOut)
            setTitle(net.minecraft.text.Text.literal(title))
            if (subtitle != null)
                setSubtitle(net.minecraft.text.Text.literal(subtitle))
        }
    }

    fun formatNumberWithSpaces(number: Int): String {
        return number.toString().reversed().chunked(3).joinToString(" ").reversed()
    }

    fun fromUppercaseToCapitalizedFirstLetters(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    /**
    * Get suitable article (A/An) depending on the passed string.
    * @param str The string to get the article for.
    * @param makeLowerCase Whether to make the returned string lowercase (a/an).
    * @returns {string} Article (A/An)
    */
    fun getArticle(str: String, makeLowerCase: Boolean = false): String {
        val isFirstLetterVowel = listOf('a', 'e', 'i', 'o', 'u').contains(str[0].lowercaseChar())
        val article = if (isFirstLetterVowel) "An" else "A"
        return if (makeLowerCase) article.lowercase() else article
    }

    /**
     * Formats the time elapsed between two dates.
     * @param lastTime The last time.
     * @return The formatted time elapsed.
     */
    fun formatTimeElapsed(lastTime: Date?): String {
        if (lastTime == null) return ""

        val now = Date()
        val diffMillis = now.time - lastTime.time
        val diffSeconds = diffMillis / 1000

        val days = TimeUnit.SECONDS.toDays(diffSeconds)
        val hours = TimeUnit.SECONDS.toHours(diffSeconds) % 24
        val minutes = TimeUnit.SECONDS.toMinutes(diffSeconds) % 60

        return when {
            days > 0 -> "${days}d ${hours}h ${minutes}m"
            hours > 0 -> "${hours}h ${minutes}m"
            minutes < 1 -> "less than 1m"
            else -> "${minutes}m"
        }
    }

    /**
     * Formats a number to a short representation (e.g., 1000 -> "1K", 1000000 -> "1M")
     * @param number The number to format.
     * @return The formatted string or null if the number is 0 or invalid.
     */
    fun toShortNumber(number: Double?): String? {
        if (number == null || number <= 0) return null
        
        return when {
            number >= 1_000_000_000 -> String.format("%.1fB", number / 1_000_000_000.0).removeSuffix(".0")
            number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0).removeSuffix(".0")
            number >= 1_000 -> String.format("%.1fK", number / 1_000.0).removeSuffix(".0")
            else -> number.toLong().toString()
        }
    }

    /**
     * Converts a rarity color code (e.g., "§6") to a rarity code number (e.g., 4 for LEGENDARY).
     * @param rarityColorCode The color code (2 characters, e.g., "§6")
     * @return The rarity code as a number
     */
    fun getRarityNumericCode(rarityColorCode: String): Int {
        return when (rarityColorCode) {
            COMMON.code -> 0
            UNCOMMON.code -> 1
            RARE.code -> 2
            EPIC.code -> 3
            LEGENDARY.code -> 4
            MYTHIC.code -> 5
            DIVINE.code -> 6
            SPECIAL.code -> 7
            else -> 0
        }
    }

    /**
     * Converts a rarity color code (e.g., "§6") to a rarity description (e.g., "Legendary").
     * @param rarityColorCode The color code (2 characters, e.g., "§6")
     * @return The rarity description
     */
    fun getRarityDescription(rarityColorCode: String): String {
        return when (rarityColorCode) {
            COMMON.code -> "Common"
            UNCOMMON.code -> "Uncommon"
            RARE.code -> "Rare"
            EPIC.code -> "Epic"
            LEGENDARY.code -> "Legendary"
            MYTHIC.code -> "Mythic"
            DIVINE.code -> "Divine"
            SPECIAL.code -> "Special"
            else -> ""
        }
    }
}