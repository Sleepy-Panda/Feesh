package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.ArmorStandLoadedEvent
import com.github.sleepypanda.feesh.events.models.GameRenderEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.FishingHookTimerMode
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
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
    var hypixelTimerUuid: UUID? = null,
    var hypixelTimerText: String = ""
)

object FishingHookTimer {
    private var fishingHookTimer: FishingHookTimerData? = null
    private var potentialArmorStands: MutableList<ArmorStand> = mutableListOf()

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
        EventBus.subscribe(ArmorStandLoadedEvent::class, ::onArmorStandLoaded)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(GameRenderEvent::class, ::onRender)
    }

    private fun onArmorStandLoaded(event: ArmorStandLoadedEvent) {
        potentialArmorStands.add(event.entity)
    }

    private fun reset() {
        fishingHookTimer = null
        potentialArmorStands.clear()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        reset()
        gui.clearLines()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (!Overlays.fishingHookTimerOverlay ||
            !WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld() ||
            !PlayerUtils.hasFishingRodInHotbar()
        ) {
            reset()
            return
        }

        val fishingHook = FishingHookUtils.getFishingHook() ?: run {
            reset()
            return
        }

        fishingHookTimer = FishingHookTimerData(
            ticksExisted = fishingHook.age,
            fishState = FishState.NONE
        )

        if (potentialArmorStands.isNotEmpty()) {
            val timers = potentialArmorStands.filter {
                it.distanceToSqr(fishingHook.x, fishingHook.y, fishingHook.z) <= 25.0 &&
                it.customName != null &&
                (it.customName?.getFormattedString()?.matches(FISHING_HOOK_TIMER_UNTIL_REEL_IN_REGEX) == true || it.customName?.getFormattedString() == FISH_ARRIVED)
            }
            if (timers.isNotEmpty()) {
                val timer = timers.first()
                val customName = timer.customName?.getFormattedString() ?: ""
                val fishState = if (customName == FISH_ARRIVED) FishState.ARRIVED else FishState.ARRIVING
                fishingHookTimer = fishingHookTimer!!.copy(
                    hypixelTimerUuid = timer.uuid,
                    fishState = fishState,
                    hypixelTimerText = customName
                )
            }
        }
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
            gui.setLines(listOf(text))
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
}