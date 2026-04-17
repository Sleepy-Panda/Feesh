package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import java.util.Date
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import net.minecraft.network.chat.Component

object CommonUtils {
    fun showTitle(title: String, subtitle: String? = null, fadeIn: Int = 0, stay: Int = 40, fadeOut: Int = 10) {      
        val mc = FeeshMod.mc

        mc.gui.apply {
            setTimes(fadeIn, stay, fadeOut)
            setTitle(Component.literal(title))
            setSubtitle(Component.literal(subtitle ?: " "))
        }
    }

    fun formatNumberWithSpaces(number: Int): String {
        val isNegative = number < 0
        val absNumber = Math.abs(number)
        val formatted = absNumber.toString().reversed().chunked(3).joinToString(" ").reversed()
        return if (isNegative) "-$formatted" else formatted
    }

    /**
     * Converts a text from uppercase to capitalized first letters (e.g., "FLYING_FISH" -> "Flying Fish").
     * @param text The text to convert.
     * @param separator The separator to split the original text.
     * @return The converted text.
     */
    fun fromUppercaseToCapitalizedFirstLetters(text: String, separator: String = " "): String {
        return text.split(separator).joinToString(" ") { word ->
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
     * Formats the time elapsed between some date and now.
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
     * Formats a number of seconds to a time elapsed string (e.g., 120 -> "2m", 3600 -> "1h", 86400 -> "1d 0h").
     * @param seconds The number of seconds to format.
     * @return The formatted time elapsed string.
     */
    fun formatTimeElapsed(seconds: Int): String {
        val hours = TimeUnit.SECONDS.toHours(seconds.toLong())
        val minutes = TimeUnit.SECONDS.toMinutes(seconds.toLong()) % 60
        val secs = seconds % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m ${secs}s"
            minutes > 0 -> "${minutes}m ${secs}s"
            else -> "${secs}s"
        }
    }

    /**
     * Formats a date to a string in the format "yyyy-MM-dd HH:mm:ss".
     * @param date The date to format.
     * @return The formatted date string.
     */
    fun formatDate(date: Date?): String {
        if (date == null) return ""
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return formatter.format(date)
    }

    /**
     * Formats a number to a short representation (e.g., 1000 -> "1k", 1000000 -> "1M", 100500 -> "100.5k", 1500000 -> "1.5M")
     * @param number The number to format.
     * @return The formatted string or null if the number is 0 or invalid.
     */
    fun toShortNumber(number: Double?): String? {
        if (number == null) return null

        val isNegative = number < 0
        val absNumber = Math.abs(number)
 
        val formattedNumber = when {
            absNumber >= 1_000_000_000 -> {
                String.format("%.1fB", absNumber / 1_000_000_000.0)
                    .replace(".0B", "B")
                    .replace(",0B", "B")
            }
            absNumber >= 1_000_000 -> {
                String.format("%.1fM", absNumber / 1_000_000.0)
                    .replace(".0M", "M")
                    .replace(",0M", "M")
            }
            absNumber >= 1_000 -> {
                String.format("%.1fk", absNumber / 1_000.0)
                    .replace(".0k", "k")
                    .replace(",0k", "k")
            }
            else -> absNumber.toLong().toString()
        }

        return if (isNegative) "-$formattedNumber" else formattedNumber
    }

    /**
     * Parses a short number string to a Double (e.g., "1K" -> 1000.0, "69M" -> 69000000.0, "521.8k" -> 521800.0)
     * @param shortNumber The short number string to parse.
     * @return The parsed number as Double, or 0.0 if parsing fails.
     */
    fun parseShortNumber(shortNumber: String): Double {
        if (shortNumber.isBlank()) return 0.0
        
        val cleaned = shortNumber.trim().uppercase()
        
        return try {
            when {
                cleaned.endsWith("B") -> {
                    cleaned.dropLast(1).toDoubleOrNull()?.let { it * 1_000_000_000 } ?: 0.0
                }
                cleaned.endsWith("M") -> {
                    cleaned.dropLast(1).toDoubleOrNull()?.let { it * 1_000_000 } ?: 0.0
                }
                cleaned.endsWith("K") -> {
                    cleaned.dropLast(1).toDoubleOrNull()?.let { it * 1_000 } ?: 0.0
                }
                else -> {
                    cleaned.replace(",", "").toDoubleOrNull() ?: 0.0
                }
            }
        } catch (e: Exception) {
            0.0
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
     * Converts a rarity code number (e.g., 0 for COMMON) to a rarity color code (e.g., "§f" for COMMON).
     * @param rarityCode The rarity code as a number
     * @return The rarity color code
     */
    fun getRarityColorCode(rarityCode: Int): String {
        return when (rarityCode) {
            0 -> COMMON.code
            1 -> UNCOMMON.code
            2 -> RARE.code
            3 -> EPIC.code
            4 -> LEGENDARY.code
            5 -> MYTHIC.code
            6 -> DIVINE.code
            7 -> SPECIAL.code
            else -> COMMON.code
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

    /**
     * Generates a random message ID to avoid "You cannot send the same message twice" in all chat.
     * @return The message ID.
     */
    fun getMessageId(): String {
        return "@" + (1..10).map { (('0'..'9') + ('a'..'z') + ('A'..'Z')).random() }.joinToString("")
    }

    /**
     * Formats the location coordinates to a Patcher formattted string.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     * @return The formatted location: "x: 123, y: 123, z: 123".
     */
    fun getFormattedLocation(x: Double, y: Double, z: Double): String {
        return "x: ${Math.round(x)}, y: ${Math.round(y)}, z: ${Math.round(z)}"
    }

    /*
     * Runs a block of code with catching exceptions and logging them.
     * @param message The message to log together with the exception.
     * @param onError The block of code to run on error. If provided, it will be called after the exception is logged.
     * @param block The block of code to run.
     */
    internal inline fun runWithCatching(
        message: String,
        noinline onError: (() -> Unit)? = null,
        block: () -> Unit
    ) {
        try {
            block()
        } catch (e: Exception) {
            val shortDetails = e.stackTrace.firstOrNull()?.let { stackTrace ->
                val file = stackTrace.fileName.ifBlank { "Unknown File" }
                val line = stackTrace.lineNumber.toString()
                val method = stackTrace.methodName.ifBlank { "Unknown Method" }
                "Error occurred in $file:$line in method $method"
            } ?: ""

            FeeshMod.LOGGER.error("[${FeeshMod.MOD_NAME}] $shortDetails - $message", e)
            onError?.invoke()
        }
    }
}