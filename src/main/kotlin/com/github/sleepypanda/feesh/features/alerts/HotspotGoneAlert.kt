package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
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
import java.util.Timer
import kotlin.concurrent.timerTask

object HotspotGoneAlert {
    private const val HOTSPOT_CHECK_RANGE = 30.0

    fun init() {
        EventBus.subscribe(ArmorStandDespawnedEvent::class, ::onHotspotDespawned)
    }

    private fun onHotspotDespawned(event: ArmorStandDespawnedEvent) {
        if (!Alerts.alertOnHotspotGone || !WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return
        val hotspot = HotspotUtils.getRetainedLastFishedHotspot() ?: return
        if (hotspot.armorStandUuid != event.armorStand.uuid) return
        if (event.armorStand.customName.getUnformattedString() != "HOTSPOT") return

        val player = FeeshMod.mc.player ?: return
        val distance = EntityUtils.getDistance(player.x, player.y, player.z, hotspot.x, hotspot.y, hotspot.z)
        if (distance > HOTSPOT_CHECK_RANGE) return

        val perk = hotspot.perk

        Timer(true).schedule(timerTask {
            FeeshMod.mc.execute {
                if (FeeshMod.mc.level == null) return@execute // Leaving the world or game
                playAlert(perk)
            }
        }, 150) // Armor stands despawn before a server change. Wait so world unload can cancel the alert.
    }

    private fun playAlert(perk: String?) {
        CommonUtils.showTitle("${LIGHT_PURPLE}Hotspot ${RED}is gone")
        ChatUtils.sendLocalChat("${perk ?: ""} ${RESET}${LIGHT_PURPLE}Hotspot ${WHITE}is gone, time to find another one!", true)
        SoundUtils.playMcSound()
    }
}
