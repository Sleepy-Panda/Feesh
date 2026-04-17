package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.NearbyEntitiesCounterTypes
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3
import java.util.UUID

object NearbyEntitiesCounter {
    private var playersCount = 0
    private var fishingHooksCount = 0
    private var chumcapBucketsCount = 0
    private var tickCounter = 0

    private const val LEGION_DISTANCE = 30.0
    private const val MAX_LEGION_COUNT = 20
    private const val BOBBING_TIME_DISTANCE = 30.0
    private const val MAX_BOBBING_TIME_COUNT = 5
    private const val CHUMCAP_BUCKET_DISTANCE = 30.0
    private const val MAX_CHUMCAP_BUCKETS_COUNT = 4
    private const val TICKS_PER_CHECK = 10

    private val gui = FeeshGui()
        .setCoordsDataKey("nearbyEntitiesCounter")
        .setClickable(false)
        .setSampleLines(listOf(
            "${LIGHT_PURPLE}${BOLD}Legion${GRAY}: ${WHITE}2 ${GRAY}players",
            "${LIGHT_PURPLE}${BOLD}Bobbin' Time${GRAY}: ${WHITE}3 ${GRAY}hooks",
            "${UNCOMMON}${BOLD}Chumcap${GRAY}: ${WHITE}2 ${GRAY}buckets"
        ))
        .setSettingsKey { Overlays.nearbyEntitiesCounterOverlay }
        .setApplyCustomStyleKey { Overlays.nearbyEntitiesCounterCustomStyle }
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
        chumcapBucketsCount = 0
        gui.clearLines()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        if (!Overlays.nearbyEntitiesCounterOverlay ||
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
        chumcapBucketsCount = getChumcapBucketsCount()
    }

    private fun getPlayersCount(): Int {
        if (!Overlays.nearbyEntitiesCounterTypes.contains(NearbyEntitiesCounterTypes.LEGION)) return 0

        val player = FeeshMod.mc.player ?: return 0
        val world = FeeshMod.mc.level ?: return 0

        val players = world.players()
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
        if (!Overlays.nearbyEntitiesCounterTypes.contains(NearbyEntitiesCounterTypes.BOBBING_TIME)) return 0

        val player = FeeshMod.mc.player ?: return 0
        val world = FeeshMod.mc.level ?: return 0

        val fishingHooks = world.entitiesForRendering()
            .filterIsInstance<FishingHook>()
            .filter { hook ->
                val distance = EntityUtils.getDistance(player, hook)
                if (distance > BOBBING_TIME_DISTANCE) return@filter false

                val owner = hook.owner
                if (owner == null) return@filter true

                val ownerName = owner.name.string ?: ""
                return@filter !ownerName.contains("Phantom Fisher", ignoreCase = true)
            }

        return fishingHooks.size
    }

    private fun getPlayerPing(client: Minecraft, uuid: UUID): Int {
        return client.connection?.getPlayerInfo(uuid)?.latency ?: 0
    }

    private fun getChumcapBucketsCount(): Int {
        if (!Overlays.nearbyEntitiesCounterTypes.contains(NearbyEntitiesCounterTypes.CHUMCAP_BUCKETS)) return 0
        if (WorldUtils.getWorldName() == WorldUtils.CRIMSON_ISLE) return 0
        
        val player = FeeshMod.mc.player ?: return 0
        val buckets = EntityUtils.getArmorStandsInRange(Vec3(player.x, player.y, player.z), CHUMCAP_BUCKET_DISTANCE, "Chumcap Bucket", allowContains = true)
        return buckets.size
    }

    private fun updateGuiLines() {
        val types = Overlays.nearbyEntitiesCounterTypes
        val lines = mutableListOf<String>()

        if (types.contains(NearbyEntitiesCounterTypes.LEGION)) {
            val playersColor = if (playersCount >= MAX_LEGION_COUNT) GREEN else WHITE
            val playersText = "${LIGHT_PURPLE}${BOLD}Legion${GRAY}: ${playersColor}${playersCount} ${GRAY}${if (playersCount == 1) "player" else "players"}"
            lines.add(playersText)
        }

        if (types.contains(NearbyEntitiesCounterTypes.BOBBING_TIME)) {
            val hooksColor = if (fishingHooksCount >= MAX_BOBBING_TIME_COUNT) GREEN else WHITE
            val hooksText = "${LIGHT_PURPLE}${BOLD}Bobbin' Time${GRAY}: ${hooksColor}${fishingHooksCount} ${GRAY}${if (fishingHooksCount == 1) "hook" else "hooks"}"
            lines.add(hooksText)
        }

        if (types.contains(NearbyEntitiesCounterTypes.CHUMCAP_BUCKETS) && WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) {
            val bucketsColor = if (chumcapBucketsCount >= MAX_CHUMCAP_BUCKETS_COUNT) GREEN else WHITE
            val bucketsText = "${UNCOMMON}${BOLD}Chumcap${GRAY}: ${bucketsColor}${chumcapBucketsCount} ${GRAY}${if (chumcapBucketsCount == 1) "bucket" else "buckets"}"
            lines.add(bucketsText)
        }

        gui.setLines(lines)
    }
}