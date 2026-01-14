package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier

object PlayTestSoundCommand {
    const val COMMAND_NAME = "feeshPlayTestSound"

    fun init() {
        RegisterUtils.command(COMMAND_NAME, ::handleCommand)
    }
    
    private fun handleCommand(args: Array<String>) {
        if (args.isEmpty()) {
            ChatUtils.sendLocalChat("${RED}Usage: /${COMMAND_NAME} <fileName.ogg|mc:soundEventName>", true)
            return
        }
        
        val soundParam = args[0]
        
        when {
            soundParam.endsWith(".ogg", ignoreCase = true) -> {
                try {
                    SoundUtils.playCustomSound(soundParam)
                } catch (e: Exception) {
                    FeeshMod.LOGGER.error("[Feesh] Failed to play custom sound: $soundParam", e)
                    ChatUtils.sendLocalChat("${RED}Failed to play custom sound: ${YELLOW}$soundParam${RED}. Check logs for details.", true)
                }
            }
            soundParam.startsWith("mc:") -> {
                val soundName = soundParam.removePrefix("mc:")
                try {
                    val soundEvent = getSoundEvent(soundName)
                    SoundUtils.playSound(soundEvent)
                    ChatUtils.sendLocalChat("${GREEN}Playing Minecraft sound: ${YELLOW}${soundEvent.id}", true)
                } catch (e: Exception) {
                    FeeshMod.LOGGER.error("[Feesh] Failed to play Minecraft sound: $soundName", e)
                    ChatUtils.sendLocalChat("${RED}Failed to play sound: ${YELLOW}$soundName${RED}. Check logs for details.", true)
                }
            }
            else -> {
                ChatUtils.sendLocalChat("${RED}Invalid format. Use ${YELLOW}fileName.ogg${RED} or ${YELLOW}mc:soundEventName", true)
            }
        }
    }
    
    private fun getSoundEvent(soundName: String): SoundEvent {
        // Use getDeclaredField to access all fields (including non-public ones)
        val field = SoundEvents::class.java.getDeclaredField(soundName)
        field.isAccessible = true // Make field accessible even if it's private
        
        if (SoundEvent::class.java.isAssignableFrom(field.type)) {
            @Suppress("UNCHECKED_CAST")
            return field.get(null) as SoundEvent
        } else {
            throw IllegalArgumentException("$soundName is not a SoundEvents field")
        }
    }
}
