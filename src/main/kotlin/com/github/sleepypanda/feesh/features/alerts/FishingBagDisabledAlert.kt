package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GuiOpenedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import java.util.Timer
import kotlin.concurrent.timerTask

object FishingBagDisabledAlert {
    private var isAlerted = false
    private var tickCounter = 0

    private const val TICKS_PER_CHECK = 20
    private val USE_BAITS_FROM_FISHING_BAG_DISABLED_PATTERN = Regex("^Use Baits From Bag is now disabled\\!$")
    private val USE_BAITS_FROM_FISHING_BAG_ENABLED_PATTERN = Regex("^Use Baits From Bag is now enabled\\!$")
    private const val FISHING_BAG_TITLE_CONTAINS = "Fishing Bag"
    private const val TOGGLE_SLOT_NUMBER = 47
    private const val USE_BAITS_FROM_BAG_ITEM_NAME = "Use Baits From Bag"
    private const val CLICK_TO_DISABLE_TEXT = "Click to disable!"

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(GuiOpenedEvent::class, ::onGuiOpened)
        
        RegisterUtils.chat(USE_BAITS_FROM_FISHING_BAG_DISABLED_PATTERN) { _, _ ->
            if (Alerts.alertOnFishingBagDisabled && WorldUtils.isInSkyblock()) {
                setFishingBagState(false)
            }
        }
        
        RegisterUtils.chat(USE_BAITS_FROM_FISHING_BAG_ENABLED_PATTERN) { _, _ ->
            if (Alerts.alertOnFishingBagDisabled && WorldUtils.isInSkyblock()) {
                setFishingBagState(true)
            }
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        isAlerted = false
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!Alerts.alertOnFishingBagDisabled || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        
        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0
        
        alertOnFishingBagDisabled()
    }

    private fun alertOnFishingBagDisabled() {
        CommonUtils.runWithCatching("Failed to check fishing bag state") {
            if (isAlerted ||
                !Alerts.alertOnFishingBagDisabled ||
                PersistentDataManager.feeshData.isFishingBagEnabled != false || // false means disabled, null means unknown
                !WorldUtils.isInSkyblock() ||
                !PlayerUtils.hasFishingRodInHotbar() ||
                !WorldUtils.isInFishingWorld()
            ) return

            val currentScreen = FeeshMod.mc.screen
            if (currentScreen is AbstractContainerScreen<*>) {
                val title = currentScreen.title.string

                // When player opens disabled fishing bag, avoid receiving alert again while it's disabled
                if (title.contains(FISHING_BAG_TITLE_CONTAINS)) return
            }

            val isHookActive = FishingHookUtils.isFishingHookActive()
            if (!isHookActive) return

            CommonUtils.showTitle("${RED}Enable fishing bag!")
            SoundUtils.playSound()
            isAlerted = true

            ChatUtils.sendLocalChatWithCommand("${WHITE}Using baits from Fishing Bag is disabled. Click to open Fishing Bag!", "fb", true)
        }
    }

    private fun onGuiOpened(event: GuiOpenedEvent) {
        val screen = event.screen
        if (screen !is AbstractContainerScreen<*> || !Alerts.alertOnFishingBagDisabled || !WorldUtils.isInSkyblock()) return
        
        onFishingBagOpened(event)
    }

    private fun onFishingBagOpened(@Suppress("UNUSED_PARAMETER") event: GuiOpenedEvent) {
        // Schedule task to check after GUI is fully loaded (~2 ticks delay)
        Timer().schedule(timerTask {
            CommonUtils.runWithCatching("Failed to check fishing bag state on GUI opened") {
                val currentScreen = event.screen
                if (currentScreen !is AbstractContainerScreen<*>) return@timerTask

                val title = currentScreen.title.string
                if (!title.contains(FISHING_BAG_TITLE_CONTAINS)) return@timerTask

                val handler = currentScreen.menu
                val item = handler.getSlot(TOGGLE_SLOT_NUMBER)?.item ?: return@timerTask
                
                val itemName = item.hoverName.string.removeFormatting()
                if (itemName != USE_BAITS_FROM_BAG_ITEM_NAME) return@timerTask

                val lore = item.get(DataComponents.LORE)?.lines()?.map { it.string } ?: return@timerTask
                val isEnabled = lore.any { line -> line.contains(CLICK_TO_DISABLE_TEXT) }
                setFishingBagState(isEnabled)
            }
        }, 100)        
    }

    private fun setFishingBagState(isEnabled: Boolean) {
        PersistentDataManager.feeshData.isFishingBagEnabled = isEnabled
        PersistentDataManager.saveFeeshDataToFileAsync()

        if (PersistentDataManager.feeshData.isFishingBagEnabled == false) {
            isAlerted = false
        }
    }
}
