package com.github.sleepypanda.feesh.features.sounds

import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.resources.ResourceLocation

object MuteJadeDragonSound {
    @JvmStatic
    fun shouldCancel(soundId: ResourceLocation?): Boolean {
        if (soundId == null || soundId.namespace != "minecraft" || !soundId.path.startsWith("entity.ender_dragon.")) return false
        if (!WorldRendering.muteJadeDragon) return false
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.GALATEA || 
            (WorldUtils.getZoneName() != WorldUtils.DRAGON_LAIR && WorldUtils.getZoneName() != WorldUtils.MURKWATER_DEPTHS)
        ) return false

        return true
    }
}
