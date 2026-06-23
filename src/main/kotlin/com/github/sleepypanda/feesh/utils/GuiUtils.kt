package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.getScreenCompat
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.GuiClosedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import java.util.Date
import java.util.Timer
import kotlin.concurrent.timerTask

data class LastKatUpgrade(
    var lastPetClaimedAt: Date? = null,
    var petName: String? = null
)

data class LastGfsCommand(
    var executedAt: Date? = null,
    var itemName: String? = null
)

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
    private val KAT_UPGRADE_PATTERN = Regex("^\\[NPC\\] Kat: I was able to upgrade your pet (.+) to .*")
    private val ABIPHONE_CALL_PATTERN = Regex("^\\[NPC\\] Kat: ✆ Hi! I've finished training your (.+)!.*")
    private val GFS_COMMAND_PATTERN = Regex("^Moved [\\d,]+ (.+) from your Sacks to your inventory\\.$")

    private var cachedIsInInventoryOrChat: Boolean = false
    private var timer: Timer? = null

    val lastGuisClosed = LastGuisClosed()
    val lastKatUpgrade = LastKatUpgrade()
    val lastGfsCommand = LastGfsCommand()

    fun init() {
        startTimer()
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(GuiClosedEvent::class, ::onGuiClosed)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock()) return

        // When talking to NPC.
        // [NPC] Kat: I was able to upgrade your pet Guardian to LEGENDARY.
        KAT_UPGRADE_PATTERN.find(event.unformattedText)?.run {
            lastKatUpgrade.lastPetClaimedAt = Date()
            lastKatUpgrade.petName = this.groupValues[1].removeFormatting()
            return@onChat
        }

        // Abiphone call.
        // [NPC] Kat: ✆ Hi! I've finished training your Guardian!
        ABIPHONE_CALL_PATTERN.find(event.unformattedText)?.run {
            lastKatUpgrade.lastPetClaimedAt = Date()
            lastKatUpgrade.petName = this.groupValues[1].removeFormatting()
            return@onChat
        }

        // Moved 3,900 Enchanted Sea Lumies from your Sacks to your inventory.        // Moved 3,900 Enchanted Sea Lumies from your Sacks to your inventory.
        GFS_COMMAND_PATTERN.find(event.unformattedText)?.run {
            lastGfsCommand.executedAt = Date()
            lastGfsCommand.itemName = this.groupValues[1].removeFormatting()
            return@onChat
        }
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
            chestName.contains("Bazaar Orders") || chestName.contains("Order options") || chestName.contains("Instant Buy") ->
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

    /*
     * Check if the player is a GUI which is non-storage (you can't take items from it).
     * This is used to check if to ignore inventory changes when in some GUI.
     * @returns {Boolean}
     */
    fun isInNonStorageGui(): Boolean {
        val guiName = getCurrentChestName() ?: return false
        return guiName.startsWith("Wardrobe") || 
            guiName.startsWith("Your Equipment") || 
            guiName.startsWith("Abiphone") || 
            guiName.startsWith("Chocolate") ||
            guiName.startsWith("Hoppity") || 
            guiName == "Slayer" || 
            guiName == "Accessory Bag Thaumaturgy" ||
            guiName == "Stats Tuning" ||
            guiName == "SkyBlock Menu" ||
            guiName == "Your Stats Breakdown" ||
            guiName == "Your Skills" ||
            guiName == "Calendar and Events"
    }

    fun isInSacksGui(): Boolean {
        val title = GuiUtils.getCurrentChestName() ?: return false
        return (title.endsWith("Sack"))
    }

    fun isInSupercraftGui(): Boolean {
        val title = GuiUtils.getCurrentChestName() ?: return false
        return (title.endsWith("Recipe"))
    }
}
