package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.getScreenCompat
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GuiOpenedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.ItemUtils
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import java.util.Timer
import kotlin.concurrent.timerTask

object FishingBaitSackDisabledAlert {
    private var isAlerted = false
    private var tickCounter = 0

    // Use Baits From Sacks are now disabled!
    private val USE_BAITS_DISABLED_PATTERN = Regex("^Use Baits From (Bag|Sacks) (is|are) now disabled!$")
    // Use Baits From Sacks are now enabled!
    private val USE_BAITS_ENABLED_PATTERN = Regex("^Use Baits From (Bag|Sacks) (is|are) now enabled!$")
    private const val BAG_TITLE_CONTAINS = "Fishing Bag" // TODO: Remove after Bait Sack release
    private const val BAG_TOGGLE_SLOT_NUMBER = 49
    private const val USE_BAITS_FROM_BAG_ITEM_NAME = "Use Baits From Bag"

    private const val BAIT_SACK_TITLE_CONTAINS = "Bait Sack"
    private const val USE_BAITS_FROM_SACKS_ITEM_NAME = "Use Baits From Sacks"
    private const val SACK_TOGGLE_SLOT_NUMBER = 61

    private const val CLICK_TO_DISABLE_TEXT = "Click to disable!"
    private const val TICKS_PER_CHECK = 20

    fun init() {
        EventBus.subscribe(ChatCancellableEvent::class, ::onChat)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(GuiOpenedEvent::class, ::onGuiOpened)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        isAlerted = false
    }

    private fun onChat(event: ChatCancellableEvent) {
        if (!Alerts.alertOnFishingBagDisabled || !WorldUtils.isInSkyblock()) return

        if (USE_BAITS_DISABLED_PATTERN.matches(event.unformattedText)) {
            setFishingBaitSackState(false)
        } else if (USE_BAITS_ENABLED_PATTERN.matches(event.unformattedText)) {
            setFishingBaitSackState(true)
        }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!Alerts.alertOnFishingBagDisabled || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        
        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0
        
        alertOnFishingBaitUsageDisabled()
    }

    private fun alertOnFishingBaitUsageDisabled() {
        CommonUtils.runWithCatching("Failed to check fishing bait sack state") {
            if (isAlerted ||
                !Alerts.alertOnFishingBagDisabled ||
                PersistentDataManager.feeshData.isFishingBagEnabled != false || // false means disabled, null means unknown
                !WorldUtils.isInSkyblock() ||
                !PlayerUtils.hasFishingRodInHotbar() ||
                !WorldUtils.isInFishingWorld()
            ) return

            val currentScreen = FeeshMod.mc.getScreenCompat()
            if (currentScreen is AbstractContainerScreen<*>) {
                val title = currentScreen.title.getUnformattedString()

                // When player opens disabled fishing bag/sack, avoid receiving alert again while it's disabled
                if (title.contains(BAG_TITLE_CONTAINS)) return
                if (title.contains(BAIT_SACK_TITLE_CONTAINS)) return
            }

            val isHookActive = FishingHookUtils.isFishingHookSubmerged()
            if (!isHookActive) return

            CommonUtils.showTitle("${RED}Bait usage disabled!")
            SoundUtils.playSound()
            isAlerted = true 
            ChatUtils.sendLocalChatWithCommand("${WHITE}Using baits from Fishing Bag is disabled. Click to open Fishing Bag!", "fb", true)
        }
    }

    private fun onGuiOpened(event: GuiOpenedEvent) {
        val screen = event.screen
        if (screen !is AbstractContainerScreen<*> || !Alerts.alertOnFishingBagDisabled || !WorldUtils.isInSkyblock()) return
        
        onFishingBaitSackOpened(event)
    }

    private fun onFishingBaitSackOpened(event: GuiOpenedEvent) {
        // Schedule task to check after GUI is fully loaded (~2 ticks delay)
        Timer(true).schedule(timerTask {
            CommonUtils.runWithCatching("Failed to check fishing bait sack state on GUI opened") {
                val currentScreen = event.screen
                if (currentScreen !is AbstractContainerScreen<*>) return@timerTask

                val title = currentScreen.title.getUnformattedString()
                if (!title.contains(BAG_TITLE_CONTAINS) && !title.contains(BAIT_SACK_TITLE_CONTAINS)) return@timerTask // TODO: Cleanup after Bait Sack release

                if (title.contains(BAG_TITLE_CONTAINS)) {
                    val handler = currentScreen.menu
                    val item = handler.getSlot(BAG_TOGGLE_SLOT_NUMBER).item
                    
                    val itemName = item.hoverName.getUnformattedString()
                    if (itemName != USE_BAITS_FROM_BAG_ITEM_NAME) return@timerTask
    
                    val lore = ItemUtils.getUnformattedLoreLines(item)
                    val isEnabled = lore.any { line -> line.contains(CLICK_TO_DISABLE_TEXT) }
                    setFishingBaitSackState(isEnabled)
                } else if (title.contains(BAIT_SACK_TITLE_CONTAINS)) {
                    val handler = currentScreen.menu
                    val item = handler.getSlot(SACK_TOGGLE_SLOT_NUMBER).item
                    
                    val itemName = item.hoverName.getUnformattedString()
                    if (itemName != USE_BAITS_FROM_SACKS_ITEM_NAME) return@timerTask
    
                    val lore = ItemUtils.getUnformattedLoreLines(item)
                    val isEnabled = lore.any { line -> line.contains(CLICK_TO_DISABLE_TEXT) }
                    setFishingBaitSackState(isEnabled)
                }
            }
        }, 100)        
    }

    private fun setFishingBaitSackState(isEnabled: Boolean) {
        PersistentDataManager.feeshData.isFishingBagEnabled = isEnabled
        PersistentDataManager.saveFeeshDataToFileAsync()

        if (PersistentDataManager.feeshData.isFishingBagEnabled == false) {
            isAlerted = false
        }
    }
}
