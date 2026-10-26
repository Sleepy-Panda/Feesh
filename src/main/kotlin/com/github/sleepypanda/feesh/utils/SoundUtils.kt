package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.Sounds
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import com.github.sleepypanda.feesh.utils.data.CustomSoundsManager
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundEvent
import net.minecraft.resources.Identifier

object SoundUtils {
    const val SOUNDS_IDENTIFIER_PREFIX = "feesh"

    fun playSound(sound: SoundEvent = SoundEvents.EXPERIENCE_ORB_PICKUP, pitch: Float = 1.0f, volume: Float = 1.0f, skipSoundModeCheck: Boolean = false) {
        if (General.soundMode == SoundMode.OFF && !skipSoundModeCheck) return

        val mc = FeeshMod.mc
        mc.soundManager.play(SimpleSoundInstance.forUI(sound, pitch, volume))
    }
    
    fun playCustomSound(fileName: String?, skipSoundModeCheck: Boolean = false) {
        if (General.soundMode == SoundMode.OFF && !skipSoundModeCheck) return

        if (fileName.isNullOrBlank()) return
        
        val file = CustomSoundsManager.resolveUserSoundFile(fileName)
        if (file != null) {
            CommonUtils.runWithCatching("Failed to play sound file: ${file.name}") {
                FeeshMod.mc.soundManager.play(FileSoundInstance(file))
            }
            return
        }

        val bundled = Sounds.BUNDLED_FILE_NAMES.any { it.equals(fileName, ignoreCase = true) }
        if (!bundled) {
            ChatUtils.sendLocalChat(
                "${RED}Sound ${YELLOW}$fileName${RED} not found. Put the .ogg file in ${YELLOW}${CustomSoundsManager.soundsDir.absolutePath}",
                true,
            )
            return
        }

        CommonUtils.runWithCatching("Failed to play sound from file name: $fileName") {
            val nameWithoutExtension = fileName.removeSuffix(".ogg")
            val identifier = Identifier.fromNamespaceAndPath(SOUNDS_IDENTIFIER_PREFIX, nameWithoutExtension)
            val soundEvent = SoundEvent.createVariableRangeEvent(identifier)
            playSound(soundEvent)
        }
    }
}