package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import net.minecraft.sounds.SoundEvents

object OutOfAirAlert {
    private var hasAlerted = false
    private var tickCounter = 0
    private const val TICKS_PER_CHECK = 20
    private const val AIR_PERCENT_THRESHOLD = 15.0

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        hasAlerted = false
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!Alerts.alertOnOutOfAir || !WorldUtils.isInSkyblock()) return
        if (WorldUtils.getWorldName() != WorldUtils.GALATEA && WorldUtils.getWorldName() != WorldUtils.MOONGLADE_MARSH && WorldUtils.getWorldName() != WorldUtils.TORRHUS_CANYON) return

        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        CommonUtils.runWithCatching("Failed to check out of air state") {
            val player = FeeshMod.mc.player ?: return
            val maxAir = player.maxAirSupply
            if (maxAir <= 0) return

            val airPercent = player.airSupply.toDouble() * 100.0 / maxAir

            if (airPercent > AIR_PERCENT_THRESHOLD) {
                hasAlerted = false
                return
            }

            if (hasAlerted) return

            hasAlerted = true
            ChatUtils.sendLocalChat("${WHITE}You are almost out of air!", true)
            CommonUtils.showTitle("${RED}Out of air soon!")
            SoundUtils.playSound(SoundEvents.PLAYER_HURT_DROWN)
        }
    }
}
