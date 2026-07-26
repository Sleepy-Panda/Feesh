package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.settings.categories.HotspotChatSource
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.HotspotUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.events.models.ArmorStandDespawnedEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.ClickEvent.RunCommand
import net.minecraft.network.chat.HoverEvent.ShowText
import net.minecraft.world.phys.Vec3
import java.util.UUID

object HotspotFoundMessage {
    private var lastClosestHotspot: HotspotUtils.HotspotData? = null
    private var lastFoundHotspotIds = mutableListOf<UUID>() // Last 2 found hotspots, to not alert again and again when moving between 2 close hotspots
    private var knownFarHotspotIds = mutableSetOf<UUID>() // All seen hotspot IDs for far-alert dedup
    private var tickCounter = 0
    private const val TICKS_PER_CHECK = 10
    private const val NEAREST_HOTSPOT_RANGE_FROM_PLAYER = 10.0
    private const val FAR_HOTSPOT_SCAN_RANGE = 128.0

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(ArmorStandDespawnedEvent::class, ::onHotspotDespawned)
    }

    fun shareNearestHotspotToParty() {
        sendMessageWithNearestHotspot(true)
    }

    fun shareNearestHotspotToAll() {
        sendMessageWithNearestHotspot(false)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        lastClosestHotspot = null
        lastFoundHotspotIds.clear()
        knownFarHotspotIds.clear()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!Chat.messageOnHotspotFound && !Chat.autoMessageOnHotspotFound && !Chat.messageOnFarHotspotFound) return
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return

        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        sendMessageOnHotspotFound()
        sendMessageOnFarHotspotFound()
    }

    private fun onHotspotDespawned(@Suppress("UNUSED_PARAMETER") event: ArmorStandDespawnedEvent) {
        if (!Chat.messageOnHotspotFound && !Chat.autoMessageOnHotspotFound && !Chat.messageOnFarHotspotFound) return
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return

        val hotspotId = event.armorStand.uuid
        knownFarHotspotIds.remove(hotspotId)

        if (!lastFoundHotspotIds.contains(hotspotId) && lastClosestHotspot?.entity?.uuid != hotspotId) return

        val player = FeeshMod.mc.player ?: return
        val distance = event.armorStand.distanceTo(player)
        if (distance > 30.0) return // Probably user just moved away so the nametag is not rendered anymore

        lastFoundHotspotIds.remove(hotspotId)
        if (lastClosestHotspot != null && lastClosestHotspot!!.entity.uuid == hotspotId) {
            lastClosestHotspot = null
        }
    }

    private fun sendMessageWithNearestHotspot(isParty: Boolean) {
        CommonUtils.runWithCatching("Failed to share nearby Hotspot") {
            if (!WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld()) return

            val player = FeeshMod.mc.player ?: return
            val closestHotspot = HotspotUtils.findClosestHotspotInRange(Vec3(player.x, player.y, player.z), NEAREST_HOTSPOT_RANGE_FROM_PLAYER)
            
            if (closestHotspot != null) {
                announceNearestHotspot(closestHotspot.x, closestHotspot.y, closestHotspot.z, closestHotspot.perk, isParty)
            } else {
                ChatUtils.sendLocalChat("${WHITE}No Hotspot found nearby, move closer to be in ${NEAREST_HOTSPOT_RANGE_FROM_PLAYER.toInt()} blocks range!", true)
            }
        }
    }

    private fun sendMessageOnHotspotFound() {
        CommonUtils.runWithCatching("Failed to send message on Hotspot found") {
            if (!Chat.messageOnHotspotFound && !Chat.autoMessageOnHotspotFound) return
            if (!WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return

            val player = FeeshMod.mc.player ?: return
            val closestHotspot = HotspotUtils.findClosestHotspotInRange(Vec3(player.x, player.y, player.z), NEAREST_HOTSPOT_RANGE_FROM_PLAYER) ?: return

            val closestHotspotId = closestHotspot.entity.uuid
            knownFarHotspotIds.add(closestHotspotId) // Avoid far-alert if we later move away from this hotspot

            if (lastFoundHotspotIds.contains(closestHotspotId)) return

            if (lastClosestHotspot == null || lastClosestHotspot!!.entity.uuid != closestHotspotId) {
                announceFoundHotspot(closestHotspot.x, closestHotspot.y, closestHotspot.z, closestHotspot.perk)

                lastFoundHotspotIds.add(0, closestHotspotId)
                if (lastFoundHotspotIds.size > 2) {
                    lastFoundHotspotIds.removeAt(lastFoundHotspotIds.size - 1)
                }
            }

            lastClosestHotspot = closestHotspot
        }
    }

    private fun sendMessageOnFarHotspotFound() {
        CommonUtils.runWithCatching("Failed to send message on far Hotspot found") {
            if (!Chat.messageOnFarHotspotFound) return
            if (!WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return

            val player = FeeshMod.mc.player ?: return
            val hotspots = HotspotUtils.findHotspotsInRange(player, FAR_HOTSPOT_SCAN_RANGE)

            for (hotspot in hotspots) {
                val hotspotId = hotspot.entity.uuid
                if (knownFarHotspotIds.contains(hotspotId)) continue
                knownFarHotspotIds.add(hotspotId)

                val distance = EntityUtils.getDistance(player, hotspot.entity)
                if (distance <= NEAREST_HOTSPOT_RANGE_FROM_PLAYER) continue

                announceFarHotspotFound(hotspot, distance)
            }
        }
    }

    private fun announceFarHotspotFound(hotspot: HotspotUtils.HotspotData, distance: Double) {
        val perkText = if (hotspot.perk != null) "${hotspot.perk} " else ""
        val location = CommonUtils.getFormattedLocation(hotspot.x, hotspot.y, hotspot.z)
        ChatUtils.sendLocalChat(
            "${WHITE}New ${perkText}${LIGHT_PURPLE}Hotspot${WHITE} found ${YELLOW}${distance.toInt()} ${WHITE}blocks away (${location}).",
            true
        )
        SoundUtils.playSound()
    }

    private fun announceFoundHotspot(x: Double, y: Double, z: Double, perk: String?) {
        if (Chat.messageOnHotspotFound) {
            val perkText = if (perk != null) "${perk} " else ""
            
            ChatUtils.sendLocalChat("${WHITE}You found ${perkText}${LIGHT_PURPLE}Hotspot${WHITE}.", true)
            
            val partyMessage = getMessage(x, y, z, perk, false)   
            val partyChatText = Component.literal("${WHITE}${BOLD}[Share to ${BLUE}${BOLD}PARTY ${WHITE}${BOLD}chat]")
                .setStyle(
                    Style.EMPTY
                        .withClickEvent(RunCommand("/pchat $partyMessage"))
                        .withHoverEvent(ShowText(Component.literal("Click to share to PARTY chat")))
                )
            
            val orText = Component.literal(" ${RESET}${GRAY}or ")
            
            val allMessage = getMessage(x, y, z, perk, true)
            val allChatText = Component.literal("${WHITE}${BOLD}[Share to ${YELLOW}${BOLD}ALL ${WHITE}${BOLD}chat]")
                .setStyle(
                    Style.EMPTY
                        .withClickEvent(RunCommand("/achat $allMessage"))
                        .withHoverEvent(ShowText(Component.literal("Click to share to ALL chat")))
                )
            
            val shareText = partyChatText.append(orText).append(allChatText)
            ChatUtils.sendLocalChat(shareText)
        }

        if (Chat.autoMessageOnHotspotFound) {
            val isParty = Chat.autoMessageOnHotspotFoundSource == HotspotChatSource.PARTY_CHAT
            announceNearestHotspot(x, y, z, perk, isParty)
        }

        SoundUtils.playSound()
    }

    private fun announceNearestHotspot(x: Double, y: Double, z: Double, perk: String?, isParty: Boolean) {
        val message = getMessage(x, y, z, perk, !isParty)
        if (isParty) {
            ChatUtils.sendPartyChat(message)
        } else {
            ChatUtils.sendAllChat(message)
        }
    }

    private fun getMessage(x: Double, y: Double, z: Double, perk: String?, needsMessageId: Boolean): String {
        val location = CommonUtils.getFormattedLocation(x, y, z)
        val zone = if (WorldUtils.getWorldName() == WorldUtils.BACKWATER_BAYOU) null else WorldUtils.getZoneName() // Bayou has single zone so no need to show it
        val messageId = if (needsMessageId) " | ${CommonUtils.getMessageId()}" else ""

        val perkText = if (perk != null) "${perk.removeFormatting()} " else ""
        val zoneText = if (zone != null) " at ${zone.removeFormatting()}" else ""
        
        return "${location} | ${perkText}Hotspot${zoneText}${messageId}"
    }
}

