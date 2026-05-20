package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerTeam

object WorldUtils {
    const val CRIMSON_ISLE = "Crimson Isle"
    const val HUB = "Hub"
    const val PRIVATE_ISLAND = "Private Island"
    const val CRYSTAL_HOLLOWS = "Crystal Hollows"
    const val DWARVEN_MINES = "Dwarven Mines"
    const val ABANDONED_QUARRY = "Abandoned Quarry"
    const val BACKWATER_BAYOU = "Backwater Bayou"
    const val JERRY_WORKSHOP = "Jerry's Workshop"
    const val SPIDERS_DEN = "Spider's Den"
    const val PARK = "The Park"
    const val FARMING_ISLANDS = "The Farming Islands"
    const val KUUDRA = "Kuudra"
    const val DUNGEONS = "Catacombs"
    const val GARDEN = "Garden"
    const val DUNGEON_HUB = "Dungeon Hub"
    const val THE_END = "The End"
    const val GLACITE_MINESHAFTS = "Glacite Mineshafts"
    const val RIFT = "Rift Dimension"
    const val GALATEA = "Galatea"
    const val LOTUS_ATOLL = "Lotus Atoll"

    // Zones
    const val PLHLEGBLAST_POOL = "Plhlegblast Pool"
    const val MURKWATER_DEPTHS = "Murkwater Depths"
    const val DRAGON_LAIR = "Dragon's Lair"

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
        GALATEA,
        LOTUS_ATOLL,
    )

    val HOTSPOT_WORLDS = listOf(
        BACKWATER_BAYOU,
        SPIDERS_DEN,
        HUB,
        JERRY_WORKSHOP,
        LOTUS_ATOLL,
        PARK,
        CRIMSON_ISLE
    )

    val WATER_HOTSPOT_WORLDS = listOf(
        BACKWATER_BAYOU,
        SPIDERS_DEN,
        HUB,
        JERRY_WORKSHOP,
        LOTUS_ATOLL,
        PARK,
    )

    private var cachedIsInSkyblock: Boolean = false
    private var cachedWorldName: String? = null
    private var cachedZoneName: String? = null

    private const val TICKS_PER_UPDATE = 20
    private var tickCounter = 0

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        updateCache()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        tickCounter = TICKS_PER_UPDATE
        cachedIsInSkyblock = false
        cachedWorldName = null
        cachedZoneName = null

        updateCache()
    }

    private fun updateCache() {
        CommonUtils.runWithCatching("Failed to update world utils cache") {
            cachedIsInSkyblock = readIsInSkyblock()

            cachedWorldName = if (cachedIsInSkyblock) {
                readWorldName()
            } else null

            cachedZoneName = if (cachedIsInSkyblock && !cachedWorldName.isNullOrEmpty()) {
                readZoneName()
            } else null        
        }
    }
    
    private fun readWorldName(): String? {
        val worldName = TabListUtils.getLineAfter("Area:")
        return worldName.ifEmpty { null }
    }

    private fun readZoneName(): String? {
        val scoreboard = FeeshMod.mc.level?.scoreboard ?: return null
        val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return null

        val zoneLine = scoreboard.listPlayerScores(objective)
            .filter { entry -> !entry.isHidden }
            .map { entry ->
                val team = scoreboard.getPlayersTeam(entry.owner)
                PlayerTeam.formatNameForTeam(team, entry.ownerName()).string.removeFormatting()
            }
            .find { line -> line.contains("⏣") || line.contains("ф") }
        if (zoneLine.isNullOrEmpty()) return null

        // ⏣ Abandoned🐍 Quarry -> Abandoned Quarry
        var zoneName = zoneLine
            .replace("⏣", "")
            .replace("ф", "")
            .replace(Regex("[^\\u0000-\\u007F]"), "")
            .trim()

        // Some lava in Phlegblast area does not belong to Plhlegblast Pool zone but needs to be counted
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

        val scoreboard = FeeshMod.mc.level?.scoreboard ?: return false
        val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return false
        val title = objective.displayName.string
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