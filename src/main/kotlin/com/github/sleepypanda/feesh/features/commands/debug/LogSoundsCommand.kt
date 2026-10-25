package com.github.sleepypanda.feesh.features.commands.debug

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.SoundPlayedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import java.util.Timer
import kotlin.concurrent.timerTask

/*
 * Logs all sounds played for 5 seconds.
 * Useful for getting sounds information for mute sound features.
 */
object LogSoundsCommand {
    const val COMMAND_NAME = "feeshDebugLogSounds"
    private const val RECORD_DURATION_MS = 5_000L

    private var recordingUntilMs = 0L
    private var session = 0

    fun init() {
        RegisterUtils.command(COMMAND_NAME) {
            startRecording()
        }
        EventBus.subscribe(SoundPlayedEvent::class, ::onSoundPlayed)
    }

    private fun startRecording() {
        session++
        val thisSession = session
        recordingUntilMs = System.currentTimeMillis() + RECORD_DURATION_MS
        ChatUtils.sendLocalChat("${GREEN}Logging sounds for 5 seconds...", true)

        Timer("Feesh-LogSounds", true).schedule(timerTask {
            if (thisSession != session) return@timerTask
            FeeshMod.mc.execute {
                ChatUtils.sendLocalChat("${GREEN}Sounds logging finished.", true)
            }
        }, RECORD_DURATION_MS)
    }

    private fun onSoundPlayed(event: SoundPlayedEvent) {
        if (System.currentTimeMillis() > recordingUntilMs) return

        val path = event.soundId.path
        val name = event.soundId.toString()
        val line = "path=$path name=$name volume=${event.volume} pitch=${event.pitch}"
        FeeshMod.LOGGER.info("[Feesh] Sound $line")
    }
}
