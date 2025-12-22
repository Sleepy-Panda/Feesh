package com.github.sleepypanda.feesh

import net.fabricmc.api.ModInitializer
import com.github.sleepypanda.feesh.settings.Settings
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.utils.Register
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
import com.github.sleepypanda.feesh.features.chat.CompactCatchMessages

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

        registerFeeshCommand()
        
        RareCatches.init()
        CompactCatchMessages.init()   
        
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
        
        LOGGER.info("$MOD_NAME loaded successfully!")
    }
    
    private fun registerFeeshCommand() {
        Register.command("feesh", "feeshnotifier", "fn") {
			mc.send {
				mc.setScreen(ResourcefulConfigScreen.getFactory("feesh").apply(null))
			}
		}
    }
    //Register.onChatMessageCancable(
    //        Pattern.compile("^§eThe election room is now closed\\. Clerk Seraphine is doing a final count of the votes\\.\\.\\.$", Pattern.DOTALL)
    //    ) { _, _ ->
    //        sleep(10000) {
    //            checkMayorTracker()
    //        }
    //        true
    //    }
    //    
    ///**
    // * Registers an event that listens for chat messages that match a regex.
    // * The action receives both the message and the regex match result for easy value extraction.
    // *
    // * @param regex The regular expression to filter messages with.
    // * @param action The action to execute. It receives the message and the `MatchResult`.
    // */
    //fun onChatMessage(
    //    regex: Regex,
    //    noFormatting: Boolean = false,
    //    action: (message: Text, matchResult: MatchResult) -> Unit
    //) {
    //    ClientReceiveMessageEvents.GAME.register { message, _ ->
    //        var text = message.formattedString()
//
    //        if (noFormatting) text = text.removeFormatting()
//
    //        regex.find(text)?.let { result ->
    //            action(message, result)
    //        }
    //    }
    //}

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