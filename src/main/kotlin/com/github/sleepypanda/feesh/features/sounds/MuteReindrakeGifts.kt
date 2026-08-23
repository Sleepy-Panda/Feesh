package com.github.sleepypanda.feesh.features.sounds

import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.resources.Identifier

object MuteReindrakeGifts {
    private val mutedSounds = mapOf(
        "minecraft:item.totem.use" to 0.5f
    )

    fun init() {
    }

    @JvmStatic
    fun shouldCancel(soundId: Identifier?, volume: Float): Boolean {
        if (!WorldRendering.muteReindrakeGifts) return false
        if (!isInJerryWorkshop()) return false
        if (soundId == null) return false

        val fullSoundPath = "${soundId.namespace}:${soundId.path}"
        val expectedVolume = mutedSounds[fullSoundPath] ?: return false
        return volume == expectedVolume
    }

    private fun isInJerryWorkshop(): Boolean {
        return WorldUtils.isInSkyblock() && WorldUtils.getWorldName() == WorldUtils.JERRY_WORKSHOP
    }
}
