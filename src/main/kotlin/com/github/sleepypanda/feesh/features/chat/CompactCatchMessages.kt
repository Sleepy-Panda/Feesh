package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ColorUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.HexColorCodes
import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.SeaCreatures.SeaCreatureInfo
import com.github.sleepypanda.feesh.settings.categories.Chat as ChatSettings
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import net.minecraft.network.chat.Component

object CompactCatchMessages {
    const val DEFAULT_DOUBLE_HOOK_TEMPLATE = "§b§lDOUBLE HOOK!"
    const val DEFAULT_CATCH_TEMPLATE = "§7{Article} {sc} §7has spawned!"

    private val SAMPLE_SEA_CREATURE = SeaCreatures.allSeaCreatures.find { it.name == SeaCreatureNames.LORD_JAWBUS }!!

    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
    }

    fun sendTestChatMessage() {
        ChatUtils.sendLocalChat(buildCatchMessage(SAMPLE_SEA_CREATURE, isDoubleHook = true))
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!WorldUtils.isInSkyblock() || !ChatSettings.compactSeaCreaturesMessages) return

        CommonUtils.runWithCatching("Failed to send compact sea creature catch message") {
            ChatUtils.sendLocalChat(buildCatchMessage(event.seaCreatureInfo, event.isDoubleHook))
        }
    }

    private fun buildCatchMessage(seaCreatureInfo: SeaCreatureInfo, isDoubleHook: Boolean): Component {
        val catchTemplate = (ChatSettings.compactCatchMessageTemplate.firstOrNull() ?: "").ifEmpty { DEFAULT_CATCH_TEMPLATE }
        val articleLower = CommonUtils.getArticle(seaCreatureInfo.name, makeLowerCase = true)
        val articleCapitalized = CommonUtils.getArticle(seaCreatureInfo.name, makeLowerCase = false)
        val templateWithArticles = catchTemplate
            .replace("{Article}", articleCapitalized)
            .replace("{article}", articleLower)

        val parts = templateWithArticles.split("{sc}")
        val catchPart = Component.empty()
        parts.forEachIndexed { index, part ->
            if (index > 0) catchPart.append(buildSeaCreatureName(seaCreatureInfo))
            catchPart.append(Component.literal(part))
        }

        return if (isDoubleHook) {
            val dhTemplate = (ChatSettings.compactDoubleHookMessageTemplate.firstOrNull() ?: "").ifEmpty { DEFAULT_DOUBLE_HOOK_TEMPLATE }
            Component.literal("$dhTemplate ").append(catchPart)
        } else {
            catchPart
        }
    }

    private fun buildSeaCreatureName(seaCreatureInfo: SeaCreatureInfo): Component {
        if (!ChatSettings.compactSeaCreaturesMessagesUseGradientColors) {
            return Component.literal(seaCreatureInfo.boldDisplayName)
        }

        val hexColor = HexColorCodes.getHexColorForRarity(seaCreatureInfo.rarityColorCode)
        return if (hexColor != null) {
            ColorUtils.createGradientText(seaCreatureInfo.name, hexColor.gradientColorCode1, hexColor.gradientColorCode2, bold = true)
        } else {
            Component.literal(seaCreatureInfo.boldDisplayName)
        }
    }
}
