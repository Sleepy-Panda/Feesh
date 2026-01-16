package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier

/**
 * This command allows you to play test sounds in the game.
 * Examples:
 * /feeshPlayTestSound mc:entity.experience_orb.pickup
 * /feeshPlayTestSound fileName.ogg
 * @param args The arguments passed to the command.
 * @param fileName The file name of the sound to play.
 * @param soundName The name of the sound event to play.
 */
object PlayTestSoundCommand {
    const val COMMAND_NAME = "feeshPlayTestSound"

    fun init() {
        RegisterUtils.command(COMMAND_NAME, ::handleCommand)
    }

    private fun handleCommand(args: Array<String>) {
        if (args.isEmpty()) {
            ChatUtils.sendLocalChat("${RED}Usage: /${COMMAND_NAME} <fileName.ogg|mc:soundName>", true)
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
                } catch (e: Exception) {
                    FeeshMod.LOGGER.error("[Feesh] Failed to play Minecraft sound: $soundName", e)
                    ChatUtils.sendLocalChat("${RED}Failed to play sound: ${YELLOW}$soundName${RED}. Check logs for details.", true)
                }
            }
            else -> {
                ChatUtils.sendLocalChat("${RED}Invalid format. Use ${YELLOW}fileName.ogg${RED} or ${YELLOW}mc:soundName", true)
            }
        }
    } 

    private fun getSoundEvent(soundName: String): SoundEvent {
        val path = soundName.lowercase()
        val id = Identifier.of("minecraft", path)
        return SoundEvent.of(id)
    }
}
