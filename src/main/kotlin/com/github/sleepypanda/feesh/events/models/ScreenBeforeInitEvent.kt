package com.github.sleepypanda.feesh.events.models

import net.minecraft.client.gui.screen.Screen

/*
 * Called from ScreenEvents.BEFORE_INIT, before a GUI screen is initialized.
 * @param screen The screen that is about to be initialized.
 */
data class ScreenBeforeInitEvent(val screen: Screen)
