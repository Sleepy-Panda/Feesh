package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.utils.AlertUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import net.minecraft.sounds.SoundEvents

object GoldenFishSpawnAlert {
    const val GOLDEN_FISH_SPAWNED_MESSAGE = "You spot a Golden Fish surface from beneath the lava!"

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnGoldenFishSpawn) return
        if (event.unformattedText != GOLDEN_FISH_SPAWNED_MESSAGE) return

        AlertUtils.alert(
            title = AlertUtils.TitleConfig("${WHITE}Catch the ${GOLD}Golden Fish"),
            sound = AlertUtils.SoundConfig.McSound(SoundEvents.FISH_SWIM),
            priority = AlertUtils.AlertPriority.LOW,
        )
    }
}
