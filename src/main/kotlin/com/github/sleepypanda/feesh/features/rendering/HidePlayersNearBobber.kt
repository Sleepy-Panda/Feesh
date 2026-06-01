package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import net.minecraft.world.entity.player.Player
import kotlin.math.pow

object HidePlayersNearBobber {
    /**
     * Returns true if the given player entity should not be rendered (hidden).
     * Hides other players when the local player has fishing rod casted and the player
     * is within the configured distance from the local player's fishing hook.
     * Also applies an optional delay to keep players hidden for a while after the fishing hook disappears, to avoid flickering while recasting.
     */
    @JvmStatic
    fun shouldHidePlayer(entity: Player): Boolean {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return false
        if (!WorldRendering.hidePlayersNearBobber) return false

        val localPlayer = FeeshMod.mc.player ?: return false
        if (entity == localPlayer) return false

        val version = entity.uuid.version()
        if (!(version == 4 || version == 1)) return false // Exclude NPCs and mobs

        val unhideDelay = WorldRendering.hidePlayersNearBobberUnhideDelay.coerceAtLeast(0)
        val wasFishingHookActive = FishingHookUtils.getActiveFishingHook() != null ||
            (unhideDelay > 0 && FishingHookUtils.wasFishingHookActiveSecondsAgo(unhideDelay))
        if (!wasFishingHookActive) return false

        val hook = FishingHookUtils.getLastActiveFishingHook() ?: return false
        val maxDistanceSqr = WorldRendering.hidePlayersNearBobberDistance.toDouble().pow(2)
        val actualDistanceSqr = EntityUtils.getDistanceSqr(hook.x, hook.y, hook.z, entity.x, entity.y, entity.z)
        
        return actualDistanceSqr <= maxDistanceSqr
    }
}
