package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.HotspotUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ArmorStandDespawnedEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldUnloadEvent
import java.util.Timer
import java.util.UUID
import kotlin.concurrent.timerTask
import net.minecraft.world.phys.Vec3

object HotspotGoneAlert {
    private var lastClosestHotspot: HotspotUtils.HotspotData? = null
    private var tickCounter = 0
    private const val TICKS_PER_CHECK = 10
    private const val HOTSPOT_CHECK_RANGE = 30.0
    private const val NEAREST_HOTSPOT_RANGE_FROM_HOOK = 5.0

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldUnloadEvent::class, ::onWorldChanged)
        EventBus.subscribe(ArmorStandDespawnedEvent::class, ::onHotspotDespawned)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldUnloadEvent) {
        lastClosestHotspot = null
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!Alerts.alertOnHotspotGone || !WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return

        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        trackLatestHotspot()
    }

    private fun onHotspotDespawned(@Suppress("UNUSED_PARAMETER") event: ArmorStandDespawnedEvent) {
        if (!Alerts.alertOnHotspotGone || !WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return
        val hotspot = lastClosestHotspot ?: return
        if (hotspot.entity.uuid != event.armorStand.uuid) return
        if (event.armorStand.customName.getUnformattedString() != "HOTSPOT") return

        val player = FeeshMod.mc.player ?: return
        val distance = EntityUtils.getDistance(player, hotspot.entity)
        if (distance > HOTSPOT_CHECK_RANGE) return

        val perk = hotspot.perk
        val hotspotUuid: UUID = event.armorStand.uuid

        Timer(true).schedule(timerTask {
            FeeshMod.mc.execute {
                if (FeeshMod.mc.level == null) return@execute // Leaving the world or game
                if (lastClosestHotspot == null) return@execute
                if (lastClosestHotspot!!.entity.uuid != hotspotUuid) return@execute
                playAlert(perk)
                lastClosestHotspot = null
            }
        }, 150) // If player changes server, it causes armor stand to despawn before, but we don't need alert. Let onWorldChanged trigger first.
    }

    private fun trackLatestHotspot() {
        CommonUtils.runWithCatching("Failed to track latest Hotspot") {
            if (!Alerts.alertOnHotspotGone || !WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return

            val isHookActive = FishingHookUtils.isFishingHookSubmerged()
            if (!isHookActive) return

            val playerHook = FishingHookUtils.getSubmergedFishingHook() ?: return
            val closestHotspot = HotspotUtils.findClosestHotspotInRange(Vec3(playerHook.x, playerHook.y, playerHook.z), NEAREST_HOTSPOT_RANGE_FROM_HOOK) ?: return

            lastClosestHotspot = closestHotspot
        }
    }

    private fun playAlert(perk: String?) {
        CommonUtils.showTitle("${LIGHT_PURPLE}Hotspot ${RED}is gone")
        ChatUtils.sendLocalChat("${perk ?: ""} ${RESET}${LIGHT_PURPLE}Hotspot ${WHITE}is gone, time to find another one!", true)
        SoundUtils.playSound()
    }
}
