package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.world.entity.player.Player
import java.util.UUID
import kotlin.math.pow

object HidePlayersNearBobber {
    private const val IN_RANGE_NO_DELAY = Long.MAX_VALUE

    private val hiddenPlayers = mutableMapOf<UUID, Long>() // player ID -> timestamp (ms) when they may be shown again

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    /**
     * Returns true if the given player entity should not be rendered (hidden).
     * Hides other players within range of the local player's fishing hook while it is cast.
     * Players stay hidden for [WorldRendering.hidePlayersNearBobberUnhideDelay] after leaving range
     * or after the hook despawns, to avoid flicker while recasting or swapping to a weapon.
     */
    @JvmStatic
    fun shouldHidePlayer(entity: Player): Boolean {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return false
        if (!WorldRendering.hidePlayersNearBobber) return false
        if (entity == FeeshMod.mc.player) return false
        if (!isPlayer(entity)) return false

        val unhideAt = hiddenPlayers[entity.uuid] ?: return false
        if (unhideAt == IN_RANGE_NO_DELAY) return true

        val now = System.currentTimeMillis()
        if (now < unhideAt) return true

        hiddenPlayers.remove(entity.uuid)
        return false
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld() || !WorldRendering.hidePlayersNearBobber) {
            clearHiddenPlayers()
            return
        }
        refreshHiddenPlayers()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        clearHiddenPlayers()
    }

    fun onUnhideDelayChanged() {
        clearHiddenPlayers()
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld() || !WorldRendering.hidePlayersNearBobber) return
        refreshHiddenPlayers()
    }

    private fun isPlayer(entity: Player): Boolean {
        val version = entity.uuid.version()
        return version == 4 || version == 1
    }

    private fun refreshHiddenPlayers() {
        val now = System.currentTimeMillis()
        val unhideDelay = WorldRendering.hidePlayersNearBobberUnhideDelay.coerceAtLeast(0)

        if (unhideDelay == 0) {
            hiddenPlayers.clear()
        }

        pruneExpiredHiddenPlayers(now)

        if (FishingHookUtils.getActiveFishingHook() == null) return

        val hook = FishingHookUtils.getLastActiveFishingHook() ?: return
        val localPlayer = FeeshMod.mc.player ?: return
        val world = FeeshMod.mc.level ?: return

        val maxDistanceSqr = WorldRendering.hidePlayersNearBobberDistance.toDouble().pow(2)

        for (player in world.players()) {
            if (player == localPlayer || !isPlayer(player)) continue

            val distanceSqr = EntityUtils.getDistanceSqr(
                hook.x, hook.y, hook.z,
                player.x, player.y, player.z,
            )
            if (distanceSqr > maxDistanceSqr) continue

            hiddenPlayers[player.uuid] = if (unhideDelay > 0) {
                now + unhideDelay * 1000L
            } else {
                IN_RANGE_NO_DELAY
            }
        }
    }

    private fun pruneExpiredHiddenPlayers(now: Long = System.currentTimeMillis()) {
        hiddenPlayers.entries.removeIf { (_, unhideAt) ->
            unhideAt != IN_RANGE_NO_DELAY && now >= unhideAt
        }
    }

    private fun clearHiddenPlayers() {
        hiddenPlayers.clear()
    }
}
