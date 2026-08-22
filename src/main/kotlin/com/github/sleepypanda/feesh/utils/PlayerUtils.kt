package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import net.minecraft.world.entity.EquipmentSlot

data class FishingRodInHandCache(
    val itemNameUnformatted: String? = null,
    val itemNameFormatted: String? = null,
)

data class EquippedArmorPiece(
    val itemId: String? = null,
    val itemName: String? = null,
    val loreLines: List<String> = emptyList(),
)

object PlayerUtils {
    private var cachedHasFishingRodInHotbar: Boolean = false
    private var fishingRodInHandCache = null as FishingRodInHandCache?
    private var cachedHasDirtRodInHand: Boolean = false

    private var cachedEquippedArmorPieces: Array<EquippedArmorPiece>? = null
    private var cachedIsInTrophyArmor: Boolean = false
    private var cachedIsInFrozenBlaze: Boolean = false

    private var tickCounter = 0

    private const val TICKS_PER_UPDATE = 5

    private val TROPHY_ARMOR_ID_PREFIXES = listOf(
        "FROGGLES", "RED_SWEATER",
        "BRONZE_HUNTER_", "SILVER_HUNTER_", "GOLD_HUNTER_", "DIAMOND_HUNTER_",
    )

    private val FROZEN_BLAZE_ARMOR_IDS = listOf(
        "FROZEN_BLAZE_HELMET", "FROZEN_BLAZE_CHESTPLATE", "FROZEN_BLAZE_LEGGINGS", "FROZEN_BLAZE_BOOTS",
    )

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        CommonUtils.runWithCatching("Failed to update player utils cache") {
            setHasFishingRodInHotbar()
            setFishingRodInHand()
            cachedEquippedArmorPieces = readEquippedArmorPieces()
            setIsInTrophyArmor(cachedEquippedArmorPieces)
            setIsInFrozenBlaze(cachedEquippedArmorPieces)
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        cachedHasFishingRodInHotbar = false
        fishingRodInHandCache = null
        cachedHasDirtRodInHand = false
        cachedEquippedArmorPieces = null
        cachedIsInTrophyArmor = false
        cachedIsInFrozenBlaze = false
        tickCounter = 0
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

    /** Get the player's equipped armor pieces. */
    fun getEquippedArmorPieces(): Array<EquippedArmorPiece>? {
        return cachedEquippedArmorPieces
    }

    /** Whether the player is wearing the full armor set for trophy fishing / trophy frogging. */
    fun isInTrophyArmor(): Boolean {
        return cachedIsInTrophyArmor
    }

    /** Whether the player is wearing a full Frozen Blaze armor set. */
    fun isInFrozenBlaze(): Boolean {
        return cachedIsInFrozenBlaze
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

    private fun readEquippedArmorPieces(): Array<EquippedArmorPiece>? {
        val player = FeeshMod.mc.player ?: return null
        val armorSlots = arrayOf(
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
        )

        return armorSlots.map { slot ->
            val armorPiece = player.getItemBySlot(slot)
            if (armorPiece == null || armorPiece.isEmpty) {
                EquippedArmorPiece()
            } else {
                EquippedArmorPiece(
                    itemId = ItemUtils.getCustomData(armorPiece)?.let { ItemUtils.getCustomDataId(it) },
                    itemName = armorPiece.hoverName?.getUnformattedString(),
                    loreLines = ItemUtils.getUnformattedLoreLines(armorPiece),
                )
            }
        }.toTypedArray()
    }

    private fun setIsInTrophyArmor(armorPieces: Array<EquippedArmorPiece>?) {
        cachedIsInTrophyArmor = armorPieces?.all { armorPiece ->
            val itemId = armorPiece.itemId
            if (itemId != null && TROPHY_ARMOR_ID_PREFIXES.any { itemId.startsWith(it) }) return@all true

            val itemName = armorPiece.itemName ?: return@all false
            return@all itemName.contains("Hunter", ignoreCase = true) ||
                itemName.contains("Froggles", ignoreCase = true) ||
                itemName.contains("Red Sweater", ignoreCase = true)
        } ?: false
    }

    private fun setIsInFrozenBlaze(armorPieces: Array<EquippedArmorPiece>?) {
        cachedIsInFrozenBlaze = armorPieces?.all { armorPiece ->
            val itemId = armorPiece.itemId
            if (itemId != null && FROZEN_BLAZE_ARMOR_IDS.any { itemId == it }) return@all true

            val itemName = armorPiece.itemName ?: return@all false
            return@all itemName.contains("Frozen Blaze Helmet", ignoreCase = true) ||
                itemName.contains("Frozen Blaze Chestplate", ignoreCase = true) ||
                itemName.contains("Frozen Blaze Leggings", ignoreCase = true) ||
                itemName.contains("Frozen Blaze Boots", ignoreCase = true)
        } ?: false
    }
}