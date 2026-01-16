package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.ChatEvent
import com.github.sleepypanda.feesh.events.RareDropEvent
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.FeeshMod

object RareDropsPublisher {
    // §6§lRARE DROP! §dRadioactive Vial §b(+§b236 §b✯ Magic Find§b)
    // §6§lRARE DROP! §6Tiki Mask §b(+§b236 §b✯ Magic Find§b)
    val RARE_DROP_PATTERN = Regex("^§6§lRARE DROP! (?<item>.+?) §b\\(\\+§b(?<mf>\\d+) §b✯ Magic Find§b\\)$")

    // §6§lPET DROP! §6§lLEGENDARY §6Baby Yeti (SH format)
    // §6§lPET DROP! §6Baby Yeti
    val PET_DROP_PATTERN = Regex("^§6§lPET DROP! (?<shRarity>(§6§lLEGENDARY |§5§lEPIC |§9§lRARE |§a§lUNCOMMON |§f§lCOMMON ))?+(?<pet>(.+))$")

    // WOW! [MVP+] MoonTheSadFisher found an Aquamarine Dye #95!
    // WOW! [MVP+] MoonTheSadFisher found a Treasure Dye!
    // §d§lWOW! §b[MVP§r§c+§r§b] §bMoonTheSadFisher§f §6found an §bAquamarine Dye §8#95§6!
    val DYE_DROP_PATTERN = Regex("^§d§lWOW! (?<playerAndRank>.+?) §6found (a|an) (?<dyeName>.+?)( §8#\\d+)?§6!.*$")

    // Wow! [MVP+] MoonTheSadFisher found a Phoenix pet!
    // &eWow! &r&b[MVP&r&c+&r&b] &bMoonTheSadFisher&r&r&f &r&efound a &r&cPhoenix &r&epet!
    val PHOENIX_PET_DROP_PATTERN = Regex("^Wow\\! (?<playerAndRank>.+?) found a Phoenix pet\\!.*")

    // TODO: Squid pet catch

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock()) return
        
        val formattedMessage = event.message.getFormattedString()
        if (formattedMessage.isNullOrEmpty()) return
        val unformattedMessage = event.message.string ?: return

        val playerName = PlayerUtils.getName() ?: return
        
        if (RARE_DROP_PATTERN.containsMatchIn(formattedMessage)) {
            val match = RARE_DROP_PATTERN.matchEntire(formattedMessage) ?: return
            val item = match.groups.get("item")?.value ?: return
            val magicFind = match.groups.get("mf")?.value?.toIntOrNull()
            EventBus.publish(RareDropEvent(item.removeFormatting(), item, magicFind))
        } else if (PET_DROP_PATTERN.containsMatchIn(formattedMessage)) {
            val match = PET_DROP_PATTERN.matchEntire(formattedMessage) ?: return
            val petDisplayName = match.groups.get("pet")?.value ?: return
            val rarityStr = CommonUtils.getRarityDescription(petDisplayName.substring(0, 2))
            val petName = "${petDisplayName.removeFormatting()} ($rarityStr)"
            EventBus.publish(RareDropEvent(petName, petDisplayName, null))
        } else if (PHOENIX_PET_DROP_PATTERN.containsMatchIn(unformattedMessage)) {
            val match = PHOENIX_PET_DROP_PATTERN.matchEntire(unformattedMessage) ?: return
            val playerAndRank = match.groups.get("playerAndRank")?.value ?: return
            if (!playerAndRank.contains(playerName, ignoreCase = false)) return

            EventBus.publish(RareDropEvent("Phoenix", "${SPECIAL}Phoenix", null))
        } else if (DYE_DROP_PATTERN.containsMatchIn(formattedMessage)) {
            val match = DYE_DROP_PATTERN.matchEntire(formattedMessage) ?: return
            val playerAndRank = match.groups.get("playerAndRank")?.value ?: return
            if (!playerAndRank.removeFormatting().contains(playerName, ignoreCase = false)) return

            val dyeName = match.groups.get("dyeName")?.value ?: return
            EventBus.publish(RareDropEvent(dyeName.removeFormatting(), dyeName, null))
        }
    }
}

