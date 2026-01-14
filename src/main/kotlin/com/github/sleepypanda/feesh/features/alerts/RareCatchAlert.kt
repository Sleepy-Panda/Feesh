package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypes
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.PartyChatEvent
import com.github.sleepypanda.feesh.events.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.data.CustomSoundsManager
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.constants.Sounds
import net.minecraft.text.Text
import kotlin.text.MatchResult

object RareCatchAlert {
    // §9Party §8> §b[MVP§d+§b] DeadlyMetal§f: --> A YETI has spawned <--
    // §9Компания §8> §b[MVP] PivoTheSadFisher§f: --> A Deep Sea Orb has dropped <--
    // §9Party §8> §6[MVP§3++§6] vadim31§f: --> A THE LOCH EMPEROR has spawned <--
    val FEESH_PCHAT_PATTERN = Regex("^--> (A|An) (?<uppercaseScName>(.*)) has spawned <--$")
    val FEESH_PCHAT_DH_PATTERN = Regex("^--> DOUBLE HOOK! Two (?<uppercaseScName>(.*))s have spawned <--$")
    val SH_PCHAT_PATTERN = Regex("^(?<dh>(DOUBLE HOOK: )?)I caught (a|an) (?<scName>(.*))\\!$")

    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onOwnSeaCreature)
        EventBus.subscribe(PartyChatEvent::class, ::onPartyChatSeaCreature)
    }

    private fun onOwnSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareSeaCreatures) return

        val playerName = PlayerUtils.getFormattedName() ?: return
        val seaCreatureName = event.seaCreatureName
        val isDoubleHook = event.isDoubleHook
        showAlert(seaCreatureName, isDoubleHook, playerName)
    }

    private fun onPartyChatSeaCreature(event: PartyChatEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareSeaCreatures) return

        val playerName = PlayerUtils.getName() ?: ""
        if (!playerName.isNullOrEmpty() && event.rankAndPlayer.removeFormatting().contains(playerName, ignoreCase = true)) return

        val message = event.messagePayload.removeFormatting()
        
        if (FEESH_PCHAT_PATTERN.containsMatchIn(message)) {
            val match = FEESH_PCHAT_PATTERN.matchEntire(message) ?: return
            val uppercaseSeaCreatureName = match.groups.get("uppercaseScName")?.value ?: return
            showAlert(uppercaseSeaCreatureName, false, playerName)
        } else if (FEESH_PCHAT_DH_PATTERN.containsMatchIn(message)) {
            val match = FEESH_PCHAT_DH_PATTERN.matchEntire(message) ?: return
            val uppercaseSeaCreatureName = match.groups.get("uppercaseScName")?.value ?: return
            showAlert(uppercaseSeaCreatureName, true, playerName)
        } else if (SH_PCHAT_PATTERN.containsMatchIn(message)) {
            val match = SH_PCHAT_PATTERN.matchEntire(message) ?: return
            val dh = match.groups.get("dh")?.value ?: return
            var seaCreatureName = match.groups.get("scName")?.value ?: return
            if (seaCreatureName == "The Sea Emperor") seaCreatureName = "The Loch Emperor"
            val isDoubleHook = dh.isNotEmpty()
            showAlert(seaCreatureName, isDoubleHook, playerName)
        }
    }

    private fun showAlert(seaCreatureName: String, isDoubleHook: Boolean, playerName: String) {
        var seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name.uppercase() == seaCreatureName.uppercase() } ?: return
        if (!seaCreatureInfo.isRare) return

        val type = try {
            RareSeaCreatureTypes.valueOf(seaCreatureName.uppercase().replace(" ", "_"))
        } catch (_: IllegalArgumentException) {
            return
        }

        if (!Alerts.alertOnSeaCreaturesTypes.contains(type)) return

        val title = SeaCreatures.getTitle(seaCreatureInfo.name, isDoubleHook)
        CommonUtils.showTitle(title, playerName)
        
        // TODO: Play custom sound for this sea creature
        val soundData = CustomSoundsManager.getCatchSoundData(seaCreatureInfo.name)
        val soundFileName = soundData?.source
        SoundUtils.playCustomSound(Sounds.FEESH_NOTIFICATION_BELL)
        //SoundUtils.playSoundFromFileName(soundFileName)
    }
}
