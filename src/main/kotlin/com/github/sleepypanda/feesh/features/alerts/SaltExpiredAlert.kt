package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import net.minecraft.text.Text
import kotlin.text.MatchResult

object SaltExpiredAlert {
    // §d§lSALT§c: Your Lushlilac has expired!
    private const val SALT_EXPIRED_PATTERN = "^SALT: Your (?<saltName>.*?) has expired!$"

    fun init() {
        RegisterUtils.chat(Regex(SALT_EXPIRED_PATTERN)) { _, matchResult -> 
            val saltName = matchResult.groups.get("saltName")?.value ?: return@chat
            playAlertOnSaltExpired(saltName)
        }
    }

    private fun playAlertOnSaltExpired(saltName: String) {
        try {
            if (!Alerts.alertOnSaltExpired || !WorldUtils.isInSkyblock() || saltName.isNullOrEmpty()) return

            val cleanSaltName = saltName.removeFormatting()
            val title = "${RED}${cleanSaltName} ${RED}has expired"
            CommonUtils.showTitle(title)
            SoundUtils.playSound()
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("Failed to play alert on Salt expired", e)
        }
    }
}