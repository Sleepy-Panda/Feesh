package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.HotspotSpawnedEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object HotspotSpawnedAlert {
    fun init() {
        EventBus.subscribe(HotspotSpawnedEvent::class, ::onHotspotSpawned)
    }

    private fun onHotspotSpawned(event: HotspotSpawnedEvent) {
        if (!Alerts.alertOnHotspotSeen || !WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld()) return

        CommonUtils.runWithCatching("Failed to announce Hotspot spawned") {
            val location = CommonUtils.getFormattedLocation(event.position.x, event.position.y, event.position.z)
            ChatUtils.sendLocalChat("${event.perk} ${RESET}${LIGHT_PURPLE}Hotspot ${WHITE}spawned at ${location}!", true)
        }
    }
}
