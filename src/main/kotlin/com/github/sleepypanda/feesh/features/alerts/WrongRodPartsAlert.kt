package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.TYPE_SPOOKY
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.settings.categories.Alerts
import net.minecraft.component.DataComponentTypes

private var isHotspotAlertShown = false
private var isJerryWorkshopAlertShown = false
private var isSpookyAlertShown = false

object WrongRodPartsAlert {
    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onOwnSeaCreatureCaught)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        isHotspotAlertShown = false
        isJerryWorkshopAlertShown = false
        isSpookyAlertShown = false
    }

    private fun onOwnSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        try {
            if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
            if (!Alerts.checkForIcySinkerWhenFishingOnJerrysWorkshop && !Alerts.checkForHotspotHookAndSinkerWhenFishingInHotspot && !Alerts.checkForPhantomHookWhenSpookyIsActive) return
            if (isHotspotAlertShown && isJerryWorkshopAlertShown && isSpookyAlertShown) return
    
            val heldItem = FeeshMod.mc.player?.mainHandStack ?: return
            if (!ItemUtils.isFishingRod(heldItem) || heldItem.name.string.contains("Dirt Rod")) return

            val loreLines = heldItem.get(DataComponentTypes.LORE)?.lines?.map { it.string } ?: listOf()
            val hasIcySinker = loreLines.any { it.contains("Icy Sinker") }
            val hasHotspotHook = loreLines.any { it.contains("Hotspot Hook") }
            val hasHotspotSinker = loreLines.any { it.contains("Hotspot Sinker") }
            val hasPhantomHook = loreLines.any { it.contains("Phantom Hook") }

            if (Alerts.checkForIcySinkerWhenFishingOnJerrysWorkshop &&
                WorldUtils.getWorldName() == WorldUtils.JERRY_WORKSHOP &&
                !isJerryWorkshopAlertShown &&
                !hasIcySinker
            ) {
                isJerryWorkshopAlertShown = true
                alertOnWrongRodParts(listOf("${RARE}Icy Sinker"), "Jerry Workshop")
                return
            }

            if (Alerts.checkForHotspotHookAndSinkerWhenFishingInHotspot &&
                WorldUtils.isInHotspotFishingWorld() &&
                FishingHookUtils.wasFishingHookActiveInHotspotSecondsAgo(5) &&
                !isHotspotAlertShown &&
                (!hasHotspotHook || !hasHotspotSinker)
            ) {
                isHotspotAlertShown = true
                alertOnWrongRodParts(listOf("${RARE}Hotspot Hook", "${RARE}Hotspot Sinker"), "Hotspot")
                return
            }

            if (Alerts.checkForPhantomHookWhenSpookyIsActive && !isSpookyAlertShown) {
                val seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name == event.seaCreatureName } ?: return
                if (seaCreatureInfo.types.contains(TYPE_SPOOKY) && !hasPhantomHook) {
                    isSpookyAlertShown = true
                    alertOnWrongRodParts(listOf("${RARE}Phantom Hook"), "Spooky")
                    return
                }
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to check fishing rod parts", e)
        }
    }

    private fun alertOnWrongRodParts(partNames: List<String>, fishingType: String) {
        if (partNames.isEmpty()) return
        CommonUtils.showTitle("${YELLOW}Check fishing rod parts!")
        SoundUtils.playSound()
        ChatUtils.sendLocalChatWithCommand("${WHITE}${partNames.joinToString(" ${WHITE}and ")} ${WHITE}recommended for ${fishingType} fishing. Click to call Roddy!", "call roddy", true)
    }
}