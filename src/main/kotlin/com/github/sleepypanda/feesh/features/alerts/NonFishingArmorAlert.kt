package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import net.minecraft.world.item.ItemStack
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.EquipmentSlot
import java.util.Date

object NonFishingArmorAlert {
    private var lastHookDetectedAt: Date? = null
    private var tickCounter = 0
    private const val TICKS_PER_CHECK = 10
    private val SPECIAL_ARMOR_ITEM_NAMES = listOf("Hunter", "Squid Hat", "Froggles", "Red Sweater")
    private val FISHING_STATS_LORE_LINES = listOf("Sea Creature Chance:", "Fishing Speed:", "Treasure Chance:", "Trophy Chance:")

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        lastHookDetectedAt = null
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!Alerts.alertOnNonFishingArmor || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return

        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        checkAndAlertOnNonFishingArmor()
    }

    private fun checkAndAlertOnNonFishingArmor() {
        CommonUtils.runWithCatching("Failed to check and alert on non-fishing armor") {
            if (!Alerts.alertOnNonFishingArmor || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return
            if (isPlayerWearingFishingArmor()) return

            if (lastHookDetectedAt != null) {
                val now = Date()
                val diffMillis = now.time - lastHookDetectedAt!!.time
                if (diffMillis < 10_000) return
            }

            val isHookActive = FishingHookUtils.isFishingHookSubmerged()
            if (!isHookActive) return

            lastHookDetectedAt = Date()

            CommonUtils.showTitle("${RED}Equip fishing armor!")
            SoundUtils.playSound()
        }
    }

    private fun isPlayerWearingFishingArmor(): Boolean {
        val player = FeeshMod.mc.player ?: return false
        
        val helmet = player.getItemBySlot(EquipmentSlot.HEAD)
        val chestplate = player.getItemBySlot(EquipmentSlot.CHEST)
        val leggings = player.getItemBySlot(EquipmentSlot.LEGS)
        val boots = player.getItemBySlot(EquipmentSlot.FEET)
        val armorPieces = listOf(helmet, chestplate, leggings, boots)
        val fishingArmorCount = armorPieces.count { armorPiece -> isFishingArmor(armorPiece) }

        return fishingArmorCount >= 3 // Helmet may be replaced with something else
    }

    private fun isFishingArmor(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty) return false

        val itemName = item.hoverName.string
        val loreLines = item.get(DataComponents.LORE)?.lines()?.map { it?.string?.removeFormatting() ?: "" } ?: listOf()
        
        if (itemName.isEmpty() || loreLines.isEmpty()) return false

        if (SPECIAL_ARMOR_ITEM_NAMES.any { itemName.contains(it, ignoreCase = true) }) return true

        return loreLines.any { loreLine ->
            FISHING_STATS_LORE_LINES.any { loreLine.startsWith(it) }
        }
    }
}
