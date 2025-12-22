package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod

object Common {
    fun showTitle(title: String, subtitle: String?, fadeIn: Int = 0, stay: Int = 20, fadeOut: Int = 10) {      
        var mc = FeeshMod.mc

        mc.inGameHud.apply {
            setTitleTicks(fadeIn, stay, fadeOut)
            setTitle(net.minecraft.text.Text.literal(title))
            if (subtitle != null)
                setSubtitle(net.minecraft.text.Text.literal(subtitle))
        }
    }
}