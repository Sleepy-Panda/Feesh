package com.github.sleepypanda.feesh.features.sounds

import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.resources.Identifier

object MuteJadeDragonSound {
    @JvmStatic
    fun shouldCancel(soundId: Identifier?): Boolean {
        if (!WorldRendering.muteJadeDragon) return false
        if (!WorldUtils.isInSkyblock() || !isInGalatea() || 
            (WorldUtils.getZoneName() != WorldUtils.DRAGON_LAIR && WorldUtils.getZoneName() != WorldUtils.MURKWATER_DEPTHS)
        ) return false
        if (soundId == null || soundId.namespace != "minecraft" || !soundId.path.startsWith("entity.ender_dragon.")) return false

        return true
    }

    private fun isInGalatea(): Boolean {
        return WorldUtils.getWorldName() == WorldUtils.GALATEA || WorldUtils.getWorldName() == WorldUtils.MOONGLADE_MARSH
    }
}
