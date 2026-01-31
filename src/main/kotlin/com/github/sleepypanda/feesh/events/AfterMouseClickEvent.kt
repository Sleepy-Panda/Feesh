package com.github.sleepypanda.feesh.events

import net.minecraft.client.gui.screen.Screen

data class AfterMouseClickEvent(
    val screen: Screen,
    val mouseX: Double,
    val mouseY: Double,
    val button: Int
)
