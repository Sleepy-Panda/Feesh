package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.FishingProfitDrops
import com.github.sleepypanda.feesh.constants.FishingProfitDropInfo
import com.github.sleepypanda.feesh.events.EventBus
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

// Need to ignore special cases of drops being taken from special GUIs, where simple dragging is not the case:
// [Bazaar] Cancelled! Refunded 26x Titanoboa Shard from cancelling Sell Offer!
// [Bazaar] Cancelled! Refunded 1x Prosperity I from cancelling Sell Offer! - Book name
// [Bazaar] Bought 64x Raw Cod for 25,146 coins!
// [Bazaar] Claimed 640x Raw Cod worth 193,088 coins bought for 301.7 each!
// Has drop name in inventory title? For bazar guis

// (1/4) Pets
// 
// You claimed True Ice from [MVP+] Minecraft_Kides's auction! - Individual msg for each drop
// You bought back Lily Pad x64 for 640 Coins!
// You bought back Lucky Hoof x1 for 50,000 Coins!
// Fishing Bag - baits
// Accessory Bag (1/4)
// Time Pocket
// Sack of Sacks, ... Sack

// You Supercrafted Polished Pumpkin x7!
object InventoryItemPickupPublisher {
    private var previousInventory: MutableMap<String, Int>? = null
    private var previousContainer: MutableMap<String, Int>? = null

    private const val TICKS_INVENTORY_SCAN = 5
    private var tickCounter = 0

    private const val INVENTORY_STABLE_MS = 3_500L // After world change, items are loaded not instantly / partially, we want to skip "false" item pickups
    private var isInventoryLoaded = false
    private var lastFingerprint: String? = null
    private var fingerprintStableSince: Long? = null

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(GuiClosedEvent::class, ::onGuiClosed)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
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
        } else if (slotItemName.startsWith("[Lvl 1] ")) {
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

        ChatUtils.sendLocalChat("Added: ${itemId} x${difference}") // TODO: Remove this

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
        val now = Date()
        val lastGuisClosed = GuiUtils.lastGuisClosed

        if (itemId.startsWith("MAGMA_FISH") && lastGuisClosed.lastOdgerGuiClosedAt != null &&
            now.time - lastGuisClosed.lastOdgerGuiClosedAt!!.time < 1000) return true // User probably just filleted trophy fish

        if (itemId.startsWith("LOTUS") && lastGuisClosed.lastTrophyFrogsGuiClosedAt != null &&
            now.time - lastGuisClosed.lastTrophyFrogsGuiClosedAt!!.time < 1000) return true // User probably just exchanged trophy frogs

        if (dropInfo.categories.contains(FishingProfitDrops.PET_ITEM_CATEGORY) && lastGuisClosed.lastPetItemSwapGuiClosedAt != null && 
            now.time - lastGuisClosed.lastPetItemSwapGuiClosedAt!!.time < 1000) return true

        if (lastGuisClosed.lastAuctionGuiClosedAt != null && now.time - lastGuisClosed.lastAuctionGuiClosedAt!!.time < 3_000) return true

        val lastKatUpgrade = GuiUtils.lastKatUpgrade
        if (lastKatUpgrade.lastPetClaimedAt != null && now.time - lastKatUpgrade.lastPetClaimedAt!!.time < 7_000) { // It takes some time for pet to appear in the inventory after claiming from Kat
            val katPetName = lastKatUpgrade.petName?.removeFormatting() ?: return false
            if (dropInfo.itemName.contains(katPetName)) return true
        }

        val lastGfsCommand = GuiUtils.lastGfsCommand
        if (lastGfsCommand.executedAt != null && now.time - lastGfsCommand.executedAt!!.time < 1_000) {
            val gfsItemName = lastGfsCommand.itemName ?: return false
            if (dropInfo.itemName.removeFormatting().equals(gfsItemName, ignoreCase = true)) return true
        }

        return false
    }

    private fun getItemOnCursor(): ItemStack? {
        val player = FeeshMod.mc.player ?: return null
        val cursor = player.containerMenu.carried
        if (cursor.isEmpty) return null
        return cursor
    }
}
