package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.settings.models.RareSeaCreatureTypesAllChat
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent

object RareCatchAllChatMessage {
    private var lastSentCoords: Triple<Long, Long, Long>? = null

    private val randomMessageParts = listOf("spawned", "appeared", "summoned")
    private val randomZonePrefixes = listOf("at", "@")

    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!WorldUtils.isInSkyblock() || !Chat.shareRareSeaCreaturesAllChat) return

        val seaCreatureName = event.seaCreatureName

        val type = try {
            RareSeaCreatureTypesAllChat.valueOf(seaCreatureName.uppercase().replace(" ", "_"))
        } catch (_: IllegalArgumentException) {
            return
        }
        if (!Chat.shareRareSeaCreaturesTypesAllChat.contains(type)) return

        val isDoubleHook = event.isDoubleHook
        val message = getAllChatMessage(seaCreatureName, isDoubleHook)
        ChatUtils.sendAllChat(message)
    }

    private fun getAllChatMessage(seaCreatureName: String, isDoubleHooked: Boolean): String {
        val player = FeeshMod.mc.player ?: return ""
        val isGiantIsopod = seaCreatureName.equals(SeaCreatureNames.GIANT_ISOPOD, ignoreCase = true)
        val (x, y, z) = when {
            isGiantIsopod -> getCoords(-642.0, 157.0, 183.0) // Center of Springs lake
            else -> getCoords(player.getX(), player.getY(), player.getZ())
        }
        val xyzPosition = CommonUtils.getFormattedLocation(x.toDouble(), y.toDouble(), z.toDouble())
        val scMessage = if (isDoubleHooked) "$seaCreatureName x2" else seaCreatureName
        val zone = when {
            isGiantIsopod -> WorldUtils.TORRHUS_SPRINGS
            WorldUtils.getWorldName() == WorldUtils.BACKWATER_BAYOU -> null // Bayou has single zone so no need to show it
            else -> WorldUtils.getZoneName()
        }
        val messageId = CommonUtils.getMessageId()

        return "$xyzPosition | ${getRandomizedSeaCreatureText(scMessage, zone)} | $messageId"
    }

    // Slightly randomize the coords if they are the same as the last sent, to avoid Hypixel rejecting duplicated messages
    private fun getCoords(x: Double, y: Double, z: Double): Triple<Long, Long, Long> {
        val rounded = Triple(Math.round(x), Math.round(y), Math.round(z))
        val coords = if (lastSentCoords == rounded) getRandomizedCoords(rounded) else rounded
        lastSentCoords = coords
        return coords
    }

    private fun getRandomizedCoords(coords: Triple<Long, Long, Long>): Triple<Long, Long, Long> {
        val adjusted = Triple(
            coords.first + (-5..5).random(),
            coords.second + (-5..5).random(),
            coords.third + (-5..5).random(),
        )
        return adjusted
    }

    private fun getRandomizedSeaCreatureText(name: String, zone: String?): String {
        val text = "$name ${randomMessageParts.random()}"
        if (zone.isNullOrEmpty()) return text
        return "$text ${randomZonePrefixes.random()} $zone"
    }
}
