package com.github.sleepypanda.feesh.events.models

/**
 * Event for when a GUI screen is closed.
 * @param guiName Display name of the closed GUI: for [net.minecraft.client.gui.screens.inventory.AbstractContainerScreen]
 *  (e.g. chest, inventory container) the screen title; for other screens the class simple name.
 */
data class GuiClosedEvent(val guiName: String)
