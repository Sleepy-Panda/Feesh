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

    fun playMcSound(sound: SoundEvent = SoundEvents.EXPERIENCE_ORB_PICKUP, pitch: Float = 1.0f, volume: Float = 1.0f, bypassSoundModeCheck: Boolean = false) {
        if (General.soundMode == SoundMode.OFF && !bypassSoundModeCheck) return

        val mc = FeeshMod.mc
        mc.soundManager.play(SimpleSoundInstance.forUI(sound, pitch, volume))
    }
    
    fun playCustomSound(fileName: String?, bypassSoundModeCheck: Boolean = false) {
        if (General.soundMode == SoundMode.OFF && !bypassSoundModeCheck) return

        if (fileName.isNullOrBlank()) return

        // Files added by the player to the /sounds folder
        val customUserFile = CustomSoundsManager.resolveUserSoundFile(fileName)
        if (customUserFile != null) {
            CommonUtils.runWithCatching("Failed to play sound file: ${customUserFile.name}") {
                FeeshMod.mc.soundManager.play(FileSoundInstance(customUserFile))
            }
            return
        }

        // TODO: Re-enable this when we don't want to support sounds from the legacy resource pack
        //val bundledModFile = Sounds.BUNDLED_FILE_NAMES.any { it.equals(fileName, ignoreCase = true) }
        //if (!bundledModFile) {
        //    ChatUtils.sendLocalChat(
        //        "${RED}Sound ${YELLOW}$fileName${RED} not found. Make sure to put the .ogg file in ${YELLOW}${CustomSoundsManager.soundsDir.path}${RED}!",
        //        true,
        //    )
        //    return
        //}

        // Files from embedded mod assets, or legacy resource pack
        CommonUtils.runWithCatching("Failed to play sound file: $fileName") {
            val nameWithoutExtension = fileName.removeSuffix(".ogg")
            val identifier = Identifier.fromNamespaceAndPath(SOUNDS_IDENTIFIER_PREFIX, nameWithoutExtension)
            val soundEvent = SoundEvent.createVariableRangeEvent(identifier)
            playMcSound(soundEvent, volume = 0.8f)
        }
    }
}
