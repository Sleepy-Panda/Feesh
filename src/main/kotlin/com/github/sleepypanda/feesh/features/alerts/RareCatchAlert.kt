package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.SeaCreatureCocoonedByYouEvent
import com.github.sleepypanda.feesh.events.models.PartyChatEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.settings.categories.AlertSource
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.data.CustomSoundsManager
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*

object RareCatchAlert {
    // §9Party §8> §b[MVP§d+§b] DeadlyMetal§f: --> A YETI has spawned <--
    // §9Party §8> §b[MVP§d+§b] DeadlyMetal§f: --> A YETI was cocooned <--
    // §9Компания §8> §b[MVP] PivoTheSadFisher§f: --> A Deep Sea Orb has dropped <--
    // §9Party §8> §6[MVP§3++§6] vadim31§f: --> A THE LOCH EMPEROR has spawned <--
    val FEESH_PCHAT_PATTERN = Regex("^--> (A|An) (?<uppercaseScName>(.*)) has spawned (.*)<--$")
    val FEESH_PCHAT_DH_PATTERN = Regex("^--> DOUBLE HOOK! Two (?<uppercaseScName>(.*))s have spawned (.*)<--$")
    val FEESH_PCHAT_COCOON_PATTERN = Regex("^--> (A|An) (?<uppercaseScName>(.*)) was cocooned (.*)<--$")
    val SH_PCHAT_PATTERN = Regex("^(?<dh>(DOUBLE HOOK: )?)I caught (a|an) (?<scName>(.*))\\!$")

    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onOwnSeaCreature)
        EventBus.subscribe(PartyChatEvent::class, ::onPartyChatSeaCreature)
        EventBus.subscribe(SeaCreatureCocoonedByYouEvent::class, ::onSeaCreatureCocooned)
    }

    private fun onOwnSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareSeaCreatures) return

        val playerName = PlayerUtils.getFormattedNameWithoutPrefix() ?: return
        val seaCreatureName = event.seaCreatureName
        val isDoubleHook = event.isDoubleHook
        showCaughtAlert(seaCreatureName, isDoubleHook, playerName)
    }

    private fun onSeaCreatureCocooned(event: SeaCreatureCocoonedByYouEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareSeaCreatures || !Alerts.alertOnSeaCreaturesIncludeCocooned) return

        val playerName = PlayerUtils.getFormattedNameWithoutPrefix() ?: return
        showCocoonAlert(event.seaCreatureName, playerName)
    }

    private fun onPartyChatSeaCreature(event: PartyChatEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareSeaCreatures || Alerts.alertOnRareSeaCreaturesSource != AlertSource.OWN_AND_PARTY) return

        val me = PlayerUtils.getName() ?: return
        val playerName = PlayerUtils.getFormattedPlayerNameFromPartyChat(event.rankAndPlayer) ?: return
        if (!playerName.isNullOrEmpty() && !me.isNullOrEmpty() && playerName.removeFormatting().contains(me)) return

        val message = event.messagePayload.removeFormatting()
        
        if (FEESH_PCHAT_PATTERN.containsMatchIn(message)) {
            val match = FEESH_PCHAT_PATTERN.matchEntire(message) ?: return
            val uppercaseSeaCreatureName = match.groups.get("uppercaseScName")?.value ?: return
            showCaughtAlert(uppercaseSeaCreatureName, false, playerName)
        } else if (FEESH_PCHAT_DH_PATTERN.containsMatchIn(message)) {
            val match = FEESH_PCHAT_DH_PATTERN.matchEntire(message) ?: return
            val uppercaseSeaCreatureName = match.groups.get("uppercaseScName")?.value ?: return
            showCaughtAlert(uppercaseSeaCreatureName, true, playerName)
        } else if (FEESH_PCHAT_COCOON_PATTERN.containsMatchIn(message)) {
            if (!Alerts.alertOnSeaCreaturesIncludeCocooned) return
            val match = FEESH_PCHAT_COCOON_PATTERN.matchEntire(message) ?: return
            val uppercaseSeaCreatureName = match.groups.get("uppercaseScName")?.value ?: return
            showCocoonAlert(uppercaseSeaCreatureName, playerName)
        } else if (SH_PCHAT_PATTERN.containsMatchIn(message)) {
            val match = SH_PCHAT_PATTERN.matchEntire(message) ?: return
            val dh = match.groups.get("dh")?.value ?: return
            var seaCreatureName = match.groups.get("scName")?.value ?: return
            if (seaCreatureName == "The Sea Emperor") seaCreatureName = SeaCreatureNames.THE_LOCH_EMPEROR
            val isDoubleHook = dh.isNotEmpty()
            showCaughtAlert(seaCreatureName, isDoubleHook, playerName)
        }
    }

    private fun showCaughtAlert(seaCreatureName: String, isDoubleHook: Boolean, playerName: String) {
        val enabledScNames = Alerts.alertOnSeaCreaturesList.map { it.displayName }
        if (!enabledScNames.contains(seaCreatureName)) return

        var seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name.equals(seaCreatureName, ignoreCase = true) } ?: return

        val title = SeaCreatures.getTitle(seaCreatureInfo.name, isDoubleHook)
        CommonUtils.showTitle(title, playerName)
        
        val soundData = CustomSoundsManager.getCatchSoundData(seaCreatureInfo.name)
        val soundFileName = soundData?.source

        if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(soundFileName)
        else SoundUtils.playSound()

        if (seaCreatureInfo.name == SeaCreatureNames.NESSIE) {
            ChatUtils.sendLocalChatWithCommand("Click to warp to Murkwater Loch!", "warp murk", true)
        }
    }

    private fun showCocoonAlert(seaCreatureName: String, playerName: String) {
        val enabledScNames = Alerts.alertOnSeaCreaturesList.map { it.displayName }
        if (!enabledScNames.contains(seaCreatureName)) return

        var seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name.equals(seaCreatureName, ignoreCase = true) } ?: return

        val title = "${seaCreatureInfo.boldDisplayName} ${RED}cocooned"
        CommonUtils.showTitle(title, playerName)

        SoundUtils.playSound()
    }
}
