package com.github.sleepypanda.feesh.events.models

import net.minecraft.resources.Identifier

/**
 * Called when the client sound engine starts playing a sound.
 * @param soundId The sound identifier.
 * @param volume The raw sound instance volume (before category/attenuation).
 * @param pitch The raw sound instance pitch.
 * @param x Sound X position.
 * @param y Sound Y position.
 * @param z Sound Z position.
 * @param relative Whether the sound is relative to the listener (not world-positional).
 */
data class SoundPlayedEvent(
    val soundId: Identifier,
    val volume: Float,
    val pitch: Float,
    val x: Double,
    val y: Double,
    val z: Double,
    val relative: Boolean,
)
