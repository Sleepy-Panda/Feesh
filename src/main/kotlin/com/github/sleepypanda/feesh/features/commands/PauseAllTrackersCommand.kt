package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.features.overlays.SeaCreaturesPerHourTracker
import com.github.sleepypanda.feesh.features.overlays.FishingProfitTracker
import com.github.sleepypanda.feesh.features.overlays.MagmaCoreFishingTracker

object PauseAllTrackersCommand {
    const val COMMAND_NAME = "feeshPauseAllTrackers"

    fun init() {
        RegisterUtils.command(COMMAND_NAME) {
            pauseAllTrackers()
        }
    }

    fun triggerPauseAllTrackers() {
        pauseAllTrackers()
    }

    private fun pauseAllTrackers() {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        SeaCreaturesPerHourTracker.pause()
        FishingProfitTracker.pauseFishingProfitTracker()
        MagmaCoreFishingTracker.pauseMagmaCoreFishingTracker()
    }
}