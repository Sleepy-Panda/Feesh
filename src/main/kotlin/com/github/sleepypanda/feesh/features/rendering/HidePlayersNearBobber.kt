package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import net.minecraft.entity.player.PlayerEntity

object HidePlayersNearBobber {
    /**
     * Returns true if the given player entity should not be rendered (hidden).
     * Hides other players when the local player has fishing rod cast and the player
     * is within the configured distance from the local player's fishing hook.
     */
    @JvmStatic
    fun shouldHidePlayer(entity: PlayerEntity): Boolean {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return false
        if (!WorldRendering.hidePlayersNearBobber) return false

        val localPlayer = FeeshMod.mc.player ?: return false
        if (entity == localPlayer) return false

        if (!(entity.uuid.version() == 4 || entity.uuid.version() == 1)) return false // Exclude NPCs

        val hook = FishingHookUtils.getFishingHook() ?: return false
        val distanceBlocks = EntityUtils.getDistance(hook.x, hook.y, hook.z, entity.x, entity.y, entity.z)
        return distanceBlocks <= WorldRendering.hidePlayersNearBobberDistance
    }
}
