package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
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
}