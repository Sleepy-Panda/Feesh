package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatBasedRareDropEvent
import com.github.sleepypanda.feesh.constants.RareDropTypes
import com.github.sleepypanda.feesh.constants.AlertableRareDrops
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

object RareDropAlert {
    const val DEFAULT_OWN_TITLE_TEMPLATE = "{dropName} (+{price})"
    const val DEFAULT_OWN_SUBTITLE_TEMPLATE = "#{dropNumber}, +{magicFind} Magic Find"
    const val DEFAULT_PARTY_TITLE_TEMPLATE = "{dropName} (+{price})"
    const val DEFAULT_PARTY_SUBTITLE_TEMPLATE = "{playerName}"

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
            val itemName = event.itemNameUnformatted
            val playerName = PlayerUtils.getFormattedNameWithoutPrefix() ?: return@onOwnDrop

            showAlert(
                itemName = itemName,
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
    
            showAlert(
                itemName = itemName,
                playerName = playerName,
                isOwnDrop = false,
                magicFind = Regex("\\+(\\d+)").find(metadata)?.groupValues?.get(1).orEmpty(),
                dropNumber = Regex("#(\\d+)").find(metadata)?.groupValues?.get(1).orEmpty()
            )
        }
    }

    private fun showAlert(itemName: String, playerName: String, isOwnDrop: Boolean, magicFind: String, dropNumber: String) {
        val dropInfo = AlertableRareDrops.rareDrops.find { it.itemName == itemName || it.alternateNames.contains(itemName) } ?: return
        val type = RareDropTypes.values().find { it.displayName == dropInfo.itemName } ?: return // Rare drop not supported by the mod
    
        if (!Alerts.alertOnRareDropTypes.contains(RareDropTypes.ALL) && !Alerts.alertOnRareDropTypes.contains(type)) return

        val price = getPrice(dropInfo.id, dropInfo.npcPrice)
        val priceStr = if (price > 0.0) CommonUtils.toShortNumber(price).orEmpty() else ""

        val values = mapOf(
            "dropName" to dropInfo.getTitle(),
            "playerName" to playerName,
            "price" to priceStr,
            "dropNumber" to dropNumber,
            "magicFind" to magicFind
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

        val title = applyTemplate(titleTemplate, values)
        val subtitle = applyTemplate(subtitleTemplate, values)
        CommonUtils.showTitle(title, subtitle.ifEmpty { null })
        
        val soundData = CustomSoundsManager.getDropSoundData(dropInfo.id)
        val soundFileName = soundData?.source

        if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(soundFileName)
        // Do not play MC sound in other cases because SB already plays rare drop sound for those items
    }

    private fun applyTemplate(template: String, values: Map<String, String>): String {
        var result = template
        if (values["price"].isNullOrEmpty()) {
            result = result.replace(" (+{price})", "").replace("(+{price})", "")
        }
        if (values["magicFind"].isNullOrEmpty()) {
            result = result
                .replace(", +{magicFind} Magic Find", "")
                .replace("+{magicFind} Magic Find", "")
        }
        if (values["dropNumber"].isNullOrEmpty()) {
            result = result.replace("#{dropNumber}, ", "").replace("#{dropNumber}", "")
        }
        values.forEach { (key, value) ->
            result = result.replace("{$key}", value)
        }
        return result.trim().trim(',', ' ')
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
