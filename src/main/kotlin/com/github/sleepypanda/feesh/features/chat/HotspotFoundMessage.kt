package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.settings.categories.HotspotChatSource
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.HotspotUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.ClientTickEvent
import com.github.sleepypanda.feesh.events.WorldChangedEvent
import java.util.UUID
import kotlin.random.Random
import net.minecraft.text.Text
import net.minecraft.text.Style
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.ClickEvent.RunCommand
import net.minecraft.text.HoverEvent.ShowText
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import org.lwjgl.glfw.GLFW

object HotspotFoundMessage {
    private lateinit var shareNearestHotspotToPartyChatKeyBind: KeyBinding
    private lateinit var shareNearestHotspotToAllChatKeyBind: KeyBinding
    private val FEESH_CATEGORY = KeyBinding.Category(Identifier.of("feesh", "keybinds")) // Keys are localized in resources/assets/feesh/lang/en_us.json

    private var lastClosestHotspot: HotspotUtils.HotspotData? = null
    private var lastFoundHotspotIds = mutableListOf<UUID>()
    private var tickCounter = 0
    private const val TICKS_PER_CHECK = 20

    fun init() {
        registerKeybinds()

        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun registerKeybinds() {
        shareNearestHotspotToPartyChatKeyBind = KeyBinding(
            "key.feesh.shareHotspotPartyChat",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            FEESH_CATEGORY
        )
        KeyBindingHelper.registerKeyBinding(shareNearestHotspotToPartyChatKeyBind)

        shareNearestHotspotToAllChatKeyBind = KeyBinding(
            "key.feesh.shareHotspotAllChat",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            FEESH_CATEGORY
        )
        KeyBindingHelper.registerKeyBinding(shareNearestHotspotToAllChatKeyBind)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        lastClosestHotspot = null
        lastFoundHotspotIds.clear()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!Chat.messageOnHotspotFound && !Chat.autoMessageOnHotspotFound) return
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return

        if (shareNearestHotspotToPartyChatKeyBind.wasPressed()) {
            sendMessageWithNearestHotspot(true)
        }
        if (shareNearestHotspotToAllChatKeyBind.wasPressed()) {
            sendMessageWithNearestHotspot(false)
        }

        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        sendMessageOnHotspotFound()
    }

    private fun sendMessageWithNearestHotspot(isParty: Boolean) {
        try {
            if (!WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld()) return

            val player = FeeshMod.mc.player ?: return
            val closestHotspot = HotspotUtils.findClosestHotspotInRange(player)
            
            if (closestHotspot != null) {
                announceNearestHotspot(closestHotspot.x, closestHotspot.y, closestHotspot.z, closestHotspot.perk, isParty)
            } else {
                ChatUtils.sendLocalChat("${WHITE}No Hotspot found nearby, move closer to be in ${HotspotUtils.HOTSPOT_RANGE.toInt()} blocks range!", true)
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to share nearby Hotspot.", e)
        }
    }

    private fun sendMessageOnHotspotFound() {
        try {
            if ((!Chat.messageOnHotspotFound && !Chat.autoMessageOnHotspotFound) || !WorldUtils.isInSkyblock() || !WorldUtils.isInHotspotFishingWorld() || !PlayerUtils.hasFishingRodInHotbar()) return

            val player = FeeshMod.mc.player ?: return
            val closestHotspot = HotspotUtils.findClosestHotspotInRange(player)
            val closestHotspotId = closestHotspot?.entity?.uuid

            if (closestHotspot != null && 
                closestHotspotId != null &&
                !lastFoundHotspotIds.contains(closestHotspotId) && (
                    lastClosestHotspot == null ||
                    (lastClosestHotspot != null && !(
                        closestHotspot.x == lastClosestHotspot!!.x &&
                        closestHotspot.y == lastClosestHotspot!!.y &&
                        closestHotspot.z == lastClosestHotspot!!.z
                    ))
                )
            ) {
                announceFoundHotspot(closestHotspot.x, closestHotspot.y, closestHotspot.z, closestHotspot.perk)

                lastFoundHotspotIds.add(0, closestHotspotId)
                if (lastFoundHotspotIds.size > 2) {
                    lastFoundHotspotIds.removeAt(lastFoundHotspotIds.size - 1)
                }
            }

            if (closestHotspot != null) {
                lastClosestHotspot = closestHotspot
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to send message on Hotspot found.", e)
        }
    }

    private fun announceFoundHotspot(x: Double, y: Double, z: Double, perk: String?) {
        if (Chat.messageOnHotspotFound) {
            val perkText = if (perk != null) "${perk} " else ""
            
            ChatUtils.sendLocalChat("${WHITE}You found ${perkText}${LIGHT_PURPLE}Hotspot${WHITE}.", true)
            
            val partyMessage = getMessage(x, y, z, perk, false)   
            val partyChatText = Text.literal("${WHITE}${BOLD}[Share to ${BLUE}${BOLD}PARTY ${WHITE}${BOLD}chat]")
                .setStyle(
                    Style.EMPTY
                        .withClickEvent(RunCommand("/pchat $partyMessage"))
                        .withHoverEvent(ShowText(Text.literal("Click to share to PARTY chat")))
                )
            
            val orText = Text.literal(" ${RESET}${GRAY}or ")
            
            val allMessage = getMessage(x, y, z, perk, true)
            val allChatText = Text.literal("${WHITE}${BOLD}[Share to ${YELLOW}${BOLD}ALL ${WHITE}${BOLD}chat]")
                .setStyle(
                    Style.EMPTY
                        .withClickEvent(RunCommand("/achat $allMessage"))
                        .withHoverEvent(ShowText(Text.literal("Click to share to ALL chat")))
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
        val zone = WorldUtils.getZoneName()
        val messageId = if (needsMessageId) " | ${CommonUtils.getMessageId()}" else ""

        val perkText = if (perk != null) "${perk.removeFormatting()} " else ""
        val zoneText = if (zone != null) " at ${zone.removeFormatting()}" else ""
        
        return "${location} | ${perkText}Hotspot${zoneText}${messageId}"
    }
}

