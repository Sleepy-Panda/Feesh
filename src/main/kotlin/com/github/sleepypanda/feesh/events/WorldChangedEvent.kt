package com.github.sleepypanda.feesh.events

import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld

/*
 * Called after the world is changed.
 * @param mc The MinecraftClient instance.
 * @param world The new ClientWorld instance.
 */
class WorldChangedEvent(val mc: MinecraftClient, val world: ClientWorld)