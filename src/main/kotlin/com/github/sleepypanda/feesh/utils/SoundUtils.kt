package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.sound.SoundEvent
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
}