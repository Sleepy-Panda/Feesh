package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.events.models.RareDropEvent
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils

object RareDropsPublisher {
    // §6§lRARE DROP! §dRadioactive Vial §b(+§b236 §b✯ Magic Find§b)
    // §6§lRARE DROP! §6Tiki Mask §b(+§b236 §b✯ Magic Find§b)
    val RARE_DROP_PATTERN = Regex("^§6§lRARE DROP! (?<item>.+?) §b\\(\\+§b(?<mf>\\d+) §b✯ Magic Find§b\\)$")

    // §6§lRARE DROP! §fEnchanted Book (Charm 1§f) §b(+§b293 §b✯ Magic Find§b)
    // §6§lRARE DROP! §fEnchanted Book (§d§lFlash 1§f) §b(+§b392 §b✯ Magic Find§b)
    val RARE_DROP_BOOK_PATTERN = Regex("^§6§lRARE DROP! §fEnchanted Book \\((?<bookName>.+?)§f\\) §b\\(\\+§b(?<mf>\\d+) §b✯ Magic Find§b\\)$")

    // §6§lPET DROP! §6§lLEGENDARY §6Baby Yeti (SH format)
    // §6§lPET DROP! §6Baby Yeti
    val PET_DROP_PATTERN = Regex("^§6§lPET DROP! (?<shRarity>(§6§lLEGENDARY |§5§lEPIC |§9§lRARE |§a§lUNCOMMON |§f§lCOMMON ))?+(?<pet>(.+))$")

    // WOW! [MVP+] MoonTheSadFisher found an Aquamarine Dye #95!
    // WOW! [MVP+] MoonTheSadFisher found a Treasure Dye!
    // §d§lWOW! §b[MVP§r§c+§r§b] §bMoonTheSadFisher§f §6found an §bAquamarine Dye §8#95§6!
    val DYE_DROP_PATTERN = Regex("^§d§lWOW! (?<playerAndRank>.+?) §6found (a|an) (?<dyeName>.+?)( §8#\\d+)?§6!.*$")

    // Wow! [MVP+] MoonTheSadFisher found a Phoenix pet!
    // §eWow! §b[MVP§r§c+§r§b] §bMoonTheSadFisher§f §efound a §cPhoenix §epet!
    val PHOENIX_PET_DROP_PATTERN = Regex("^§eWow\\! (?<playerAndRank>.+?) §efound a §cPhoenix §epet\\!.*")

    // §6⛃ §6§lGREAT CATCH! §fYou caught a §7[Lvl 1] §9Squid§f!
    // §d⛃ §d§lOUTSTANDING CATCH! §fYou caught a §7[Lvl 1] §6Squid§f!
    // SH format with pet rarity:
    // §6⛃ §r§6§lGREAT CATCH! §r§fYou caught a §r§7[Lvl 1] §f§lCOMMON §fSquid§r§f!
    // §6⛃ §r§6§lGREAT CATCH! §r§fYou caught a §r§7[Lvl 1] §a§lUNCOMMON §aSquid§r§f!
    // §d⛃ §r§d§lOUTSTANDING CATCH! §r§fYou caught a §r§7[Lvl 1] §6§lLEGENDARY §6Squid§r§f!
    val PET_CATCH_PATTERN = Regex("^(§6⛃ (§r)?§6§lGREAT CATCH!|§d⛃ (§r)?§d§lOUTSTANDING CATCH!) (§r)?§fYou caught a (§r)?(§r)?§7\\[Lvl 1\\] (?<shRarity>(§6§lLEGENDARY |§5§lEPIC |§9§lRARE |§a§lUNCOMMON |§f§lCOMMON ))?+(?<pet>.+?)§f!$")

    fun init() {
        EventBus.subscribe(ChatCancellableEvent::class, ::onChat)
    }

    private fun onChat(event: ChatCancellableEvent) {
        if (!WorldUtils.isInSkyblock()) return
        
        CommonUtils.runWithCatching("Failed to handle rare drop in publisher.") {
            val playerName = PlayerUtils.getUnformattedName()
            if (playerName.isNullOrEmpty()) return@onChat
            
            val rareDropBookMatch = RARE_DROP_BOOK_PATTERN.matchEntire(event.formattedText)
            if (rareDropBookMatch != null) {
                val item = rareDropBookMatch.groups.get("bookName")?.value ?: return@onChat
                val magicFind = rareDropBookMatch.groups.get("mf")?.value?.toIntOrNull()
                EventBus.publish(RareDropEvent(item.removeFormatting(), item, magicFind))
                return@onChat
            }
            
            val rareDropMatch = RARE_DROP_PATTERN.matchEntire(event.formattedText)
            if (rareDropMatch != null) {
                val item = rareDropMatch.groups.get("item")?.value ?: return@onChat
                val magicFind = rareDropMatch.groups.get("mf")?.value?.toIntOrNull()
                EventBus.publish(RareDropEvent(item.removeFormatting(), item, magicFind))
                return@onChat
            }
            
            val petDropMatch = PET_DROP_PATTERN.matchEntire(event.formattedText)
            if (petDropMatch != null) {
                val petDisplayName = petDropMatch.groups.get("pet")?.value ?: return@onChat
                val rarityStr = CommonUtils.getRarityDescription(petDisplayName.substring(0, 2))
                val petName = "${petDisplayName.removeFormatting()} ($rarityStr)"
                EventBus.publish(RareDropEvent(petName, petDisplayName, null))
                return@onChat
            }
            
            val petCatchMatch = PET_CATCH_PATTERN.matchEntire(event.formattedText)
            if (petCatchMatch != null) {
                val petDisplayName = petCatchMatch.groups.get("pet")?.value ?: return@onChat
                val rarityStr = CommonUtils.getRarityDescription(petDisplayName.substring(0, 2))
                val petName = "${petDisplayName.removeFormatting()} ($rarityStr)"
                EventBus.publish(RareDropEvent(petName, petDisplayName, null))
                return@onChat
            }
    
            val phoenixMatch = PHOENIX_PET_DROP_PATTERN.matchEntire(event.formattedText)
            if (phoenixMatch != null) {
                val playerAndRank = phoenixMatch.groups.get("playerAndRank")?.value ?: return@onChat
                if (playerAndRank.removeFormatting().contains(playerName, ignoreCase = false)) {
                    EventBus.publish(RareDropEvent("Phoenix", "${SPECIAL}Phoenix", null))
                }
                return@onChat
            }
            
            val dyeMatch = DYE_DROP_PATTERN.matchEntire(event.formattedText)
            if (dyeMatch != null) {
                val playerAndRank = dyeMatch.groups.get("playerAndRank")?.value ?: return@onChat
                val playerAndRankUnformatted = playerAndRank.removeFormatting()
                if (playerAndRankUnformatted.contains(playerName, ignoreCase = false)) {
                    val dyeName = dyeMatch.groups.get("dyeName")?.value ?: return@onChat
                    EventBus.publish(RareDropEvent(dyeName.removeFormatting(), dyeName, null))
                }
                return@onChat
            }
        }
    }
}
