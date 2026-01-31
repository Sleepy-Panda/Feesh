package com.github.sleepypanda.feesh.events

/**
 * Event for when a GUI screen is closed.
 * @param guiName Display name of the closed GUI: for [net.minecraft.client.gui.screen.ingame.HandledScreen]
 *  (e.g. chest, inventory container) the screen title; for other screens the class simple name.
 */
data class GuiClosedEvent(val guiName: String)
