package com.github.sleepypanda.feesh.events.models

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel

/*
 * Called after the world is changed.
 * @param mc The Minecraft instance.
 * @param world The new ClientLevel instance.
 */
class WorldChangedEvent(val mc: Minecraft, val world: ClientLevel?)
