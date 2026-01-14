package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.RareDropEvent
import com.github.sleepypanda.feesh.constants.RareDropTypes
import com.github.sleepypanda.feesh.constants.RareDrops
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.events.PartyChatEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PriceUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
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
        val playerName = PlayerUtils.getFormattedName() ?: return

        showAlert(itemName, playerName)
    }

    private fun onPartyChatDrop(event: PartyChatEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareDrops) return

        val message = event.messagePayload.removeFormatting()

        if (!FEESH_PCHAT_PATTERN.containsMatchIn(message)) return

        val match = FEESH_PCHAT_PATTERN.matchEntire(message) ?: return
        val itemName = match.groups.get("itemName")?.value ?: return

        val me = PlayerUtils.getName() ?: ""
        val playerName = event.rankAndPlayer
        if (!playerName.isNullOrEmpty() && !me.isNullOrEmpty() && playerName.removeFormatting().contains(me, ignoreCase = true)) return

        showAlert(itemName, playerName)
    }

    private fun showAlert(itemName: String, playerName: String) {
        val dropInfo = RareDrops.rareDrops.find { it.itemName == itemName } ?: return

        val type = try {
            RareDropTypes.valueOf(itemName.uppercase().replace(" (", "").replace(") ", "").replace(" ", "_")) // Baby Yeti (Epic) -> BABY_YETI_EPIC
        } catch (_: IllegalArgumentException) {
            return
        }

        if (!Alerts.alertOnRareDropTypes.contains(type)) return

        val title = dropInfo.boldDisplayName
        val price = if (Alerts.includePriceIntoRareDropAlert) getPrice(dropInfo.id) else 0.0
        val priceStr = if (price > 0.0) " ${GRAY}(${GREEN}+${CommonUtils.toShortNumber(price)}${GRAY})" else ""

        CommonUtils.showTitle(title + priceStr, playerName)
        SoundUtils.playSound() // TODO: custom sounds
    }

    // TODO: Sell offer or insta sell
    // TODO: Move this into PriceUtils and reuse
    private fun getPrice(itemId: String): Double {
        val bazaarPrices = PriceUtils.getBazaarItemPrices(itemId)
        var itemPrice = bazaarPrices?.sellOffer
        
        if (bazaarPrices == null) {
            val auctionPrices = PriceUtils.getAuctionItemPrice(itemId)
            itemPrice = auctionPrices?.lbin
        }
        
        return itemPrice ?: 0.0
    }
}
