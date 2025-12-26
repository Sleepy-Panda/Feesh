package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypes
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*

object RareCatchAlert {
    fun init() {
        // TODO: DOUBLE HOOK
        // TODO: Add Vanquisher
        // TODO: Add party source
        // TODO: Add SH format
        // TODO: Check ANY reindrake logic
        // TODO: 
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onOwnSeaCreature)
    }

    private fun onOwnSeaCreature(event: OwnSeaCreatureCaughtEvent) {
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

        val isDoubleHook = event.isDoubleHook
        val playerName = PlayerUtils.getName()
        val title = getTitle(seaCreatureInfo.boldDisplayName, rarityColorCode, isDoubleHook)
        CommonUtils.showTitle(title, playerName)
        SoundUtils.playSound()
    }

    private fun getTitle(boldDisplayName: String, rarityColorCode: String, isDoubleHook: Boolean): String {
        val dh = if (isDoubleHook) " ${RESET}${RED}${BOLD}X2${RESET}" else ""
        return if (rarityColorCode == MYTHIC.code) "${GOLD}${OBFUSCATED}x${RESET} ${boldDisplayName}${dh} ${GOLD}${OBFUSCATED}x${RESET}" 
        else "${boldDisplayName}${RESET}${dh}${RESET}"
    }
}
