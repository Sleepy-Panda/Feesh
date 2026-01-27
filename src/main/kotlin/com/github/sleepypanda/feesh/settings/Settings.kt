package com.github.sleepypanda.feesh.settings

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.Commands
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import net.minecraft.util.Util

object Settings : ConfigKt("${FeeshMod.MOD_ID}/config") {
    override val name: TranslatableValue
        get() = Literal("${FeeshMod.MOD_NAME} ${FeeshMod.version}")
    override val description = Literal("QOL mod for Hypixel Skyblock fishing.")
    
    init {
        separator {
            title = "Welcome to ${FeeshMod.MOD_NAME}!"
            description = "Fishing enhancements mod for Hypixel Skyblock. From MoonTheSadFisher with ❤"
        }
        
        button {
            title = "Modrinth"
            description = "Find available releases here."
            text = "Open"
            onClick {
                openLink("https://modrinth.com/project/feesh")
            }
        }

        button {
            title = "GitHub"
            description = "Find source code, latest release notes, docs and README here."
            text = "Open"
            onClick {
                openLink("https://github.com/Sleepy-Panda/Feesh")
            }
        }

        // Settings categories
        category(General)
        category(Alerts)        
        category(Chat)
        category(Overlays)
        category(Commands)
    }
    
    fun save() = FeeshMod.INSTANCE.settings.save()

    private fun openLink(url: String) {
        Util.getOperatingSystem().open(url)
    }
}