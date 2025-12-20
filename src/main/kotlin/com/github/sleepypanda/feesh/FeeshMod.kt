package com.github.sleepypanda.feesh

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory

object FeeshMod : ModInitializer {
    const val MOD_ID = "feesh"
    const val MOD_NAME = "Feesh"
    const val VERSION = "1.0.0"
    
    private val LOGGER = LoggerFactory.getLogger(MOD_ID)
    
    override fun onInitialize() {
        LOGGER.info("Initializing $MOD_NAME v$VERSION")
        
        // Initialize settings
        settings.Settings
        
        // Register chat listener
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, _ ->
            if (settings.Settings.general.rareCatchesEnabled) {
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
        // Get Minecraft client
        val client = net.minecraft.client.MinecraftClient.getInstance()
        
        // Show title on player's screen
        client.inGameHud.setTitle(net.minecraft.text.Text.literal("§6§lRARE CATCH!"))
        client.inGameHud.setSubtitle(net.minecraft.text.Text.literal("§7Yeti"))
        
        // Reset title timer
        client.inGameHud.titleRemainTicks = 20
        client.inGameHud.titleFadeInTicks = 10
        client.inGameHud.titleFadeOutTicks = 10
    }
}