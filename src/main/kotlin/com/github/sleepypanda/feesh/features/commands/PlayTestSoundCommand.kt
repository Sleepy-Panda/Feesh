package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent

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

    // Any language letters/digits, space, - _ ( ) . ' !
    private val OGG_FILE_NAME = Regex("""^[\p{L}\p{N} _\-().'!]+\.ogg$""", RegexOption.IGNORE_CASE)

    fun init() {
        RegisterUtils.command(COMMAND_NAME, ::handleCommand)
    }

    private fun handleCommand(args: Array<String>) {
        if (args.isEmpty()) {
            ChatUtils.sendLocalChat("${RED}Usage: /${COMMAND_NAME} <fileName.ogg|mc:soundName>", true)
            return
        }
        
        val soundParam = args.joinToString(" ")

        when {
            OGG_FILE_NAME.matches(soundParam) -> {
                CommonUtils.runWithCatching(
                    message = "Failed to play custom sound: $soundParam",
                    onError = {
                        ChatUtils.sendLocalChat("${RED}Failed to play custom sound: ${YELLOW}$soundParam${RED}. Check logs for details.", true)
                    }
                ) {
                    SoundUtils.playCustomSound(soundParam, bypassSoundModeCheck = true)
                }
            }
            soundParam.startsWith("mc:") -> {
                val soundName = soundParam.removePrefix("mc:")
                CommonUtils.runWithCatching(
                    message = "Failed to play Minecraft sound: $soundParam",
                    onError = {
                        ChatUtils.sendLocalChat("${RED}Failed to play Minecraft sound: ${YELLOW}$soundName${RED}. Check logs for details.", true)
                    }
                ) {
                    val soundEvent = getSoundEvent(soundName)
                    SoundUtils.playMcSound(soundEvent, bypassSoundModeCheck = true)
                }
            }
            else -> {
                ChatUtils.sendLocalChat(
                    "${RED}Invalid argument: ${YELLOW}$soundParam${RED}. Use ${YELLOW}file name.ogg${RED} for custom sounds or ${YELLOW}mc:soundName${RED} for Minecraft sounds.",
                    true,
                )
            }
        }
    } 

    private fun getSoundEvent(soundName: String): SoundEvent {
        val path = soundName.lowercase()
        val id = Identifier.fromNamespaceAndPath("minecraft", path)
        return SoundEvent.createVariableRangeEvent(id)
    }
}
