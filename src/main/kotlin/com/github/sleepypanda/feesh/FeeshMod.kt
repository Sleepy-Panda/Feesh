package com.github.sleepypanda.feesh

import net.fabricmc.api.ModInitializer
import com.github.sleepypanda.feesh.settings.Settings
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import org.slf4j.LoggerFactory
import com.github.sleepypanda.feesh.features.alerts.RareCatches
import com.github.sleepypanda.feesh.features.alerts.ChumBucketAutoPickup
import com.github.sleepypanda.feesh.features.alerts.PetLevelUp
import com.github.sleepypanda.feesh.features.chat.CompactCatchMessages
import com.github.sleepypanda.feesh.features.commands.SpiderRainSchedule
import com.github.sleepypanda.feesh.features.commands.Feesh as FeeshCommand

class FeeshMod : ModInitializer {
    companion object {
        internal const val MOD_ID = "feesh"
        internal const val MOD_NAME = "Feesh"
        
        internal val LOGGER = LoggerFactory.getLogger(MOD_ID)
        
        lateinit var version: String

        @JvmField
        val mc: MinecraftClient = MinecraftClient.getInstance()

        @JvmStatic
        lateinit var INSTANCE: FeeshMod
            private set
    }

    val configurator = Configurator("feesh")
    public val settings = Settings.register(configurator)
    
    init {
        INSTANCE = this
    }

    override fun onInitialize() {   
        version = FabricLoader.getInstance().getModContainer(MOD_ID)
			.map { it.metadata.version.friendlyString }
			.orElse("unspecified")

        LOGGER.info("Loading $MOD_NAME v$version...")

        FeeshCommand.init()
        
        // Alerts
        RareCatches.init()
        ChumBucketAutoPickup.init()
        PetLevelUp.init()

        // Chat
        CompactCatchMessages.init()

        // Commands
        SpiderRainSchedule.init()   
                
        LOGGER.info("$MOD_NAME loaded successfully!")
    }
}