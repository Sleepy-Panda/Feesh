package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import net.minecraft.sounds.SoundEvents

object GoldenFishSpawnAlert {
    val PATTERN = Regex("^You spot a Golden Fish surface from beneath the lava\\!$")

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnGoldenFishSpawn) return
        if (!PATTERN.matches(event.unformattedText)) return

        CommonUtils.showTitle("${WHITE}Catch the ${GOLD}Golden Fish")
        SoundUtils.playSound(SoundEvents.FISH_SWIM)
    }
}
