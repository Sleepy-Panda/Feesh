package com.github.sleepypanda.feesh.settings

import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.Alerts
import net.minecraft.util.Util

object Settings : ConfigKt("${FeeshMod.MOD_ID}/config") {
    override val name: TranslatableValue
        get() = Literal("${FeeshMod.MOD_NAME} ${FeeshMod.version}")
    override val description = Literal("Feesh mod for Hypixel Skyblock fishing enhancements")
    
    init {
        separator {
            title = "Welcome to ${FeeshMod.MOD_NAME}!"
            description = "A fishing enhancement mod for Hypixel Skyblock"
        }
        
        button {
            title = "GitHub"
            description = "View the source code"
            text = "Open"
            onClick {
                Util.getOperatingSystem().open("https://github.com/Sleepy-Panda/Feesh")
            }
        }

        // Settings categories
        category(General)
        category(Alerts)        
    }
    
    fun save() = FeeshMod.INSTANCE.settings.save()
}