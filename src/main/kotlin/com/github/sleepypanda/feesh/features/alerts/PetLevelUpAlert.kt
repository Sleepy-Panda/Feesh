package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.PetLevelUpEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object PetLevelUpAlert {
    fun init() {
        EventBus.subscribe(PetLevelUpEvent::class, ::onPetLevelUp)
    }

    private fun onPetLevelUp(event: PetLevelUpEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnPetLevelUp) return

        CommonUtils.showTitle("${event.petDisplayName} ${RESET}${WHITE}is maxed", "${WHITE}Level ${event.level}")
        SoundUtils.playSound()
    }
}
