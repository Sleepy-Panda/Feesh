package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import java.util.Timer
import kotlin.concurrent.timerTask
import net.minecraft.world.entity.EquipmentSlot

object PlayerUtils {
    private var cachedHasFishingRodInHotbar: Boolean = false
    private var cachedHasDirtRodInHand: Boolean = false
    private var cachedIsInTrophyArmor: Boolean = false
    private var timer: Timer? = null

    fun init() {
        startTimer()
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun startTimer() {
        timer?.cancel()
        timer = Timer()
        
        val task = timerTask {
            CommonUtils.runWithCatching("Failed to update player utils cache") {
                setHasFishingRodInHotbar()
                setHasDirtRodInHand()
                setIsInTrophyArmor()
            }
        }
        timer?.scheduleAtFixedRate(task, 0, 500)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        cachedHasFishingRodInHotbar = false
        cachedHasDirtRodInHand = false
        cachedIsInTrophyArmor = false
    }

    /*
     * Get the player's name without formatting and prefixes, e.g. MoonTheSadFisher.
     * @returns {String} The player's name.
     */
    fun getName() : String? {      
        val mc = FeeshMod.mc
        val nameText = mc.player?.name?.string?.removeFormatting() ?: return null
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

    private fun setHasDirtRodInHand() {
        val player = FeeshMod.mc.player ?: run {
            cachedHasDirtRodInHand = false
            return
        }
        val heldItem = player.mainHandItem
        cachedHasDirtRodInHand = ItemUtils.isDirtRod(heldItem)
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
            if (armorPiece.isEmpty) return@all false
            val itemName = armorPiece.hoverName.string
            return@all itemName.contains("Hunter", ignoreCase = true) || 
                itemName.contains("Froggles", ignoreCase = true) || 
                itemName.contains("Red Sweater", ignoreCase = true)
        }
    }
}