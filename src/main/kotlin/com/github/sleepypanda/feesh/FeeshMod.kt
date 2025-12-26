package com.github.sleepypanda.feesh

import net.fabricmc.api.ModInitializer
import com.github.sleepypanda.feesh.settings.Settings
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import org.slf4j.LoggerFactory
import com.github.sleepypanda.feesh.features.alerts.RareCatchAlert
import com.github.sleepypanda.feesh.features.alerts.ChumBucketAutoPickupAlert
import com.github.sleepypanda.feesh.features.alerts.PetLevelUpAlert
import com.github.sleepypanda.feesh.features.alerts.SpiritMaskAlert
import com.github.sleepypanda.feesh.features.alerts.ThunderBottleChargedAlert
import com.github.sleepypanda.feesh.features.chat.RareCatchMessage
import com.github.sleepypanda.feesh.features.chat.CompactCatchMessages
import com.github.sleepypanda.feesh.features.commands.SpiderRainSchedule
import com.github.sleepypanda.feesh.features.commands.FeeshCommand
import com.github.sleepypanda.feesh.features.overlays.JerryWorkshopTracker
import com.github.sleepypanda.feesh.features.inventory.ThunderBottleProgress

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
        version = getModVersion()
        LOGGER.info("Loading $MOD_NAME v$version...")

        FeeshCommand.init()
        
        // Alerts
        RareCatchAlert.init()
        SpiritMaskAlert.init()
        ChumBucketAutoPickupAlert.init()
        PetLevelUpAlert.init() // TODO formatting
        ThunderBottleChargedAlert.init()

        // Chat
        RareCatchMessage.init()
        CompactCatchMessages.init()

        // Commands
        SpiderRainSchedule.init()
          
        // Overlays
        JerryWorkshopTracker.init()

        // Inventory
        ThunderBottleProgress.init()
                
        LOGGER.info("$MOD_NAME loaded successfully!")
    }

    private fun getModVersion(): String {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
            .map { it.metadata.version.friendlyString }
            .orElse("unspecified")
    }
}