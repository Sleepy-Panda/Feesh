package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundEvent
import net.minecraft.resources.ResourceLocation

object SoundUtils {
    const val SOUNDS_IDENTIFIER_PREFIX = "feesh"

    fun playSound(sound: SoundEvent = SoundEvents.EXPERIENCE_ORB_PICKUP, skipSoundModeCheck: Boolean = false) {
        if (General.soundMode == SoundMode.OFF && !skipSoundModeCheck) return

        val mc = FeeshMod.mc
        mc.soundManager.play(SimpleSoundInstance.forUI(sound, 1.0f, 1.0f))
    }
    
    fun playCustomSound(fileName: String?, skipSoundModeCheck: Boolean = false) {
        if (General.soundMode == SoundMode.OFF && !skipSoundModeCheck) return

        if (fileName.isNullOrBlank()) return
        
        CommonUtils.runWithCatching("Failed to play sound from file name: $fileName") {
            // Use file name as-is (remove .ogg extension)
            val nameWithoutExtension = fileName.removeSuffix(".ogg")
            val identifier = ResourceLocation.fromNamespaceAndPath(SOUNDS_IDENTIFIER_PREFIX, nameWithoutExtension)
            val soundEvent = SoundEvent.createVariableRangeEvent(identifier)
            playSound(soundEvent)
        }
    }
}