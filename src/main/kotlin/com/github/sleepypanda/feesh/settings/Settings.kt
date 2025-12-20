package com.github.sleepypanda.feesh.settings

import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.General
import net.minecraft.util.Util

object Settings : ConfigKt("${FeeshMod.MOD_ID}/config") {
    override val name: TranslatableValue
        get() = Literal("${FeeshMod.MOD_NAME} ${FeeshMod.VERSION}")
    override val description = Literal("Feesh mod for Hypixel Skyblock fishing enhancements")
    
    init {
        separator {
            title = "Welcome to ${FeeshMod.MOD_NAME}!"
            description = "A fishing enhancement mod for Hypixel Skyblock"
        }
        
        // Settings categories
        category(General)
        
        button {
            title = "GitHub"
            description = "View the source code"
            text = "Open"
            onClick {
                Util.getOperatingSystem().open("https://github.com/yourname/feesh")
            }
        }
    }
    
    // Accessor for settings
    val general: General
        get() = getCategory(General::class.java)
    
    fun save() = FeeshMod.settings.save()
}