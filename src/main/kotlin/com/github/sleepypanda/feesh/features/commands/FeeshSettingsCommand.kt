package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.setScreenCompat
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen

object FeeshSettingsCommand {
    fun init() {
        RegisterUtils.command("feesh") { args ->
            val query = args.joinToString(" ").trim()
            val mc = FeeshMod.mc
            mc.schedule {
                mc.setScreenCompat(
                    ResourcefulConfigScreen.make(FeeshMod.INSTANCE.settings)
                        .withQuery(query)
                        .build()
                )
            }
        }
    }
}