package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.RareDropEvent
import com.github.sleepypanda.feesh.constants.RareDropTypes
import com.github.sleepypanda.feesh.constants.RareDrops
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
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.PricingModeWithNpc
import com.github.sleepypanda.feesh.settings.categories.RareDropPriceScope
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting

object RareDropAlert {
    // §9Компания §8> §b[MVP] PivoTheSadFisher§f: --> A Deep Sea Orb has dropped <--
    // §9Party §8> §6[MVP§3++§6] vadim31§f: --> A Deep Sea Orb has dropped (#10, +365 ✯ Magic Find) <--
    val FEESH_PCHAT_PATTERN = Regex("^--> (A|An) (?<itemName>(.*)) has dropped (.*)<--$")

    fun init() {
        EventBus.subscribe(RareDropEvent::class, ::onOwnDrop)
        EventBus.subscribe(PartyChatEvent::class, ::onPartyChatDrop)
    }

    private fun onOwnDrop(event: RareDropEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareDrops) return

        val itemName = event.itemName
        val playerName = PlayerUtils.getFormattedNameWithoutPrefix() ?: return

        showAlert(itemName, playerName, isOwnDrop = true)
    }

    private fun onPartyChatDrop(event: PartyChatEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareDrops || Alerts.alertOnRareDropsSource != AlertSource.OWN_AND_PARTY) return

        val message = event.messagePayload.removeFormatting()

        if (!FEESH_PCHAT_PATTERN.containsMatchIn(message)) return

        val match = FEESH_PCHAT_PATTERN.matchEntire(message) ?: return
        val itemName = match.groups.get("itemName")?.value ?: return

        val me = PlayerUtils.getName() ?: return
        val playerName = PlayerUtils.getFormattedPlayerNameFromPartyChat(event.rankAndPlayer) ?: return
        if (!playerName.isNullOrEmpty() && !me.isNullOrEmpty() && playerName.removeFormatting().contains(me)) return

        showAlert(itemName, playerName, isOwnDrop = false)
    }

    private fun showAlert(itemName: String, playerName: String, isOwnDrop: Boolean) {
        val dropInfo = RareDrops.rareDrops.find { it.itemName == itemName } ?: return
        val type = RareDropTypes.values().find { it.displayName == itemName } ?: return

        if (!Alerts.alertOnRareDropTypes.contains(type)) return

        val showPrice = when (Alerts.rareDropAlertShowPriceFor) {
            RareDropPriceScope.OWN -> isOwnDrop
            RareDropPriceScope.OWN_AND_PARTY -> true
            RareDropPriceScope.OFF -> false
        }
        val title = dropInfo.getTitle()
        val price = if (showPrice) getPrice(dropInfo.id, dropInfo.npcPrice) else 0.0
        val priceStr = if (price > 0.0) " ${GRAY}(${GREEN}+${GOLD}${CommonUtils.toShortNumber(price)}${GRAY})" else ""

        CommonUtils.showTitle(title + priceStr, playerName)
        
        val soundData = CustomSoundsManager.getDropSoundData(dropInfo.id)
        val soundFileName = soundData?.source

        if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(soundFileName)
        // Do not play MC sound in other cases because SB already plays rare drop sound for those items
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
