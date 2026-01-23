package com.github.sleepypanda.feesh.events

import net.minecraft.client.gui.screen.Screen

/*
 * Called when a GUI screen is opened.
 * @param screen The screen that was opened. Can be null if screen was closed.
 */
data class GuiOpenedEvent(val screen: Screen?)
