package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.SeaCreatures.SeaCreatureInfo
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.SeaCreatureCocoonedByYouEvent
import com.github.sleepypanda.feesh.events.models.PartyChatEvent
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.settings.categories.AlertSource
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.SeaCreatureAlertTemplateSettings
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import com.github.sleepypanda.feesh.utils.AlertUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.data.CustomSoundsManager

object RareCatchAlert {
    const val DEFAULT_TITLE = "{scRarityColor}§l{scName}§r"
    const val DEFAULT_SUBTITLE = "{playerName}"
    const val DEFAULT_TITLE_DOUBLE_HOOK = "{scRarityColor}§l{scName}§r §c§lX2§r"
    const val DEFAULT_SUBTITLE_DOUBLE_HOOK = "{playerName}"
    const val DEFAULT_TITLE_COCOON = "{scRarityColor}§l{scName} §ccocooned"
    const val DEFAULT_SUBTITLE_COCOON = "{playerName}"
    const val DEFAULT_DURATION_TICKS = 50 // 2.5 seconds

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

        CommonUtils.runWithCatching("Failed to show own sea creature caught alert") {
            val playerName = PlayerUtils.getFormattedNameWithoutPrefix() ?: return
            val seaCreatureName = event.seaCreatureName
            val isDoubleHook = event.isDoubleHook
            showCaughtAlert(seaCreatureName, isDoubleHook, playerName, isOwn = true)
        }
    }

    private fun onSeaCreatureCocooned(event: SeaCreatureCocoonedByYouEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareSeaCreatures || !Alerts.alertOnSeaCreaturesIncludeCocooned) return

        CommonUtils.runWithCatching("Failed to show own sea creature cocooned alert") {
            val playerName = PlayerUtils.getFormattedNameWithoutPrefix() ?: return
            showCocoonAlert(event.seaCreatureName, playerName, isOwn = true)
        }
    }

    private fun onPartyChatSeaCreature(event: PartyChatEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareSeaCreatures || Alerts.alertOnRareSeaCreaturesSource != AlertSource.OWN_AND_PARTY) return

        CommonUtils.runWithCatching("Failed to show party sea creature caught alert") {
            val me = PlayerUtils.getUnformattedName()
            if (me.isNullOrEmpty()) return
    
            val playerName = PlayerUtils.getFormattedPlayerNameFromPartyChat(event.rankAndPlayer) ?: return
            if (playerName.isNotEmpty() && playerName.removeFormatting().contains(me)) return
    
            val message = event.messagePayload.removeFormatting()
    
            FEESH_PCHAT_PATTERN.matchEntire(message)?.let { match ->
                val seaCreatureName = match.groups["uppercaseScName"]?.value ?: return
                showCaughtAlert(seaCreatureName, false, playerName, isOwn = false)
                return
            }
            FEESH_PCHAT_DH_PATTERN.matchEntire(message)?.let { match ->
                val seaCreatureName = match.groups["uppercaseScName"]?.value ?: return
                showCaughtAlert(seaCreatureName, true, playerName, isOwn = false)
                return
            }
            FEESH_PCHAT_COCOON_PATTERN.matchEntire(message)?.let { match ->
                if (!Alerts.alertOnSeaCreaturesIncludeCocooned) return
                val seaCreatureName = match.groups["uppercaseScName"]?.value ?: return
                showCocoonAlert(seaCreatureName, playerName, isOwn = false)
                return
            }
            SH_PCHAT_PATTERN.matchEntire(message)?.let { match ->
                val dh = match.groups["dh"]?.value ?: return
                val scName = getSkyHanniScName(match) ?: return
                showCaughtAlert(scName, dh.isNotEmpty(), playerName, isOwn = false)
                return
            }
            SH_COCOON_PCHAT_PATTERN.matchEntire(message)?.let { match ->
                if (!Alerts.alertOnSeaCreaturesIncludeCocooned) return
                val scName = getSkyHanniScName(match) ?: return
                showCocoonAlert(scName, playerName, isOwn = false)
                return
            }
        }
    }

    private fun getSkyHanniScName(match: MatchResult): String? {
        val name = match.groups["scName"]?.value ?: return null
        return if (name == "The Sea Emperor") SeaCreatureNames.THE_LOCH_EMPEROR else name
    }

    private fun showCaughtAlert(seaCreatureName: String, isDoubleHook: Boolean, playerName: String, isOwn: Boolean) {
        val seaCreatureInfo = findEnabledSeaCreature(seaCreatureName) ?: return

        val template = when {
            isOwn && isDoubleHook -> Alerts.rareCatchOwnDoubleHookAlertTemplate
            isOwn && !isDoubleHook -> Alerts.rareCatchOwnAlertTemplate
            !isOwn && isDoubleHook -> Alerts.rareCatchPartyDoubleHookAlertTemplate
            !isOwn && !isDoubleHook -> Alerts.rareCatchPartyAlertTemplate
            else -> return
        }
        val defaultTitle = if (isDoubleHook) DEFAULT_TITLE_DOUBLE_HOOK else DEFAULT_TITLE
        val defaultSubtitle = if (isDoubleHook) DEFAULT_SUBTITLE_DOUBLE_HOOK else DEFAULT_SUBTITLE

        val soundFileName = CustomSoundsManager.getCatchSoundData(seaCreatureInfo.name)?.source
        val sound = when (General.soundMode) {
            SoundMode.MEME -> AlertUtils.SoundConfig.CustomSound(soundFileName)
            SoundMode.NORMAL -> AlertUtils.SoundConfig.McSound()
            else -> null
        }
        when (seaCreatureInfo.name) {
            SeaCreatureNames.NESSIE -> ChatUtils.sendLocalChatWithCommand("Click to warp to Murkwater Loch!", "warp murk", true)
            SeaCreatureNames.GIANT_ISOPOD -> ChatUtils.sendLocalChatWithCommand("Click to warp to Torrhus Springs!", "warp springs", true)
        }

        showAlertFromTemplates(template, defaultTitle, defaultSubtitle, seaCreatureInfo, playerName, sound)
    }

    private fun showCocoonAlert(seaCreatureName: String, playerName: String, isOwn: Boolean) {
        val seaCreatureInfo = findEnabledSeaCreature(seaCreatureName) ?: return
        val template = if (isOwn) Alerts.rareCatchOwnCocoonAlertTemplate else Alerts.rareCatchPartyCocoonAlertTemplate
        showAlertFromTemplates(
            template,
            DEFAULT_TITLE_COCOON,
            DEFAULT_SUBTITLE_COCOON,
            seaCreatureInfo,
            playerName,
            AlertUtils.SoundConfig.McSound(),
        )
    }

    private fun findEnabledSeaCreature(seaCreatureName: String): SeaCreatureInfo? {
        val enabledScNames = Alerts.alertOnSeaCreaturesList.map { it.displayName }
        if (!enabledScNames.any { it.equals(seaCreatureName, ignoreCase = true) }) return null
        return SeaCreatures.allSeaCreatures.find { it.name.equals(seaCreatureName, ignoreCase = true) }
    }

    private fun showAlertFromTemplates(
        template: SeaCreatureAlertTemplateSettings,
        defaultTitle: String,
        defaultSubtitle: String,
        seaCreatureInfo: SeaCreatureInfo,
        playerName: String,
        sound: AlertUtils.SoundConfig?,
    ) {
        AlertUtils.alert(
            title = AlertUtils.TitleConfig(
                title = buildStringFromTemplate(template.titleTemplate, defaultTitle, seaCreatureInfo, playerName),
                subtitle = buildStringFromTemplate(template.subtitleTemplate, defaultSubtitle, seaCreatureInfo, playerName),
                stay = Alerts.rareCatchAlertDurationTicks,
            ),
            sound = sound,
            priority = AlertUtils.AlertPriority.HIGH,
        )
    }

    private fun buildStringFromTemplate(
        templateLines: Array<String>,
        defaultTemplate: String,
        seaCreatureInfo: SeaCreatureInfo,
        playerName: String,
    ): String {
        val template = (templateLines.firstOrNull() ?: "").ifEmpty { defaultTemplate }
        return template
            .replace("{playerName}", playerName)
            .replace("{scName}", seaCreatureInfo.name)
            .replace("{scRarityColor}", seaCreatureInfo.rarityColorCode)
    }
}
