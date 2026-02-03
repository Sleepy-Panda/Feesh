package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import java.util.Timer
import kotlin.concurrent.timerTask

object GuiUtils {
    private var cachedIsInInventoryOrChat: Boolean = false
    private var timer: Timer? = null

    fun init() {
        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = Timer()

        val task = timerTask {
            updateCache()
        }
        timer?.scheduleAtFixedRate(task, 0, 200)
    }

    private fun updateCache() {
        cachedIsInInventoryOrChat = readIsInInventoryOrChat()
    }

    private fun readIsInInventoryOrChat(): Boolean {
        if (!WorldUtils.isInSkyblock()) return false

        val screen = FeeshMod.mc.currentScreen ?: return false
        return screen is InventoryScreen || screen is ChatScreen
    }

    fun isInInventoryOrChat(): Boolean {
        return cachedIsInInventoryOrChat
    }
}
