package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import java.util.Timer
import kotlin.concurrent.timerTask
import net.minecraft.world.entity.EquipmentSlot

data class FishingRodInHandCache(
    val itemNameUnformatted: String? = null,
    val itemNameFormatted: String? = null,
)

object PlayerUtils {
    private var cachedHasFishingRodInHotbar: Boolean = false
    private var fishingRodInHandCache = null as FishingRodInHandCache?
    private var cachedHasDirtRodInHand: Boolean = false
    private var cachedIsInTrophyArmor: Boolean = false
    private var timer: Timer? = null

    private val TROPHY_ARMOR_ID_PREFIXES = listOf(
        "FROGGLES", "RED_SWEATER",
        "BRONZE_HUNTER_", "SILVER_HUNTER_", "GOLD_HUNTER_", "DIAMOND_HUNTER_",
    )

    fun init() {
        startTimer()
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun startTimer() {
        timer?.cancel()
        timer = Timer("Feesh-PlayerUtils", true)
        
        val task = timerTask {
            CommonUtils.runWithCatching("Failed to update player utils cache") {
                setHasFishingRodInHotbar()
                setFishingRodInHand()
                setIsInTrophyArmor()
            }
        }
        timer?.scheduleAtFixedRate(task, 0, 250)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        cachedHasFishingRodInHotbar = false
        fishingRodInHandCache = null
        cachedHasDirtRodInHand = false
        cachedIsInTrophyArmor = false
    }

    /*
     * Get the player's name without formatting and prefixes, e.g. MoonTheSadFisher.
     * @returns {String} The player's name.
     */
    fun getUnformattedName() : String? {
        val mc = FeeshMod.mc
        val nameText = mc.player?.name?.getUnformattedString()
        if (nameText.isNullOrEmpty()) return null
        return nameText
    }

    /*
     * Get the player's formatted name as [Level] Nickname Emblem, e.g. §8[§d326§8] §bMoonTheSadFisher §7α§7
     * @returns {String} The player's formatted name.
     */
    fun getFormattedName() : String? {
        val mc = FeeshMod.mc
        val nameText = mc.player?.getCustomName() ?: mc.player?.displayName ?: return null
        val displayName = nameText.getFormattedString()
        return displayName
    }

    /*
     * Get the player's formatted name (e.g. §bMoonTheSadFisher) without level or other prefixes.
     * @returns {String} The player's formatted name.
     */
    fun getFormattedNameWithoutPrefix() : String? {    
        val nameText = getFormattedName() ?: return null
        val displayName = nameText.split("] ").lastOrNull()?.split(" ")?.firstOrNull() ?: return null
        return displayName
    }

    fun getFormattedPlayerNameFromPartyChat(playerAndRank: String) : String? { // §b[MVP§d+§b] DeadlyMetal§f: blah-blah-blah...
        if (playerAndRank.isNullOrEmpty()) return null
        val color = playerAndRank.substring(0, 2)
        val nameWithoutRank = playerAndRank.split("] ").last();
        return "${color}${nameWithoutRank}"
    }

    fun hasFishingRodInHotbar(): Boolean {
        return cachedHasFishingRodInHotbar
    }

    fun hasFishingRodInHandUncached(): Boolean {
        CommonUtils.runWithCatching("Failed to check if player is holding a fishing rod") {
            val player = FeeshMod.mc.player ?: return false
            val heldItem = player.mainHandItem ?: return false
            return ItemUtils.isFishingRod(heldItem)
        }
        return false
    }

    fun hasFishingRodInHand(): Boolean {
        return fishingRodInHandCache != null
    }

    fun getFishingRodInHand(): FishingRodInHandCache? {
        return fishingRodInHandCache
    }

    fun hasDirtRodInHand(): Boolean {
        return cachedHasDirtRodInHand
    }

    /** Whether the player is wearing the armor for trophy fishing / trophy frogging (Bronze/Silver/Gold/Diamond Hunter, Froggles, Red Sweater). */
    fun isInTrophyArmor(): Boolean {
        return cachedIsInTrophyArmor
    }

    private fun setHasFishingRodInHotbar() {
        val player = FeeshMod.mc.player ?: run {
            cachedHasFishingRodInHotbar = false
            return
        }
        for (i in 0..8) {
            val stack = player.inventory.getItem(i)
            if (ItemUtils.isFishingRod(stack)) {
                cachedHasFishingRodInHotbar = true
                return
            }
        }
        cachedHasFishingRodInHotbar = false
    }

    private fun setFishingRodInHand() {
        val player = FeeshMod.mc.player ?: run {
            fishingRodInHandCache = null
            cachedHasDirtRodInHand = false
            return
        }
        val heldItem = player.mainHandItem ?: run {
            fishingRodInHandCache = null
            cachedHasDirtRodInHand = false
            return
        }

        if (ItemUtils.isFishingRod(heldItem)) {
            fishingRodInHandCache = FishingRodInHandCache(
                itemNameUnformatted = heldItem.hoverName.getUnformattedString(),
                itemNameFormatted = heldItem.hoverName.getFormattedString(),
            )
            cachedHasDirtRodInHand = ItemUtils.isDirtRod(heldItem)
        } else {
            fishingRodInHandCache = null
            cachedHasDirtRodInHand = false
        }
    }

    private fun setIsInTrophyArmor() {
        val player = FeeshMod.mc.player ?: run {
            cachedIsInTrophyArmor = false
            return
        }
        
        val helmet = player.getItemBySlot(EquipmentSlot.HEAD)
        val chestplate = player.getItemBySlot(EquipmentSlot.CHEST)
        val leggings = player.getItemBySlot(EquipmentSlot.LEGS)
        val boots = player.getItemBySlot(EquipmentSlot.FEET)
        
        val armorPieces = listOf(helmet, chestplate, leggings, boots)

        cachedIsInTrophyArmor = armorPieces.all { armorPiece ->
            if (armorPiece == null || armorPiece.isEmpty) return@all false

            val itemId = ItemUtils.getCustomData(armorPiece)?.let { ItemUtils.getCustomDataId(it) }
            if (itemId != null && TROPHY_ARMOR_ID_PREFIXES.any { itemId.startsWith(it) }) return@all true

            val itemName = armorPiece.hoverName?.getUnformattedString() ?: return@all false
            return@all itemName.contains("Hunter", ignoreCase = true) || 
                itemName.contains("Froggles", ignoreCase = true) || 
                itemName.contains("Red Sweater", ignoreCase = true)
        }
    }
}