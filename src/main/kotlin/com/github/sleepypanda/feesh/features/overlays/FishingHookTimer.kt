package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ArmorStandCustomNameChangedEvent
import com.github.sleepypanda.feesh.events.models.ArmorStandDespawnedEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.FishingHookTimerMode
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.enums.Alignment
import net.minecraft.world.entity.decoration.ArmorStand
import java.util.UUID

enum class FishState {
    NONE,
    ARRIVING,
    ARRIVED
}

data class FishingHookTimerData(
    var ticksExisted: Int = 0,
    var fishState: FishState = FishState.NONE,
    // while hypixel timer / !!! armorstand is visible, those fields will be set
    var hypixelTimerEntityId: Int? = null,
    var hypixelTimerUuid: UUID? = null,
    var hypixelTimerText: String = ""
)

object FishingHookTimer {
    private var fishingHookTimer: FishingHookTimerData? = null

    private const val HYPIXEL_FISH_ARRIVED_NAMETAG = "§c§l!!!";

    private val FISHING_HOOK_TIMER_UNTIL_REEL_IN_REGEX = Regex("§e§l(\\d+(\\.\\d+)?)");
    private val DEFAULT_FISH_ARRIVED_TEMPLATE = "${RED}${BOLD}!!!"
    private val DEFAULT_TIMER_TEMPLATE = "${YELLOW}${BOLD}{timer}"

    private val gui = FeeshGui()
        .setCoordsDataKey("fishingHookTimer")
        .setClickable(false)
        .setAlignment(Alignment.CENTER)
        .setSampleLines(listOf(
            "${YELLOW}${BOLD}2.0s"
        ))
        .setSettingsKey { Overlays.fishingHookTimerOverlay }
        .setApplyCustomStyleKey { Overlays.fishingHookTimerCustomStyle }
        .setCondition {
            WorldUtils.isInFishingWorld() &&
            PlayerUtils.hasFishingRodInHotbar()
        }

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(ArmorStandCustomNameChangedEvent::class, ::onArmorStandCustomNameChanged)
        EventBus.subscribe(ArmorStandDespawnedEvent::class, ::onArmorStandDespawned)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        reset()
    }

    private fun reset() {
        fishingHookTimer = null
        gui.clearLines()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        CommonUtils.runWithCatching("Failed to check state for Fishing hook timer") {
            if (!Overlays.fishingHookTimerOverlay ||
                !WorldUtils.isInSkyblock() ||
                !WorldUtils.isInFishingWorld() ||
                !PlayerUtils.hasFishingRodInHotbar()
            ) {
                reset()
                return
            }
    
            val fishingHook = FishingHookUtils.getActiveFishingHook() ?: run {
                reset()
                return
            }
    
            if (fishingHookTimer == null) fishingHookTimer = FishingHookTimerData()
            fishingHookTimer!!.ticksExisted = fishingHook.age
    
            val entityId = fishingHookTimer!!.hypixelTimerEntityId
            if (entityId != null && EntityUtils.getMcEntityById(entityId) == null) {
                clearHypixelTimer()
            }
    
            updateGuiLines()
        }
    }

    private fun onArmorStandCustomNameChanged(event: ArmorStandCustomNameChangedEvent) {
        CommonUtils.runWithCatching("Failed to handle armor stand custom name changed event for Fishing hook timer") {
            if (!Overlays.fishingHookTimerOverlay ||
                !WorldUtils.isInSkyblock() ||
                !WorldUtils.isInFishingWorld() ||
                !PlayerUtils.hasFishingRodInHotbar()
            ) return
    
            val customName = event.customName.formatted

            // Armor stand is something different from timer / !!!
            val fishState = getFishStateFromNametag(customName) ?: run {
                if (fishingHookTimer?.hypixelTimerEntityId == event.entityId) {
                    clearHypixelTimer()
                    updateGuiLines()
                }
                return
            }
    
            // The same armor stand but custom name text is updated, e.g. 0.1s -> 0.2s
            if (fishingHookTimer != null && fishingHookTimer!!.hypixelTimerEntityId == event.entityId) {
                fishingHookTimer!!.fishState = fishState
                fishingHookTimer!!.hypixelTimerText = customName
                updateGuiLines()
                return
            }
    
            if (fishingHookTimer?.hypixelTimerEntityId != null) return
    
            // New armor stand, need to check if it's close enough to the fishing hook
            val fishingHook = FishingHookUtils.getActiveFishingHook() ?: return
            val distance = EntityUtils.getDistance(
                fishingHook.x, fishingHook.y, fishingHook.z,
                event.position.x, event.position.y, event.position.z
            )
            if (distance > 5.0) return

            val armorStand = EntityUtils.getMcEntityById(event.entityId) as? ArmorStand ?: return

            if (fishingHookTimer == null) fishingHookTimer = FishingHookTimerData(ticksExisted = fishingHook.age)
    
            fishingHookTimer!!.hypixelTimerEntityId = event.entityId
            fishingHookTimer!!.hypixelTimerUuid = armorStand.uuid
            fishingHookTimer!!.fishState = fishState
            fishingHookTimer!!.hypixelTimerText = customName
            updateGuiLines()
        }
    }

    private fun onArmorStandDespawned(event: ArmorStandDespawnedEvent) {
        CommonUtils.runWithCatching("Failed to handle armor stand despawned event for Fishing hook timer") {
            if (fishingHookTimer == null) return
            if (fishingHookTimer!!.hypixelTimerEntityId != event.armorStand.id && fishingHookTimer!!.hypixelTimerUuid != event.armorStand.uuid) return
    
            clearHypixelTimer()
            updateGuiLines()    
        }
    }

    private fun updateGuiLines() {
        CommonUtils.runWithCatching("Failed to update gui lines for Fishing hook timer") {
            val timer = fishingHookTimer
            if (timer == null ||
                !Overlays.fishingHookTimerOverlay ||
                !WorldUtils.isInSkyblock() ||
                !PlayerUtils.hasFishingRodInHotbar() ||
                !WorldUtils.isInFishingWorld()
            ) {
                gui.clearLines()
                return
            }
    
            val text = when {
                timer.fishState == FishState.ARRIVED -> {
                    val template = Overlays.fishingHookFishArrivedTemplate
                    if (template.isNotEmpty()) template else DEFAULT_FISH_ARRIVED_TEMPLATE
                }
                timer.fishState == FishState.ARRIVING && Overlays.fishingHookTimerMode == FishingHookTimerMode.UNTIL_REEL_IN -> {
                    val template = Overlays.fishingHookFishTimerTemplate
                    (if (template.isNotEmpty()) template else DEFAULT_TIMER_TEMPLATE).replace("{timer}", timer.hypixelTimerText)
                }
                Overlays.fishingHookTimerMode == FishingHookTimerMode.SINCE_CASTED -> {
                    val template = Overlays.fishingHookFishTimerTemplate
                    val seconds = String.format("%.1f", timer.ticksExisted / 20.0)
                    (if (template.isNotEmpty()) template else DEFAULT_TIMER_TEMPLATE).replace("{timer}", seconds)
                }
                else -> null
            }
    
            if (text != null) {
                gui.setLines(listOf(LineInfo(text)))
            } else {
                gui.clearLines()
            }    
        }
    }

    @JvmStatic
    fun shouldCancelArmorStandRendering(entityUuid: UUID?): Boolean {
        if (entityUuid == null) return false
        if (!Overlays.fishingHookTimerOverlay ||
            !WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld() ||
            !PlayerUtils.hasFishingRodInHotbar()
        ) {
            return false
        }

        return fishingHookTimer?.hypixelTimerUuid == entityUuid
    }

    private fun getFishStateFromNametag(customName: String): FishState? {
        return when {
            customName == HYPIXEL_FISH_ARRIVED_NAMETAG -> FishState.ARRIVED // !!!
            customName.matches(FISHING_HOOK_TIMER_UNTIL_REEL_IN_REGEX) -> FishState.ARRIVING // 1.1, 2.0, etc.
            else -> null
        }
    }

    private fun clearHypixelTimer() {
        if (fishingHookTimer == null) return
        fishingHookTimer!!.hypixelTimerEntityId = null
        fishingHookTimer!!.hypixelTimerUuid = null
        fishingHookTimer!!.hypixelTimerText = ""
        fishingHookTimer!!.fishState = FishState.NONE
    }
}
