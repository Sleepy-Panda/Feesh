package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerTeam

object TabListAndScoreboardUtils {
    
    fun getUnformattedLines(): List<String> {
        val playerList = getPlayerList()

        return playerList.map { entry ->
            val displayName = entry.tabListDisplayName ?: entry.profile.name.let { Component.literal(it) }
            return@map displayName.getUnformattedString()
        }
    }

    /*
     * Get the TabList line after the specified startsWith string. No formatting preserved.
     * E.g. getLineAfter("Area:") will return the trimmed text after the "Area:" string.
     * @param startsWith The unformatted string to search for.
     * @returns {String} The line after the specified startsWith string.
     */
    fun getLineAfter(startsWith: String): String {
        val playerList = getPlayerList()
        if (playerList.isEmpty()) return ""
        
        for (entry in playerList) {
            val displayName = entry.tabListDisplayName ?: entry.profile.name.let { Component.literal(it) }
            val text = displayName.getUnformattedString()
            
            if (text.contains(startsWith)) {
                val entryIndex = text.indexOf(startsWith)
                if (entryIndex != -1) {
                    val value = text.substring(entryIndex + startsWith.length).trim()
                    return value.ifEmpty { "" }
                }
            }
        }
        
        return ""
    }

    fun getUnformattedScoreboardLines(): List<String> {
        val scoreboard = FeeshMod.mc.level?.scoreboard ?: return emptyList()
        val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return emptyList()

        val lines = scoreboard.listPlayerScores(objective)
            .filter { entry -> !entry.isHidden }
            .map { entry ->
                val team = scoreboard.getPlayersTeam(entry.owner)
                PlayerTeam.formatNameForTeam(team, entry.ownerName()).getUnformattedString()
            }

        return lines
    }

    private fun getPlayerList(): Collection<PlayerInfo> {
        val networkHandler = FeeshMod.mc.connection ?: return emptyList()
        return networkHandler.listedOnlinePlayers
    }
}
