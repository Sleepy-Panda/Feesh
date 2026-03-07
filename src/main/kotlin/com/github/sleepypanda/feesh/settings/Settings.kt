package com.github.sleepypanda.feesh.settings

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.Commands
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.features.help.VersionChecker
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import net.minecraft.util.Util

object Settings : ConfigKt("${FeeshMod.MOD_ID}/config") {
    override val name: TranslatableValue
        get() = Literal("${FeeshMod.MOD_NAME} ${FeeshMod.version}")
        
    override val description: TranslatableValue
        get() = Literal(
            "QOL mod for Hypixel Skyblock fishing. ${if (VersionChecker.cachedLatestVersion.isNullOrEmpty()) "" else "Latest on Modrinth: ${if (VersionChecker.isLatestVersion) "${GREEN}${BOLD}${VersionChecker.cachedLatestVersion} - Up to date" else "${RED}${BOLD}${VersionChecker.cachedLatestVersion} - Update available"}"}"
        )
    
    init {
        separator {
            title = "Welcome to ${FeeshMod.MOD_NAME}!"
            description = "${GRAY}Fishing enhancements mod for Hypixel Skyblock. From ${AQUA}MoonTheSadFisher ${GRAY}with ${RED}❤"
        }
        
        button {
            title = "Modrinth"
            description = "Find official releases here. Open to check latest version."
            text = "Open"
            onClick {
                openLink("https://modrinth.com/project/feesh/versions")
            }
        }

        button {
            title = "GitHub"
            description = "Find source code, docs and report issues/suggestions/feedback here."
            text = "Open"
            onClick {
                openLink("https://github.com/Sleepy-Panda/Feesh")
            }
        }

        button {
            title = "Discord"
            description = "m00nlight_sky - contact me if you have any questions, feedback, or suggestions."
            text = "Button does nothing"
            onClick { }
        }

        // Settings categories
        category(General)
        category(Alerts)        
        category(Chat)
        category(Overlays)
        category(WorldRendering)
        category(Commands)
    }
    
    fun save() = FeeshMod.INSTANCE.settings.save()

    private fun openLink(url: String) {
        Util.getOperatingSystem().open(url)
    }
}