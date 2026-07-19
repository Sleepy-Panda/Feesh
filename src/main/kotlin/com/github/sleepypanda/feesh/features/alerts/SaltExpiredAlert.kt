package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.constants.Sounds
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.SoundMode

object SaltExpiredAlert {
    // §d§lSALT§c: Your Lushlilac has expired!
    val SALT_EXPIRED_PATTERN = Regex("^SALT: Your (?<saltName>.*?) has expired!$")

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!Alerts.alertOnSaltExpired || !WorldUtils.isInSkyblock()) return
        
        CommonUtils.runWithCatching("Failed to play alert on Salt expired") {
            val matchResult = SALT_EXPIRED_PATTERN.matchEntire(event.unformattedText) ?: return@onChat
            val saltName = matchResult.groups.get("saltName")?.value ?: return@onChat
            if (saltName.isNullOrEmpty()) return@onChat

            val cleanSaltName = saltName.removeFormatting()
            CommonUtils.showTitle("${RED}${cleanSaltName} ${RED}has expired")

            if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(Sounds.FEESH_NOTIFICATION_BELL)
            else SoundUtils.playSound()
        }
    }
}