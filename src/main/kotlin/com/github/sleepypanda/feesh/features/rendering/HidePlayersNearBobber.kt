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
    private val hiddenPlayers = mutableMapOf<UUID, Long>() // hidden player ID -> timestamp (ms) when they may be shown again

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
        val unhideDelaySeconds = WorldRendering.hidePlayersNearBobberUnhideDelay.coerceAtLeast(0)

        if (unhideDelaySeconds == 0) {
            hiddenPlayers.clear()
        }

        pruneExpiredHiddenPlayers(now)

        if (FishingHookUtils.getActiveFishingHook() == null) return

        val hook = FishingHookUtils.getLastActiveFishingHook() ?: return
        val localPlayer = FeeshMod.mc.player ?: return
        val world = FeeshMod.mc.level ?: return

        val maxDistanceSqr = WorldRendering.hidePlayersNearBobberDistance.toDouble().pow(2)

        for (player in world.players().filter { it != localPlayer && isPlayer(it) }) {
            val distanceSqr = EntityUtils.getDistanceSqr(
                hook.x, hook.y, hook.z,
                player.x, player.y, player.z,
            )
            if (distanceSqr > maxDistanceSqr) continue

            hiddenPlayers[player.uuid] = unhideAt(now, unhideDelaySeconds)
        }
    }

    private fun unhideAt(now: Long, unhideDelaySeconds: Int): Long {
        return now + if (unhideDelaySeconds > 0) {
            unhideDelaySeconds * 1000L
        } else {
            100L // Some time to keep player hidden until next tick
        }
    }

    private fun pruneExpiredHiddenPlayers(now: Long = System.currentTimeMillis()) {
        hiddenPlayers.entries.removeIf { (_, unhideAt) -> now >= unhideAt }
    }

    private fun clearHiddenPlayers() {
        hiddenPlayers.clear()
    }
}
