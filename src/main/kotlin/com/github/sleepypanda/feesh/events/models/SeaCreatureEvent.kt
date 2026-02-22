package com.github.sleepypanda.feesh.events.models

import net.minecraft.entity.LivingEntity

class SeaCreatureEvent (
    val entity: LivingEntity,
    val name: String
)