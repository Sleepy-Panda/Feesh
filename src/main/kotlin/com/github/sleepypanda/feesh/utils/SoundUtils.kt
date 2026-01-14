package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundInstance

object SoundUtils {
    fun playSound(sound: SoundEvent = SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP) {
        val mc = FeeshMod.mc
        val currentPlayer = mc.player ?: return
        val soundId = sound.id
        val soundInstance = PositionedSoundInstance(
            soundId,
            SoundCategory.PLAYERS,
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
    
    fun playCustomSound(fileName: String?) {
        if (fileName == null || fileName.isBlank()) {
            playSound() // Fallback to default
            return
        }
        
        try {
            // Use file name as-is (remove .ogg extension)
            val nameWithoutExtension = fileName.removeSuffix(".ogg")
            val identifier = Identifier.of("feesh", nameWithoutExtension)
            val soundEvent = SoundEvent.of(identifier)
            playSound(soundEvent)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to play sound from file name: $fileName", e)
            playSound() // Fallback to default
        }
    }
}