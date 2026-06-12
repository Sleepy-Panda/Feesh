package com.github.sleepypanda.feesh.features.sounds

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.client.resources.sounds.SoundInstance

object MuteFrostySnowballs {
    private const val SNOWBALL_THROW_SOUND = "entity.snow_golem.shoot"
    // Check for entity.snow_golem.shoot?

    @JvmStatic
    fun shouldCancel(soundId: ResourceLocation?, sound: SoundInstance): Boolean {
        if (!WorldRendering.muteSnowGolemSnowball) return false
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return false
        //if (soundId == null || soundId.namespace != "minecraft" || soundId.path != SNOWBALL_THROW_SOUND) return false
        if (soundId == null || soundId.namespace != "minecraft" || !soundId.path.contains("snow")) return false
        FeeshMod.LOGGER.info(soundId.path)
        return true
    }
}
