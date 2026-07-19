package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.setScreenCompat
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen

object FeeshSettingsCommand {
    fun init() {
        RegisterUtils.command("feesh") {
            val mc = FeeshMod.mc
            mc.schedule {
                mc.setScreenCompat(ResourcefulConfigScreen.getFactory("feesh").apply(null))
            }
        }
    }
}