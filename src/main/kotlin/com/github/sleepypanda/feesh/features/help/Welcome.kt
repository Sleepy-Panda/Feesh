package com.github.sleepypanda.feesh.features.help

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.gui.MoveGuis

object Welcome {
    fun init() {
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        if (FeeshMod.mc.player == null || FeeshMod.mc.gui == null) return
        if (PersistentDataManager.feeshData.isWelcomeMessageShown) return

        showWelcomeMessage()
        
        PersistentDataManager.feeshData.isWelcomeMessageShown = true
        PersistentDataManager.saveFeeshDataToFileAsync()
    }

    private fun showWelcomeMessage() {
        val chatBreak = "${GRAY}${ChatUtils.getChatBreak("-")}"
        
        ChatUtils.sendLocalChat(chatBreak)
        ChatUtils.sendLocalChat("${AQUA}α ${WHITE}${BOLD}Welcome to ${GOLD}${BOLD}${FeeshMod.MOD_NAME}${WHITE}${BOLD}!")
        ChatUtils.sendLocalChat("${GRAY}- ${WHITE}To open the settings, do ${AQUA}/feesh")
        ChatUtils.sendLocalChat("${GRAY}- ${WHITE}To move the enabled GUIs, do ${AQUA}/${MoveGuis.COMMAND_NAME}")
        ChatUtils.sendLocalChat("${GRAY}Happy Fishing!")
        ChatUtils.sendLocalChat(chatBreak)
    }
}
