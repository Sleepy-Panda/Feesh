package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.FishingProfitDrops
import com.github.sleepypanda.feesh.constants.FishingProfitDropInfo
import com.github.sleepypanda.feesh.events.ClientTickEvent
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.GameClosedEvent
import com.github.sleepypanda.feesh.events.GuiOpenedEvent
import com.github.sleepypanda.feesh.events.WorldChangedEvent
import com.github.sleepypanda.feesh.constants.Sounds
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.PriceUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.enums.PricingModeWithNpc
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import java.util.Date
import java.util.Timer
import kotlin.concurrent.timerTask

// TODO isWorldLoaded
// TODO Display name for leveled pets
// Flash book not counted
// Items taken from backpacks counted
// Is in sacks, is in supercraft, etc, maybe track item creation date
// TODO refresh if settings for price modes are changed?
// TODO Event for pet level up
// TODO event for pickup item
// TODO Drops counter for Rare Drop chat message
// TODO Rely on chat message for some Rare Drops instead of pickup event?
// Items from sacks are counted
// BZ/AH prices updated event, to refresh total profits, instead of TICKS_PRICES

object FishingProfitTracker {
    enum class ViewMode {
        SESSION,
        TOTAL
    }

    data class ProfitTrackerItemEntry(
        var itemName: String = "",
        var itemId: String = "",
        var amount: Int = 0,
        var totalItemProfit: Double = 0.0
    )

    data class FishingProfitSourceData(
        var profitTrackerItems: MutableMap<String, ProfitTrackerItemEntry> = mutableMapOf(),
        var totalProfit: Double = 0.0,
        var elapsedSeconds: Int = 0
    )

    data class FishingProfitData(
        var session: FishingProfitSourceData = FishingProfitSourceData(),
        var total: FishingProfitSourceData = FishingProfitSourceData(),
        var viewMode: String = ViewMode.SESSION.name
    )

    private const val RESET_COMMAND = "feeshResetFishingProfit"
    private const val RESET_TOTAL_COMMAND = "feeshResetFishingProfitTotal"
    private const val TOGGLE_VIEW_MODE_COMMAND = "feeshToggleFishingProfitViewMode"
    private const val PAUSE_COMMAND = "feeshPauseFishingProfit"

    private const val TICKS_OVERLAY_AND_ACTIVATE = 20
    private const val TICKS_ELAPSED_TIME = 20
    private const val TICKS_INVENTORY = 5
    private const val TICKS_PRICES = 20 * 60
    private const val MAX_SECONDS_SINCE_HOOK = 60 * 5
    private const val HIDE_OVERLAY_AFTER_HOOK_MINUTES = 5

    private val data: FishingProfitData
        get() = PersistentDataManager.feeshData.fishingProfit

    private var previousInventory: MutableMap<String, Int>? = null
    private var isSessionActive = false
    private var isWorldLoaded = false
    private var tickCounter = 0

    private val baseTitle = "${AQUA}${BOLD}Fishing profit tracker"

    private val gui = FeeshGui()
        .setCoordsDataKey("fishingProfitTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            "$baseTitle ${GRAY}[${GREEN}Session${GRAY}]",
            "${GRAY}- ${WHITE}5${GRAY}x ${GOLD}Fished Coins${GRAY}: ${GOLD}1.2M",
            "${GRAY}- ${WHITE}2${GRAY}x ${LEGENDARY}Treasure Dye${GRAY}: ${GOLD}500k",
            "${GRAY}- ${WHITE}100${GRAY}x Cheap items of ${WHITE}3${GRAY} types: ${GOLD}50k",
            "",
            "${AQUA}Total: ${GOLD}${BOLD}1.7M ${RESET}${GRAY}(${GOLD}2.5M${GRAY}/h)",
            "${AQUA}Elapsed time: ${WHITE}40m 30s",
            "${GRAY}[Click to show ${GREEN}Total${GRAY}] ${DARK_GRAY}(/$TOGGLE_VIEW_MODE_COMMAND)",
            "${GRAY}[${YELLOW}Click to pause${GRAY}] ${DARK_GRAY}(/$PAUSE_COMMAND)",
            "${GRAY}[${RED}Click to reset${GRAY}] ${DARK_GRAY}(/$RESET_COMMAND)"
        ))
        .setSettingsKey { Overlays.fishingProfitTrackerOverlay }
        .setCondition {            
            isTrackerVisible()
        }

    fun init() {
        registerCommands()
        registerChatHandlers()
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(GuiOpenedEvent::class, ::onGuiOpened)
    }

    private fun registerCommands() {
        RegisterUtils.command(RESET_COMMAND) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetFishingProfitTracker(isConfirmed, getCurrentViewMode())
        }
        RegisterUtils.command(RESET_TOTAL_COMMAND) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetFishingProfitTracker(isConfirmed, ViewMode.TOTAL)
        }
        RegisterUtils.command(TOGGLE_VIEW_MODE_COMMAND) {
            toggleViewMode()
        }
        RegisterUtils.command(PAUSE_COMMAND) {
            pauseFishingProfitTracker()
        }
    }

    private fun registerChatHandlers() {
        RegisterUtils.chat(Regex("^\\[Sacks\\] \\+.*")) { message, _ ->
            onAddedToSacks(message)
        }
        RegisterUtils.chat(Regex("^⛃ GOOD CATCH! You caught ([\\d,]+) Coins.*")) { _, matchResult ->
            onCoinsFished(matchResult.groupValues[1].orEmpty())
        }
        RegisterUtils.chat(Regex("^⛃ GREAT CATCH! You caught ([\\d,]+) Coins.*")) { _, matchResult ->
            onCoinsFished(matchResult.groupValues[1].orEmpty())
        }
        RegisterUtils.chat(Regex("^⛃ OUTSTANDING CATCH! You caught ([\\d,]+) Coins.*")) { _, matchResult ->
            onCoinsFished(matchResult.groupValues[1].orEmpty())
        }
        RegisterUtils.chat(Regex("^⛃ GOOD CATCH! You caught .* Ice Essence .*x([\\d,]+).*")) { _, matchResult ->
            if (WorldUtils.getWorldName() == WorldUtils.JERRY_WORKSHOP) {
                onIceEssenceFished(matchResult.groupValues[1].orEmpty())
            }
        }
        RegisterUtils.chat(Regex("^⛃ GREAT CATCH! You caught .* Ice Essence .*x([\\d,]+).*")) { _, matchResult ->
            if (WorldUtils.getWorldName() == WorldUtils.JERRY_WORKSHOP) {
                onIceEssenceFished(matchResult.groupValues[1].orEmpty())
            }
        }
        RegisterUtils.chat(Regex("^⛃ OUTSTANDING CATCH! You caught .* Ice Essence .*x([\\d,]+).*")) { _, matchResult ->
            if (WorldUtils.getWorldName() == WorldUtils.JERRY_WORKSHOP) {
                onIceEssenceFished(matchResult.groupValues[1].orEmpty())
            }
        }
        RegisterUtils.chat(Regex("^⛃ GOOD CATCH! You caught (?:a|an) (.+) Shard.*")) { _, matchResult ->
            onShardFished(matchResult.groupValues[1].orEmpty())
        }
        //RegisterUtils.chat(Regex("^You caught (?:.*) (.+) Shard[s]?.*")) { _, matchResult ->
        //    if (shouldHandleFishingProfitEvent()) onShardCaughtInBlackHole(matchResult.groupValues[1].orEmpty())
        //}
        //RegisterUtils.chat(Regex(".*You charmed (.+) and captured its .* Shard.*")) { _, matchResult ->
        //    if (shouldHandleFishingProfitEvent()) onShardsCharmed(matchResult.groupValues[1].orEmpty(), 1)
        //}
        //RegisterUtils.chat(Regex(".*You charmed (.+) and captured .* ([\\d]+) Shards .*")) { _, matchResult ->
        //    if (shouldHandleFishingProfitEvent()) {
        //        val count = matchResult.groupValues[2].toIntOrNull() ?: 1
        //        onShardsCharmed(matchResult.groupValues[1].orEmpty(), count)
        //    }
        //}
        //RegisterUtils.chat(Regex(".*LOOT SHARE .*You received (.+) .* Shard.*")) { _, matchResult ->
        //    if (shouldHandleFishingProfitEvent()) onShardLootshared(matchResult.groupValues[1].orEmpty())
        //}
        //RegisterUtils.chat(Regex(".*LEVEL UP! .* (?:\\[Lvl ([12]00)\\])")) { _, matchResult ->
        //    if (shouldHandleFishingProfitEvent() && WorldUtils.getWorldName() == WorldUtils.CRIMSON_ISLE) {
        //        val level = matchResult.groupValues[1].toIntOrNull() ?: return@chat
        //        if (level == 100 || level == 200) onPetReachedMaxLevel(level, "")
        //    }
        //}
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        isWorldLoaded = false
        pause()
    }

    private fun onGuiOpened(@Suppress("UNUSED_PARAMETER") event: GuiOpenedEvent) {
        Timer().schedule(timerTask {
            updateGuiLines()
        }, 50) // Wait for GUI to be fully loaded (~1 tick delay)
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!Overlays.fishingProfitTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld(WorldUtils.getWorldName())) return
        tickCounter++

        if (tickCounter % TICKS_OVERLAY_AND_ACTIVATE == 0) {
            //isWorldLoaded = true // TODO move this
            activateSessionOnPlayersFishingHook()
            updateGuiLines()
        }
        if (tickCounter % TICKS_ELAPSED_TIME == 0) {
            refreshElapsedTime()
        }
        if (tickCounter % TICKS_INVENTORY == 0) {
            detectInventoryChanges()
        }
        if (tickCounter % TICKS_PRICES == 0) {
            refreshTotalItemsProfits()
        }
    }

    private fun isTrackerVisible(): Boolean {
        if (!Overlays.fishingProfitTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld(WorldUtils.getWorldName())) return false
        if (!PlayerUtils.isFishingHookSeenMinutesAgo(HIDE_OVERLAY_AFTER_HOOK_MINUTES)) return false

        val viewMode = getCurrentViewMode()
        val session = data.session
        val total = data.total
        val hasSessionData = session.totalProfit > 0.0 || session.profitTrackerItems.isNotEmpty() || session.elapsedSeconds > 0
        val hasTotalData = total.totalProfit > 0.0 || total.profitTrackerItems.isNotEmpty() || total.elapsedSeconds > 0
        val hasData = if (viewMode == ViewMode.SESSION) hasSessionData else hasTotalData

        return hasData
    }

    private fun isInChatOrInventoryGui(): Boolean {
        val screen = FeeshMod.mc.currentScreen ?: return false
        return screen is ChatScreen || screen is InventoryScreen
    }

    private fun pause() {
        previousInventory = null
        isSessionActive = false
    }

    fun resetFishingProfitTracker(isConfirmed: Boolean, resetViewMode: ViewMode) {
        try {
            val viewModeText = getViewModeDisplayText(resetViewMode)
            if (!isConfirmed) {
                val resetAction = when (resetViewMode) {
                    ViewMode.SESSION -> "$RESET_COMMAND noconfirm"
                    ViewMode.TOTAL -> "$RESET_TOTAL_COMMAND noconfirm"
                }
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to reset Fishing profit tracker $viewModeText${WHITE}? ${RED}${BOLD}[Click to confirm]",
                    resetAction,
                    true
                )
                return
            }
            previousInventory = null
            isSessionActive = false
            when (resetViewMode) {
                ViewMode.SESSION -> resetSession()
                ViewMode.TOTAL -> resetTotal()
            }
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Fishing profit tracker $viewModeText ${WHITE}was reset.", true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to reset Fishing profit tracker.", e)
        }
    }

    fun pauseFishingProfitTracker() {
        try {
            if (!isSessionActive || !isTrackerVisible()) return
            pause()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Fishing profit tracker is paused. Continue fishing to resume it.", true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to pause Fishing profit tracker.", e)
        }
    }

    private fun activateSessionOnPlayersFishingHook() {
        if (!Overlays.fishingProfitTrackerOverlay || /*!isWorldLoaded || */!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld(WorldUtils.getWorldName())) return
        val player = FeeshMod.mc.player ?: return
        val isHookActive = EntityUtils.isFishingHookActive(player)
        if (isHookActive) {
            isSessionActive = true
            activateTimerInMode(ViewMode.SESSION)
            activateTimerInMode(ViewMode.TOTAL)
            refreshTotalItemsProfits()
        }
    }

    private fun activateTimerInMode(viewMode: ViewMode) {
        val sourceObj = getSourceObject(viewMode)
        if (sourceObj.elapsedSeconds == 0) {
            sourceObj.elapsedSeconds = 1
            saveData()
        }
    }

    private fun refreshElapsedTime() {
        if (!isSessionActive || !isTrackerVisible()) return
        val lastHook = PlayerUtils.lastFishingHookSeenAt() ?: return
        val elapsedSinceHook = (Date().time - lastHook.time) / 1000
        if (elapsedSinceHook < MAX_SECONDS_SINCE_HOOK) {
            data.session.elapsedSeconds += 1
            data.total.elapsedSeconds += 1
            saveData()
        } else {
            pause()
        }
        updateGuiLines()
    }

    private fun refreshTotalItemsProfits() {
        if (!isTrackerVisible()) return
        refreshTotalItemsProfitsInMode(ViewMode.SESSION)
        refreshTotalItemsProfitsInMode(ViewMode.TOTAL)
        saveData()
        updateGuiLines()
    }

    private fun refreshTotalItemsProfitsInMode(viewMode: ViewMode) {
        val sourceObj = getSourceObject(viewMode)
        val priceMode = Overlays.fishingProfitTrackerPriceMode
        sourceObj.profitTrackerItems.forEach { (key, value) ->
            val isMaxLevelPet = key.contains("+100") || key.contains("+200")
            if (isMaxLevelPet) {
                if (priceMode == PricingModeWithNpc.NPC_SELL) {
                    value.totalItemProfit = 0.0
                    return@forEach
                }
                val itemIdFirstLvl = key.split("+").firstOrNull() ?: key
                val firstLvlPrice = PriceUtils.getAuctionItemPrice(itemIdFirstLvl)?.lbin ?: 0.0
                val maxLvlPrice = PriceUtils.getAuctionItemPrice(key)?.lbin ?: 0.0
                val profitPerPet = if (firstLvlPrice > 0 && maxLvlPrice > 0) maxLvlPrice - firstLvlPrice else 0.0
                value.totalItemProfit = value.amount * profitPerPet
            } else {
                val dropInfo = FishingProfitDrops.items.find { it.itemId == key }
                if (dropInfo != null) {
                    val itemPrice = getItemPrice(dropInfo)
                    value.totalItemProfit = value.amount * itemPrice
                }
            }
        }
        sourceObj.totalProfit = sourceObj.profitTrackerItems.values.sumOf { it.totalItemProfit }
        saveData()
    }

    private fun getItemPrice(dropInfo: FishingProfitDropInfo): Double {
        if (dropInfo.amountOfMagmaFish != null) {
            val magmaPrice = getPriceByMode("MAGMA_FISH")
            return dropInfo.amountOfMagmaFish * magmaPrice
        }
        if (Overlays.calculateProfitInCrimsonEssence && dropInfo.salvage != null && dropInfo.salvage.essenceItemId == "ESSENCE_CRIMSON") {
            if (Overlays.fishingProfitTrackerPriceMode == PricingModeWithNpc.NPC_SELL) return 0.0
            val bazaar = PriceUtils.getBazaarItemPrices(dropInfo.salvage.essenceItemId)
            val price = when (Overlays.fishingProfitTrackerPriceMode) {
                PricingModeWithNpc.INSTA_SELL -> bazaar?.instaSell ?: 0.0
                else -> bazaar?.sellOffer ?: 0.0
            }
            return dropInfo.salvage.essenceCount * price
        }
        return getPriceByMode(dropInfo.itemId)
    }

    private fun getPriceByMode(itemId: String): Double {
        val dropInfo = FishingProfitDrops.items.find { it.itemId == itemId } ?: return 0.0
        if (Overlays.fishingProfitTrackerPriceMode == PricingModeWithNpc.NPC_SELL) {
            return dropInfo.npcPrice ?: 0.0
        }
        val bazaar = PriceUtils.getBazaarItemPrices(itemId)
        if (bazaar != null) {
            return when (Overlays.fishingProfitTrackerPriceMode) {
                PricingModeWithNpc.INSTA_SELL -> bazaar.instaSell
                else -> bazaar.sellOffer
            }
        }
        val auction = PriceUtils.getAuctionItemPrice(itemId)
        if (auction != null) return auction.lbin
        return dropInfo.npcPrice ?: 0.0
    }

    private fun onAddedToSacks(message: Text) {
        if (!isSessionActive || !isTrackerVisible()) return
        // If in SACKS or sacks closed recently?
        if (isInSacksGui()) return
        //if (isSacksClosedRecently()) return

        val added = getItemsAddedToSacks(message)
        var isUpdated = false
        for (item in added) {
            if (item.difference <= 0 || item.itemName.isBlank()) continue
            val dropInfo = getFishingProfitItemByName(item.itemName) ?: continue
            addProfitTrackerItem(dropInfo.itemId, dropInfo.itemName, item.difference, null, true)
            isUpdated = true
        }
        if (isUpdated) refreshTotalItemsProfits()
    }

    private fun isInSacksGui(): Boolean {
        val screen = FeeshMod.mc.currentScreen ?: return false
        if (screen !is HandledScreen<*>) return false
        val title = screen.title.string
        return (title.endsWith("Sack"))
    }

    private data class ItemAddedToSack(val itemName: String, val difference: Int, val sackName: String)

    /**
     * Parses sack notification using the same approach as the other mod: iterate message.siblings,
     * for each part containing " item" use part.style.hoverEvent, and when it is ShowText parse
     * hover value with regex for "+N item (Sack)" lines. Trigger remains Regex("^\\[Sacks\\] \\+.*").
     */
    private fun getItemsAddedToSacks(message: Text): List<ItemAddedToSack> {
        val items = mutableListOf<ItemAddedToSack>()
        val regex = Regex("""(\+[\d,]+) (.+) \((.+)\)""")
        message.siblings.forEach { part ->
            if (!part.string.contains(" item")) return@forEach
            val hover = part.style?.hoverEvent ?: return@forEach
            if (hover is HoverEvent.ShowText) {
                val plain = hover.value.string
                regex.findAll(plain).forEach { match ->
                    val diffStr = match.groupValues[1].replace("+", "").replace(",", "")
                    val difference = diffStr.toIntOrNull() ?: 0
                    val itemName = match.groupValues[2].trim()
                    val sackName = match.groupValues[3].removeFormatting()
                    if (difference != 0 && itemName.isNotBlank()) {
                        items.add(ItemAddedToSack(itemName = itemName, difference = difference, sackName = sackName))
                    }
                }
            }
        }
        return items
    }

    // --- old implementation (reflection-based), commented per request ---
    // /**
    //  * Parses sack notification hover text: "Added items:" with lines like "+1,344 Pufferfish (Fishing Sack)".
    //  * We use reflection to call HoverEvent.getValue(action) because in Kotlin the name "getValue" is reserved
    //  * for property delegates (kotlin.properties.getValue, Map.getValue, etc.). The compiler then treats
    //  * `hoverEvent.getValue(action)` as one of those and reports "receiver type mismatch" instead of calling
    //  * the Java method HoverEvent.getValue(HoverEvent.Action).
    //  */
    // private fun getItemsAddedToSacks(message: Text): List<ItemAddedToSack> {
    //     val items = mutableListOf<ItemAddedToSack>()
    //     val hoverPart = message.siblings.find { part ->
    //         getHoverEventText(part)?.contains("Added items:") == true
    //     } ?: return items
    //     val addedItemsMessage = getHoverEventText(hoverPart) ?: return items
    //     val regex = Regex("""(\+[\d,]+) (.+) \((.+)\)""")
    //     var match = regex.find(addedItemsMessage)
    //     while (match != null) {
    //         val diffStr = match.groupValues[1].replace("+", "").replace(",", "")
    //         val difference = diffStr.toIntOrNull() ?: 0
    //         val itemName = match.groupValues[2].trim()
    //         val sackName = match.groupValues[3].removeFormatting()
    //         if (difference != 0 && itemName.isNotBlank()) {
    //             items.add(ItemAddedToSack(itemName = itemName, difference = difference, sackName = sackName))
    //         }
    //         match = match.next()
    //     }
    //     return items
    // }
    //
    // /** Calls HoverEvent.getValue(action) via reflection to avoid Kotlin's reserved getValue. */
    // private fun getHoverEventText(part: Text): String? {
    //     val he = part.style?.hoverEvent ?: return null
    //     return try {
    //         val method = he.javaClass.methods.find { it.name == "getValue" && it.parameterCount == 1 }
    //             ?: return null
    //         (method.invoke(he, he.action) as? Text)?.string
    //     } catch (_: Exception) {
    //         null
    //     }
    // }

    private fun onCoinsFished(coinsStr: String) {
        if (!isSessionActive || !isTrackerVisible()) return
        val coins = coinsStr.replace(",", "").toDoubleOrNull() ?: return
        addProfitTrackerItem("FISHED_COINS", "Fished Coins", 1, coins)
    }

    private fun onIceEssenceFished(countStr: String) {
        if (!isSessionActive || !isTrackerVisible()) return
        val count = countStr.replace(",", "").toIntOrNull() ?: return
        findAndAddProfitTrackerItem({ it.itemId == "ESSENCE_ICE" }, count)
    }

    private fun onShardFished(shardText: String) {
        if (!isSessionActive || !isTrackerVisible()) return
        val parts = shardText.removeFormatting().split(" ")
        val shardName = parts.drop(1).joinToString(" ") + " Shard"
        findAndAddProfitTrackerItem({ it.itemName.equals(shardName, ignoreCase = true) }, 1)
    }

    private fun onShardCaughtInBlackHole(shardsText: String) {
        if (!isSessionActive || !isTrackerVisible()) return
        val parts = shardsText.removeFormatting().split(" ")
        val countText = parts.firstOrNull() ?: "a"
        val count = when (countText) {
            "a", "an" -> 1
            else -> countText.replace("x", "").toIntOrNull() ?: 1
        }
        val shardName = parts.drop(1).joinToString(" ") + " Shard"
        findAndAddProfitTrackerItem({ it.itemName.equals(shardName, ignoreCase = true) }, count)
    }

    private fun onShardsCharmed(mobNameText: String, shardsCount: Int) {
        if (!isSessionActive || !isTrackerVisible() || shardsCount <= 0) return
        val parts = mobNameText.removeFormatting().split(" ")
        val shardName = parts.drop(1).joinToString(" ") + " Shard"
        findAndAddProfitTrackerItem({ it.itemName.equals(shardName, ignoreCase = true) }, shardsCount)
    }

    private fun onShardLootshared(shardsText: String) {
        if (!isSessionActive || !isTrackerVisible()) return
        val parts = shardsText.removeFormatting().split(" ")
        val countText = parts.firstOrNull() ?: "a"
        val count = when (countText) {
            "a", "an" -> 1
            else -> countText.toIntOrNull() ?: 1
        }
        val shardName = parts.drop(1).joinToString(" ") + " Shard"
        findAndAddProfitTrackerItem({ it.itemName.equals(shardName, ignoreCase = true) }, count)
    }

    private fun onPetReachedMaxLevel(level: Int, petDisplayName: String) {
        if (!isSessionActive || !isTrackerVisible()) return
        val petName = petDisplayName.removeFormatting().ifBlank { "Pet" }
        val baseItemId = petName.split(" ").joinToString("_").uppercase()
        val rarityCode = 4
        val itemIdMaxLevel = "$baseItemId;$rarityCode+$level"
        addProfitTrackerItem(itemIdMaxLevel, petName, 1, null)
    }

    private fun findAndAddProfitTrackerItem(predicate: (FishingProfitDropInfo) -> Boolean, amountToAdd: Int) {
        val dropInfo = FishingProfitDrops.items.find(predicate) ?: return
        addProfitTrackerItem(dropInfo.itemId, dropInfo.itemName, amountToAdd, null)
    }

    private fun addProfitTrackerItem(
        itemId: String,
        itemName: String,
        amountToAdd: Int,
        coinsToAdd: Double?,
        isBulk: Boolean = false
    ) {
        addProfitTrackerItemInMode(ViewMode.SESSION, itemId, itemName, amountToAdd, coinsToAdd)
        addProfitTrackerItemInMode(ViewMode.TOTAL, itemId, itemName, amountToAdd, coinsToAdd)
        if (!isBulk) refreshTotalItemsProfits()
    }

    private fun addProfitTrackerItemInMode(
        viewMode: ViewMode,
        itemId: String,
        itemName: String,
        amountToAdd: Int,
        coinsToAdd: Double?
    ) {
        val sourceObj = getSourceObject(viewMode)
        val existing = sourceObj.profitTrackerItems[itemId]
        val currentAmount = existing?.amount ?: 0
        val currentProfit = existing?.totalItemProfit ?: 0.0
        sourceObj.profitTrackerItems[itemId] = ProfitTrackerItemEntry(
            itemName = itemName,
            itemId = itemId,
            amount = currentAmount + amountToAdd,
            totalItemProfit = currentProfit + (coinsToAdd ?: 0.0)
        )
        saveData()
    }

    private fun detectInventoryChanges() {
        if (/*!isWorldLoaded || */!isSessionActive || !isTrackerVisible()) {
            previousInventory = null
            return
        }
        if (previousInventory == null) {
            previousInventory = getFishingProfitItemsInCurrentInventory().toMutableMap()
            return
        }
        if (isPlayerMovingItem()) return
        val currentInventory = getFishingProfitItemsInCurrentInventory()
        val screen = FeeshMod.mc.currentScreen
        if (screen != null && screen.javaClass.name.contains("HandledScreen")) { // TODO is in chest - screen !is HandledScreen<*>
            previousInventory = currentInventory.toMutableMap()
            return
        }
        var isUpdated = false
        for ((itemId, currentTotal) in currentInventory) {
            val previousTotal = previousInventory!![itemId] ?: 0
            if (currentTotal > previousTotal) {
                onItemAddedToInventory(itemId, previousTotal, currentTotal)
                isUpdated = true
            }
        }
        previousInventory = currentInventory.toMutableMap()
        if (isUpdated) refreshTotalItemsProfits()
    }

    private fun getFishingProfitItemsInCurrentInventory(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        val player = FeeshMod.mc.player ?: return result
        for (i in 0..35) {
            val stack = player.inventory.getStack(i)
            if (stack.isEmpty) continue
            var slotItemName = getCleanItemName(stack.name.string)
            if (slotItemName.isBlank()) continue
            val dropInfo = getFishingProfitItemByName(slotItemName)
            if (dropInfo != null) {
                result[dropInfo.itemId] = (result[dropInfo.itemId] ?: 0) + stack.count
            }
        }
        return result
    }

    private fun getCleanItemName(itemName: String): String {
        if (itemName.isBlank()) return ""
        var s = itemName
        if (Regex(".+ §r§8x\\d+$").matches(s)) {
            s = s.split(" ").dropLast(1).joinToString(" ")
        }
        return s.removeFormatting()
    }

    private fun getFishingProfitItemByName(itemName: String): FishingProfitDropInfo? {
        val lower = itemName.lowercase()
        return FishingProfitDrops.items.find {
            it.itemName.lowercase() == lower ||
                it.itemAlternateNames.any { alt -> alt.lowercase() == lower }
        }
    }

    private fun onItemAddedToInventory(itemId: String, previousCount: Int, newCount: Int) {
        val dropInfo = FishingProfitDrops.items.find { it.itemId == itemId } ?: return
        val difference = newCount - previousCount
        if (difference <= 0) return
        addProfitTrackerItem(itemId, dropInfo.itemName, difference, null, true)
        if (Overlays.shouldAnnounceRareDropsWhenPickup && dropInfo.shouldAnnounceRareDrop) {
            val diffText = if (difference > 1) " ${RESET}${GRAY}${difference}x" else ""
            ChatUtils.sendLocalChat("${GOLD}${BOLD}RARE DROP! ${RESET}${dropInfo.itemDisplayName}$diffText", true)
            if (General.soundMode != SoundMode.OFF) SoundUtils.playCustomSound(Sounds.FEESH_RARE_DROP)
        }
    }

    private fun isPlayerMovingItem(): Boolean {
        val player = FeeshMod.mc.player ?: return false
        val cursor = player.currentScreenHandler.cursorStack
        return !cursor.isEmpty
    }

    private fun getCurrentViewMode(): ViewMode {
        return try {
            ViewMode.valueOf(data.viewMode)
        } catch (_: Exception) {
            ViewMode.SESSION
        }
    }

    private fun toggleViewMode() {
        val newMode = if (getCurrentViewMode() == ViewMode.SESSION) ViewMode.TOTAL else ViewMode.SESSION
        data.viewMode = newMode.name
        saveData()
        updateGuiLines()
    }

    private fun getSourceObject(viewMode: ViewMode): FishingProfitSourceData {
        return when (viewMode) {
            ViewMode.SESSION -> data.session
            ViewMode.TOTAL -> data.total
        }
    }

    private fun getViewModeDisplayText(viewMode: ViewMode): String {
        return when (viewMode) {
            ViewMode.SESSION -> "${GRAY}[${GREEN}Session${GRAY}]"
            ViewMode.TOTAL -> "${GRAY}[${GREEN}Total${GRAY}]"
        }
    }

    private fun resetSession() {
        data.session = FishingProfitSourceData()
        saveData()
    }

    private fun resetTotal() {
        data.total = FishingProfitSourceData()
        saveData()
    }

    private fun updateGuiLines() {
        try {
            gui.clearLines()
            if (!isTrackerVisible()) {
                pause()
                return
            }
            val viewMode = getCurrentViewMode()
            val viewModeText = getViewModeDisplayText(viewMode)
            val displayData = getDisplayTrackerData(viewMode)
            val lines = mutableListOf<String>()

            if (isInChatOrInventoryGui()) {
                val nextMode = if (viewMode == ViewMode.SESSION) ViewMode.TOTAL else ViewMode.SESSION
                val nextText = getViewModeDisplayText(nextMode)
                lines.add("${GRAY}[Click to show $nextText${GRAY}] ${DARK_GRAY}(/$TOGGLE_VIEW_MODE_COMMAND)")
                lines.add("${GRAY}[${YELLOW}Click to pause${GRAY}] ${DARK_GRAY}(/$PAUSE_COMMAND)")
                val resetCmd = if (viewMode == ViewMode.SESSION) RESET_COMMAND else RESET_TOTAL_COMMAND
                lines.add("${GRAY}[${RED}Click to reset${GRAY}] ${DARK_GRAY}(/$resetCmd)")
            }

            lines.add("$baseTitle $viewModeText")
            for (entry in displayData.entriesToShow) {
                val profitStr = CommonUtils.toShortNumber(entry.profit) ?: "0"
                lines.add("${GRAY}- ${WHITE}${entry.amount}${GRAY}x ${entry.item}${GRAY}: ${GOLD}$profitStr")
            }
            if (displayData.entriesToHide.isNotEmpty()) {
                val profitStr = CommonUtils.toShortNumber(displayData.totalCheapItemsProfit) ?: "0"
                lines.add("${GRAY}- ${WHITE}${displayData.totalCheapItemsCount}${GRAY}x Cheap items of ${WHITE}${displayData.totalCheapItemsTypesCount} ${GRAY}types: ${GOLD}$profitStr")
            }
            val totalStr = CommonUtils.toShortNumber(displayData.totalProfit) ?: "0"
            val perHourStr = CommonUtils.toShortNumber(displayData.profitPerHour) ?: "0"
            lines.add("")
            lines.add("${AQUA}Total: ${GOLD}${BOLD}$totalStr ${RESET}${GRAY}(${GOLD}$perHourStr${GRAY}/h)")
            val elapsedStr = CommonUtils.formatTimeElapsed(displayData.elapsedTime)
            val pausedSuffix = if (isSessionActive) "" else " ${GRAY}[Paused]"
            lines.add("${AQUA}Elapsed time: ${WHITE}$elapsedStr$pausedSuffix")

            gui.setLines(lines)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to refresh tracker data for Fishing profit tracker.", e)
        }
    }

    private data class DisplayTrackerData(
        val entriesToShow: List<EntryDisplay>,
        val entriesToHide: List<EntryDisplay>,
        val totalCheapItemsCount: Int,
        val totalCheapItemsTypesCount: Int,
        val totalCheapItemsProfit: Double,
        val elapsedTime: Int,
        val totalProfit: Double,
        val profitPerHour: Double
    )

    private data class EntryDisplay(val itemId: String, val item: String, val amount: Int, val profit: Double)

    private fun getDisplayNameForGui(itemId: String, itemName: String): String {
        return when {
            itemId == "FISHED_COINS" -> "${GOLD}Fished Coins"
            itemId.contains("+100") -> "${GRAY}[Lvl 100] $itemName"
            itemId.contains("+200") -> "${GRAY}[Lvl 200] $itemName"
            else -> FishingProfitDrops.items.find { it.itemId == itemId }?.itemDisplayName ?: itemName
        }
    }

    private fun getDisplayTrackerData(viewMode: ViewMode): DisplayTrackerData {
        val sourceObj = getSourceObject(viewMode)
        val minPrice = if (viewMode == ViewMode.SESSION) Overlays.fishingProfitTrackerHideCheaperThan.toDouble() else Overlays.fishingProfitTrackerHideCheaperThanTotal.toDouble()
        val topN = Overlays.fishingProfitTrackerShowTop.coerceIn(1, 50)
        val entries = sourceObj.profitTrackerItems.values.map { v ->
            EntryDisplay(v.itemId, getDisplayNameForGui(v.itemId, v.itemName), v.amount, v.totalItemProfit)
        }.sortedByDescending { it.profit }
        val expensive = entries.filter { it.profit >= minPrice || it.item.contains("Kuudra Key") }
        val cheap = entries.filter { it.profit < minPrice && !it.item.contains("Kuudra Key") }
        val toShow = expensive.take(topN)
        val toHide = expensive.drop(topN) + cheap
        val elapsedHours = sourceObj.elapsedSeconds / 3600.0
        val profitPerHour = if (elapsedHours > 0) sourceObj.totalProfit / elapsedHours else 0.0
        return DisplayTrackerData(
            entriesToShow = toShow,
            entriesToHide = toHide,
            totalCheapItemsCount = toHide.sumOf { it.amount },
            totalCheapItemsTypesCount = toHide.size,
            totalCheapItemsProfit = toHide.sumOf { it.profit },
            elapsedTime = sourceObj.elapsedSeconds,
            totalProfit = sourceObj.totalProfit,
            profitPerHour = profitPerHour
        )
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (!Overlays.fishingProfitTrackerOverlay || !Overlays.resetFishingProfitTrackerOnGameClosed) return
        val session = data.session
        if (session.profitTrackerItems.isNotEmpty() || session.elapsedSeconds > 0 || session.totalProfit != 0.0) {
            resetSession()
            FeeshMod.LOGGER.info("[Feesh] Automatically reset Fishing profit tracker [Session] on game closed.")
        }
    }

    private fun saveData() {
        PersistentDataManager.saveFeeshDataToFileAsync()
    }
}
