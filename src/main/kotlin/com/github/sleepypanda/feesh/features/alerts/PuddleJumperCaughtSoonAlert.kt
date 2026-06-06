package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import net.minecraft.world.entity.LivingEntity
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.math.sqrt

object PuddleJumperCaughtSoonAlert {
    private const val PUDDLE_JUMPER_DEBUG_DURATION_S = 55
    private const val PUDDLE_JUMPER_DEBUG_INTERVAL_S = 1
    private const val PUDDLE_JUMPER_ARMOR_STAND_SEARCH_RANGE = 64.0
    private const val PUDDLE_JUMPER_FROG_ENTITY_SHIFT = 2
    private const val FROG_TRACKING_START_SECONDS = 6
    private const val FROG_STABLE_MOVE_THRESHOLD = 2.0
    private const val FROG_STABLE_SECONDS_FOR_PERIOD = 3
    private const val FROG_STABLE_SECONDS_BEFORE_JUMP = 2
    private const val FROG_STABLE_PERIODS_FOR_ALERT = 4
    private const val PUDDLE_JUMPER_CAUGHT_MESSAGE = "Puddle Jumper: Wow! You caught me!"
    private const val PUDDLE_JUMPER_TROPHY_FROG_MESSAGE = "TROPHY FROG! You caught a Puddle Jumper"
    private var nextPuddleJumperTimerId = 0
    private val puddleJumperDebugTimers = mutableMapOf<Int, ActiveDebugTimer>()

    private data class ActiveDebugTimer(
        val timer: Timer,
        var lastTickSeconds: Int = 0,
        var frogEntityId: Int? = null,
        var lastFrogX: Double? = null,
        var lastFrogZ: Double? = null,
        var consecutiveStableSeconds: Int = 0,
        var stablePeriodCountedForLanding: Boolean = false,
        var stablePeriodCount: Int = 0,
        var lootStableAlertSent: Boolean = false,
    )

    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onOwnSeaCreature)
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        cancelAllPuddleJumperDebugTimers()
    }

    private fun onOwnSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnRareSeaCreatures) return // TODO

        val playerName = PlayerUtils.getFormattedNameWithoutPrefix() ?: return
        if (shouldStartDebug(event.seaCreatureName, playerName)) {
            startPuddleJumperDebugTimer()
        }
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock() || puddleJumperDebugTimers.isEmpty()) return

        val message = event.unformattedText
        when {
            message.contains(PUDDLE_JUMPER_CAUGHT_MESSAGE) -> announceLongestRunningTimer()
            message.contains(PUDDLE_JUMPER_TROPHY_FROG_MESSAGE) -> cancelLongestRunningTimerAndAnnounce()
        }
    }

    private fun shouldStartDebug(seaCreatureName: String, playerName: String): Boolean {
        return seaCreatureName.equals(SeaCreatureNames.PUDDLE_JUMPER, ignoreCase = true)
    }

    private fun startPuddleJumperDebugTimer() {
        val timerId = ++nextPuddleJumperTimerId
        val timer = Timer("PuddleJumperDebug-$timerId", true)
        val frogEntityId = findPuddleJumperFrogEntityId()
        puddleJumperDebugTimers[timerId] = ActiveDebugTimer(timer, frogEntityId = frogEntityId)

        for (seconds in PUDDLE_JUMPER_DEBUG_INTERVAL_S..PUDDLE_JUMPER_DEBUG_DURATION_S step PUDDLE_JUMPER_DEBUG_INTERVAL_S) {
            val delayMs = seconds * 1000L
            timer.schedule(timerTask {
                FeeshMod.mc.execute {
                    if (!WorldUtils.isInSkyblock() || !puddleJumperDebugTimers.containsKey(timerId)) return@execute
                    onPuddleJumperDebugTick(timerId, seconds)
                    if (seconds >= PUDDLE_JUMPER_DEBUG_DURATION_S) {
                        cancelPuddleJumperDebugTimer(timerId)
                    }
                }
            }, delayMs)
        }
    }

    private fun cancelPuddleJumperDebugTimer(timerId: Int) {
        puddleJumperDebugTimers.remove(timerId)?.timer?.cancel()
    }

    private fun getLongestRunningTimer(): Pair<Int, ActiveDebugTimer>? {
        return puddleJumperDebugTimers.maxWithOrNull(
            compareBy<Map.Entry<Int, ActiveDebugTimer>> { it.value.lastTickSeconds }.thenBy { it.key }
        )?.toPair()
    }

    private fun announceLongestRunningTimer() {
        val (timerId, activeTimer) = getLongestRunningTimer() ?: return
        ChatUtils.sendLocalChat("${YELLOW}Puddle Jumper #$timerId timer: ${activeTimer.lastTickSeconds}s (Wow message)", true)
    }

    private fun cancelLongestRunningTimerAndAnnounce() {
        val (timerId, activeTimer) = getLongestRunningTimer() ?: return

        val elapsedSeconds = activeTimer.lastTickSeconds
        cancelPuddleJumperDebugTimer(timerId)
        ChatUtils.sendLocalChat("${YELLOW}Puddle Jumper #$timerId: caught after ${elapsedSeconds}s", true)
    }

    private fun cancelAllPuddleJumperDebugTimers() {
        puddleJumperDebugTimers.values.forEach { it.timer.cancel() }
        puddleJumperDebugTimers.clear()
    }

    private fun onPuddleJumperDebugTick(timerId: Int, seconds: Int) {
        val activeTimer = puddleJumperDebugTimers[timerId] ?: return
        activeTimer.lastTickSeconds = seconds
        val frogEntityId = resolveFrogEntityId(activeTimer)
        trackFrogPosition(timerId, seconds, activeTimer, frogEntityId)
        logFrogEntityDebug(timerId, seconds, activeTimer, frogEntityId)
        val label = "#$timerId"

        if (seconds >= PUDDLE_JUMPER_DEBUG_DURATION_S) {
            return
        }

        ChatUtils.sendLocalChat("${YELLOW}Puddle Jumper $label timer: ${seconds}s", true)
    }

    // First 6 seconds are ignored as the frog can move after the player chaotically before first jump
    // Then the following: Jump1 -> Stable Period 1 -> Jump2 -> Stable Period 2 -> Jump3 -> Stable Period 3 -> Jump4 -> Stable Period 4 -> Loot.
    private fun trackFrogPosition(timerId: Int, seconds: Int, activeTimer: ActiveDebugTimer, frogEntityId: Int?) {
        val frog = frogEntityId?.let { EntityUtils.getMcEntityById(it) as? LivingEntity }
        if (frog == null) {
            activeTimer.lastFrogX = null
            activeTimer.lastFrogZ = null
            activeTimer.consecutiveStableSeconds = 0
            activeTimer.stablePeriodCountedForLanding = false
            return
        }

        val x = frog.x
        val z = frog.z
        val lastX = activeTimer.lastFrogX
        val lastZ = activeTimer.lastFrogZ

        if (seconds > FROG_TRACKING_START_SECONDS && lastX != null && lastZ != null) {
            val horizontalMove = getHorizontalDistance(lastX, lastZ, x, z)

            when {
                horizontalMove > FROG_STABLE_MOVE_THRESHOLD -> {
                    if (!activeTimer.stablePeriodCountedForLanding &&
                        activeTimer.consecutiveStableSeconds >= FROG_STABLE_SECONDS_BEFORE_JUMP
                    ) {
                        recordStablePeriod(timerId, seconds, activeTimer, "jump")
                    }
                    activeTimer.consecutiveStableSeconds = 0
                    activeTimer.stablePeriodCountedForLanding = false
                }
                horizontalMove < FROG_STABLE_MOVE_THRESHOLD -> {
                    activeTimer.consecutiveStableSeconds++
                    if (!activeTimer.stablePeriodCountedForLanding &&
                        activeTimer.consecutiveStableSeconds >= FROG_STABLE_SECONDS_FOR_PERIOD
                    ) {
                        activeTimer.stablePeriodCountedForLanding = true
                        recordStablePeriod(timerId, seconds, activeTimer, "landed")
                    }
                }
            }
        }

        activeTimer.lastFrogX = x
        activeTimer.lastFrogZ = z
    }

    private fun recordStablePeriod(timerId: Int, seconds: Int, activeTimer: ActiveDebugTimer, reason: String) {
        activeTimer.stablePeriodCount++
        FeeshMod.LOGGER.info(
            "[Feesh] Puddle Jumper #$timerId timer ${seconds}s: stable period ${activeTimer.stablePeriodCount}/$FROG_STABLE_PERIODS_FOR_ALERT ($reason)"
        )
        if (activeTimer.stablePeriodCount >= FROG_STABLE_PERIODS_FOR_ALERT && !activeTimer.lootStableAlertSent) {
            activeTimer.lootStableAlertSent = true
            ChatUtils.sendLocalChat(
                "${YELLOW}Puddle Jumper #$timerId: stable period $FROG_STABLE_PERIODS_FOR_ALERT — loot soon!",
                true,
            )
        }
    }

    private fun getHorizontalDistance(xa: Double, za: Double, xb: Double, zb: Double): Double {
        val dx = xb - xa
        val dz = zb - za
        return sqrt(dx * dx + dz * dz)
    }

    private fun findPuddleJumperFrogEntityId(): Int? {
        val player = FeeshMod.mc.player ?: return null
        val armorStand = EntityUtils.getArmorStandsInRange(
            player.position(),
            PUDDLE_JUMPER_ARMOR_STAND_SEARCH_RANGE,
            SeaCreatureNames.PUDDLE_JUMPER,
            allowContains = true,
        ).minByOrNull { EntityUtils.getDistance(it, player) } ?: return null

        val frogEntity = EntityUtils.getMcEntityById(armorStand.id - PUDDLE_JUMPER_FROG_ENTITY_SHIFT) as? LivingEntity
        return frogEntity?.id
    }

    private fun resolveFrogEntityId(activeTimer: ActiveDebugTimer): Int? {
        activeTimer.frogEntityId?.let { frogEntityId ->
            if (EntityUtils.getMcEntityById(frogEntityId) is LivingEntity) return frogEntityId
        }

        val frogEntityId = findPuddleJumperFrogEntityId()
        activeTimer.frogEntityId = frogEntityId
        return frogEntityId
    }

    private fun logFrogEntityDebug(timerId: Int, seconds: Int, activeTimer: ActiveDebugTimer, frogEntityId: Int?) {
        if (frogEntityId == null) {
            FeeshMod.LOGGER.info("[Feesh] Puddle Jumper #$timerId timer ${seconds}s: FrogEntity not found")
            return
        }

        val frog = EntityUtils.getMcEntityById(frogEntityId) as? LivingEntity
        if (frog == null) {
            FeeshMod.LOGGER.info("[Feesh] Puddle Jumper #$timerId timer ${seconds}s: FrogEntity #$frogEntityId missing")
            return
        }

        FeeshMod.LOGGER.info(
            "[Feesh] Puddle Jumper #$timerId timer ${seconds}s: FrogEntity id=$frogEntityId " +
                "pos=(${frog.x}, ${frog.y}, ${frog.z}) stablePeriods=${activeTimer.stablePeriodCount} " +
                "consecutiveStable=${activeTimer.consecutiveStableSeconds}s"
        )
    }
}
