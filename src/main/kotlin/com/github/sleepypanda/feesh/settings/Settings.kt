package com.github.sleepypanda.feesh.settings

import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.settings.categories.Chat
import net.minecraft.util.Util

object Settings : ConfigKt("${FeeshMod.MOD_ID}/config") {
    override val name: TranslatableValue
        get() = Literal("${FeeshMod.MOD_NAME} ${FeeshMod.version}")
    override val description = Literal("QOL mod for Hypixel Skyblock fishing.")
    
    init {
        separator {
            title = "Welcome to ${FeeshMod.MOD_NAME}!"
            description = "A fishing enhancement mod for Hypixel Skyblock. From MoonTheSadFisher with ❤"
        }
        
        button {
            title = "GitHub"
            description = "Find latest release notes, docs and README here."
            text = "Open"
            onClick {
                Util.getOperatingSystem().open("https://github.com/Sleepy-Panda/Feesh")
            }
        }

        // Settings categories
        category(General)
        category(Alerts)        
        category(Chat)    
    }
    
    fun save() = FeeshMod.INSTANCE.settings.save()
}