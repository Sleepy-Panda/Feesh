package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.features.overlays.EfficiencyTracker
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
        EfficiencyTracker.pause()
        FishingProfitTracker.pauseFishingProfitTracker()
        MagmaCoreFishingTracker.pauseMagmaCoreFishingTracker()
    }
}