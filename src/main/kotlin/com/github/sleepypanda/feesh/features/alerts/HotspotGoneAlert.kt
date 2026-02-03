package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.HotspotUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import net.minecraft.entity.projectile.FishingBobberEntity

object HotspotGoneAlert {
    private var lastClosestHotspot: HotspotUtils.HotspotData? = null
    private var tickCounter = 0
    private const val TICKS_PER_CHECK = 10
    private const val HOTSPOT_CHECK_RANGE = 20.0

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        lastClosestHotspot = null
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!Alerts.alertOnHotspotGone || !WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return

        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        playAlertOnHotspotGone()
    }

    private fun playAlertOnHotspotGone() {
        try {
            if (!Alerts.alertOnHotspotGone || !WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return

            val player = FeeshMod.mc.player ?: return
            val nearbyHotspots = HotspotUtils.findHotspotsInRange(player, HOTSPOT_CHECK_RANGE)

            // If player moved away from the last hotspot, clear it
            if (lastClosestHotspot != null) {
                val distance = EntityUtils.getDistance(player, lastClosestHotspot!!.entity)
                if (distance > HOTSPOT_CHECK_RANGE) {
                    lastClosestHotspot = null
                }
            }

            // Check if the last hotspot is gone
            if (lastClosestHotspot != null) {
                val distance = EntityUtils.getDistance(player, lastClosestHotspot!!.entity)
                if (distance <= HOTSPOT_CHECK_RANGE) {
                    val hotspotStillExists = nearbyHotspots.any { hotspot ->
                        hotspot.x == lastClosestHotspot!!.x &&
                        hotspot.y == lastClosestHotspot!!.y &&
                        hotspot.z == lastClosestHotspot!!.z
                    }

                    if (!hotspotStillExists) {
                        playAlert(lastClosestHotspot!!.perk)
                        lastClosestHotspot = null
                    }
                }
            }

            // Update lastClosestHotspot if fishing hook is active
            val isHookActive = EntityUtils.isFishingHookActive(player)
            if (isHookActive) {
                val playerHook = EntityUtils.getPlayersFishingHook()
                if (playerHook != null) {
                    val closestHotspot = HotspotUtils.findClosestHotspotInRange(playerHook, HotspotUtils.HOTSPOT_RANGE)
                    if (closestHotspot != null) {
                        lastClosestHotspot = closestHotspot
                    }
                }
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to check if Hotspot is gone", e)
        }
    }

    private fun playAlert(perk: String?) {
        CommonUtils.showTitle("${LIGHT_PURPLE}Hotspot ${RED}is gone")
        ChatUtils.sendLocalChat("${perk ?: ""} ${RESET}${LIGHT_PURPLE}Hotspot ${WHITE}is gone, time to find another one!", true)
        SoundUtils.playSound()
    }
}

