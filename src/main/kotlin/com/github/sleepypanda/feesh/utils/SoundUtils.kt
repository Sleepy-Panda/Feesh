package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundInstance

object SoundUtils {
    val SOUNDS_IDENTIFIER_PREFIX = "feesh"

    fun playSound(sound: SoundEvent = SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP) {
        if (General.soundMode == SoundMode.OFF) return

        val mc = FeeshMod.mc
        val currentPlayer = mc.player ?: return
        val soundId = sound.id
        val soundInstance = PositionedSoundInstance(
            soundId,
            SoundCategory.MASTER,
            1.0f,
            1.0f,
            currentPlayer.random,
            false,
            0,
            SoundInstance.AttenuationType.LINEAR,
            currentPlayer.x,
            currentPlayer.y,
            currentPlayer.z,
            false
        )

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