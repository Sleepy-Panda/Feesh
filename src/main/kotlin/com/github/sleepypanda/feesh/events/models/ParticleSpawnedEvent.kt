package com.github.sleepypanda.feesh.events.models

import net.minecraft.particle.ParticleTypes
import net.minecraft.particle.ParticleType

data class ParticleSpawnedEvent(
    val particle: ParticleType<*>,
    val count: Int,
    val speed: Double,
    val x: Double,
    val y: Double,
    val z: Double
)