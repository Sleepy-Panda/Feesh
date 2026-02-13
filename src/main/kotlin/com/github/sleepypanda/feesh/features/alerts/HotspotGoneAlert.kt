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
import com.github.sleepypanda.feesh.events.models.ArmorStandDespawnedEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent

object HotspotGoneAlert {
    private var lastClosestHotspot: HotspotUtils.HotspotData? = null
    private var tickCounter = 0
    private const val TICKS_PER_CHECK = 10
    private const val HOTSPOT_CHECK_RANGE = 30.0
    private const val NEAREST_HOTSPOT_RANGE_FROM_HOOK = 5.0

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(ArmorStandDespawnedEvent::class, ::onHotspotDespawned)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
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
        if (lastClosestHotspot == null) return
        if (lastClosestHotspot!!.entity.uuid != event.armorStand.uuid) return
        if (event.armorStand.customName?.string != "HOTSPOT") return

        val player = FeeshMod.mc.player ?: return
        val distance = EntityUtils.getDistance(player, lastClosestHotspot!!.entity)
        if (distance > HOTSPOT_CHECK_RANGE) return

        playAlert(lastClosestHotspot!!.perk)
        lastClosestHotspot = null
    }

    private fun trackLatestHotspot() {
        try {
            if (!Alerts.alertOnHotspotGone || !WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return

            val player = FeeshMod.mc.player ?: return
            val isHookActive = EntityUtils.isFishingHookActive(player)
            if (!isHookActive) return

            val playerHook = EntityUtils.getPlayersFishingHook()
            if (playerHook == null) return

            val closestHotspot = HotspotUtils.findClosestHotspotInRange(playerHook, NEAREST_HOTSPOT_RANGE_FROM_HOOK)
            if (closestHotspot != null) {
                lastClosestHotspot = closestHotspot
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to track latest Hotspot", e)
        }
    }

    private fun playAlert(perk: String?) {
        CommonUtils.showTitle("${LIGHT_PURPLE}Hotspot ${RED}is gone")
        ChatUtils.sendLocalChat("${perk ?: ""} ${RESET}${LIGHT_PURPLE}Hotspot ${WHITE}is gone, time to find another one!", true)
        SoundUtils.playSound()
    }
}
