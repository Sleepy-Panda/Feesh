package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.client.MinecraftClient
import java.util.UUID

object LegionBobbingTimeTracker {
    private var playersCount = 0
    private var fishingHooksCount = 0
    private var tickCounter = 0

    private const val LEGION_DISTANCE = 30.0
    private const val MAX_LEGION_COUNT = 20
    private const val BOBBING_TIME_DISTANCE = 30.0
    private const val MAX_BOBBING_TIME_COUNT = 5
    private const val TICKS_PER_CHECK = 10

    private val gui = FeeshGui()
        .setCoordsDataKey("legionBobbingTimeTracker")
        .setClickable(false)
        .setSampleLines(listOf(
            "${LIGHT_PURPLE}${BOLD}Legion${GRAY}: ${WHITE}2 ${GRAY}players",
            "${LIGHT_PURPLE}${BOLD}Bobbin' Time${GRAY}: ${WHITE}3 ${GRAY}hooks"
        ))
        .setSettingsKey { Overlays.legionBobbingTimeTrackerOverlay }
        .setCondition {
            PlayerUtils.hasFishingRodInHotbar() &&
            WorldUtils.isInFishingWorld()
        }

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        playersCount = 0
        fishingHooksCount = 0
        gui.clearLines()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        if (!Overlays.legionBobbingTimeTrackerOverlay ||
            !WorldUtils.isInSkyblock() ||
            !PlayerUtils.hasFishingRodInHotbar() ||
            !WorldUtils.isInFishingWorld()
        ) {
            gui.clearLines()
            return
        }

        trackPlayersAndFishingHooksNearby()
        updateGuiLines()
    }

    private fun trackPlayersAndFishingHooksNearby() {
        fishingHooksCount = getFishingHooksCount()
        playersCount = getPlayersCount()
    }

    private fun getPlayersCount(): Int {
        val player = FeeshMod.mc.player ?: return 0
        val world = FeeshMod.mc.world ?: return 0

        val players = world.players
            .filter { it ->
                it.uuid != player.uuid &&
                (it.uuid.version() == 4 || it.uuid.version() == 1) && // Players and Watchdog have version 4, nicked players have version 1, this is done to exclude NPCs
                getPlayerPing(FeeshMod.mc, it.uuid) > 0 // -1 is watchdog and ghost players, also there is a ghost player with high ping value when joining a world
            }
            .filter { it ->
                val distance = EntityUtils.getDistance(player, it)
                return@filter distance <= LEGION_DISTANCE
            }
            .distinctBy { it -> it.uuid }

        return players.size
    }

    private fun getFishingHooksCount(): Int {
        val player = FeeshMod.mc.player ?: return 0
        val world = FeeshMod.mc.world ?: return 0

        val fishingHooks = world.entities
            .filterIsInstance<FishingBobberEntity>()
            .filter { hook ->
                val distance = EntityUtils.getDistance(player, hook)
                if (distance > BOBBING_TIME_DISTANCE) return@filter false

                val owner = hook.playerOwner
                if (owner == null) return@filter true

                val ownerName = owner.displayName?.string ?: return@filter true
                return@filter !ownerName.contains("Phantom Fisher", ignoreCase = true)
            }

        return fishingHooks.size
    }

    private fun getPlayerPing(client: MinecraftClient, uuid: UUID): Int {
        return client.networkHandler?.getPlayerListEntry(uuid)?.latency ?: 0
    }

    private fun updateGuiLines() {
        val playersColor = if (playersCount >= MAX_LEGION_COUNT) GREEN else WHITE
        val playersText = "${LIGHT_PURPLE}${BOLD}Legion${GRAY}: ${playersColor}${playersCount} ${GRAY}${if (playersCount == 1) "player" else "players"}"

        val hooksColor = if (fishingHooksCount >= MAX_BOBBING_TIME_COUNT) GREEN else WHITE
        val hooksText = "${LIGHT_PURPLE}${BOLD}Bobbin' Time${GRAY}: ${hooksColor}${fishingHooksCount} ${GRAY}${if (fishingHooksCount == 1) "hook" else "hooks"}"

        gui.setLines(listOf(playersText, hooksText))
    }
}