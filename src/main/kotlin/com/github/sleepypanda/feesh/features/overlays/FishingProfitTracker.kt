package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.FishingProfitDrops
import com.github.sleepypanda.feesh.constants.FishingProfitDropInfo
import com.github.sleepypanda.feesh.events.ClientTickEvent
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.GameClosedEvent
import com.github.sleepypanda.feesh.events.GuiOpenedEvent
import com.github.sleepypanda.feesh.events.GuiClosedEvent
import com.github.sleepypanda.feesh.events.WorldChangedEvent
import com.github.sleepypanda.feesh.events.PetLevelUpEvent
import com.github.sleepypanda.feesh.events.SacksItemsPickupEvent
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
import com.github.sleepypanda.feesh.utils.gui.GuiButton
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

// TODO Shards no workie
// TODO Display name for leveled pets
// Items taken from Bazaar counted
// Is in sacks, is in supercraft, etc, maybe track item creation date
// TODO refresh if settings for price modes are changed?
// TODO event for pickup item
// TODO Drops counter for Rare Drop chat message
// TODO Rely on chat message for some Rare Drops instead of pickup event?
// Items from sacks are counted
// BZ/AH prices updated event, to refresh total profits, instead of TICKS_PRICES
// Hide buttons on chat/inventory gui closed, add this to FeeshGui, as well as gui opened

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
        EventBus.subscribe(GuiClosedEvent::class, ::onGuiClosed)
        EventBus.subscribe(PetLevelUpEvent::class, ::onPetReachedMaxLevel)
        EventBus.subscribe(SacksItemsPickupEvent::class, ::onSacksItemsPickup)
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

    private fun onGuiOpened(@Suppress("UNUSED_PARAMETER") event: GuiOpenedEvent) {       
        Timer().schedule(timerTask {
            updateGuiLines() // TODO update if GUI is a chat or inventory
        }, 50) // Wait for GUI to be fully loaded (~1 tick delay)
    }

    private fun onGuiClosed(event: GuiClosedEvent) {
        // TODO detect inventory changes if closed chest?
        if (event.guiName == "Chat" || event.guiName == "Inventory") {
            Timer().schedule(timerTask {
                updateGuiLines()
            }, 50) // Wait for GUI to be fully closed so we are not in chat or inventory anymore (~1 tick delay)
        }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!Overlays.fishingProfitTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld(WorldUtils.getWorldName())) return
        tickCounter++

        if (tickCounter % TICKS_OVERLAY_AND_ACTIVATE == 0) {
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
        if (!Overlays.fishingProfitTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld(WorldUtils.getWorldName())) return
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

    private fun onSacksItemsPickup(event: SacksItemsPickupEvent) {
        if (!isSessionActive || !isTrackerVisible()) return
        if (isInSacksGui()) return

        //if (isInSacksGui() || new Date() - lastGuisClosed.lastSacksGuiClosedAt < 15 * 1000) return; // Sacks closed < 15 seconds ago
        //if (isInSupercraftGui() || new Date() - lastGuisClosed.lastSupercraftGuiClosedAt < 15 * 1000) return; // Supercraft closed < 15 seconds ago

        var added = false
        for (item in event.items) {
            if (item.amount <= 0 || item.itemName.isBlank()) continue
            val dropInfo = getFishingProfitItemByName(item.itemName) ?: continue
            addProfitTrackerItem(dropInfo.itemId, dropInfo.itemName, item.amount, null, true)
            added = true
        }
        if (added) refreshTotalItemsProfits()
    }

    private fun isInSacksGui(): Boolean {
        val screen = FeeshMod.mc.currentScreen ?: return false
        if (screen !is HandledScreen<*>) return false
        val title = screen.title.string
        return (title.endsWith("Sack"))
    }

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
        val screen = FeeshMod.mc.currentScreen
        if (screen != null && screen is HandledScreen<*>) { // In chest
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
            val nextMode = if (viewMode == ViewMode.SESSION) ViewMode.TOTAL else ViewMode.SESSION
            val nextText = getViewModeDisplayText(nextMode)

            val displayData = getDisplayTrackerData(viewMode)

            val lines = mutableListOf<String>()
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

            if (isInChatOrInventoryGui()) gui.setButtons(listOf(
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
        if (itemId.endsWith("+100") || itemId.endsWith("+200")) { // FLYING_FISH;4+100
            val level = itemId.split("+")[1]
            val rarityNumericCode = itemId.split(";")[1].substringBefore("+").toInt()
            val rarityCode = CommonUtils.getRarityColorCode(rarityNumericCode)
            return "${GRAY}[Lvl ${level}] ${rarityCode}${itemName}"
        }

        return when {
            itemId == "FISHED_COINS" -> "${GOLD}Fished Coins"
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
