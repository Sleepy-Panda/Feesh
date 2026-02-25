package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GameRenderEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.FishingHookTimerMode
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.enums.Alignment
import com.github.sleepypanda.feesh.events.models.ParticleSpawnedEvent
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.Vec3d
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
    var hypixelTimerText: String = "",
    var hasParticle: Boolean = false
)

object FishingHookTimer {
    private var fishingHookTimer: FishingHookTimerData? = null
    private var lastParticlePosition: Vec3d? = null
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
        .setCondition {
            WorldUtils.isInFishingWorld() &&
            PlayerUtils.hasFishingRodInHotbar()
        }

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(GameRenderEvent::class, ::onRender)
        EventBus.subscribe(ParticleSpawnedEvent::class, ::onParticleSpawned)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        fishingHookTimer = null
        lastParticlePosition = null
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

        val fishingHook = getPlayerFishingHook() ?: run {
            fishingHookTimer = null
            gui.clearLines()
            return
        }

        fishingHookTimer = FishingHookTimerData(
            ticksExisted = fishingHook.age,
            fishState = FishState.NONE
        )

        val hypixelHookTimer = getHypixelFishingHookTimer(fishingHook)
        if (hypixelHookTimer != null) {
            fishingHookTimer = fishingHookTimer!!.copy(
                hypixelTimerUuid = hypixelHookTimer.uuid,
                fishState = hypixelHookTimer.fishState,
                hypixelTimerText = hypixelHookTimer.name,
                hasParticle = lastParticlePosition != null
            )
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
                val timer = (if (template.isNotEmpty()) template else DEFAULT_TIMER_TEMPLATE).replace("{timer}", timerText)
                if (fishingHookTimer!!.hasParticle) "$timer${GREEN}*" else timer
            }
            Overlays.fishingHookTimerMode == FishingHookTimerMode.SINCE_CASTED -> {
                val template = Overlays.fishingHookFishTimerTemplate
                val seconds = String.format("%.1f", fishingHookTimer!!.ticksExisted / 20.0)
                (if (template.isNotEmpty()) template else DEFAULT_TIMER_TEMPLATE).replace("{timer}", seconds)
                // TODO same
            }
            else -> null
        }

        if (text != null) {
            gui.setLines(listOf(text))
        } else {
            gui.clearLines()
        }
    }

    private fun onParticleSpawned(event: ParticleSpawnedEvent) {
        if (!Overlays.fishingHookTimerOverlay ||
            !WorldUtils.isInSkyblock() ||
            !WorldUtils.isInHotspotFishingWorld() ||
            !PlayerUtils.hasFishingRodInHotbar()
        ) {
            lastParticlePosition = null
            return
        }

        if (!(event.particle == ParticleTypes.HAPPY_VILLAGER && event.count == 1 && event.speed == 0.0)) return
        lastParticlePosition = null

        if (fishingHookTimer == null || (fishingHookTimer!!.fishState != FishState.ARRIVING && fishingHookTimer!!.fishState != FishState.NONE)) return

        val fishingHook = getPlayerFishingHook() ?: return
        if (EntityUtils.getDistance(fishingHook, event.x, event.y, event.z) > 5) return

        lastParticlePosition = Vec3d(event.x, event.y, event.z)
       // FeeshMod.LOGGER.info("Particle spawned: ${event.particle.javaClass.name } ${event.particle.javaClass.canonicalName } ${event.count} ${event.speed}")
        //FeeshMod.LOGGER.info("Particle spawned: ${event.particle == ParticleTypes.COMPOSTER} ${event.particle == ParticleTypes.HAPPY_VILLAGER} ${event.particle == ParticleTypes.EGG_CRACK}")
        //fishingHookTimer = fishingHookTimer!!.copy(
        //    hasParticle = true
        //)
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

    private fun getPlayerFishingHook(): FishingBobberEntity? {
        val player = FeeshMod.mc.player ?: return null
        val world = FeeshMod.mc.world ?: return null

        return world.entities
            .filterIsInstance<FishingBobberEntity>()
            .firstOrNull { it.owner == player }
    }

    private fun getHypixelFishingHookTimer(fishingHook: FishingBobberEntity): HypixelTimerData? {
        val world = FeeshMod.mc.world ?: return null

        val armorStands = world.entities
            .filterIsInstance<ArmorStandEntity>()
            .filter { armorStand ->
                val distance = EntityUtils.getDistance(fishingHook, armorStand)
                distance <= 5.0 && armorStand.isCustomNameVisible
            }

        for (armorStand in armorStands) {
            val customName = armorStand.customName?.getFormattedString() ?: continue
            if (customName.matches(FISHING_HOOK_TIMER_UNTIL_REEL_IN_REGEX) || customName == FISH_ARRIVED) {
                val fishState = if (customName == FISH_ARRIVED) FishState.ARRIVED else FishState.ARRIVING
                return HypixelTimerData(uuid = armorStand.uuid, name = customName, fishState = fishState)
            }
        }

        return null
    }

    private data class HypixelTimerData(
        val uuid: UUID,
        val name: String,
        val fishState: FishState
    )
}