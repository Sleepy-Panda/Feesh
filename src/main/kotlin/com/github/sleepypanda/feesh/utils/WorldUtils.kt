package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod

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

    fun isInSkyblock(): Boolean {
        //val title = ScoreBoard.getTitle().lowercase()
        val serverAddress = FeeshMod.mc.currentServerEntry?.address ?: return false
        return serverAddress.contains("hypixel", ignoreCase = true)
    }

    /**
    * Check whether specified world name supports fishing features.
    * @returns {Boolean}
    */
    fun isInFishingWorld(worldName: String?): Boolean {
    	if (worldName.isNullOrEmpty()) return false
    	return !NO_FISHING_WORLDS.contains(worldName)
    }

    /**
    * Check whether current world name supports fishing features.
    * @returns {Boolean}
    */
    fun isInFishingWorld(): Boolean {
        val worldName = getWorldName()
    	if (worldName.isNullOrEmpty()) return false
    	return !NO_FISHING_WORLDS.contains(worldName)
    }

    /**
    * Get the current world name or null if not found.
    * @returns {String?}
    */
    fun getWorldName(): String? {
        val worldName = TabListUtils.getLineAfter("Area:")
        return if (worldName.isNotEmpty()) worldName else null
    }

    fun getZoneName(): String? {
        return null
    }
}