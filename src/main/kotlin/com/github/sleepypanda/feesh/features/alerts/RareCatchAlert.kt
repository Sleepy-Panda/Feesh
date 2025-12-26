package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypes
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.SeaCreatureSpawnedEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object RareCatchAlert {
    const val REINDRAKE_PATTERN = "^WOAH\\! A Reindrake was summoned from the depths\\!$"

    fun init() {
        // TODO: DOUBLE HOOK
        // TODO: Add Vanquisher
        // TODO: Add party source
        // TODO: Add SH format
        // TODO: Check ANY reindrake logic
        // TODO: 
        EventBus.subscribe(SeaCreatureSpawnedEvent::class, ::onSeaCreature)

        val reindrake = SeaCreatures.allSeaCreatures.find { it.name == "Reindrake" }!!
        RegisterUtils.chat(Regex(REINDRAKE_PATTERN)) { _, _ -> onAnyReindrake(reindrake.name, reindrake.rarityColorCode) }
    }

    private fun onSeaCreature(event: SeaCreatureSpawnedEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareSeaCreatures) return

        val seaCreatureName = event.seaCreatureName
        var seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name == event.seaCreatureName } ?: return
        if (!seaCreatureInfo.isRare) return

        val rarityColorCode = seaCreatureInfo.rarityColorCode

        val type = try {
            RareSeaCreatureTypes.valueOf(seaCreatureName.uppercase().replace(" ", "_"))
        } catch (_: IllegalArgumentException) {
            return
        }

        if (!Alerts.alertOnSeaCreaturesTypes.contains(type)) return

        val isDoubleHook = ChatUtils.isDoubleHook()
        val playerName = PlayerUtils.getName()
        val seaCreatureDisplayName = getSeaCreatureDisplayName(seaCreatureName, rarityColorCode)
        val title = if (isDoubleHook) "${seaCreatureDisplayName} ${RED}${BOLD}X2" else seaCreatureDisplayName
        CommonUtils.showTitle(title, playerName)
        SoundUtils.playSound()
    }

    private fun onAnyReindrake(seaCreatureName: String, rarityColorCode: String) {
        if (seaCreatureName.isNullOrEmpty() || rarityColorCode.isNullOrEmpty()) return
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnAnyReindrake) return

        CommonUtils.showTitle(getSeaCreatureDisplayName(seaCreatureName, rarityColorCode))
        SoundUtils.playSound()
    }

    private fun getSeaCreatureDisplayName(seaCreatureName: String, rarityColorCode: String): String {
        return if (rarityColorCode == MYTHIC.code) "${YELLOW}${OBFUSCATED}x${RESET} ${rarityColorCode}${BOLD}${seaCreatureName}${RESET} ${YELLOW}${OBFUSCATED}x${RESET}" 
        else "${rarityColorCode}${BOLD}${seaCreatureName}"
    }
}
