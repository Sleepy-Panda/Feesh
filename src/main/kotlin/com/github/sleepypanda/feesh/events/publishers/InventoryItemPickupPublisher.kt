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

object InventoryItemPickupPublisher {
    private var previousInventory: MutableMap<String, Int>? = null

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

        if (previousInventory == null) {
            previousInventory = getFishingProfitItemsInCurrentInventory().toMutableMap()
            return
        }

        //if (isPlayerMovingItem()) return

        val currentInventory = getFishingProfitItemsInCurrentInventory()

        // Allow being in some GUIs, because we are often in them when killing mobs and getting drops
        if (GuiUtils.isInChest() && !GuiUtils.isInNonStorageGui()) {
            previousInventory = currentInventory.toMutableMap()
            return
        }

        for ((itemId, currentTotal) in currentInventory) {
            val movingStack = getItemOnCursor()
            if (movingStack != null) {
                val movingItemName = getFishingProfitItemNameFromStack(movingStack)
                val dropInfo = FishingProfitDrops.getFishingProfitItemByName(movingItemName ?: "")
                if (dropInfo != null && dropInfo.itemId == itemId) {
                    continue; // Skip if user is moving this item via mouse
                }
            }
            val previousTotal = previousInventory!![itemId] ?: 0
            if (currentTotal > previousTotal) {
                onItemAddedToInventory(itemId, previousTotal, currentTotal)
            }
        }

        previousInventory = currentInventory.toMutableMap()
    }

    private fun resetInventoryState() {
        previousInventory = null
        isInventoryLoaded = false
        lastFingerprint = null
        fingerprintStableSince = null
    }

    private fun updateInventoryLoadedState() {
     
        fun getInventoryFingerprint(): String {
            val player = FeeshMod.mc.player ?: return ""
            return (0..35).joinToString(";") { i ->
                if (i == 8) return@joinToString "" // Skyblock Manu / Bait Bag preview
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
        ChatUtils.sendLocalChat("Inventory loaded")
    }

    private fun getFishingProfitItemsInCurrentInventory(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        val player = FeeshMod.mc.player ?: return result

        for (i in 0..35) {
            if (i == 8) continue // Bottom-right slot in player inventory UI (hotbar rightmost slot) which contains Bait Bag preview
            val stack = player.inventory.getItem(i)
            val slotItemName = getFishingProfitItemNameFromStack(stack) ?: continue
            val dropInfo = FishingProfitDrops.getFishingProfitItemByName(slotItemName)
            if (dropInfo != null) {
                result[dropInfo.itemId] = (result[dropInfo.itemId] ?: 0) + stack.count
            }
        }
        return result
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
        val cursor = player.inventoryMenu.carried
        if (cursor.isEmpty) return null
        return cursor
    }
}
