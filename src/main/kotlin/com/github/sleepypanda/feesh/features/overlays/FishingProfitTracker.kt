package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.FishingProfitDrops
import com.github.sleepypanda.feesh.constants.FishingProfitDropInfo
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.CatchEvent
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.GameClosedEvent
import com.github.sleepypanda.feesh.events.models.InventoryProfitItemPickupEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.events.models.PetLevelUpEvent
import com.github.sleepypanda.feesh.events.models.SacksProfitItemsPickupEvent
import com.github.sleepypanda.feesh.events.models.ShardCaughtEvent
import com.github.sleepypanda.feesh.events.models.PricesUpdatedEvent
import com.github.sleepypanda.feesh.events.models.IceEssenceStatusBarEvent
import com.github.sleepypanda.feesh.events.models.BaitConsumedEvent
import com.github.sleepypanda.feesh.events.models.MobyDuckConsumedEvent
import com.github.sleepypanda.feesh.events.models.ShurikenUsedEvent
import com.github.sleepypanda.feesh.constants.Sounds
import com.github.sleepypanda.feesh.constants.StarlynContests
import com.github.sleepypanda.feesh.constants.TrophyFish
import com.github.sleepypanda.feesh.features.chat.RareDropMessage
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.CrimsonIsleTrashGearDropsPriceMode
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.PriceUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.GuiButton
import com.github.sleepypanda.feesh.utils.gui.LineAction
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.gui.Table
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.enums.PricingModeWithNpc
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.features.overlays.base.IResettableViewModeTracker
import com.github.sleepypanda.feesh.features.overlays.base.TrackerViewMode
import net.minecraft.network.chat.Component
import java.util.Date

// TODO Drops counter for Rare Drop chat message
// TODO Rely on chat message for some Rare Drops instead of pickup event?

object FishingProfitTracker : IResettableViewModeTracker {
    
    data class ProfitTrackerItemEntry(
        var itemName: String = "",
        var itemId: String = "",
        var amount: Int = 0,
        var totalItemProfit: Double = 0.0
    )

    data class ProfitTrackerCostEntry(
        var itemName: String = "",
        var itemId: String = "",
        var amount: Int = 0,
        var totalItemCost: Double = 0.0
    )

    data class FishingProfitSourceData(
        var profitTrackerItems: MutableMap<String, ProfitTrackerItemEntry> = mutableMapOf(),
        var totalProfit: Double = 0.0, // Sum of all items prices before subtracting costs
        var costItems: MutableMap<String, ProfitTrackerCostEntry> = mutableMapOf(),
        var totalCost: Double = 0.0, // Sum of all costs
        var elapsedSeconds: Int = 0,
        var catchesCount: Int = 0
    )

    data class FishingProfitData(
        var session: FishingProfitSourceData = FishingProfitSourceData(),
        var total: FishingProfitSourceData = FishingProfitSourceData(),
        var viewMode: String = TrackerViewMode.SESSION.name
    )

    override val trackerName = "Fishing profit tracker"

    const val RESET_COMMAND = "feeshResetFishingProfitTracker"
    const val RESET_TOTAL_COMMAND = "feeshResetFishingProfitTrackerTotal"
    override val resetSessionCommand = RESET_COMMAND
    override val resetTotalCommand = RESET_TOTAL_COMMAND

    const val PAUSE_COMMAND = "feeshPauseFishingProfitTracker"
    const val TOGGLE_VIEW_MODE_COMMAND = "feeshToggleFishingProfitTrackerViewMode"

    private val COINS_CATCH_PATTERN = Regex("^. (?:GOOD|GREAT|OUTSTANDING) CATCH! You caught (?:(?:a|an) )?([\\d,]+) Coins!")
    private val ICE_ESSENCE_CATCH_PATTERN = Regex("^. (?:GOOD|GREAT|OUTSTANDING) CATCH! You caught (?:(?:a|an) )?Ice Essence x([\\d,]+)!")
    private val AGATHA_CONTEST_BRACKET_PATTERN = Regex("^\\[NPC] Agatha: You reached the (COMMON|UNCOMMON|RARE|EPIC|LEGENDARY|MYTHIC|DIVINE|SPECIAL) Bracket in my contest!$")
    private val MIRIA_CONTEST_BRACKET_PATTERN = Regex("^\\[NPC] Miria: You reached the (COMMON|UNCOMMON|RARE|EPIC|LEGENDARY|MYTHIC|DIVINE|SPECIAL) Bracket in my contest!$")

    private const val TICKS_TIMER_ELAPSED_TIME = 20
    private const val HIDE_OVERLAY_AFTER_HOOK_MINUTES = 5
    internal const val FISHED_COINS_ITEM_ID = "FISHED_COINS"

    private val data: FishingProfitData
        get() = PersistentDataManager.feeshData.fishingProfit

    private var isSessionActive = false
    private var tickCounter = 0

    private val baseTitle = "${AQUA}${BOLD}Fishing profit tracker"

    private val gui = FeeshGui()
        .setCoordsDataKey("fishingProfitTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            "$baseTitle ${GRAY}[${GREEN}Total${GRAY}]",
            "${GRAY}- ${WHITE}1982${GRAY}x ${BLUE}Scorched Crab Stick${GRAY}: ${GOLD}333.9M",
            "${GRAY}- ${WHITE}5${GRAY}x ${LIGHT_PURPLE}${BOLD}Radioactive Vial${GRAY}: ${GOLD}288.1M",
            "${GRAY}- ${WHITE}44954${GRAY}x ${DARK_PURPLE}Silver Magmafish${GRAY}: ${GOLD}269.7M",
            "${GRAY}- ${WHITE}16${GRAY}x ${GRAY}[Lvl 100] ${LIGHT_PURPLE}Hermit Crab${GRAY}: ${GOLD}240M",
            "${GRAY}- ${WHITE}4${GRAY}x ${GRAY}[Lvl 100] ${GOLD}Baby Yeti${GRAY}: ${GOLD}53.3M",
            "${GRAY}- ${WHITE}318${GRAY}x ${GOLD}Nether Star${GRAY}: ${GOLD}45M",
            "${GRAY}- ${WHITE}100500${GRAY}x Other items: ${GOLD}1.8B",
            "",
            "${AQUA}Costs: ${RED}-12.5M",
            "${AQUA}Net profit: ${GOLD}${BOLD}2.99B ${RESET}${GRAY}(${GOLD}53.7M${GRAY}/h) ${DARK_GRAY}[offer]",
            "${AQUA}Catches: ${WHITE}57 234 ${GRAY}(${WHITE}1 020${GRAY}/h)",
            "${AQUA}Elapsed time: ${WHITE}56h 23m 3s",
        ))
        .setSettingsKey { Overlays.fishingProfitTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.fishingProfitTrackerCustomStyle }
        .setCondition {            
            isTrackerVisible()
        }

    fun init() {
        registerViewModeResetCommands()
        registerCommands()
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(CatchEvent::class, ::onCatch)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(PetLevelUpEvent::class, ::onPetReachedMaxLevel)
        EventBus.subscribe(SacksProfitItemsPickupEvent::class, ::onSacksProfitItemsPickup)
        EventBus.subscribe(InventoryProfitItemPickupEvent::class, ::onInventoryProfitItemPickup)
        EventBus.subscribe(ShardCaughtEvent::class, ::onShardCaught)
        EventBus.subscribe(IceEssenceStatusBarEvent::class, ::onIceEssenceStatusBar)
        EventBus.subscribe(PricesUpdatedEvent::class, ::onPricesUpdated)
        EventBus.subscribe(BaitConsumedEvent::class, ::onBaitConsumed)
        EventBus.subscribe(ShurikenUsedEvent::class, ::onShurikenUsed)
        EventBus.subscribe(MobyDuckConsumedEvent::class, ::onMobyDuckConsumed)
    }

    override fun onBeforeReset() {
        isSessionActive = false
    }

    override fun getCurrentViewMode(): TrackerViewMode {
        return try {
            TrackerViewMode.valueOf(data.viewMode)
        } catch (_: Exception) {
            TrackerViewMode.SESSION
        }
    }

    override fun hasSessionData(): Boolean {
        val session = getSourceObject(TrackerViewMode.SESSION)
        return session.totalProfit > 0.0 || session.profitTrackerItems.isNotEmpty() ||
            session.totalCost > 0.0 || session.costItems.isNotEmpty() ||
            session.elapsedSeconds > 0 || session.catchesCount > 0
    }

    override fun hasTotalData(): Boolean {
        val total = getSourceObject(TrackerViewMode.TOTAL)
        return total.totalProfit > 0.0 || total.profitTrackerItems.isNotEmpty() ||
            total.totalCost > 0.0 || total.costItems.isNotEmpty() ||
            total.elapsedSeconds > 0 || total.catchesCount > 0
    }

    override fun resetSessionData(force: Boolean) {
        data.session = FishingProfitSourceData()
        saveData(force)
        RareDropMessage.reset(force) // TODO Make them not dependent
    }

    override fun resetTotalData(force: Boolean) {
        data.total = FishingProfitSourceData()
        saveData(force)
    }

    override fun refreshGui() {
        updateGuiLines()
    }

    private fun registerCommands() {
        FishingProfitTrackerCommands.init()

        RegisterUtils.command(TOGGLE_VIEW_MODE_COMMAND) {
            toggleViewMode()
        }
        RegisterUtils.command(PAUSE_COMMAND) {
            pauseFishingProfitTracker()
        }
    }

    private fun onChat(event: ChatEvent) {
        if (!Overlays.fishingProfitTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

        // [NPC] Agatha: You reached the SPECIAL Bracket in my contest!
        AGATHA_CONTEST_BRACKET_PATTERN.find(event.unformattedText)?.run {
            onAgathaContestBracketReached(this.groupValues[1].orEmpty())
            return@onChat
        }

        // [NPC] Miria: You reached the LEGENDARY Bracket in my contest!
        MIRIA_CONTEST_BRACKET_PATTERN.find(event.unformattedText)?.run {
            onMiriaContestBracketReached(this.groupValues[1].orEmpty())
            return@onChat
        }

        // ⛃ GOOD CATCH! You caught 43,642 Coins!
        //  GOOD CATCH! You caught a 47,385 Coins!
        COINS_CATCH_PATTERN.find(event.unformattedText)?.run {
            onCoinsFished(this.groupValues[1].orEmpty())
            return@onChat
        }

        //  GREAT CATCH! You caught an Ice Essence x56!
        ICE_ESSENCE_CATCH_PATTERN.find(event.unformattedText)?.run {
            if (WorldUtils.getWorldName() == WorldUtils.JERRY_WORKSHOP) {
                onIceEssenceFished(this.groupValues[1].orEmpty())
            }
            return@onChat
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        pause()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_TIMER_ELAPSED_TIME) return
        tickCounter = 0

        refreshElapsedTime()
        updateGuiLines()
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetFishingProfitTrackerOnGameClosed) {
            resetOnGameClosed()
        }
    }

    private fun onPricesUpdated(@Suppress("UNUSED_PARAMETER") event: PricesUpdatedEvent) {
        refreshTotalItemsProfits()
    }

    private fun isTrackerDisabled(): Boolean {
        if (!Overlays.fishingProfitTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return true
        if (Overlays.shouldBeInactiveWhenInTrophyArmor && PlayerUtils.isInTrophyArmor()) return true
        return false
    }

    private fun isTrackerVisible(): Boolean {
        if (isTrackerDisabled()) return false
        if (!FishingHookUtils.wasFishingHookSubmergedMinutesAgo(HIDE_OVERLAY_AFTER_HOOK_MINUTES)) return false

        val viewMode = getCurrentViewMode()
        val hasData = if (viewMode == TrackerViewMode.SESSION) hasSessionData() else hasTotalData()
        return hasData
    }

    private fun pause() {
        isSessionActive = false
    }

    private fun onResetCostsInline() {
        FishingProfitTrackerCommands.onResetCostsCommand(emptyArray(), getCurrentViewMode())
    }

    private fun onResetCatchesInline() {
        FishingProfitTrackerCommands.onResetCatchesCommand(emptyArray(), getCurrentViewMode())
    }


    fun pauseFishingProfitTracker() {
        CommonUtils.runWithCatching("Failed to pause Fishing profit tracker") {
            if (!isSessionActive || !isTrackerVisible()) return
            pause()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Fishing profit tracker is paused.", true)
        }
    }

    private fun activateTimerInMode(viewMode: TrackerViewMode) {
        val sourceObj = getSourceObject(viewMode)
        if (sourceObj.elapsedSeconds == 0) {
            sourceObj.elapsedSeconds = 1
        }
    }

    private fun refreshElapsedTime() {
        if (isTrackerDisabled()) {
            pause()
            return
        }

        val prevIsActive = isSessionActive
        val isHookActive = FishingHookUtils.isFishingHookSubmerged()

        // Start fishing timer after pause or when tracker was empty
        if (isHookActive) {
            isSessionActive = true
            activateTimerInMode(TrackerViewMode.SESSION)
            activateTimerInMode(TrackerViewMode.TOTAL)
            saveData()

            if (!prevIsActive) {
                refreshTotalItemsProfits()
                return
            }
        }

        if (!isSessionActive || !isTrackerVisible()) {
            pause()
            return
        }
        val lastHookSeenAt = FishingHookUtils.lastSubmergedFishingHookSeenAt() ?: return
        val elapsedSinceHook = (Date().time - lastHookSeenAt.time) / 1000
        if (elapsedSinceHook < Overlays.trackersAutoPauseSeconds) {
            data.session.elapsedSeconds += 1
            data.total.elapsedSeconds += 1
            saveData()
        } else {
            pauseFishingProfitTracker()
        }
    }

    fun refreshTotalItemsProfits() {
        if (!isTrackerVisible()) return
        refreshTotalItemsProfitsInMode(TrackerViewMode.SESSION)
        refreshTotalItemsProfitsInMode(TrackerViewMode.TOTAL)
        saveData()
        updateGuiLines()
    }

    internal fun refreshTotalItemsProfitsInMode(viewMode: TrackerViewMode) {
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

        sourceObj.costItems.forEach { (itemId, entry) ->
            val itemPrice = getCostItemPrice(itemId)
            entry.totalItemCost = entry.amount * itemPrice
        }
        sourceObj.totalCost = sourceObj.costItems.values.sumOf { it.totalItemCost }
        saveData()
    }

    private fun getItemPrice(dropInfo: FishingProfitDropInfo): Double {
        if (dropInfo.amountOfMagmaFish != null) {
            return getTrophyFishItemPrice(dropInfo.amountOfMagmaFish)
        } else if (dropInfo.amountOfLotus != null) {
            val lotusPrice = getPriceByMode("LOTUS")
            return dropInfo.amountOfLotus * lotusPrice
        }

        if (dropInfo.categories.contains(FishingProfitDrops.CRIMSON_ISLE_TRASH_GEAR_CATEGORY)) {
            return when (Overlays.priceModeForCrimsonIsleTrashGearDrops) {
                CrimsonIsleTrashGearDropsPriceMode.ESSENCE -> {
                    if (Overlays.fishingProfitTrackerPriceMode == PricingModeWithNpc.NPC_SELL) return 0.0
                    if (dropInfo.salvage == null) return 0.0
                    val bazaar = PriceUtils.getBazaarItemPrices(dropInfo.salvage.essenceItemId)
                    val price = when (Overlays.fishingProfitTrackerPriceMode) {
                        PricingModeWithNpc.INSTA_SELL -> bazaar?.instaSell ?: 0.0
                        else -> bazaar?.sellOffer ?: 0.0
                    }
                    dropInfo.salvage.essenceCount * price
                }
                CrimsonIsleTrashGearDropsPriceMode.NPC_PRICE -> {
                    dropInfo.npcPrice ?: 0.0
                }
                CrimsonIsleTrashGearDropsPriceMode.NORMAL -> getPriceByMode(dropInfo.itemId)
            }
        }

        return getPriceByMode(dropInfo.itemId)
    }

    private fun getTrophyFishItemPrice(amountOfMagmaFish: Int): Double {
        val magmaPrice = getPriceByMode("MAGMA_FISH")
        return amountOfMagmaFish * magmaPrice
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

    private fun getCostItemPrice(itemId: String): Double {
        if (Overlays.fishingProfitTrackerPriceMode == PricingModeWithNpc.NPC_SELL) {
            return 0.0
        }

        if (itemId.startsWith("OBFUSCATED")) {
            val trophyFishInfo = TrophyFish.ALL_TROPHY_FISH_BY_ID[itemId] ?: return 0.0
            return getTrophyFishItemPrice(trophyFishInfo.amountOfMagmaFish)
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

        return 0.0
    }

    private fun addCostTrackerItem(itemId: String, itemName: String, amountToAdd: Int) {
        addCostTrackerItemInMode(TrackerViewMode.SESSION, itemId, itemName, amountToAdd)
        addCostTrackerItemInMode(TrackerViewMode.TOTAL, itemId, itemName, amountToAdd)
        refreshTotalItemsProfits()
    }

    private fun addCostTrackerItemInMode(
        viewMode: TrackerViewMode,
        itemId: String,
        itemName: String,
        amountToAdd: Int
    ) {
        val sourceObj = getSourceObject(viewMode)
        val existing = sourceObj.costItems[itemId]
        val currentAmount = existing?.amount ?: 0
        sourceObj.costItems[itemId] = ProfitTrackerCostEntry(
            itemName = itemName,
            itemId = itemId,
            amount = currentAmount + amountToAdd,
            totalItemCost = existing?.totalItemCost ?: 0.0
        )
        saveData()
    }

    private fun onBaitConsumed(event: BaitConsumedEvent) {
        CommonUtils.runWithCatching("Failed to add bait to cost tracker in Fishing profit tracker") {
            if (!isCostOrNetProfitTrackingEnabled()) return
            if (!isSessionActive || !isTrackerVisible()) return
            if (event.baitName.isBlank() || event.baitId.isBlank()) return
            val itemName = event.baitName
            addCostTrackerItem(event.baitId, itemName, 1)
        }
    }

    private fun onShurikenUsed(event: ShurikenUsedEvent) {
        CommonUtils.runWithCatching("Failed to add Shuriken to cost tracker in Fishing profit tracker") {
            if (!isCostOrNetProfitTrackingEnabled()) return
            if (!isTrackerVisible()) return // Track if overlay is visible even if paused
            if (event.itemName.isBlank() || event.itemId.isBlank()) return
            addCostTrackerItem(event.itemId, event.itemName, 1)
        }
    }

    private fun onMobyDuckConsumed(event: MobyDuckConsumedEvent) {
        CommonUtils.runWithCatching("Failed to add Moby-Duck to cost tracker in Fishing profit tracker") {
            if (!isCostOrNetProfitTrackingEnabled()) return
            if (isTrackerDisabled()) return // Track if overlay enabled even if not visible
            if (event.itemName.isBlank() || event.itemId.isBlank()) return
            addCostTrackerItem(event.itemId, event.itemName, 1)
        }
    }

    private fun onInventoryProfitItemPickup(event: InventoryProfitItemPickupEvent) {
        
        fun onItemAddedToInventory(itemId: String, difference: Int) {
            val dropInfo = FishingProfitDrops.items.find { it.itemId == itemId } ?: return            
            if (difference <= 0) return
    
            addProfitTrackerItem(itemId, dropInfo.itemName, difference, null, true)
    
            if (Overlays.shouldAnnounceRareDropsWhenPickup && dropInfo.shouldAnnounceRareDrop) {
                announceRareDropInChat(dropInfo, difference)
            }
        }

        if (!isSessionActive || !isTrackerVisible()) return

        CommonUtils.runWithCatching("Failed to process inventory profit item pickup event in $trackerName") {
            onItemAddedToInventory(event.itemId, event.amount)
            refreshTotalItemsProfits()
        }
    }

    private fun onSacksProfitItemsPickup(event: SacksProfitItemsPickupEvent) {
        if (!isSessionActive || !isTrackerVisible()) return

        CommonUtils.runWithCatching("Failed to process sacks profit items pickup event in $trackerName") {
            for (item in event.items) {
                val dropInfo = FishingProfitDrops.items.find { it.itemId == item.itemId } ?: continue
                addProfitTrackerItem(dropInfo.itemId, dropInfo.itemName, item.amount, null, true)

                if (Overlays.shouldAnnounceRareDropsWhenPickup && dropInfo.shouldAnnounceRareDrop) {
                    announceRareDropInChat(dropInfo, item.amount)
                }
            }
            refreshTotalItemsProfits()
        }
    }

    private fun onIceEssenceStatusBar(event: IceEssenceStatusBarEvent) {
        if (WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return
        if (!isSessionActive || !isTrackerVisible()) return
        findAndAddProfitTrackerItem({ it.itemId == "ESSENCE_ICE" }, event.amount)
    }

    private fun onCatch(@Suppress("UNUSED_PARAMETER") event: CatchEvent) {
        CommonUtils.runWithCatching("Failed to track catch in $trackerName") {
            if (!Overlays.shouldShowCatchesInFishingProfitTracker) return
            if (!isSessionActive || !isTrackerVisible()) return
            data.session.catchesCount += 1
            data.total.catchesCount += 1
            saveData()
            updateGuiLines()
        }
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

    private fun onShardCaught(event: ShardCaughtEvent) {
        CommonUtils.runWithCatching("Failed to add shard to Fishing profit tracker") {
            if (!isSessionActive || !isTrackerVisible() || event.count <= 0) return@onShardCaught
            val shardName = event.shardName
            val predicate = { item: FishingProfitDropInfo -> item.itemName.equals(shardName, ignoreCase = true) || item.itemAlternateNames.any { alt -> alt.equals(shardName, ignoreCase = true) } }
            findAndAddProfitTrackerItem(predicate, event.count)    
        }
    }

    private fun onAgathaContestBracketReached(bracket: String) {
        if (!isTrackerVisible()) return
        if (WorldUtils.getWorldName() != WorldUtils.MOONGLADE_MARSH) return

        val (agathaCouponCount, forestEssenceCount) = StarlynContests.AGATHA_CONTEST_BRACKET_REWARDS_MAP[bracket.uppercase()] ?: return
        findAndAddProfitTrackerItem({ it.itemId == "AGATHA_COUPON" }, agathaCouponCount)
        findAndAddProfitTrackerItem({ it.itemId == "ESSENCE_FOREST" }, forestEssenceCount)
    }

    private fun onMiriaContestBracketReached(bracket: String) {
        if (!isTrackerVisible()) return
        if (WorldUtils.getWorldName() != WorldUtils.TORRHUS_CANYON) return

        val (miriaCouponCount, forestEssenceCount) = StarlynContests.MIRIA_CONTEST_BRACKET_REWARDS_MAP[bracket.uppercase()] ?: return
        findAndAddProfitTrackerItem({ it.itemId == "MIRIA_COUPON" }, miriaCouponCount)
        findAndAddProfitTrackerItem({ it.itemId == "ESSENCE_FOREST" }, forestEssenceCount)
    }

    private fun onPetReachedMaxLevel(event: PetLevelUpEvent) {
        CommonUtils.runWithCatching("Failed to add max level pet to Fishing profit tracker") {
            if (!isTrackerVisible()) return@onPetReachedMaxLevel
            val itemIdMaxLevel = ItemUtils.getMaxedPetId(event.petDisplayName, event.level)
            addProfitTrackerItem(itemIdMaxLevel, event.petName, 1, null)
        }
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
        addProfitTrackerItemInMode(TrackerViewMode.SESSION, itemId, itemName, amountToAdd, coinsToAdd)
        addProfitTrackerItemInMode(TrackerViewMode.TOTAL, itemId, itemName, amountToAdd, coinsToAdd)
        if (!isBulk) refreshTotalItemsProfits()
    }

    private fun addProfitTrackerItemInMode(
        viewMode: TrackerViewMode,
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

    private fun announceRareDropInChat(dropInfo: FishingProfitDropInfo, count: Int) {
        if (!Overlays.shouldAnnounceRareDropsWhenPickup || !dropInfo.shouldAnnounceRareDrop) return

        val diffText = if (count > 1) " ${RESET}${GRAY}${count}x" else ""
        ChatUtils.sendLocalChat("${GOLD}${BOLD}RARE DROP! ${RESET}${dropInfo.itemDisplayName}$diffText", true)

        if (General.soundMode != SoundMode.OFF) SoundUtils.playCustomSound(Sounds.FEESH_RARE_DROP)
    }

    private fun toggleViewMode() {
        val newMode = if (getCurrentViewMode() == TrackerViewMode.SESSION) TrackerViewMode.TOTAL else TrackerViewMode.SESSION
        data.viewMode = newMode.name
        saveData()
        updateGuiLines()
    }

    internal fun getSourceObject(viewMode: TrackerViewMode): FishingProfitSourceData {
        return when (viewMode) {
            TrackerViewMode.SESSION -> data.session
            TrackerViewMode.TOTAL -> data.total
        }
    }

    private fun isCostOrNetProfitTrackingEnabled(): Boolean {
        return Overlays.shouldShowCostsInFishingProfitTracker || Overlays.shouldShowNetProfitInFishingProfitTracker
    }

    private fun onLineItemIncrease(itemId: String) {
        CommonUtils.runWithCatching("Failed to change item count in Fishing profit tracker") {
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
        }
    }

    private fun onLineItemDecrease(itemId: String) {
        CommonUtils.runWithCatching("Failed to change item count in the Fishing profit tracker") {
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
        }
    }

    private fun onLineItemDelete(itemId: String) {
        CommonUtils.runWithCatching("Failed to delete item from Fishing profit tracker") {
            if (!isTrackerVisible()) return

            val viewMode = getCurrentViewMode()
            FishingProfitTrackerCommands.onDeleteItemCommand(arrayOf(itemId), viewMode)
        }
    }

    internal fun updateGuiLines() {
        CommonUtils.runWithCatching("Failed to update Fishing profit tracker GUI lines") {
            gui.clearLines()
            if (!isTrackerVisible()) return

            val viewMode = getCurrentViewMode()
            val nextMode = if (viewMode == TrackerViewMode.SESSION) TrackerViewMode.TOTAL else TrackerViewMode.SESSION

            GuiLinesBuilder(
                displayData = getDisplayTrackerData(viewMode),
                viewMode = viewMode,
                nextMode = nextMode,
            )
                .buildTitle()
                .buildItemsList()
                .buildHiddenItems()
                .buildProfitsAndCosts()
                .buildCatches()
                .buildElapsedTimer()
                .buildButtons()
                .finish()
        }
    }

    private class GuiLinesBuilder(
        private val displayData: DisplayTrackerData,
        private val viewMode: TrackerViewMode,
        private val nextMode: TrackerViewMode
    ) {
        private val viewModeText = getViewModeDisplayText(viewMode)
        private val nextViewModeText = getViewModeDisplayText(nextMode)
        private val lines = mutableListOf<LineInfo>()
        private val hideTimerAndCoinsPerHour = Overlays.shouldHideTimerInTotal && viewMode == TrackerViewMode.TOTAL
        private val hiddenItemsRow: List<String>? = if (displayData.entriesToHide.isNotEmpty()) {
            val profitStr = CommonUtils.toShortNumber(displayData.hiddenItemsPrice) ?: "0"
            val countStr = CommonUtils.formatNumberWithSpaces(displayData.hiddenItemsCount)
            val typesStr = CommonUtils.formatNumberWithSpaces(displayData.hiddenItemsTypesCount)
            TrackerLineColumns(
                item = "${GRAY}- ${WHITE}${countStr}${GRAY}x items of ${WHITE}${typesStr} ${GRAY}types",
                price = "${GOLD}$profitStr",
            ).toCells()
        } else {
            null
        }
        private val tableLayout = Table.layout(
            FeeshMod.mc.font,
            displayData.entriesToShow.map { getProfitTrackerLineColumns(it).toCells() } + listOfNotNull(hiddenItemsRow),
            getColumnsSeparator(),
        )
        private var tableRowIndex = 0

        fun buildTitle(): GuiLinesBuilder {
            lines.add(LineInfo("$baseTitle $viewModeText"))
            return this
        }

        fun buildItemsList(): GuiLinesBuilder {
            for (entry in displayData.entriesToShow) {
                val itemId = entry.itemId
                val actions = if (itemId == FISHED_COINS_ITEM_ID) {
                    listOf(LineAction("${GRAY}[${RED}x${GRAY}]") { onLineItemDelete(itemId) })
                } else {
                    listOf(
                        LineAction("${GRAY}[${GREEN}+${GRAY}]") { onLineItemIncrease(itemId) },
                        LineAction("${GRAY}[${RED}-${GRAY}]") { onLineItemDecrease(itemId) },
                        LineAction("${GRAY}[${RED}x${GRAY}]") { onLineItemDelete(itemId) },
                    )
                }
                lines.add(
                    LineInfo.withCells(
                        cells = tableLayout.rows[tableRowIndex++],
                        tableWidth = tableLayout.tableWidth,
                        actions = actions,
                    )
                )
            }
            return this
        }

        fun buildHiddenItems(): GuiLinesBuilder {
            if (hiddenItemsRow == null) return this
            lines.add(
                LineInfo.withCells(
                    cells = tableLayout.rows[tableRowIndex++],
                    tableWidth = tableLayout.tableWidth,
                )
            )
            return this
        }

        fun buildProfitsAndCosts(): GuiLinesBuilder {
            val showProfit = Overlays.shouldShowTotalProfitInFishingProfitTracker
            val showCosts = Overlays.shouldShowCostsInFishingProfitTracker
            val showNetProfit = Overlays.shouldShowNetProfitInFishingProfitTracker
            if (!showProfit && !showCosts && !showNetProfit) return this

            val priceModeStr = when (Overlays.fishingProfitTrackerPriceMode) {
                PricingModeWithNpc.SELL_OFFER -> "${DARK_GRAY}[offer]"
                PricingModeWithNpc.INSTA_SELL -> "${DARK_GRAY}[insta]"
                PricingModeWithNpc.NPC_SELL -> "${DARK_GRAY}[NPC sell]"
            }

            lines.add(LineInfo(""))
            if (showProfit) {
                val totalStr = CommonUtils.toShortNumber(displayData.totalProfit) ?: "0"
                if (hideTimerAndCoinsPerHour) {
                    lines.add(LineInfo("${AQUA}Profit: ${GOLD}${BOLD}$totalStr $priceModeStr"))
                } else {
                    val perHourStr = CommonUtils.toShortNumber(displayData.totalProfitPerHour) ?: "0"
                    lines.add(LineInfo("${AQUA}Profit: ${GOLD}${BOLD}$totalStr ${RESET}${GRAY}(${GOLD}$perHourStr${GRAY}/h) $priceModeStr"))
                }
            }
            if (showCosts) {
                val costStr = CommonUtils.toShortNumber(displayData.costs) ?: "0"
                lines.add(
                    LineInfo(
                        text = "${AQUA}Costs: ${RED}-${costStr}",
                        tooltip = getCostsTooltip(displayData),
                        actions = listOf(LineAction("${GRAY}[${RED}x${GRAY}]") { onResetCostsInline() }),
                    )
                )
            }
            if (showNetProfit) {
                val netProfitNumberStr = CommonUtils.toShortNumber(displayData.netProfit) ?: "0"
                val netProfitColor = if (displayData.netProfit < 0) RED else GOLD
                val netProfitStr = "${AQUA}Net profit: ${netProfitColor}${BOLD}${netProfitNumberStr}"
                val text = if (hideTimerAndCoinsPerHour) {
                    "${netProfitStr} ${priceModeStr}"
                } else {
                    val netProfitPerHourStr = CommonUtils.toShortNumber(displayData.netProfitPerHour) ?: "0"
                    "${netProfitStr} ${RESET}${GRAY}(${netProfitColor}${netProfitPerHourStr}${GRAY}/h) ${priceModeStr}"
                }
                lines.add(LineInfo(text = text, tooltip = getNetProfitTooltip(displayData)))
            }
            return this
        }

        private fun getCostsTooltip(displayData: DisplayTrackerData): List<Component> {
      
            fun getCostItemLines(displayData: DisplayTrackerData): List<String> {
                val sorted = displayData.costEntries.sortedByDescending { it.cost }
                val maxEntriesToShow = 10
                val topEntries = sorted.take(maxEntriesToShow)
                val otherEntries = sorted.drop(maxEntriesToShow)
    
                fun getFormattedCostLine(entry: CostEntryData): String {
                    val countStr = CommonUtils.formatNumberWithSpaces(entry.amount)
                    val costStr = CommonUtils.toShortNumber(entry.cost) ?: "0"
                    val unitPrice = if (entry.amount > 0) entry.cost / entry.amount else 0.0
                    val unitStr = CommonUtils.toShortNumber(unitPrice) ?: "0"
                    return "${GRAY}- ${WHITE}${countStr}${GRAY}x ${entry.item}${GRAY}: ${RED}$costStr ${DARK_GRAY}(${RED}$unitStr ${DARK_GRAY}each)"
                }
    
                val itemLines = topEntries.map { getFormattedCostLine(it) }.toMutableList()
                if (otherEntries.isNotEmpty()) {
                    val otherCost = otherEntries.sumOf { it.cost }
                    val otherCostStr = CommonUtils.toShortNumber(otherCost) ?: "0"
                    itemLines.add("${GRAY}- Other items: ${RED}$otherCostStr")
                }
                return itemLines
            }

            val totalStr = CommonUtils.toShortNumber(displayData.costs) ?: "0"
            val tooltipLines = listOf("${AQUA}${BOLD}Costs") +
                "${GRAY}It's cost of the bait, shurikens, and Moby-Ducks spent while fishing." +
                "" +
                getCostItemLines(displayData) +
                "" +
                "${AQUA}Total coins spent: ${RED}${totalStr}"
            return tooltipLines.map { Component.literal(it) }
        }

        private fun getNetProfitTooltip(displayData: DisplayTrackerData): List<Component> {
            val profitStr = CommonUtils.toShortNumber(displayData.totalProfit) ?: "0"
            val costStr = CommonUtils.toShortNumber(displayData.costs) ?: "0"
            val netStr = CommonUtils.toShortNumber(displayData.netProfit) ?: "0"
            val netColor = if (displayData.netProfit < 0) RED else GOLD
            val profitPerHourPart = if (hideTimerAndCoinsPerHour) {
                ""
            } else {
                val perHourStr = CommonUtils.toShortNumber(displayData.totalProfitPerHour) ?: "0"
                " ${GRAY}(${GOLD}$perHourStr${GRAY}/h)"
            }
            val netPerHourPart = if (hideTimerAndCoinsPerHour) {
                ""
            } else {
                val perHourStr = CommonUtils.toShortNumber(displayData.netProfitPerHour) ?: "0"
                " ${GRAY}(${netColor}$perHourStr${GRAY}/h)"
            }
            val tooltipLines = listOf(
                "${AQUA}${BOLD}Net profit",
                "${GRAY}It's Profit (value of all items) minus Costs spent while fishing.",
                "",
                "${AQUA}Profit: ${GOLD}$profitStr$profitPerHourPart",
                "${AQUA}Costs: ${RED}-$costStr",
                "${AQUA}Net profit: ${netColor}${BOLD}$netStr$netPerHourPart"
            )
            return tooltipLines.map { Component.literal(it) }
        }

        fun buildCatches(): GuiLinesBuilder {

            fun getCatchesTooltip(displayData: DisplayTrackerData): List<Component> {
                val lines = mutableListOf<String>()
                if (Overlays.shouldShowTotalProfitInFishingProfitTracker) {
                    val profitStr = CommonUtils.toShortNumber(displayData.profitPerCatch) ?: "0"
                    lines.add("${AQUA}Profit per catch: ${GOLD}$profitStr")
                }
                if (Overlays.shouldShowNetProfitInFishingProfitTracker) {
                    val netProfitStr = CommonUtils.toShortNumber(displayData.netProfitPerCatch) ?: "0"
                    val netProfitColor = if (displayData.netProfitPerCatch < 0) RED else GOLD
                    lines.add("${AQUA}Net profit per catch: ${netProfitColor}$netProfitStr")
                }
                return lines.map { Component.literal(it) }
            }
    
            if (!Overlays.shouldShowCatchesInFishingProfitTracker || displayData.catchesCount <= 0) return this

            val catchesStr = CommonUtils.formatNumberWithSpaces(displayData.catchesCount)
            val catchesLine = if (hideTimerAndCoinsPerHour) {
                "${AQUA}Catches: ${WHITE}$catchesStr"
            } else {
                val perHourStr = CommonUtils.formatNumberWithSpaces(displayData.catchesPerHour)
                "${AQUA}Catches: ${WHITE}$catchesStr ${GRAY}(${WHITE}$perHourStr${GRAY}/h)"
            }
            lines.add(
                LineInfo(
                    text = catchesLine,
                    tooltip = getCatchesTooltip(displayData),
                    actions = listOf(LineAction("${GRAY}[${RED}x${GRAY}]") { onResetCatchesInline() }),
                )
            )
            return this
        }

        fun buildElapsedTimer(): GuiLinesBuilder {
            if (hideTimerAndCoinsPerHour) return this
            val elapsedStr = CommonUtils.formatTimeElapsed(displayData.elapsedSeconds)
            val pausedSuffix = if (isSessionActive) "" else " ${GRAY}[Paused]"
            lines.add(LineInfo("${AQUA}Elapsed time: ${WHITE}$elapsedStr$pausedSuffix"))
            return this
        }

        fun buildButtons(): GuiLinesBuilder {
            gui.setButtons(
                listOf(
                    GuiButton(0, "${GRAY}[Click to show ${nextViewModeText}${GRAY}]", { toggleViewMode() }),
                    GuiButton(1, "${GRAY}[${YELLOW}Click to pause${GRAY}]", { pauseFishingProfitTracker() }),
                    getResetGuiButton(2) { requestReset() },
                )
            )
            return this
        }

        fun finish() {
            gui.setLines(lines)
        }
    }

    private data class DisplayTrackerData(
        val entriesToShow: List<ItemEntryData>,
        val entriesToHide: List<ItemEntryData>,
        val hiddenItemsCount: Int,
        val hiddenItemsTypesCount: Int,
        val hiddenItemsPrice: Double,
        val elapsedSeconds: Int,
        val totalProfit: Double,
        val totalProfitPerHour: Double,
        val costEntries: List<CostEntryData>,
        val costs: Double,
        val netProfit: Double,
        val netProfitPerHour: Double,
        val catchesCount: Int,
        val catchesPerHour: Int,
        val profitPerCatch: Double,
        val netProfitPerCatch: Double
    )

    private data class ItemEntryData(val itemId: String, val item: String, val amount: Int, val profit: Double)

    private data class CostEntryData(val itemId: String, val item: String, val amount: Int, val cost: Double)

    private data class TrackerLineColumns(val item: String, val price: String) {
        fun toCells(): List<String> = listOf(item, price)
    }

    private fun getColumnsSeparator(): String = " "

    private fun getProfitTrackerLineColumns(entry: ItemEntryData): TrackerLineColumns {
        val countStr = CommonUtils.formatNumberWithSpaces(entry.amount)
        val profitStr = CommonUtils.toShortNumber(entry.profit) ?: "0"
        return TrackerLineColumns(
            item = "${GRAY}- ${WHITE}${countStr}${GRAY}x ${entry.item}",
            price = "${GOLD}$profitStr",
        )
    }

    internal fun getDisplayNameForGui(itemId: String, itemName: String): String {
        return when {
            ItemUtils.isMaxedPet(itemId) -> ItemUtils.getLeveledPetDisplayNameByPetIdAndName(itemId, itemName)
            itemId == FISHED_COINS_ITEM_ID -> "${GOLD}Fished Coins"
            else -> FishingProfitDrops.items.find { it.itemId == itemId }?.itemDisplayName ?: itemName
        }
    }

    private fun getDisplayTrackerData(viewMode: TrackerViewMode): DisplayTrackerData {

        fun isDyeDrop(itemId: String): Boolean {
            return FishingProfitDrops.items.find { it.itemId == itemId }?.categories?.contains(FishingProfitDrops.DYE_CATEGORY) == true
        }

        val sourceObj = getSourceObject(viewMode)
        val minPrice = if (viewMode == TrackerViewMode.SESSION) Overlays.fishingProfitTrackerHideCheaperThan.toDouble() else Overlays.fishingProfitTrackerHideCheaperThanTotal.toDouble()
        val topN = Overlays.fishingProfitTrackerShowTop.coerceIn(1, 50)
        val pinDyes = Overlays.fishingProfitTrackerPriceMode == PricingModeWithNpc.NPC_SELL
        val entries = sourceObj.profitTrackerItems.values.map { v ->
            ItemEntryData(v.itemId, getDisplayNameForGui(v.itemId, v.itemName), v.amount, v.totalItemProfit)
        }.sortedWith(
            if (pinDyes) compareByDescending<ItemEntryData> { isDyeDrop(it.itemId) }.thenByDescending { it.profit }
            else compareByDescending { it.profit }
        )
        val expensive = entries.filter { it.profit >= minPrice || (pinDyes && isDyeDrop(it.itemId)) }
        val cheap = entries.filter { it.profit < minPrice && !(pinDyes && isDyeDrop(it.itemId)) }
        val toShow = expensive.take(topN)
        val toHide = expensive.drop(topN) + cheap
        val elapsedHours = sourceObj.elapsedSeconds / 3600.0
        val profitPerHour = if (elapsedHours > 0) sourceObj.totalProfit / elapsedHours else 0.0
        val costEntries = sourceObj.costItems.values.map { v ->
            CostEntryData(v.itemId, v.itemName, v.amount, v.totalItemCost)
        }.sortedByDescending { it.cost }
        val netProfit = sourceObj.totalProfit - sourceObj.totalCost
        val netProfitPerHour = if (elapsedHours > 0) netProfit / elapsedHours else 0.0
        val catchesCount = sourceObj.catchesCount
        val catchesPerHour = if (elapsedHours > 0) (catchesCount / elapsedHours).toInt() else 0
        val profitPerCatch = if (catchesCount > 0) sourceObj.totalProfit / catchesCount else 0.0
        val netProfitPerCatch = if (catchesCount > 0) netProfit / catchesCount else 0.0

        return DisplayTrackerData(
            entriesToShow = toShow,
            entriesToHide = toHide,
            hiddenItemsCount = toHide.sumOf { it.amount },
            hiddenItemsTypesCount = toHide.size,
            hiddenItemsPrice = toHide.sumOf { it.profit },
            elapsedSeconds = sourceObj.elapsedSeconds,
            totalProfit = sourceObj.totalProfit,
            totalProfitPerHour = profitPerHour,
            costEntries = costEntries,
            costs = sourceObj.totalCost,
            netProfit = netProfit,
            netProfitPerHour = netProfitPerHour,
            catchesCount = catchesCount,
            catchesPerHour = catchesPerHour,
            profitPerCatch = profitPerCatch,
            netProfitPerCatch = netProfitPerCatch
        )
    }

    internal fun saveData(force: Boolean = false) {
        if (force) {
            PersistentDataManager.forceSaveFeeshDataToFileSync()
        } else {
            PersistentDataManager.saveFeeshDataToFileAsync()
        }
    }
}
