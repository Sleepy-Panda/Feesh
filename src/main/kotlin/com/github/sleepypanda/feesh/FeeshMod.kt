package com.github.sleepypanda.feesh

import net.fabricmc.api.ModInitializer
import com.github.sleepypanda.feesh.settings.Settings
import com.github.sleepypanda.feesh.settings.categories.General
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import org.slf4j.LoggerFactory

class FeeshMod : ModInitializer {
    companion object {
        internal const val MOD_ID = "feesh"
        internal const val MOD_NAME = "Feesh"
        internal const val VERSION = "1.0.0"
        
        internal val LOGGER = LoggerFactory.getLogger(MOD_ID)
        
        @JvmStatic
        lateinit var INSTANCE: FeeshMod
            private set
    }
    
    @JvmField
	val mc: MinecraftClient = MinecraftClient.getInstance()

    val configurator = Configurator("feesh")
    public val settings = Settings.register(configurator)
    
    init {
        INSTANCE = this
    }

    override fun onInitialize() {
        LOGGER.info("Initializing $MOD_NAME v$VERSION")
        
        
        // Register chat listener
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, _ ->
            if (General.rareCatchesAlert) {
                if (message.string.contains("What is this creature?!")) {
                    // Show title on screen
                    displayRareCatchTitle()
                    return@register false // Prevent message from showing if desired
                }
            }
            true
        }
        
        LOGGER.info("$MOD_NAME initialized successfully!")
    }
    
    private fun displayRareCatchTitle() {      
        // Show title on player's screen
        mc.inGameHud.setTitle(net.minecraft.text.Text.literal("§6§lRARE CATCH!"))
        mc.inGameHud.setSubtitle(net.minecraft.text.Text.literal("§7Yeti"))
        
        // Reset title timer
        mc.inGameHud.apply {
            setTitleTicks(10, 20, 10)
            setTitle(net.minecraft.text.Text.of("§6§lRARE CATCH!"))
            setSubtitle(net.minecraft.text.Text.of("§7Yeti"))
        }
    }
}