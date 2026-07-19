package com.github.sleepypanda.feesh.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

fun Minecraft.getScreenCompat(): Screen? {
    //#if MC >= 26.2
    //$$ return gui.screen()
    //#else
    return screen
    //#endif
}

fun Minecraft.setScreenCompat(screen: Screen?) {
    //#if MC >= 26.2
    //$$ if (screen == null) gui.setScreen(null) else setScreenAndShow(screen)
    //#else
    setScreen(screen)
    //#endif
}

fun Minecraft.addClientChatMessageCompat(message: Component) {
    //#if MC >= 26.2
    //$$ gui.hud.getChat().addClientSystemMessage(message)
    //#elseif MC >= 26.1
    //$$ gui.chat.addClientSystemMessage(message)
    //#else
    gui.chat.addMessage(message)
    //#endif
}

fun Minecraft.showTitleCompat(title: Component, subtitle: Component, fadeIn: Int, stay: Int, fadeOut: Int) {
    //#if MC >= 26.2
    //$$ gui.hud.apply {
    //$$     setTimes(fadeIn, stay, fadeOut)
    //$$     setTitle(title)
    //$$     setSubtitle(subtitle)
    //$$ }
    //#else
    gui.apply {
        setTimes(fadeIn, stay, fadeOut)
        setTitle(title)
        setSubtitle(subtitle)
    }
    //#endif
}
