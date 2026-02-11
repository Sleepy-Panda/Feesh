package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import java.util.Timer
import kotlin.concurrent.timerTask
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.scoreboard.Team

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

    val WATER_FISHING_WORLDS = listOf(
        BACKWATER_BAYOU,
        SPIDERS_DEN,
        HUB,
        CRYSTAL_HOLLOWS,
        DWARVEN_MINES,
        JERRY_WORKSHOP,
        PARK,
        FARMING_ISLANDS,
        GALATEA
    )

    val HOTSPOT_WORLDS = listOf(
        BACKWATER_BAYOU,
        SPIDERS_DEN,
        HUB,
        CRIMSON_ISLE
    )

    val WATER_HOTSPOT_WORLDS = listOf(
        BACKWATER_BAYOU,
        SPIDERS_DEN,
        HUB,
    )

    private var cachedIsInSkyblock: Boolean = false
    private var cachedWorldName: String? = null
    private var cachedZoneName: String? = null
    private var timer: Timer? = null

    fun init() {
        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = Timer()

        val task = timerTask {
            try {
                updateCache()
            } catch (e: Exception) {
                FeeshMod.LOGGER.error("[Feesh] Failed to update world utils cache.", e)
            }
        }
        timer?.scheduleAtFixedRate(task, 0, 1000) // Every second
    }

    private fun updateCache() {
        cachedIsInSkyblock = readIsInSkyblock()
        
        cachedWorldName = if (cachedIsInSkyblock) {
            readWorldName()
        } else null
        
        cachedZoneName = if (cachedIsInSkyblock && !cachedWorldName.isNullOrEmpty()) {
            readZoneName()
        } else null
    }
    
    private fun readWorldName(): String? {
        val worldName = TabListUtils.getLineAfter("Area:")
        return if (worldName.isNotEmpty()) worldName else null
    }

    private fun readZoneName(): String? {
        val scoreboard = FeeshMod.mc.world?.scoreboard ?: return null
        val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return null
        
        val zoneLine = scoreboard.getScoreboardEntries(objective)
            .filter { entry -> entry?.owner != null && !entry.hidden() }
            .map { entry -> Team.decorateName(scoreboard.getScoreHolderTeam(entry.owner()), entry.name()).string.removeFormatting() }
            .find { line -> line.contains("⏣") || line.contains("ф") }
        if (zoneLine.isNullOrEmpty()) return null

        // ⏣ Abandoned🐍 Quarry -> Abandoned Quarry
        var zoneName = zoneLine.replace("⏣", "").replace("ф", "").replace(Regex("[^\\u0000-\\u007F]"), "").trim()
        
        // Some lava in Phlegblast area does not belong to Phlegblast Pool zone but needs to be counted
        val worldName = cachedWorldName
        if (worldName == CRIMSON_ISLE && zoneName == CRIMSON_ISLE) {
            val player = FeeshMod.mc.player ?: return zoneName
            val x = player.x
            val y = player.y
            val z = player.z
            
            if (isBetweenIncluding(x, -381.0, -370.0) && 
                isBetweenIncluding(y, 68.0, 72.0) && 
                isBetweenIncluding(z, -708.0, -697.0)) {
                zoneName = PLHLEGBLAST_POOL
            }
        }
        
        return zoneName
    }
    
    private fun isBetweenIncluding(value: Double, num1: Double, num2: Double): Boolean {
        return value >= num1 && value <= num2
    }

    private fun readIsInSkyblock(): Boolean {
        //val serverAddress = FeeshMod.mc.currentServerEntry?.address ?: return false
        //if (!serverAddress.contains("hypixel", ignoreCase = true)) return false
        // ^ Commented out for now, because people with reverse proxy have other server addresses
        
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
    * Check whether current world name supports water hotspot fishing features.
    * @returns {Boolean}
    */
    fun isInWaterHotspotFishingWorld(): Boolean {
        if (!isInSkyblock()) return false
        val worldName = getWorldName()
    	if (worldName.isNullOrEmpty()) return false
        return WATER_HOTSPOT_WORLDS.contains(getWorldName())
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
        return cachedZoneName
    }
}