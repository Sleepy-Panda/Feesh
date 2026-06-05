package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.AlertSource
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import java.util.Timer
import kotlin.concurrent.timerTask

object PuddleJumperCaughtSoonAlert {
    private const val PUDDLE_JUMPER_DEBUG_DURATION_S = 50
    private const val PUDDLE_JUMPER_DEBUG_INTERVAL_S = 5
    private var nextPuddleJumperTimerId = 0
    private val puddleJumperDebugTimers = mutableMapOf<Int, Timer>()

    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onOwnSeaCreature)
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

    private fun shouldStartDebug(seaCreatureName: String, playerName: String): Boolean {
        return seaCreatureName.equals(SeaCreatureNames.PUDDLE_JUMPER, ignoreCase = true)
    }

    private fun startPuddleJumperDebugTimer() {
        val timerId = ++nextPuddleJumperTimerId
        val timer = Timer("PuddleJumperDebug-$timerId", true)
        puddleJumperDebugTimers[timerId] = timer

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
        puddleJumperDebugTimers.remove(timerId)?.cancel()
    }

    private fun cancelAllPuddleJumperDebugTimers() {
        puddleJumperDebugTimers.values.forEach { it.cancel() }
        puddleJumperDebugTimers.clear()
    }

    private fun onPuddleJumperDebugTick(timerId: Int, seconds: Int) {
        val label = "#$timerId"

        if (seconds >= PUDDLE_JUMPER_DEBUG_DURATION_S) {
            ChatUtils.sendLocalChat("${YELLOW}Puddle Jumper $label: 50s elapsed", true)
            CommonUtils.showTitle("${YELLOW}50s elapsed ($label)")
            SoundUtils.playSound()
            return
        }

        ChatUtils.sendLocalChat("${YELLOW}Puddle Jumper $label timer: ${seconds}s", true)
    }
}
