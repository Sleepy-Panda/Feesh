package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ItemUtils
import net.minecraft.world.item.ItemStack
import net.minecraft.world.entity.EquipmentSlot
import java.util.Date

object NonFishingArmorAlert {
    private var lastHookDetectedAt: Date? = null
    private var tickCounter = 0
    private const val TICKS_PER_CHECK = 10

    private val FISHING_ARMOR_SET_ID_PREFIXES = listOf(
        "MAGMA_LORD_", "THUNDER_", "SHARK_SCALE_", "SPONGE_", "ANGLER_", "ABYSSAL_", "DIVER_", "SALMON_", "BACKWATER_",
        "SLUG_BOOTS", "MOOGMA_LEGGINGS", "FLAMING_CHESTPLATE", "TAURUS_HELMET",
        "TIKI_MASK", "WATER_HYDRA_HEAD", "FROGGLES", "RED_SWEATER", "SQUID_HAT",
        "BRONZE_HUNTER_", "SILVER_HUNTER_", "GOLD_HUNTER_", "DIAMOND_HUNTER_",
    )
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

        val armorPieces = listOf(
            player.getItemBySlot(EquipmentSlot.HEAD),
            player.getItemBySlot(EquipmentSlot.CHEST),
            player.getItemBySlot(EquipmentSlot.LEGS),
            player.getItemBySlot(EquipmentSlot.FEET),
        )
        val fishingArmorCount = armorPieces.count { isFishingArmor(it) == true }

        return fishingArmorCount >= 3 // Helmet may be replaced with something else
    }

    private fun isFishingArmor(item: ItemStack?): Boolean? {
        if (item == null || item.isEmpty) return false

        val itemId = ItemUtils.getCustomData(item)?.let { ItemUtils.getCustomDataId(it) }
        if (itemId != null && FISHING_ARMOR_SET_ID_PREFIXES.any { itemId.startsWith(it) }) return true

        val loreLines = ItemUtils.getUnformattedLoreLines(item)
        if (loreLines.isEmpty()) return null // There was a bug on alpha 01.08.26 when Hypixel does not return any lore every few seconds, and "Leather Chestplate" as item name

        return loreLines.any { loreLine ->
            FISHING_STATS_LORE_LINES.any { loreLine.contains(it) }
        }
    }
}
