package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import net.minecraft.item.ItemStack
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.EquipmentSlot
import java.util.Date

object NonFishingArmorAlert {
    private var lastHookDetectedAt: Date? = null
    private var tickCounter = 0
    private const val TICKS_PER_CHECK = 10

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
        try {
            if (!Alerts.alertOnNonFishingArmor || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return
            if (isPlayerWearingFishingArmor()) return

            if (lastHookDetectedAt != null) {
                val now = Date()
                val diffMillis = now.time - lastHookDetectedAt!!.time
                if (diffMillis < 10_000) return
            }

            val player = FeeshMod.mc.player ?: return
            val isHookActive = EntityUtils.isFishingHookActive(player)
            if (!isHookActive) return

            lastHookDetectedAt = Date()

            CommonUtils.showTitle("${RED}Equip fishing armor!")
            SoundUtils.playSound()
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("Failed to check fishing armor on fishing hook appeared", e)
        }
    }

    private fun isPlayerWearingFishingArmor(): Boolean {
        val player = FeeshMod.mc.player ?: return false
        
        val helmet = player.getEquippedStack(EquipmentSlot.HEAD)
        val chestplate = player.getEquippedStack(EquipmentSlot.CHEST)
        val leggings = player.getEquippedStack(EquipmentSlot.LEGS)
        val boots = player.getEquippedStack(EquipmentSlot.FEET) 
        val armorPieces = listOf(helmet, chestplate, leggings, boots)
        val fishingArmorCount = armorPieces.count { armorPiece -> isFishingArmor(armorPiece) }

        return fishingArmorCount >= 3 // Helmet may be replaced with something else
    }

    private fun isFishingArmor(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty) return false

        val itemName = item.name.string
        val loreLines = item.get(DataComponentTypes.LORE)?.lines?.map { it.string } ?: listOf()
        
        if (itemName.isEmpty() || loreLines.isEmpty()) return false

        if (itemName.contains("Hunter") || itemName.contains("Squid Hat")) return true

        return loreLines.any { loreLine ->
            loreLine.startsWith("Sea Creature Chance:") || loreLine.startsWith("Fishing Speed:")
        }
    }
}
