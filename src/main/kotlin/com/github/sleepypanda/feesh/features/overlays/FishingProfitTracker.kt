package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.FishingProfitDrops
import com.github.sleepypanda.feesh.constants.FishingProfitDropInfo
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GameClosedEvent
import com.github.sleepypanda.feesh.events.models.GuiClosedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.events.models.PetLevelUpEvent
import com.github.sleepypanda.feesh.events.models.SacksItemsPickupEvent
import com.github.sleepypanda.feesh.events.models.PricesUpdatedEvent
import com.github.sleepypanda.feesh.constants.Sounds
import com.github.sleepypanda.feesh.features.chat.RareDropMessage
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
import com.github.sleepypanda.feesh.utils.GuiUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.GuiButton
import com.github.sleepypanda.feesh.utils.gui.LineAction
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.enums.PricingModeWithNpc
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ItemUtils
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import com.google.gson.JsonParser
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import java.util.Date
import java.util.Timer
import kotlin.concurrent.timerTask

// TODO Drops counter for Rare Drop chat message
// TODO Rely on chat message for some Rare Drops instead of pickup event?

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

    const val RESET_COMMAND = "feeshResetFishingProfit"
    const val RESET_TOTAL_COMMAND = "feeshResetFishingProfitTotal"
    const val PAUSE_COMMAND = "feeshPauseFishingProfit"

    const val TOGGLE_VIEW_MODE_COMMAND = "feeshToggleFishingProfitViewMode"
    const val SET_ITEM_COUNT_COMMAND = "feeshSetItemCountFishingProfit"
    const val SET_ITEM_COUNT_TOTAL_COMMAND = "feeshSetItemCountFishingProfitTotal"
    const val DELETE_ITEM_COMMAND = "feeshDeleteItemFishingProfit"
    const val DELETE_ITEM_TOTAL_COMMAND = "feeshDeleteItemFishingProfitTotal"

    private const val TICKS_TIMER_ELAPSED_TIME = 20
    private const val TICKS_INVENTORY = 5
    private const val MAX_SECONDS_SINCE_HOOK = 60 * 5
    private const val HIDE_OVERLAY_AFTER_HOOK_MINUTES = 5
    private const val FISHED_COINS_ITEM_ID = "FISHED_COINS"

    private val data: FishingProfitData
        get() = PersistentDataManager.feeshData.fishingProfit

    private var previousInventory: MutableMap<String, Int>? = null
    private var isSessionActive = false
    private var tickCounter = 0

    private val baseTitle = "${AQUA}${BOLD}Fishing profit tracker"

    private val gui = FeeshGui()
        .setCoordsDataKey("fishingProfitTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            "$baseTitle ${GRAY}[${GREEN}Session${GRAY}]",
            "${GRAY}- ${WHITE}1982${GRAY}x ${BLUE}Scorched Crab Stick${GRAY}: ${GOLD}333.9M",
            "${GRAY}- ${WHITE}5${GRAY}x ${LIGHT_PURPLE}${BOLD}Radioactive Vial${GRAY}: ${GOLD}288.1M",
            "${GRAY}- ${WHITE}44954${GRAY}x ${DARK_PURPLE}Silver Magmafish${GRAY}: ${GOLD}269.7M",
            "${GRAY}- ${WHITE}16${GRAY}x ${GRAY}[Lvl 100] ${LIGHT_PURPLE}Hermit Crab${GRAY}: ${GOLD}240M",
            "${GRAY}- ${WHITE}4${GRAY}x ${GRAY}[Lvl 100] ${GOLD}Baby Yeti${GRAY}: ${GOLD}53.3M",
            "${GRAY}- ${WHITE}318${GRAY}x ${GOLD}Nether Star${GRAY}: ${GOLD}45M",
            "${GRAY}- ${WHITE}100500${GRAY}x Other cheap items: ${GOLD}1.8B",
            "",
            "${AQUA}Total: ${GOLD}${BOLD}3B ${RESET}${GRAY}(${GOLD}53.9M${GRAY}/h)",
            "${AQUA}Elapsed time: ${WHITE}56h 23m 3s",
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
        EventBus.subscribe(GuiClosedEvent::class, ::onGuiClosed)
        EventBus.subscribe(PetLevelUpEvent::class, ::onPetReachedMaxLevel)
        EventBus.subscribe(SacksItemsPickupEvent::class, ::onSacksItemsPickup)
        EventBus.subscribe(PricesUpdatedEvent::class, ::onPricesUpdated)
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
        RegisterUtils.command(SET_ITEM_COUNT_COMMAND) { args ->
            onSetItemCountCommand(args, ViewMode.SESSION)
        }
        RegisterUtils.command(SET_ITEM_COUNT_TOTAL_COMMAND) { args ->
            onSetItemCountCommand(args, ViewMode.TOTAL)
        }
        RegisterUtils.command(DELETE_ITEM_COMMAND) { args ->
            onDeleteItemCommand(args, ViewMode.SESSION)
        }
        RegisterUtils.command(DELETE_ITEM_TOTAL_COMMAND) { args ->
            onDeleteItemCommand(args, ViewMode.TOTAL)
        }
    }

    private fun registerChatHandlers() {
        // ⛃ GOOD CATCH! You caught 43,642 Coins!
        RegisterUtils.chat(Regex("^⛃ (?:GOOD|GREAT|OUTSTANDING) CATCH! You caught ([\\d,]+) Coins.*")) { _, matchResult ->
            onCoinsFished(matchResult.groupValues[1].orEmpty())
        }
        // ⛃ GOOD CATCH! You caught Ice Essence x5!
        RegisterUtils.chat(Regex("^⛃ (?:GOOD|GREAT|OUTSTANDING) CATCH! You caught Ice Essence x([\\d,]+).*")) { _, matchResult ->
            if (WorldUtils.getWorldName() == WorldUtils.JERRY_WORKSHOP) {
                onIceEssenceFished(matchResult.groupValues[1].orEmpty())
            }
        }
        // ⛃ GOOD CATCH! You caught a Shinyfish Shard!
        // ⛃ GOOD CATCH! You caught an Abyssal Lanternfish Shard!
        RegisterUtils.chat(Regex("^⛃ (?:GOOD|GREAT|OUTSTANDING) CATCH! You caught (?:a|an) (.+) Shard.*")) { _, matchResult ->
            onShardFished(matchResult.groupValues[1].orEmpty())
        }
        // You caught a Sea Archer Shard!
        // You caught x4 Sea Archer Shards!
        // You caught x4 Carrot King Shards!
        // You caught x2 Loch Emperor Shards!
        RegisterUtils.chat(Regex("^You caught (.+) Shard[s]?.*")) { _, matchResult ->
            onShardCaughtInBlackHole(matchResult.groupValues[1].orEmpty())
        }
        // CHARM You charmed a Loch Emperor and captured its Shard.
        // NAGA You charmed a Tadgang and captured its Shard.
        RegisterUtils.chat(Regex("^(?:CHARM|NAGA|SALT) You charmed (?:a|an) (.+) and captured its Shard.*")) { _, matchResult ->
            onShardsCharmed(matchResult.groupValues[1].orEmpty(), 1)
        }
        // SALT You charmed a Ent and captured 2 Shards from it.
        // CHARM You charmed a Flaming Spider and captured 2 Shards from it.
        // SALT You charmed a Tadgang and captured 2 Shards from it.
        // SALT You charmed a Magma Slug and captured 3 Shards from it.
        RegisterUtils.chat(Regex("^(?:CHARM|NAGA|SALT) You charmed (?:a|an) (.+) and captured ([\\d]+) Shards from it.*")) { _, matchResult ->
            val count = matchResult.groupValues[2].toIntOrNull() ?: 1
            onShardsCharmed(matchResult.groupValues[1].orEmpty(), count)
        }
        // LOOT SHARE You received 2 Titanoboa Shards for assisting CuzImCrzz!
        // LOOT SHARE You received 3 Magma Slug Shards for assisting OmeRuben!
        RegisterUtils.chat(Regex("^LOOT SHARE You received (.+) Shard.*")) { _, matchResult ->
            onShardLootshared(matchResult.groupValues[1].orEmpty())
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        pause()
    }

    private fun onGuiClosed(@Suppress("UNUSED_PARAMETER") event: GuiClosedEvent) {
        detectInventoryChanges() // Actualize inventory state after taking items from chest and quickly closing a GUI
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!Overlays.fishingProfitTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld(WorldUtils.getWorldName())) return
        tickCounter++

        if (tickCounter % TICKS_TIMER_ELAPSED_TIME == 0) {
            refreshElapsedTime()
            updateGuiLines()
        }
        if (tickCounter % TICKS_INVENTORY == 0) {
            detectInventoryChanges()
        }
    }

    private fun onPricesUpdated(@Suppress("UNUSED_PARAMETER") event: PricesUpdatedEvent) {
        refreshTotalItemsProfits()
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

    private fun onSetItemCountCommand(args: Array<String>, viewMode: ViewMode) {
        try {
            if (args.size < 2) {
                ChatUtils.sendLocalChat("${RED}Usage: /$SET_ITEM_COUNT_COMMAND <itemID> <count>", true)
                return
            }
         
            val itemId = args[0].trim()
            if (itemId.isBlank()) {
                ChatUtils.sendLocalChat("${RED}Item ID is required.", true)
                return
            }

            val count = args[1].toIntOrNull()
            if (count == null || count == 0) {
                ChatUtils.sendLocalChat("${RED}Invalid count, should be a positive number: ${args[0]}", true)
                return
            }

            val dropInfo = FishingProfitDrops.items.find { it.itemId == itemId }
            if (dropInfo == null && !ItemUtils.isMaxedPet(itemId)) {
                ChatUtils.sendLocalChat("${RED}Item not found by ID: $itemId", true)
                return
            }

            val itemName = when {
                ItemUtils.isMaxedPet(itemId) -> ItemUtils.getPetNameByPetId(itemId)
                else -> dropInfo!!.itemName
            }
            val displayName = getDisplayNameForGui(itemId, itemName)

            val sourceObj = getSourceObject(viewMode)
            val existing = sourceObj.profitTrackerItems[itemId]

            if (existing == null) {
                sourceObj.profitTrackerItems[itemId] = ProfitTrackerItemEntry(
                    itemId = itemId,
                    itemName = itemName,
                    amount = count,
                    totalItemProfit = 0.0
                )
            } else {
                existing.amount = count
            }

            saveData()
            refreshTotalItemsProfitsInMode(viewMode)
            updateGuiLines()

            val viewModeText = getViewModeDisplayText(viewMode)
            ChatUtils.sendLocalChat("${WHITE}Changed count of ${displayName} ${WHITE}to ${count} in Fishing profit tracker $viewModeText${WHITE}.", true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to change item count in Fishing profit tracker.", e)
        }
    }

    private fun onDeleteItemCommand(args: Array<String>, viewMode: ViewMode) {
        try {
            if (args.isEmpty()) {
                ChatUtils.sendLocalChat("${RED}Usage: /$DELETE_ITEM_COMMAND <itemID>", true)
                return
            }

            val itemId = args[0].trim()
            if (itemId.isBlank()) {
                ChatUtils.sendLocalChat("${RED}Item ID is required.", true)
                return
            }
          
            val sourceObj = getSourceObject(viewMode)            
            if (!sourceObj.profitTrackerItems.containsKey(itemId)) {
                ChatUtils.sendLocalChat("${RED}Item ID is not found in the tracker, nothing to delete: $itemId", true)
                return
            }

            val entry = sourceObj.profitTrackerItems[itemId] ?: return
            val viewModeText = getViewModeDisplayText(viewMode)
            val dropInfo = FishingProfitDrops.items.find { it.itemId == itemId }
            if (dropInfo == null && !ItemUtils.isMaxedPet(itemId) && itemId != FISHED_COINS_ITEM_ID) {
                ChatUtils.sendLocalChat("${RED}Item not found by ID: $itemId", true)
                return
            }

            val itemName = when {
                ItemUtils.isMaxedPet(itemId) -> ItemUtils.getPetNameByPetId(itemId)
                itemId == FISHED_COINS_ITEM_ID -> "Fished Coins"
                else -> dropInfo!!.itemName
            }
            val displayName = getDisplayNameForGui(itemId, itemName)
            val isConfirmed = args.size == 2 && args.last() == "noconfirm"

            if (!isConfirmed) {
                val deleteCommand = when (viewMode) {
                    ViewMode.SESSION -> "$DELETE_ITEM_COMMAND $itemId noconfirm"
                    ViewMode.TOTAL -> "$DELETE_ITEM_TOTAL_COMMAND $itemId noconfirm"
                }
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to delete ${WHITE}${entry.amount}x ${displayName}${WHITE} from the Fishing profit tracker ${viewModeText}${WHITE}? ${RED}${BOLD}[Click to confirm]",
                    deleteCommand,
                    true
                )
                return
            }

            sourceObj.profitTrackerItems.remove(itemId)
            saveData()
            refreshTotalItemsProfitsInMode(viewMode)
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Deleted ${WHITE}${entry.amount}x ${displayName}${WHITE} from the Fishing profit tracker ${viewModeText}${WHITE}.", true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to delete item from Fishing profit tracker.", e)
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

    private fun activateTimerInMode(viewMode: ViewMode) {
        val sourceObj = getSourceObject(viewMode)
        if (sourceObj.elapsedSeconds == 0) {
            sourceObj.elapsedSeconds = 1
        }
    }

    private fun refreshElapsedTime() {
        if (!Overlays.fishingProfitTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld(WorldUtils.getWorldName())) {
            pause()
            return
        }

        val prevIsActive = isSessionActive
        val player = FeeshMod.mc.player ?: return
        val isHookActive = EntityUtils.isFishingHookActive(player)

        // Start fishing timer after pause or when tracker was empty
        if (isHookActive) {
            isSessionActive = true
            activateTimerInMode(ViewMode.SESSION)
            activateTimerInMode(ViewMode.TOTAL)
            saveData()

            if (!prevIsActive) {
                refreshTotalItemsProfits()
                return
            }
        }

        if (!isSessionActive || !isTrackerVisible()) return
        val lastHookSeenAt = PlayerUtils.lastFishingHookSeenAt() ?: return
        val elapsedSinceHook = (Date().time - lastHookSeenAt.time) / 1000
        if (elapsedSinceHook < MAX_SECONDS_SINCE_HOOK) {
            data.session.elapsedSeconds += 1
            data.total.elapsedSeconds += 1
            saveData()
        } else {
            pause()
        }
    }

    fun refreshTotalItemsProfits() {
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
            val isMaxLevelPet = ItemUtils.isMaxedPet(key)
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

    private fun onSacksItemsPickup(event: SacksItemsPickupEvent) {
        if (!isSessionActive || !isTrackerVisible()) return
        if (GuiUtils.isInSacksGui() || GuiUtils.isInSupercraftGui()) return

        val lastGuisClosed = GuiUtils.lastGuisClosed
        val cooldownMilliseconds = 31_000

        // 30 seconds is the maximum time to receive "[Sacks] +..." message after items were added to the sack
        if (lastGuisClosed.lastSacksGuiClosedAt != null && Date().time - lastGuisClosed.lastSacksGuiClosedAt!!.time < cooldownMilliseconds) return
        if (lastGuisClosed.lastSupercraftGuiClosedAt != null && Date().time - lastGuisClosed.lastSupercraftGuiClosedAt!!.time < cooldownMilliseconds) return

        var added = false
        for (item in event.items) {
            if (item.amount <= 0 || item.itemName.isBlank()) continue
            val itemName = item.itemName.removeFormatting()
            val dropInfo = getFishingProfitItemByName(itemName) ?: continue

            if (dropInfo.itemId.startsWith("MAGMA_FISH") && 
                lastGuisClosed.lastOdgerGuiClosedAt != null && Date().time - lastGuisClosed.lastOdgerGuiClosedAt!!.time < cooldownMilliseconds) {
                continue; // User probably just filleted trophy fish
            }

            addProfitTrackerItem(dropInfo.itemId, dropInfo.itemName, item.amount, null, true)
            added = true

            if (Overlays.shouldAnnounceRareDropsWhenPickup && dropInfo.shouldAnnounceRareDrop) {
                announceRareDropInChat(dropInfo, item.amount)
            }
        }
        if (added) refreshTotalItemsProfits()
    }


    private fun onCoinsFished(coinsStr: String) {
        if (!isSessionActive || !isTrackerVisible()) return
        val coins = coinsStr.replace(",", "").toDoubleOrNull() ?: return
        addProfitTrackerItem(FISHED_COINS_ITEM_ID, "Fished Coins", 1, coins)
    }

    private fun onIceEssenceFished(countStr: String) {
        if (!isSessionActive || !isTrackerVisible()) return
        val count = countStr.replace(",", "").toIntOrNull() ?: return
        findAndAddProfitTrackerItem({ it.itemId == "ESSENCE_ICE" }, count)
    }

    private fun onShardFished(shard: String) {
        if (!isSessionActive || !isTrackerVisible()) return
        val shardName = shard + " Shard"
        findAndAddProfitTrackerItem({ it.itemName.equals(shardName, ignoreCase = true) }, 1)
    }

    private fun onShardCaughtInBlackHole(shardsText: String) { // a|an|x5 Carrot King
        if (!isSessionActive || !isTrackerVisible()) return
        val parts = shardsText.split(" ")
        val countText = parts.firstOrNull() ?: "a"
        val count = when (countText) {
            "a", "an" -> 1
            else -> countText.replace("x", "").toIntOrNull() ?: 1
        }
        val shardName = parts.drop(1).joinToString(" ") + " Shard"
        findAndAddProfitTrackerItem({ it.itemName.equals(shardName, ignoreCase = true) }, count)
    }

    private fun onShardsCharmed(mobName: String, shardsCount: Int) {
        if (!isSessionActive || !isTrackerVisible() || shardsCount <= 0) return
        val shardName = mobName + " Shard"
        findAndAddProfitTrackerItem({ it.itemName.equals(shardName, ignoreCase = true) }, shardsCount)
    }

    private fun onShardLootshared(shardsText: String) { // a|an|2 Titanoboa
        if (!isSessionActive || !isTrackerVisible()) return
        val parts = shardsText.split(" ")
        val countText = parts.firstOrNull() ?: "a"
        val count = when (countText) {
            "a", "an" -> 1
            else -> countText.toIntOrNull() ?: 1
        }
        val shardName = parts.drop(1).joinToString(" ") + " Shard"
        findAndAddProfitTrackerItem({ it.itemName.equals(shardName, ignoreCase = true) }, count)
    }

    private fun onPetReachedMaxLevel(event: PetLevelUpEvent) {
        if (!isSessionActive || !isTrackerVisible()) return
        val petName = event.petName
        val rarityCode = CommonUtils.getRarityNumericCode(event.petDisplayName.substring(0, 2))
        val baseItemId = petName.split(" ").joinToString("_").uppercase()
        val itemIdMaxLevel = "${baseItemId};${rarityCode}+${event.level}"
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
        if (!isSessionActive || !isTrackerVisible()) {
            previousInventory = null
            return
        }

        if (previousInventory == null) {
            previousInventory = getFishingProfitItemsInCurrentInventory().toMutableMap()
            return
        }

        if (isPlayerMovingItem()) return

        val currentInventory = getFishingProfitItemsInCurrentInventory()

        // Allow being in Wardrobe or Armor GUI, because we often are in them when killing mobs and getting drops
        if (GuiUtils.isInChest() && !GuiUtils.isInWardrobeOrEquipmentGui()) {
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
            var slotItemName = getCleanItemName(stack.name.getFormattedString())
            if (slotItemName.isBlank()) continue

            if (slotItemName == "Enchanted Book") {
                val loreLines = stack.get(DataComponentTypes.LORE)?.lines?.map { it.string } ?: emptyList()
                if (loreLines.size > 0) {
                    val description = loreLines[0]
                    slotItemName += " ($description)"
                }
            }

            if (slotItemName.endsWith("Exp Boost")) {
                val loreLines = stack.get(DataComponentTypes.LORE)?.lines?.map { it.string } ?: emptyList()
                val petItemLine = loreLines.find { it.endsWith("PET ITEM") }
                if (petItemLine != null) {
                    val description = petItemLine.split(" ").firstOrNull() ?: ""
                    slotItemName += " ($description)"
                }
            }

            if (slotItemName.startsWith("[Lvl 1] ")) {
                val customData = ItemUtils.getCustomData(stack)
                if (customData != null && ItemUtils.getCustomDataId(customData) == "PET") {
                    val petInfoStr = ItemUtils.getCustomDataPetInfo(customData)
                    val rarity = petInfoStr?.let { s ->
                        try {
                            JsonParser.parseString(s).asJsonObject.get("tier")?.takeIf { it.isJsonPrimitive }?.asString
                        } catch (_: Exception) {
                            null
                        }
                    }
                    slotItemName += " (${rarity?.uppercase() ?: ""})"
                }
            }

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
        if (Regex(".+ §8x\\d+$").matches(s)) { // Booster cookie menu or NPCs append the amount to the item name - e.g. §9Fish Affinity Talisman §8x1
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

        if (shouldSkipItem(itemId, dropInfo)) return

        addProfitTrackerItem(itemId, dropInfo.itemName, difference, null, true)

        if (Overlays.shouldAnnounceRareDropsWhenPickup && dropInfo.shouldAnnounceRareDrop) {
            announceRareDropInChat(dropInfo, difference)
        }
    }

    private fun announceRareDropInChat(dropInfo: FishingProfitDropInfo, count: Int) {
        if (!Overlays.shouldAnnounceRareDropsWhenPickup || !dropInfo.shouldAnnounceRareDrop) return

        val diffText = if (count > 1) " ${RESET}${GRAY}${count}x" else ""
        ChatUtils.sendLocalChat("${GOLD}${BOLD}RARE DROP! ${RESET}${dropInfo.itemDisplayName}$diffText", true)

        if (General.soundMode != SoundMode.OFF) SoundUtils.playCustomSound(Sounds.FEESH_RARE_DROP)
    }

    private fun shouldSkipItem(itemId: String, dropInfo: FishingProfitDropInfo): Boolean {
        val now = Date()
        val lastGuisClosed = GuiUtils.lastGuisClosed

        if (itemId.startsWith("MAGMA_FISH") && lastGuisClosed.lastOdgerGuiClosedAt != null &&
            now.time - lastGuisClosed.lastOdgerGuiClosedAt!!.time < 1000) return true // User probably just filleted trophy fish

        if (lastGuisClosed.lastAuctionGuiClosedAt != null && now.time - lastGuisClosed.lastAuctionGuiClosedAt!!.time < 3_000) return true

        val lastKatUpgrade = GuiUtils.lastKatUpgrade
        if (lastKatUpgrade.lastPetClaimedAt != null && now.time - lastKatUpgrade.lastPetClaimedAt!!.time < 7 * 1000) { // It takes some time for pet to appear in the inventory after claiming from Kat
            val katPetName = lastKatUpgrade.petName?.removeFormatting() ?: return false
            if (dropInfo.itemName.contains(katPetName)) return true
        }

        return false
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

    private fun onLineItemIncrease(itemId: String) {
        try {
            if (!isTrackerVisible()) return

            val viewMode = getCurrentViewMode()
            val viewModeText = getViewModeDisplayText(viewMode)
            val sourceObj = getSourceObject(viewMode)
            val entry = sourceObj.profitTrackerItems[itemId] ?: return
            val dropInfo = FishingProfitDrops.items.find { it.itemId == itemId }
            if (dropInfo == null && !ItemUtils.isMaxedPet(itemId)) return

            val displayName = dropInfo?.itemDisplayName ?: getDisplayNameForGui(itemId, entry.itemName)
            addProfitTrackerItemInMode(viewMode, itemId, entry.itemName, 1, null)
            refreshTotalItemsProfitsInMode(viewMode)
            updateGuiLines()

            val newAmount = entry.amount + 1
            ChatUtils.sendLocalChat("${WHITE}Changed count of ${displayName} ${WHITE}to ${GRAY}${newAmount}x ${WHITE}in the Fishing profit tracker ${viewModeText}${WHITE}.", true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to change item amount in Fishing profit tracker.", e)
        }
    }

    private fun onLineItemDecrease(itemId: String) {
        try {
            if (!isTrackerVisible()) return

            val viewMode = getCurrentViewMode()
            val viewModeText = getViewModeDisplayText(viewMode)
            val sourceObj = getSourceObject(viewMode)
            val entry = sourceObj.profitTrackerItems[itemId] ?: return
            val dropInfo = FishingProfitDrops.items.find { it.itemId == itemId }
            if (dropInfo == null && !ItemUtils.isMaxedPet(itemId)) return

            val newAmount = entry.amount - 1
            if (newAmount <= 0) {
                return
            }

            val displayName = dropInfo?.itemDisplayName ?: getDisplayNameForGui(itemId, entry.itemName)
            sourceObj.profitTrackerItems[itemId] = entry.copy(amount = newAmount)
            saveData()
            refreshTotalItemsProfitsInMode(viewMode)
            updateGuiLines()

            ChatUtils.sendLocalChat("${WHITE}Changed count of ${displayName} ${WHITE}to ${GRAY}${newAmount}x ${WHITE}in the Fishing profit tracker ${viewModeText}${WHITE}.", true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to change item amount in Fishing profit tracker.", e)
        }
    }

    private fun onLineItemDelete(itemId: String) {
        try {
            if (!isTrackerVisible()) return

            val viewMode = getCurrentViewMode()
            onDeleteItemCommand(arrayOf(itemId), viewMode)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to delete item from Fishing profit tracker.", e)
        }
    }

    private fun resetSession() {
        data.session = FishingProfitSourceData()        
        saveData()

        RareDropMessage.reset() // TODO Make them not dependent
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
            val nextMode = if (viewMode == ViewMode.SESSION) ViewMode.TOTAL else ViewMode.SESSION
            val nextText = getViewModeDisplayText(nextMode)

            val displayData = getDisplayTrackerData(viewMode)

            val lines = mutableListOf<String>()
            lines.add("$baseTitle $viewModeText")

            for (entry in displayData.entriesToShow) {
                val countStr = CommonUtils.formatNumberWithSpaces(entry.amount)
                val profitStr = CommonUtils.toShortNumber(entry.profit) ?: "0"
                lines.add("${GRAY}- ${WHITE}${countStr}${GRAY}x ${entry.item}${GRAY}: ${GOLD}$profitStr")
            }

            if (displayData.entriesToHide.isNotEmpty()) {
                val profitStr = CommonUtils.toShortNumber(displayData.totalCheapItemsProfit) ?: "0"
                val countStr = CommonUtils.formatNumberWithSpaces(displayData.totalCheapItemsCount)
                val typesStr = CommonUtils.formatNumberWithSpaces(displayData.totalCheapItemsTypesCount)
                lines.add("${GRAY}- ${WHITE}${countStr}${GRAY}x Cheap items of ${WHITE}${typesStr} ${GRAY}types: ${GOLD}$profitStr")
            }

            val totalStr = CommonUtils.toShortNumber(displayData.totalProfit) ?: "0"
            lines.add("")

            if (Overlays.shouldHideTimerInTotal && viewMode == ViewMode.TOTAL) {
                lines.add("${AQUA}Total: ${GOLD}${BOLD}$totalStr")
            } else {
                val perHourStr = CommonUtils.toShortNumber(displayData.profitPerHour) ?: "0"
                lines.add("${AQUA}Total: ${GOLD}${BOLD}$totalStr ${RESET}${GRAY}(${GOLD}$perHourStr${GRAY}/h)")

                val elapsedStr = CommonUtils.formatTimeElapsed(displayData.elapsedTime)
                val pausedSuffix = if (isSessionActive) "" else " ${GRAY}[Paused]"
                lines.add("${AQUA}Elapsed time: ${WHITE}$elapsedStr$pausedSuffix")    
            }

            gui.setLines(lines)

            val lineIndexToActions = mutableMapOf<Int, List<LineAction>>()
            val buttonLinesCount = 3 // Buttons count (view mode, pause, reset)
            val titleLineIndex = buttonLinesCount
            displayData.entriesToShow.forEachIndexed { index, entry ->
                val itemId = entry.itemId
                var actions: List<LineAction>
                if (entry.itemId == FISHED_COINS_ITEM_ID) {
                    actions = listOf(
                        LineAction("${GRAY}[${RED}x${GRAY}]") { onLineItemDelete(itemId) }
                    )
                } else {
                    actions = listOf(
                        LineAction("${GRAY}[${GREEN}+${GRAY}]") { onLineItemIncrease(itemId) },
                        LineAction("${GRAY}[${RED}-${GRAY}]") { onLineItemDecrease(itemId) },
                        LineAction("${GRAY}[${RED}x${GRAY}]") { onLineItemDelete(itemId) }
                    )
                }
                lineIndexToActions[titleLineIndex + 1 + index] = actions
            }
            gui.setLineActions(lineIndexToActions)

            gui.setButtons(listOf(
                GuiButton(0, "${GRAY}[Click to show $nextText${GRAY}]", { toggleViewMode() }),
                GuiButton(1, "${GRAY}[${YELLOW}Click to pause${GRAY}]", { pauseFishingProfitTracker() }),
                GuiButton(2, "${GRAY}[${RED}Click to reset${GRAY}]", { resetFishingProfitTracker(false, getCurrentViewMode()) })
            ))
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
            ItemUtils.isMaxedPet(itemId) -> ItemUtils.getItemDisplayNameByPetId(itemId, itemName)
            itemId == FISHED_COINS_ITEM_ID -> "${GOLD}Fished Coins"
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
