package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.FishingProfitDrops
import com.github.sleepypanda.feesh.constants.FishingProfitDropInfo
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GuiClosedEvent
import com.github.sleepypanda.feesh.events.models.InventoryProfitItemPickupEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.ChatUtils // TODO: Remove this
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.GuiUtils
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.google.gson.JsonParser
import net.minecraft.world.item.ItemStack
import java.util.Date
import kotlin.collections.set

// You claimed True Ice from [MVP+] Minecraft_Kides's auction! - Individual msg for each drop
// Accessory Bag (1/4)
// Ignore whatever while in Trades menu
  // Trade completed message? Check for both cancelled and completed trades
// You Supercrafted Polished Pumpkin x7!

// [Bazaar] Cancelled! Refunded 3x Shinyfish Shard from cancelling Sell Offer! - doesnt work

// You claimed Worm the Fish back from your expired auction!
// Claim from bz counts - is in Co-op Bazaar Orders or Order Options
// Gfs 

object InventoryItemPickupPublisher {
    private var previousInventory: MutableMap<String, Int>? = null
    private var previousContainer: MutableMap<String, Int>? = null

    private const val TICKS_INVENTORY_SCAN = 5
    private var tickCounter = 0

    private const val INVENTORY_STABLE_MS = 3_500L // After world change, items are loaded not instantly / partially, we want to skip "false" item pickups
    private var isInventoryLoaded = false
    private var lastFingerprint: String? = null
    private var fingerprintStableSince: Long? = null

    // [Bazaar] Cancelled! Refunded 26x Titanoboa Shard from cancelling Sell Offer!
    // [Bazaar] Cancelled! Refunded 1x Prosperity I from cancelling Sell Offer!
    // [Bazaar] Bought 64x Raw Cod for 25,146 coins!
    // [Bazaar] Claimed 640x Raw Cod worth 193,088 coins bought for 301.7 each!
    private val BAZAAR_ITEM_PATTERN = Regex("^\\[Bazaar\\] (?:Cancelled!|Bought|Claimed).*")
    private var lastBazaarMessage: String? = null
    private var lastBazaarItemAt: Date? = null

    // Bought back from Booster Cookie or NPC,
    // You bought back Lucky Hoof x1 for 50,000 Coins!
    // You bought back Lily Pad x64 for 640 Coins!
    private val BOUGHT_BACK_PATTERN = Regex("^You bought back .*")
    private var lastBoughtBackMessage: String? = null
    private var lastBoughtBackAt: Date? = null

    // You Supercrafted Polished Pumpkin x7!
    private val SUPERCRAFTED_PATTERN = Regex("^You Supercrafted .*")
    private var lastSupercraftedMessage: String? = null
    private var lastSupercraftedAt: Date? = null

    // You bought Treasure Bait x16!
    private val NPC_BOUGHT_PATTERN = Regex("^You bought (?!back).*")
    private var lastBoughtMessage: String? = null
    private var lastBoughtAt: Date? = null

    // [NPC] Kat: I was able to upgrade your pet Guardian to LEGENDARY.
    // [NPC] Kat: ✆ Hi! I've finished training your Guardian!
    private val KAT_UPGRADE_PATTERN = Regex("^\\[NPC\\] Kat: I was able to upgrade your pet (.+) to .*")
    private val ABIPHONE_KAT_CALL_PATTERN = Regex("^\\[NPC\\] Kat: ✆ Hi! I've finished training your (.+)!.*")
    private var lastKatPetName: String? = null
    private var lastKatPetClaimedAt: Date? = null

    // Moved 3,900 Enchanted Sea Lumies from your Sacks to your inventory.
    private val GFS_COMMAND_PATTERN = Regex("^Moved [\\d,]+ (.+) from your Sacks to your inventory\\.$")
    private var lastGfsItemName: String? = null
    private var lastGfsAt: Date? = null

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(ChatCancellableEvent::class, ::onChat)
        EventBus.subscribe(GuiClosedEvent::class, ::onGuiClosed)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onChat(event: ChatCancellableEvent) {
        if (!WorldUtils.isInSkyblock()) return

        CommonUtils.runWithCatching("Failed to handle chat messages in inventory item pickuppublisher.") {
            if (BAZAAR_ITEM_PATTERN.matches(event.unformattedText)) {
                lastBazaarMessage = event.unformattedText
                lastBazaarItemAt = Date()
                return@onChat
            }
            if (BOUGHT_BACK_PATTERN.matches(event.unformattedText)) {
                lastBoughtBackMessage = event.unformattedText
                lastBoughtBackAt = Date()
                return@onChat
            }
            if (SUPERCRAFTED_PATTERN.matches(event.unformattedText)) {
                lastSupercraftedMessage = event.unformattedText
                lastSupercraftedAt = Date()
                return@onChat
            }
            if (NPC_BOUGHT_PATTERN.matches(event.unformattedText)) {
                lastBoughtMessage = event.unformattedText
                lastBoughtAt = Date()
                return@onChat
            }
            KAT_UPGRADE_PATTERN.find(event.unformattedText)?.run {
                lastKatPetName = this.groupValues[1].removeFormatting()
                lastKatPetClaimedAt = Date()
                return@onChat
            }
            ABIPHONE_KAT_CALL_PATTERN.find(event.unformattedText)?.run {
                lastKatPetName = this.groupValues[1].removeFormatting()
                lastKatPetClaimedAt = Date()
                return@onChat
            }
            GFS_COMMAND_PATTERN.find(event.unformattedText)?.run {
                lastGfsItemName = this.groupValues[1].removeFormatting()
                lastGfsAt = Date()
                return@onChat
            }    
        }
    }

    private fun onGuiClosed(@Suppress("UNUSED_PARAMETER") event: GuiClosedEvent) {
        detectInventoryChanges() // Actualize inventory state after taking items from chest and quickly closing a GUI
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        resetInventoryState()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_INVENTORY_SCAN) return
        tickCounter = 0
        
        CommonUtils.runWithCatching("Failed to detect inventory changes") {
            detectInventoryChanges()
        }
    }

    private fun detectInventoryChanges() {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) {
            resetInventoryState()
            return
        }

        if (!isInventoryLoaded) {
            updateInventoryLoadedState()
            return
        }

        val currentInventory = getFishingProfitItemsInCurrentInventory()
        val currentContainer = getFishingProfitItemsInOpenContainer()

        if (previousInventory == null) {
            previousInventory = currentInventory.toMutableMap()
            previousContainer = currentContainer.toMutableMap()
            return
        }

        val previousInv = previousInventory!!
        val previousCont = previousContainer ?: emptyMap()

        for ((itemId, currentInventoryTotal) in currentInventory) {
            val previousInventoryTotal = previousInv[itemId] ?: 0
            if (currentInventoryTotal <= previousInventoryTotal) continue

            val inventoryIncrease = currentInventoryTotal - previousInventoryTotal
            val containerDecrease = (previousCont[itemId] ?: 0) - (currentContainer[itemId] ?: 0)
            val movedBetweenInventoryAndContainer = minOf(inventoryIncrease, maxOf(containerDecrease, 0))
            val pickupAmount = inventoryIncrease - movedBetweenInventoryAndContainer
            if (pickupAmount <= 0) continue
            val previousCount = currentInventoryTotal - pickupAmount

            onItemAddedToInventory(itemId, previousCount, currentInventoryTotal)
        }

        previousInventory = currentInventory.toMutableMap()
        previousContainer = currentContainer.toMutableMap()
    }

    private fun resetInventoryState() {
        previousInventory = null
        previousContainer = null
        isInventoryLoaded = false
        lastFingerprint = null
        fingerprintStableSince = null
    }

    // Track if inventory is fully loaded after world change
    private fun updateInventoryLoadedState() {
     
        fun getInventoryFingerprint(): String {
            val player = FeeshMod.mc.player ?: return ""
            return (0..35).joinToString(";") { i ->
                if (i == 8) return@joinToString "" // Skyblock Menu / Bait Bag preview
                val stack = player.inventory.getItem(i)
                if (stack.isEmpty) return@joinToString ""
                val name = stack.hoverName.getUnformattedString()
                "${name}|${stack.count}"
            }
        }

        val fingerprint = getInventoryFingerprint()
        val now = Date().time
        if (fingerprint != lastFingerprint) {
            lastFingerprint = fingerprint
            fingerprintStableSince = now
            return
        }

        val stableSince = fingerprintStableSince ?: now.also { fingerprintStableSince = it }
        if (now - stableSince < INVENTORY_STABLE_MS) return

        isInventoryLoaded = true
        ChatUtils.sendLocalChat("Inventory loaded") // TODO: Remove this
    }

    private fun getFishingProfitItemsInCurrentInventory(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        val player = FeeshMod.mc.player ?: return result

        for (i in 0..35) {
            if (i == 8) continue // Bait Bag preview slot, to avoid counting bait as a fishing profit item
            addFishingProfitStack(result, player.inventory.getItem(i))
        }

        getItemOnCursor()?.let { addFishingProfitStack(result, it) } // Item on cursor still belongs to the inventory while moving
        return result
    }

    private fun getFishingProfitItemsInOpenContainer(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        val player = FeeshMod.mc.player ?: return result
        val playerInventory = player.inventory

        for (slot in player.containerMenu.slots) {
            if (slot.container === playerInventory) continue
            addFishingProfitStack(result, slot.item)
        }
        return result
    }

    private fun addFishingProfitStack(result: MutableMap<String, Int>, stack: ItemStack) {
        val slotItemName = getFishingProfitItemNameFromStack(stack) ?: return
        val dropInfo = FishingProfitDrops.getFishingProfitItemByName(slotItemName) ?: return
        result[dropInfo.itemId] = (result[dropInfo.itemId] ?: 0) + stack.count
    }

    private fun getFishingProfitItemNameFromStack(stack: ItemStack): String? {
        if (stack.isEmpty) return null

        var slotItemName = ItemUtils.getCleanItemName(stack.hoverName.getFormattedString())
        if (slotItemName.isBlank()) return null

        if (slotItemName == "Enchanted Book") {
            val bookName = ItemUtils.getEnchantedBookName(stack) ?: ""
            if (bookName.isNotBlank()) {
                slotItemName += " ($bookName)"
            }
        } else if (slotItemName.endsWith("Exp Boost")) {
            val loreLines = ItemUtils.getUnformattedLoreLines(stack)
            val petItemLine = loreLines.find { it.endsWith("PET ITEM") }
            if (petItemLine != null) {
                val description = petItemLine.split(" ").firstOrNull() ?: ""
                slotItemName += " ($description)"
            }
        } else if (isPet(slotItemName)) {
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

        return slotItemName
    }

    private fun onItemAddedToInventory(itemId: String, previousCount: Int, newCount: Int) {
        val dropInfo = FishingProfitDrops.items.find { it.itemId == itemId } ?: return
        if (dropInfo.ignoreFromInventory) return
        
        val difference = newCount - previousCount
        if (difference <= 0) return

        if (shouldSkipItem(itemId, dropInfo)) return

        ChatUtils.sendLocalChat("&cAdded: ${itemId} x${difference}") // TODO: Remove this

        EventBus.publish(
            InventoryProfitItemPickupEvent(
                itemId = itemId,
                itemNameUnformatted = dropInfo.itemName,
                previousCount = previousCount,
                newCount = newCount,
                amount = difference
            )
        )
    }

    private fun shouldSkipItem(itemId: String, dropInfo: FishingProfitDropInfo): Boolean {

        fun wasPotentiallyClaimedFromBazaar(fishingProfitItemName: String): Boolean {
            val now = Date()

            if (GuiUtils.isInBazaarGui() || (
                GuiUtils.lastGuisClosed.lastBazaarGuiClosedAt != null && Date().time - GuiUtils.lastGuisClosed.lastBazaarGuiClosedAt!!.time < 1_000)
            ) return true

            val bazaarMessage = lastBazaarMessage ?: return false
            if (lastBazaarItemAt != null && now.time - lastBazaarItemAt!!.time < 1_000) return false

            if (bazaarMessage.contains(fishingProfitItemName, ignoreCase = true)) return true
            if (dropInfo.itemAlternateNames.any { bazaarMessage.contains(it, ignoreCase = true) }) return true

            if (fishingProfitItemName.startsWith("Enchanted Book")) {
                val bookName = Regex("^Enchanted Book \\((.+)\\)$").matchEntire(fishingProfitItemName)?.groupValues[1]
                if (bookName != null && bazaarMessage.contains(bookName, ignoreCase = true)) return true    
            }

            return false
        }

        fun wasPotentiallyBoughtBackFromNpcShop(fishingProfitItemName: String): Boolean {
            val now = Date()
            val boughtBackMessage = lastBoughtBackMessage ?: return false
            if (lastBoughtBackAt == null || now.time - lastBoughtBackAt!!.time >= 1_000) return false

            if (boughtBackMessage.contains(fishingProfitItemName, ignoreCase = true)) return true
            if (dropInfo.itemAlternateNames.any { boughtBackMessage.contains(it, ignoreCase = true) }) return true
            return false
        }

        fun wasPotentiallySupercrafted(fishingProfitItemName: String): Boolean {
            val now = Date()
            val supercraftedMessage = lastSupercraftedMessage ?: return false
            if (lastSupercraftedAt == null || now.time - lastSupercraftedAt!!.time >= 1_000) return false

            if (supercraftedMessage.contains(fishingProfitItemName, ignoreCase = true)) return true
            if (dropInfo.itemAlternateNames.any { supercraftedMessage.contains(it, ignoreCase = true) }) return true
            return false
        }

        fun wasPotentiallyBoughtFromNpcShop(fishingProfitItemName: String): Boolean {
            val now = Date()
            val boughtMessage = lastBoughtMessage ?: return false
            if (lastBoughtAt == null || now.time - lastBoughtAt!!.time >= 1_000) return false

            if (boughtMessage.contains(fishingProfitItemName, ignoreCase = true)) return true
            if (dropInfo.itemAlternateNames.any { boughtMessage.contains(it, ignoreCase = true) }) return true
            return false
        }

        // TODO: 1 second after menu closed for all below cases?

        fun wasPotentiallyClaimedFromPetMenu(chestName: String?, fishingProfitItemName: String): Boolean {
            if (chestName == null) return false
            if (chestName.endsWith("Pets") && isPet(fishingProfitItemName)) return true
            return false
        }

        fun wasPotentiallyClaimedFromHuntingBox(chestName: String?, fishingProfitItemName: String): Boolean {
            if (chestName == null) return false
            if (chestName.contains("Hunting Box", ignoreCase = true) && fishingProfitItemName.endsWith("Shard")) return true
            return false
        }

        fun wasPotentiallyClaimedFromFishingBag(chestName: String?, fishingProfitItemName: String): Boolean {
            if (chestName == null) return false
            if (chestName == "Fishing Bag" && (fishingProfitItemName.endsWith("Bait") || fishingProfitItemName.contains("Obfuscated"))) return true
            return false
        }

        fun wasPotentiallyClaimedFromAccessoryBag(chestName: String?, fishingProfitItemName: String): Boolean {
            if (chestName == null) return false
            // TODO check if is accessory (Fish Affinity Talisman etc)
            if (chestName.startsWith("Accessory Bag")) return true
            return false
        }

        fun wasPotentiallyClaimedFromTimePocket(chestName: String?, fishingProfitItemName: String): Boolean {
            if (chestName == null) return false
            if (chestName == "Time Pocket" && fishingProfitItemName.startsWith("Moby-Duck")) return true
            return false
        }

        fun wasPotentiallyFilletedTrophy(chestName: String?, fishingProfitItemId: String): Boolean {
            if (chestName == null) return false
            if (chestName.contains("Trophy Fish", ignoreCase = true) && fishingProfitItemId.startsWith("MAGMA_FISH")) return true
            if (chestName.contains("Trophy Frogs", ignoreCase = true) && fishingProfitItemId.startsWith("LOTUS")) return true
            return false
        }

        fun wasPotentiallyClaimedFromSacks(chestName: String?): Boolean {
            if (chestName == null) return false
            if (chestName.endsWith("Sack") || chestName == "Sack of Sacks") return true
            return false
        }

        fun wasPotentiallyMovedFromSacksViaGfs(fishingProfitItemName: String): Boolean {
            val now = Date()
            if (lastGfsAt == null || now.time - lastGfsAt!!.time >= 1_000) return false
            val gfsItemName = lastGfsItemName ?: return false
            return fishingProfitItemName == gfsItemName
        }

        fun wasPotentiallyClaimedFromPetItemSwap(categories: List<String>): Boolean {
            val now = Date()
            val closedAt = GuiUtils.lastGuisClosed.lastPetItemSwapGuiClosedAt ?: return false
            if (now.time - closedAt.time >= 1_000) return false
            return categories.contains(FishingProfitDrops.PET_ITEM_CATEGORY)
        }

        fun wasPotentiallyClaimedFromAuction(): Boolean {
            val now = Date()
            val closedAt = GuiUtils.lastGuisClosed.lastAuctionGuiClosedAt ?: return false
            return now.time - closedAt.time < 3_000
        }

        fun wasPotentiallyClaimedFromKat(fishingProfitItemName: String): Boolean {
            if (!isPet(fishingProfitItemName)) return false

            val now = Date()
            if (lastKatPetClaimedAt == null || now.time - lastKatPetClaimedAt!!.time >= 7_000) return false // It takes some time for pet to appear in the inventory after claiming from Kat
            val katPetName = lastKatPetName ?: return false
            return fishingProfitItemName.contains(katPetName)
        }

        val chestName = GuiUtils.getCurrentChestName()

        if (wasPotentiallyClaimedFromBazaar(dropInfo.itemName)) return true
        if (wasPotentiallyClaimedFromPetMenu(chestName, dropInfo.itemName)) return true
        if (wasPotentiallyClaimedFromHuntingBox(chestName, dropInfo.itemName)) return true
        if (wasPotentiallyClaimedFromFishingBag(chestName, dropInfo.itemName)) return true
        if (wasPotentiallyClaimedFromAccessoryBag(chestName, dropInfo.itemName)) return true
        if (wasPotentiallyClaimedFromTimePocket(chestName, dropInfo.itemName)) return true
        if (wasPotentiallyFilletedTrophy(chestName, dropInfo.itemId)) return true
        if (wasPotentiallyBoughtBackFromNpcShop(dropInfo.itemName)) return true
        if (wasPotentiallyClaimedFromSacks(chestName)) return true
        if (wasPotentiallyMovedFromSacksViaGfs(dropInfo.itemName)) return true
        if (wasPotentiallySupercrafted(dropInfo.itemName)) return true
        if (wasPotentiallyBoughtFromNpcShop(dropInfo.itemName)) return true
        if (wasPotentiallyClaimedFromPetItemSwap(dropInfo.categories)) return true
        if (wasPotentiallyClaimedFromAuction()) return true
        if (wasPotentiallyClaimedFromKat(dropInfo.itemName)) return true

        return false
    }

    private fun isPet(itemName: String): Boolean {
        return itemName.startsWith("[Lvl 1] ")
    }

    private fun getItemOnCursor(): ItemStack? {
        val player = FeeshMod.mc.player ?: return null
        val cursor = player.containerMenu.carried
        if (cursor.isEmpty) return null
        return cursor
    }
}
