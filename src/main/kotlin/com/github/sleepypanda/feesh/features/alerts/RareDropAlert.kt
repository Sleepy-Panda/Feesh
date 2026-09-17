package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatBasedRareDropEvent
import com.github.sleepypanda.feesh.constants.RareDropTypes
import com.github.sleepypanda.feesh.constants.AlertableRareDropInfo
import com.github.sleepypanda.feesh.settings.categories.AlertSource
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import com.github.sleepypanda.feesh.events.models.PartyChatEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PriceUtils
import com.github.sleepypanda.feesh.utils.data.CustomSoundsManager
import com.github.sleepypanda.feesh.utils.enums.PricingModeWithNpc
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.RareDropAlertUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*

object RareDropAlert {
    val DEFAULT_OWN_TITLE_TEMPLATE = "{itemDisplayName} ${GRAY}#${WHITE}{dropNumber}"
    val DEFAULT_OWN_SUBTITLE_TEMPLATE = "${GREEN}+${GOLD}{price}"

    val DEFAULT_PARTY_TITLE_TEMPLATE = "{itemDisplayName} ${GRAY}#${WHITE}{dropNumber}"
    val DEFAULT_PARTY_SUBTITLE_TEMPLATE = "{player}"

    // §9Компания §8> §b[MVP] PivoTheSadFisher§f: --> A Deep Sea Orb has dropped <--
    // §9Party §8> §6[MVP§3++§6] vadim31§f: --> A Deep Sea Orb has dropped (#10, +365 ✯ Magic Find) <--
    val FEESH_PCHAT_PATTERN = Regex("^--> (?:A|An) (?<itemName>.+?) has dropped(?: \\((?<metadata>[^)]*)\\))? <--$")

    fun init() {
        EventBus.subscribe(ChatBasedRareDropEvent::class, ::onOwnDrop)
        EventBus.subscribe(PartyChatEvent::class, ::onPartyChatDrop)
    }

    private fun onOwnDrop(event: ChatBasedRareDropEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareDrops) return

        CommonUtils.runWithCatching("Failed to show Own Rare Drop alert") {
            val playerName = PlayerUtils.getFormattedNameWithoutPrefix() ?: return@onOwnDrop

            showAlert(
                dropInfo = event.dropInfo,
                playerName = playerName,
                isOwnDrop = true,
                magicFind = event.magicFind?.toString().orEmpty(),
                dropNumber = event.dropNumber.toString()
            )
        }
    }

    private fun onPartyChatDrop(event: PartyChatEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareDrops || Alerts.alertOnRareDropsSource != AlertSource.OWN_AND_PARTY) return

        CommonUtils.runWithCatching("Failed to show Party Chat Rare Drop alert") {
            val message = event.messagePayload.removeFormatting()
            val match = FEESH_PCHAT_PATTERN.matchEntire(message) ?: return@onPartyChatDrop
            val itemName = match.groups["itemName"]?.value ?: return@onPartyChatDrop
            val metadata = match.groups["metadata"]?.value.orEmpty()
    
            val me = PlayerUtils.getUnformattedName()
            if (me.isNullOrEmpty()) return@onPartyChatDrop
            val playerName = PlayerUtils.getFormattedPlayerNameFromPartyChat(event.rankAndPlayer) ?: return@onPartyChatDrop
            if (!playerName.isEmpty() && playerName.removeFormatting().contains(me)) return@onPartyChatDrop
    
            val dropInfo = RareDropAlertUtils.findAlertableDropInfo(itemName) ?: return@onPartyChatDrop

            showAlert(
                dropInfo = dropInfo,
                playerName = playerName,
                isOwnDrop = false,
                magicFind = Regex("\\+(\\d+) . Magic Find").find(metadata)?.groupValues?.get(1).orEmpty(), // +number ✯ Magic Find
                dropNumber = Regex("#(\\d+)").find(metadata)?.groupValues?.get(1).orEmpty() // #number
            )
        }
    }

    private fun showAlert(dropInfo: AlertableRareDropInfo, playerName: String, isOwnDrop: Boolean, magicFind: String, dropNumber: String) {
        val settingsEntry = RareDropTypes.entries.find { it.displayName == dropInfo.itemName } ?: return
        if (!Alerts.alertOnRareDropTypes.contains(RareDropTypes.ALL) && !Alerts.alertOnRareDropTypes.contains(settingsEntry)) return

        val price = getPrice(dropInfo.id, dropInfo.npcPrice)
        val priceStr = if (price > 0.0) CommonUtils.toShortNumber(price).orEmpty() else "0"

        val keysToReplace = mapOf(
            "itemDisplayName" to dropInfo.getTitle(),
            "player" to playerName,
            "price" to priceStr,
            "dropNumber" to dropNumber.ifEmpty { "?" },
            "magicFind" to magicFind.ifEmpty { "?" }
        )

        val titleTemplate = if (isOwnDrop) {
            Alerts.rareDropAlertOwnTitleTemplate.ifEmpty { DEFAULT_OWN_TITLE_TEMPLATE }
        } else {
            Alerts.rareDropAlertPartyTitleTemplate.ifEmpty { DEFAULT_PARTY_TITLE_TEMPLATE }
        }

        val subtitleTemplate = if (isOwnDrop) {
            Alerts.rareDropAlertOwnSubtitleTemplate.ifEmpty { DEFAULT_OWN_SUBTITLE_TEMPLATE }
        } else {
            Alerts.rareDropAlertPartySubtitleTemplate.ifEmpty { DEFAULT_PARTY_SUBTITLE_TEMPLATE }
        }

        val title = replaceKeysInTemplate(titleTemplate, keysToReplace)
        val subtitle = replaceKeysInTemplate(subtitleTemplate, keysToReplace)
        CommonUtils.showTitle(title, subtitle, stay = Alerts.alertOnRareDropsDurationTicks)
        
        val soundData = CustomSoundsManager.getDropSoundData(dropInfo.id)
        val soundFileName = soundData?.source

        if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(soundFileName)
        // Do not play MC sound in other cases because SB already plays rare drop sound for those items
    }

    private fun replaceKeysInTemplate(template: String, keysToReplace: Map<String, String>): String {
        var result = template

        keysToReplace.forEach { (key, value) ->
            result = result.replace("{$key}", value)
        }
        return result.trim()
    }

    // TODO: Move this into PriceUtils and reuse
    private fun getPrice(itemId: String, npcPrice: Int?): Double {
        if (Alerts.alertOnRareDropsPriceMode == PricingModeWithNpc.NPC_SELL) return npcPrice?.toDouble() ?: 0.0

        val bazaarPrices = PriceUtils.getBazaarItemPrices(itemId)
        var itemPrice = if (Alerts.alertOnRareDropsPriceMode == PricingModeWithNpc.SELL_OFFER) bazaarPrices?.sellOffer else bazaarPrices?.instaSell
        
        if (bazaarPrices == null) {
            val auctionPrices = PriceUtils.getAuctionItemPrice(itemId)
            itemPrice = auctionPrices?.lbin
        }
        
        return itemPrice ?: 0.0
    }
}
