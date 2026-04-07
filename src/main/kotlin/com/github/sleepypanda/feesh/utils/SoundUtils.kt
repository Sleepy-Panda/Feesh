package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import net.minecraft.sound.SoundEvents
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.client.sound.PositionedSoundInstance

object SoundUtils {
    val SOUNDS_IDENTIFIER_PREFIX = "feesh"

    fun playSound(sound: SoundEvent = SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, skipSoundModeCheck: Boolean = false) {
        if (General.soundMode == SoundMode.OFF && !skipSoundModeCheck) return

        val mc = FeeshMod.mc
        val soundInstance = PositionedSoundInstance.master(sound, 1.0f, 1.0f)

        mc.soundManager.play(soundInstance)
    }
    
    fun playCustomSound(fileName: String?, skipSoundModeCheck: Boolean = false) {
        if (General.soundMode == SoundMode.OFF && !skipSoundModeCheck) return

        if (fileName == null || fileName.isBlank()) return
        
        CommonUtils.runWithCatching("Failed to play sound from file name: $fileName") {
            // Use file name as-is (remove .ogg extension)
            val nameWithoutExtension = fileName.removeSuffix(".ogg")
            val identifier = Identifier.of(SOUNDS_IDENTIFIER_PREFIX, nameWithoutExtension)
            val soundEvent = SoundEvent.of(identifier)
            playSound(soundEvent)
        }
    }
}