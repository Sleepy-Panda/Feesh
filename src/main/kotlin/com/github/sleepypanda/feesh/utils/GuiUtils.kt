package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.getScreenCompat
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.GuiClosedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import java.util.Date
import java.util.Timer
import kotlin.concurrent.timerTask

data class LastGuisClosed(
    var lastSacksGuiClosedAt: Date? = null,
    var lastOdgerGuiClosedAt: Date? = null,
    var lastTrophyFrogsGuiClosedAt: Date? = null,
    var lastAuctionGuiClosedAt: Date? = null,
    var lastSupercraftGuiClosedAt: Date? = null,
    var lastCraftGuiClosedAt: Date? = null,
    var lastStorageGuiClosedAt: Date? = null,
    var lastBazaarGuiClosedAt: Date? = null,
    var lastPetItemSwapGuiClosedAt: Date? = null,
    var lastHotmGuiClosedAt: Date? = null
)

object GuiUtils {
    private var cachedIsInInventoryOrChat: Boolean = false
    private var timer: Timer? = null

    val lastGuisClosed = LastGuisClosed()

    fun init() {
        startTimer()
        EventBus.subscribe(GuiClosedEvent::class, ::onGuiClosed)
    }

    private fun onGuiClosed(event: GuiClosedEvent) {
        val chestName = event.guiName.removeFormatting()
        if (chestName.isNullOrBlank()) return

        val now = Date()
        when {
            chestName.contains("Sack") -> lastGuisClosed.lastSacksGuiClosedAt = now
            chestName.contains("Trophy Fish") -> lastGuisClosed.lastOdgerGuiClosedAt = now
            chestName.contains("Trophy Frogs") -> lastGuisClosed.lastTrophyFrogsGuiClosedAt = now
            chestName.contains("Manage Auctions") || chestName.contains("Confirm Purchase") ||
            chestName.contains("BIN Auction View") || chestName.contains("Your Bids") ->
                lastGuisClosed.lastAuctionGuiClosedAt = now
            chestName.endsWith("Recipe") -> lastGuisClosed.lastSupercraftGuiClosedAt = now
            chestName.contains("Craft Item") -> lastGuisClosed.lastCraftGuiClosedAt = now
            chestName.contains("Backpack") || chestName.contains("Chest") || chestName.contains("Ender Chest") ->
                lastGuisClosed.lastStorageGuiClosedAt = now
            chestName.contains("Bazaar Orders") || chestName.contains("Order options") || chestName.contains("Instant Buy") || chestName.contains("➡") ->
                lastGuisClosed.lastBazaarGuiClosedAt = now
            chestName == "Swap Pet Item" || chestName == "Remove Pet Item" -> 
                lastGuisClosed.lastPetItemSwapGuiClosedAt = now
            chestName.contains("Heart of the Mountain") -> lastGuisClosed.lastHotmGuiClosedAt = now
        }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = Timer("Feesh-GuiUtils", true)

        val task = timerTask {
            CommonUtils.runWithCatching("Failed to update Gui utils cache") {
                updateCache()
            }
        }
        timer?.scheduleAtFixedRate(task, 0, 200)
    }

    private fun updateCache() {
        cachedIsInInventoryOrChat = readIsInInventoryOrChat()
    }

    private fun readIsInInventoryOrChat(): Boolean {
        if (!WorldUtils.isInSkyblock()) return false

        val screen = FeeshMod.mc.getScreenCompat() ?: return false
        return screen is InventoryScreen || screen is ChatScreen
    }

    /*
     * Check if the player is in an inventory or chat screen.
     * This is assumed to be called very often so it is periodically cached.
     * @returns {Boolean}
     */
    fun isInInventoryOrChat(): Boolean {
        return cachedIsInInventoryOrChat
    }

    fun isInChest(): Boolean {
        val screen = FeeshMod.mc.getScreenCompat() ?: return false
        return (screen is AbstractContainerScreen<*> && screen !is InventoryScreen)
    }

    fun getCurrentChestName(): String? {
        val screen = FeeshMod.mc.getScreenCompat() ?: return null
        if (screen !is AbstractContainerScreen<*>) return null
        return screen.title.getUnformattedString()
    }

    fun isInBazaarGui(): Boolean {
        val title = getCurrentChestName() ?: return false
        return (title.contains("Bazaar Orders") || title.contains("Order options") || title.contains("Instant Buy") || title.contains("➡"))
    }

    fun isInSacksGui(): Boolean {
        val title = getCurrentChestName() ?: return false
        return (title.endsWith("Sack"))
    }

    fun isInSupercraftGui(): Boolean {
        val title = getCurrentChestName() ?: return false
        return (title.endsWith("Recipe"))
    }
}
