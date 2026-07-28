package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ArmorStandCustomNameChangedEvent
import com.github.sleepypanda.feesh.events.models.ArmorStandDespawnedEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GameRenderEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.FishingHookTimerMode
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
    var hypixelTimerEntityId: Int? = null,
    var hypixelTimerUuid: UUID? = null,
    var hypixelTimerText: String = ""
)

object FishingHookTimer {
    private var fishingHookTimer: FishingHookTimerData? = null
    private const val FISH_ARRIVED = "§c§l!!!";
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
        EventBus.subscribe(GameRenderEvent::class, ::onRender)
        EventBus.subscribe(ArmorStandCustomNameChangedEvent::class, ::onArmorStandCustomNameChanged)
        EventBus.subscribe(ArmorStandDespawnedEvent::class, ::onArmorStandDespawned)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        fishingHookTimer = null
        gui.clearLines()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!Overlays.fishingHookTimerOverlay ||
            !WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld() ||
            !PlayerUtils.hasFishingRodInHotbar()
        ) {
            fishingHookTimer = null
            gui.clearLines()
            return
        }

        val fishingHook = FishingHookUtils.getActiveFishingHook() ?: run {
            fishingHookTimer = null
            gui.clearLines()
            return
        }

        val timer = fishingHookTimer ?: FishingHookTimerData().also { fishingHookTimer = it }
        timer.ticksExisted = fishingHook.age

        val entityId = timer.hypixelTimerEntityId ?: return
        if (EntityUtils.getMcEntityById(entityId) == null) {
            clearHypixelTimer(timer)
        }
    }

    private fun onArmorStandCustomNameChanged(event: ArmorStandCustomNameChangedEvent) {
        if (!Overlays.fishingHookTimerOverlay ||
            !WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld() ||
            !PlayerUtils.hasFishingRodInHotbar()
        ) return

        val customName = event.customName.formatted
        val fishState = parseFishState(customName) ?: run {
            val timer = fishingHookTimer
            if (timer?.hypixelTimerEntityId == event.entityId) clearHypixelTimer(timer)
            return
        }

        val timer = fishingHookTimer
        if (timer?.hypixelTimerEntityId == event.entityId) {
            timer.fishState = fishState
            timer.hypixelTimerText = customName
            return
        }
        if (timer?.hypixelTimerEntityId != null) return

        val fishingHook = FishingHookUtils.getActiveFishingHook() ?: return
        val distance = EntityUtils.getDistance(
            fishingHook.x, fishingHook.y, fishingHook.z,
            event.position.x, event.position.y, event.position.z
        )
        if (distance > 5.0) return

        val armorStand = EntityUtils.getMcEntityById(event.entityId) as? ArmorStand
        val updated = timer ?: FishingHookTimerData(ticksExisted = fishingHook.age).also { fishingHookTimer = it }
        updated.hypixelTimerEntityId = event.entityId
        updated.hypixelTimerUuid = armorStand?.uuid
        updated.fishState = fishState
        updated.hypixelTimerText = customName
    }

    private fun onArmorStandDespawned(event: ArmorStandDespawnedEvent) {
        val timer = fishingHookTimer ?: return
        if (timer.hypixelTimerEntityId != event.armorStand.id &&
            timer.hypixelTimerUuid != event.armorStand.uuid
        ) return
        clearHypixelTimer(timer)
    }

    private fun onRender(@Suppress("UNUSED_PARAMETER") event: GameRenderEvent) {
        if (fishingHookTimer == null ||
            !Overlays.fishingHookTimerOverlay ||
            !WorldUtils.isInSkyblock() ||
            !PlayerUtils.hasFishingRodInHotbar() ||
            !WorldUtils.isInFishingWorld()
        ) {
            gui.clearLines()
            return
        }

        val text = when {
            fishingHookTimer!!.fishState == FishState.ARRIVED -> {
                val template = Overlays.fishingHookFishArrivedTemplate
                if (template.isNotEmpty()) template else DEFAULT_FISH_ARRIVED_TEMPLATE
            }
            fishingHookTimer!!.fishState == FishState.ARRIVING && Overlays.fishingHookTimerMode == FishingHookTimerMode.UNTIL_REEL_IN -> {
                val template = Overlays.fishingHookFishTimerTemplate
                val timerText = fishingHookTimer!!.hypixelTimerText
                (if (template.isNotEmpty()) template else DEFAULT_TIMER_TEMPLATE).replace("{timer}", timerText)
            }
            Overlays.fishingHookTimerMode == FishingHookTimerMode.SINCE_CASTED -> {
                val template = Overlays.fishingHookFishTimerTemplate
                val seconds = String.format("%.1f", fishingHookTimer!!.ticksExisted / 20.0)
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

    private fun parseFishState(customName: String): FishState? {
        return when {
            customName == FISH_ARRIVED -> FishState.ARRIVED
            customName.matches(FISHING_HOOK_TIMER_UNTIL_REEL_IN_REGEX) -> FishState.ARRIVING
            else -> null
        }
    }

    private fun clearHypixelTimer(timer: FishingHookTimerData) {
        timer.hypixelTimerEntityId = null
        timer.hypixelTimerUuid = null
        timer.hypixelTimerText = ""
        timer.fishState = FishState.NONE
    }
}
