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
    //#else
    gui.chat.addClientSystemMessage(message)
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

fun openUriCompat(url: String) {
    //#if MC >= 26.3
    //$$ com.mojang.blaze3d.Blaze3D.openUri(java.net.URI.create(url))
    //#else
    net.minecraft.util.Util.getPlatform().openUri(url)
    //#endif
}

fun openPathCompat(path: java.nio.file.Path) {
    //#if MC >= 26.3
    //$$ com.mojang.blaze3d.Blaze3D.openPath(path)
    //#else
    net.minecraft.util.Util.getPlatform().openUri(path.toUri().toString())
    //#endif
}
