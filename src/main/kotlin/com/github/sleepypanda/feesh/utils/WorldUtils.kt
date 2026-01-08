package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import java.util.Timer
import kotlin.concurrent.timerTask
import net.minecraft.scoreboard.ScoreboardDisplaySlot

object WorldUtils {
    val CRIMSON_ISLE = "Crimson Isle"
    val HUB = "Hub"
    val PRIVATE_ISLAND = "Private Island"
    val CRYSTAL_HOLLOWS = "Crystal Hollows"
    val DWARVEN_MINES = "Dwarven Mines"
    val ABANDONED_QUARRY = "Abandoned Quarry"
    val BACKWATER_BAYOU = "Backwater Bayou"
    val JERRY_WORKSHOP = "Jerry's Workshop"
    val SPIDERS_DEN = "Spider's Den"
    val PARK = "The Park"
    val FARMING_ISLANDS = "The Farming Islands"
    val KUUDRA = "Kuudra"
    val DUNGEONS = "Catacombs"
    val GARDEN = "Garden"
    val DUNGEON_HUB = "Dungeon Hub"
    val THE_END = "The End"
    val GLACITE_MINESHAFTS = "Glacite Mineshafts"
    val RIFT = "Rift Dimension"
    val GALATEA = "Galatea"
    val PLHLEGBLAST_POOL = "Plhlegblast Pool"

    val NO_FISHING_WORLDS = listOf(
        RIFT,
        GARDEN,
        KUUDRA,
        DUNGEONS,
        DUNGEON_HUB,
        THE_END,
        GLACITE_MINESHAFTS
    )

    private val HOTSPOT_WORLDS = listOf(
        BACKWATER_BAYOU,
        SPIDERS_DEN,
        HUB,
        CRIMSON_ISLE
    )

    private var cachedIsInSkyblock: Boolean = false
    private var cachedWorldName: String? = null
    private var timer: Timer? = null

    fun init() {
        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = Timer()
        
        val task = timerTask {
            updateCache()
        }
        timer?.scheduleAtFixedRate(task, 0, 1000) // Every second
    }

    private fun updateCache() {
        cachedIsInSkyblock = checkIsInSkyblock()
        cachedWorldName = if (cachedIsInSkyblock) {
            val worldName = TabListUtils.getLineAfter("Area:")
            if (worldName.isNotEmpty()) worldName else null
        } else null
    }

    private fun checkIsInSkyblock(): Boolean {
        val serverAddress = FeeshMod.mc.currentServerEntry?.address ?: return false
        if (!serverAddress.contains("hypixel", ignoreCase = true)) return false
        
        val scoreboard = FeeshMod.mc.world?.scoreboard ?: return false
        val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return false
        val title = objective.displayName?.string ?: return false
        return title.contains("skyblock", ignoreCase = true)
    }

    fun isInSkyblock(): Boolean {
        return cachedIsInSkyblock
    }

    /**
    * Check whether specified world name supports fishing features.
    * @returns {Boolean}
    */
    fun isInFishingWorld(worldName: String?): Boolean {
        if (!isInSkyblock()) return false
    	if (worldName.isNullOrEmpty()) return false
    	return !NO_FISHING_WORLDS.contains(worldName)
    }

    /**
    * Check whether current world name supports fishing features.
    * @returns {Boolean}
    */
    fun isInFishingWorld(): Boolean {
        if (!isInSkyblock()) return false
        val worldName = getWorldName()
        if (worldName.isNullOrEmpty()) return false
    	return !NO_FISHING_WORLDS.contains(worldName)
    }

    /**
    * Check whether current world name supports hotspot fishing features.
    * @returns {Boolean}
    */
    fun isInHotspotFishingWorld(): Boolean {
        if (!isInSkyblock()) return false
        val worldName = getWorldName()
    	if (worldName.isNullOrEmpty()) return false
        return HOTSPOT_WORLDS.contains(getWorldName())
    }

    /**
     * Get the current Skyblock world name or null if not found / outside of Skyblock.
     * @returns {String?}
     */
    fun getWorldName(): String? {
        return cachedWorldName
    }

    /**
    * Get the current Skyblock zone name or null if not found / outside of Skyblock.
    * @returns {String?}
    */
    fun getZoneName(): String? {
        if (!isInSkyblock()) return null
        return null
    }
}