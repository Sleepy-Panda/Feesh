package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import java.util.ArrayDeque

object AlertUtils {
    private const val COOLDOWN_MS = 1000L

    /**
     * Shows a title with an optional subtitle.
     */
    data class TitleConfig(
        /** The title to show. */
        val title: String,
        /** The optional subtitle to show. */
        val subtitle: String? = null,
        /** The fade in time in ticks. */
        val fadeIn: Int? = null,
        /** The stay time in ticks. */
        val stay: Int? = null,
        /** The fade out time in ticks. */
        val fadeOut: Int? = null,
    )
    
    sealed class SoundConfig {
        data class McSound(
            val sound: SoundEvent = SoundEvents.EXPERIENCE_ORB_PICKUP,
            val pitch: Float = 1.0f,
            val volume: Float = 1.0f,
        ) : SoundConfig()

        data class CustomSound(
            val fileName: String?,
        ) : SoundConfig()
    }

    private data class QueuedAlert(
        val title: TitleConfig?,
        val sounds: List<SoundConfig>?,
    )

    private val alertQueue = ArrayDeque<QueuedAlert>()
    private var lastPlayTime: Long? = null

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    /**
     * Queues a title and its sounds.
     * The next alert waits [COOLDOWN_MS] after the previous one started, to avoid overlapping.
     */
    fun alert(
        title: TitleConfig? = null,
        sound: SoundConfig? = null,
        sounds: List<SoundConfig>? = null,
    ) {
        val queuedSounds = when {
            sounds != null && sounds.isNotEmpty() -> sounds
            sound != null -> listOf(sound)
            else -> null
        }

        if (title == null && queuedSounds == null) return

        alertQueue.add(QueuedAlert(title, queuedSounds))
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        processQueue()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        alertQueue.clear()
        lastPlayTime = null
    }

    private fun processQueue() {
        if (!WorldUtils.isInSkyblock()) return

        val next = alertQueue.firstOrNull() ?: return
        val now = System.currentTimeMillis()
        if (lastPlayTime != null && now - lastPlayTime!! < COOLDOWN_MS) return

        alertQueue.removeFirst()
        lastPlayTime = now
        play(next)
    }

    private fun play(alert: QueuedAlert) {
        CommonUtils.runWithCatching("Failed to play alert") {
            alert.title?.let(::showTitle)
            alert.sounds?.forEach(::playSound)
        }
    }

    private fun showTitle(config: TitleConfig) {
        FeeshMod.mc.showTitleCompat(
            Component.literal(config.title),
            Component.literal(config.subtitle ?: " "),
            config.fadeIn ?: 0,
            config.stay ?: 40,
            config.fadeOut ?: 10,
        )
    }

    private fun playSound(sound: SoundConfig) {
        when (sound) {
            is SoundConfig.McSound -> SoundUtils.playMcSound(sound.sound, sound.pitch, sound.volume)
            is SoundConfig.CustomSound -> SoundUtils.playCustomSound(sound.fileName)
        }
    }
}
