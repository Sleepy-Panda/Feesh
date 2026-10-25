package com.github.sleepypanda.feesh.features.alerts

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
import com.github.sleepypanda.feesh.utils.EquippedArmorPiece
import java.util.Date

object NonFishingArmorAlert {
    private var lastAlertAt: Date? = null
    private var lastArmorFingerprint: List<String>? = null
    private var hasAlertedForCurrentArmor = false
    private var tickCounter = 0
    private const val TICKS_PER_CHECK = 10
    private const val ALERT_COOLDOWN_MS = 5_000L

    private val FISHING_ARMOR_SET_ID_PREFIXES = listOf(
        "MAGMA_LORD_", "THUNDER_", "SHARK_SCALE_", "SPONGE_", "ANGLER_", "ABYSSAL_", "DIVER_", "SALMON_", "BACKWATER_",
        "SLUG_BOOTS", "MOOGMA_LEGGINGS", "FLAMING_CHESTPLATE", "TAURUS_HELMET",
        "TIKI_MASK", "WATER_HYDRA_HEAD", "FROGGLES", "RED_SWEATER", "SQUID_HAT",
        "BRONZE_HUNTER_", "SILVER_HUNTER_", "GOLD_HUNTER_", "DIAMOND_HUNTER_",
        "FROZEN_BLAZE_",
    )
    private val FISHING_STATS_LORE_LINES = listOf("Sea Creature Chance:", "Fishing Speed:", "Treasure Chance:", "Trophy Chance:") // Fallback if ID check does not return true

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        lastAlertAt = null
        lastArmorFingerprint = null
        hasAlertedForCurrentArmor = false
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

            val armorPieces = PlayerUtils.getEquippedArmorPieces() ?: return
            val fingerprint = armorPieces.map { it.itemId ?: it.itemName.orEmpty() }
            if (fingerprint != lastArmorFingerprint) {
                lastArmorFingerprint = fingerprint
                hasAlertedForCurrentArmor = false
            }

            if (!FishingHookUtils.isFishingHookSubmerged()) return
            if (hasAlertedForCurrentArmor) return
            if (lastAlertAt != null && Date().time - lastAlertAt!!.time < ALERT_COOLDOWN_MS) return
            if (isWearing3PiecesOfFishingArmor(armorPieces)) return

            lastAlertAt = Date()
            hasAlertedForCurrentArmor = true

            CommonUtils.showTitle("${RED}Equip fishing armor!")
            SoundUtils.playSound()
        }
    }

    private fun isWearing3PiecesOfFishingArmor(armorPieces: Array<EquippedArmorPiece>): Boolean {
        val fishingArmorCount = armorPieces.count { isFishingArmor(it) == true }
        return fishingArmorCount >= 3 // Helmet may be replaced with something else
    }

    private fun isFishingArmor(piece: EquippedArmorPiece): Boolean? {
        if (piece.itemId == null && piece.itemName == null) return false

        val itemId = piece.itemId
        if (itemId != null && FISHING_ARMOR_SET_ID_PREFIXES.any { itemId.startsWith(it) }) return true

        if (piece.loreLines.isEmpty()) return null // There was a bug on 01.08.26 when Hypixel does not return any lore every few seconds, and "Leather Chestplate" as item name

        return piece.loreLines.any { loreLine ->
            FISHING_STATS_LORE_LINES.any { loreLine.contains(it) }
        }
    }
}
