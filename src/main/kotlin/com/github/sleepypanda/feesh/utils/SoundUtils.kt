package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

object SoundUtils {
    fun playSound() {
        val mc = FeeshMod.mc
        val currentPlayer = mc.player
        val currentWorld = mc.world
        if (currentPlayer == null || currentWorld == null) return;

        currentWorld.playSound(currentPlayer, currentPlayer.blockPos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f)
    }
}