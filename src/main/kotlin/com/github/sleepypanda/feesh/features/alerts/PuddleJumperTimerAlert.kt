package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import net.minecraft.sounds.SoundEvents
import java.util.Timer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.timerTask

// Notes:
// Most of the Jumpers take 48-55 seconds to finish their jumping and give the loot.
// Some of them rarely take 43-45 seconds.
// "Arrives soon" alert could be tracked in a better way to detect Puddler's path / jumps count and show the alert only when it's close to the destination.
// However, I found no Frog entity properties that reflect its state normally, like health or isJumping.
// Can't use Armor stands ("RIBBIT" or "COME AND GET ME!") visibility to track the Frog, because they are visible only when close to the Frog.
// Even if track its position and calculate jumps count, Frog entity may be out of render distance so sometimes we can't track its position or other properties.

object PuddleJumperTimerAlert {
    private val activeTimers = ConcurrentHashMap<Long, Timer>()
    private val nextTimerId = AtomicLong(0)

    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onOwnSeaCreatureCaught)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onOwnSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        CommonUtils.runWithCatching("Failed to start Puddle Jumper timer") {
            if (!WorldUtils.isInSkyblock() || !Alerts.alertOnPuddleJumperTimer) return
            if (event.seaCreatureName != SeaCreatureNames.PUDDLE_JUMPER) return
    
            val timerId = nextTimerId.incrementAndGet()
            val timer = Timer("PuddleJumperTimer-$timerId", true)
            activeTimers[timerId] = timer
    
            val delayMs = Alerts.puddleJumperTimerSeconds * 1000L
            timer.schedule(timerTask {
                activeTimers.remove(timerId)
                timer.cancel()
                onPuddleJumperTimerFinished()
            }, delayMs)
        }
    }

    private fun onPuddleJumperTimerFinished() {
        if (!WorldUtils.isInSkyblock() || !Alerts.alertOnPuddleJumperTimer) return

        CommonUtils.showTitle("${GOLD}Puddle Jumper ${WHITE}arrives soon!")
        ChatUtils.sendLocalChat("Your ${GOLD}Puddle Jumper ${WHITE}arrives soon.", true)
        SoundUtils.playSound(SoundEvents.FROG_HURT)
        SoundUtils.playSound()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        cancelAllTimers()
    }

    private fun cancelAllTimers() {
        activeTimers.values.forEach { it.cancel() }
        activeTimers.clear()
    }
}
