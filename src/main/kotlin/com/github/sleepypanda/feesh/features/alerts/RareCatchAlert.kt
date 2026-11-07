package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.SeaCreatures.SeaCreatureInfo
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

object RareCatchAlert {
    // {rarityColor} is the sea creature name in its rarity color, without bold.
    // A formatting code directly before it (e.g. §l) is applied to the name, because a color code would otherwise reset it.
    const val DEFAULT_TITLE = "§l{rarityColor}§r"
    const val DEFAULT_TITLE_DOUBLE_HOOK = "§l{rarityColor}§r §c§lX2§r"
    const val DEFAULT_SUBTITLE = "{playerName}"
    const val DEFAULT_SUBTITLE_DOUBLE_HOOK = "{playerName}"
    const val DEFAULT_COCOON_TITLE = "§l{rarityColor} §ccocooned"
    const val DEFAULT_CHAT_MESSAGE = "{rarityColor}§r"
    const val DEFAULT_CHAT_MESSAGE_DOUBLE_HOOK = "{rarityColor}§r §c§lX2§r"
    const val DEFAULT_DURATION_SECONDS = 2

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
        showCaughtAlert(seaCreatureName, isDoubleHook, playerName, fromParty = false)
    }

    private fun onSeaCreatureCocooned(event: SeaCreatureCocoonedByYouEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareSeaCreatures || !Alerts.alertOnSeaCreaturesIncludeCocooned) return

        val playerName = PlayerUtils.getFormattedNameWithoutPrefix() ?: return
        showCocoonAlert(event.seaCreatureName, playerName, fromParty = false)
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
            showCaughtAlert(seaCreatureName, false, playerName, fromParty = true)
            return
        }
        FEESH_PCHAT_DH_PATTERN.matchEntire(message)?.let { match ->
            val seaCreatureName = match.groups["uppercaseScName"]?.value ?: return
            showCaughtAlert(seaCreatureName, true, playerName, fromParty = true)
            return
        }
        FEESH_PCHAT_COCOON_PATTERN.matchEntire(message)?.let { match ->
            if (!Alerts.alertOnSeaCreaturesIncludeCocooned) return
            val seaCreatureName = match.groups["uppercaseScName"]?.value ?: return
            showCocoonAlert(seaCreatureName, playerName, fromParty = true)
            return
        }
        SH_PCHAT_PATTERN.matchEntire(message)?.let { match ->
            val dh = match.groups["dh"]?.value ?: return
            val scName = getSkyHanniScName(match) ?: return
            showCaughtAlert(scName, dh.isNotEmpty(), playerName, fromParty = true)
            return
        }
        SH_COCOON_PCHAT_PATTERN.matchEntire(message)?.let { match ->
            if (!Alerts.alertOnSeaCreaturesIncludeCocooned) return
            val scName = getSkyHanniScName(match) ?: return
            showCocoonAlert(scName, playerName, fromParty = true)
            return
        }
    }

    private fun getSkyHanniScName(match: MatchResult): String? {
        val name = match.groups["scName"]?.value ?: return null
        return if (name == "The Sea Emperor") SeaCreatureNames.THE_LOCH_EMPEROR else name
    }

    private fun showCaughtAlert(seaCreatureName: String, isDoubleHook: Boolean, playerName: String, fromParty: Boolean) {
        val enabledScNames = Alerts.alertOnSeaCreaturesList.map { it.displayName }
        if (!enabledScNames.any { it.equals(seaCreatureName, ignoreCase = true) }) return

        val seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name.equals(seaCreatureName, ignoreCase = true) } ?: return

        val alertText = when {
            fromParty && isDoubleHook -> Alerts.rareCatchPartyDoubleHookAlert
            fromParty -> Alerts.rareCatchPartyAlert
            isDoubleHook -> Alerts.rareCatchOwnDoubleHookAlert
            else -> Alerts.rareCatchOwnAlert
        }
        val titleFallback = if (isDoubleHook) DEFAULT_TITLE_DOUBLE_HOOK else DEFAULT_TITLE
        val subtitleFallback = if (isDoubleHook) DEFAULT_SUBTITLE_DOUBLE_HOOK else DEFAULT_SUBTITLE
        showRareAlertTitle(
            applyPattern(alertText.titleTemplate, titleFallback, seaCreatureInfo, playerName),
            applyPattern(alertText.subtitleTemplate, subtitleFallback, seaCreatureInfo, playerName),
        )

        if (Alerts.rareCatchChatMessage) {
            val chatTemplate = if (isDoubleHook) Alerts.rareCatchChatMessageDoubleHookTemplate else Alerts.rareCatchChatMessageTemplate
            val chatFallback = if (isDoubleHook) DEFAULT_CHAT_MESSAGE_DOUBLE_HOOK else DEFAULT_CHAT_MESSAGE
            ChatUtils.sendLocalChat(applyPattern(chatTemplate, chatFallback, seaCreatureInfo, playerName), true)
        }

        val soundData = CustomSoundsManager.getCatchSoundData(seaCreatureInfo.name)
        val soundFileName = soundData?.source

        if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(soundFileName)
        else SoundUtils.playMcSound()

        if (seaCreatureInfo.name == SeaCreatureNames.NESSIE) {
            ChatUtils.sendLocalChatWithCommand("Click to warp to Murkwater Loch!", "warp murk", true)
        } else if (seaCreatureInfo.name == SeaCreatureNames.GIANT_ISOPOD) {
            ChatUtils.sendLocalChatWithCommand("Click to warp to Torrhus Springs!", "warp springs", true)
        }
    }

    private fun showCocoonAlert(seaCreatureName: String, playerName: String, fromParty: Boolean) {
        val enabledScNames = Alerts.alertOnSeaCreaturesList.map { it.displayName }
        if (!enabledScNames.any { it.equals(seaCreatureName, ignoreCase = true) }) return

        val seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name.equals(seaCreatureName, ignoreCase = true) } ?: return

        val alertText = if (fromParty) Alerts.rareCatchPartyCocoonAlert else Alerts.rareCatchOwnCocoonAlert
        showRareAlertTitle(
            applyPattern(alertText.titleTemplate, DEFAULT_COCOON_TITLE, seaCreatureInfo, playerName),
            applyPattern(alertText.subtitleTemplate, DEFAULT_SUBTITLE, seaCreatureInfo, playerName),
        )

        SoundUtils.playMcSound()
    }

    private fun showRareAlertTitle(title: String, subtitle: String) {
        CommonUtils.showTitle(title, subtitle, stay = Alerts.rareCatchAlertDurationSeconds * 20)
    }

    private fun applyPattern(pattern: String, fallback: String, seaCreatureInfo: SeaCreatureInfo, playerName: String): String {
        val template = pattern.ifEmpty { fallback }
            .replace("{playerName}", playerName)
        val withFormatting = FORMATS_BEFORE_RARITY_COLOR.replace(template) { match ->
            seaCreatureInfo.rarityColorCode + match.groupValues[1] + seaCreatureInfo.name
        }
        return withFormatting.replace("{rarityColor}", seaCreatureInfo.displayName)
    }

    private val FORMATS_BEFORE_RARITY_COLOR = Regex("((?:§[klmno])+)\\{rarityColor\\}")
}
