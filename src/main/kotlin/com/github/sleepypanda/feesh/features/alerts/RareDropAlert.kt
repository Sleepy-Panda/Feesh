package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.RareDropEvent
import com.github.sleepypanda.feesh.constants.RareDropTypes
import com.github.sleepypanda.feesh.constants.RareDrops
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object RareDropAlert {
    fun init() {
        EventBus.subscribe(RareDropEvent::class, ::onDrop)
    }

    private fun onDrop(event: RareDropEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareDrops) return

        val itemName = event.itemName
        var dropInfo = RareDrops.rareDrops.find { it.itemName == event.itemName } ?: return

        val type = try {
            RareDropTypes.valueOf(itemName.uppercase().replace(" ", "_"))
        } catch (_: IllegalArgumentException) {
            return
        }

        if (!Alerts.alertOnRareDropTypes.contains(type)) return

        val playerName = PlayerUtils.getName()
        val title = dropInfo.displayName
        CommonUtils.showTitle(title, playerName)
        SoundUtils.playSound()
    }
}
