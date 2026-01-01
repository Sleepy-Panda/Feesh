package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod

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
}