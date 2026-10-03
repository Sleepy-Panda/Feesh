package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.settings.models.RareSeaCreatureTypesAllChat
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import net.minecraft.world.entity.decoration.ArmorStand

object RareCatchAllChatMessage {
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
        //ChatUtils.sendAllChat(message) // TODO: Uncomment this when we are ready to send the message to all chat
        ChatUtils.sendLocalChat(message)
    }

    private fun getAllChatMessage(seaCreatureName: String, isDoubleHooked: Boolean): String {
        val player = FeeshMod.mc.player ?: return ""
        val isGiantIsopod = seaCreatureName.equals(SeaCreatureNames.GIANT_ISOPOD, ignoreCase = true)
        val (x, y, z) = when {
            isGiantIsopod -> findGiantIsopodPosition() // It spawns not on player but at the top of Torrhus Springs
            else -> Triple(player.getX(), player.getY(), player.getZ())
        }
        val formattedCoordinates = CommonUtils.getFormattedLocation(x, y, z)
        val scMessage = if (isDoubleHooked) "${seaCreatureName} x2" else "${seaCreatureName}"
        val zone = when {
            isGiantIsopod -> WorldUtils.TORRHUS_SPRINGS
            WorldUtils.getWorldName() == WorldUtils.BACKWATER_BAYOU -> null // Bayou has single zone so no need to show it
            else -> WorldUtils.getZoneName()
        }
        val zoneText = if (!zone.isNullOrEmpty()) " at $zone" else ""
        val messageId = CommonUtils.getMessageId()

        var message = "${formattedCoordinates} | ${scMessage}${zoneText} | ${messageId}"
        return message
    }

    private fun findGiantIsopodPosition(): Triple<Double, Double, Double> {
        val fallback = Triple(-642.0, 157.0, 183.0) // Top of Torrhus Springs
        val world = FeeshMod.mc.level ?: return fallback

        val closest = world.entitiesForRendering()
            .filterIsInstance<ArmorStand>()
            .filter { it.tickCount <= 20 }
            .mapNotNull { stand ->
                val info = EntityUtils.parseSeaCreatureNametag(stand, listOf(SeaCreatureNames.GIANT_ISOPOD)) ?: return@mapNotNull null
                if (info.baseMobName != SeaCreatureNames.GIANT_ISOPOD) return@mapNotNull null
                stand
            }
            .minByOrNull { it.tickCount } ?: return fallback

        return Triple(closest.x, closest.y, closest.z)
    }
}
