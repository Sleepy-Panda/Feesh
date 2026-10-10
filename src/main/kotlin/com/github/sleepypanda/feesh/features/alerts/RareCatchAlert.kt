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
    val SH_PCHAT_PATTERN = Regex("^(?<dh>(DOUBLE HOOK: )?)I caught (a|an) (?<scName>(.*))!$")
    val SH_COCOON_PCHAT_PATTERN = Regex("^My (?<scName>(.*)) has been cocooned!$")

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

        val me = PlayerUtils.getUnformattedName()
        if (me.isNullOrEmpty()) return

        val playerName = PlayerUtils.getFormattedPlayerNameFromPartyChat(event.rankAndPlayer) ?: return
        if (playerName.isNotEmpty() && playerName.removeFormatting().contains(me)) return

        val message = event.messagePayload.removeFormatting()

        FEESH_PCHAT_PATTERN.matchEntire(message)?.let { match ->
            val seaCreatureName = match.groups["uppercaseScName"]?.value ?: return
            showCaughtAlert(seaCreatureName, false, playerName)
            return
        }
        FEESH_PCHAT_DH_PATTERN.matchEntire(message)?.let { match ->
            val seaCreatureName = match.groups["uppercaseScName"]?.value ?: return
            showCaughtAlert(seaCreatureName, true, playerName)
            return
        }
        FEESH_PCHAT_COCOON_PATTERN.matchEntire(message)?.let { match ->
            if (!Alerts.alertOnSeaCreaturesIncludeCocooned) return
            val seaCreatureName = match.groups["uppercaseScName"]?.value ?: return
            showCocoonAlert(seaCreatureName, playerName)
            return
        }
        SH_PCHAT_PATTERN.matchEntire(message)?.let { match ->
            val dh = match.groups["dh"]?.value ?: return
            val scName = getSkyHanniScName(match) ?: return
            showCaughtAlert(scName, dh.isNotEmpty(), playerName)
            return
        }
        SH_COCOON_PCHAT_PATTERN.matchEntire(message)?.let { match ->
            if (!Alerts.alertOnSeaCreaturesIncludeCocooned) return
            val scName = getSkyHanniScName(match) ?: return
            showCocoonAlert(scName, playerName)
            return
        }
    }

    private fun getSkyHanniScName(match: MatchResult): String? {
        val name = match.groups["scName"]?.value ?: return null
        return if (name == "The Sea Emperor") SeaCreatureNames.THE_LOCH_EMPEROR else name
    }

    private fun showCaughtAlert(seaCreatureName: String, isDoubleHook: Boolean, playerName: String) {
        val enabledScNames = Alerts.alertOnSeaCreaturesList.map { it.displayName }
        if (!enabledScNames.any { it.equals(seaCreatureName, ignoreCase = true) }) return

        val seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name.equals(seaCreatureName, ignoreCase = true) } ?: return

        val title = SeaCreatures.getTitle(seaCreatureInfo.name, isDoubleHook)
        CommonUtils.showTitle(title, playerName)
        
        val soundData = CustomSoundsManager.getCatchSoundData(seaCreatureInfo.name)
        val soundFileName = soundData?.source

        if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(soundFileName)
        else SoundUtils.playSound()

        if (seaCreatureInfo.name == SeaCreatureNames.NESSIE) {
            ChatUtils.sendLocalChatWithCommand("Click to warp to Murkwater Loch!", "warp murk", true)
        } else if (seaCreatureInfo.name == SeaCreatureNames.GIANT_ISOPOD) {
            ChatUtils.sendLocalChatWithCommand("Click to warp to Torrhus Springs!", "warp springs", true)
        }
    }

    private fun showCocoonAlert(seaCreatureName: String, playerName: String) {
        val enabledScNames = Alerts.alertOnSeaCreaturesList.map { it.displayName }
        if (!enabledScNames.any { it.equals(seaCreatureName, ignoreCase = true) }) return

        val seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name.equals(seaCreatureName, ignoreCase = true) } ?: return

        val title = "${seaCreatureInfo.boldDisplayName} ${RED}cocooned"
        CommonUtils.showTitle(title, playerName)

        SoundUtils.playSound()
    }
}
