package com.github.sleepypanda.feesh.features.personalbests

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.BlizzardInABottleConsumedEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import java.util.Date

object BlizzardPersonalBest {

    fun init() {
        EventBus.subscribe(BlizzardInABottleConsumedEvent::class, ::onBlizzardStarted)
    }

    private fun onBlizzardStarted(@Suppress("UNUSED_PARAMETER") event: BlizzardInABottleConsumedEvent) {
        if (!WorldUtils.isInSkyblock()) return

        updatePersonalBest()
    }

    private fun updatePersonalBest() {
        CommonUtils.runWithCatching("Failed to check and announce Blizzard PB") {
            val personalBestEntry = PersistentDataManager.feeshData.personalBest.totalBlizzardsStarted
            personalBestEntry.amount++
            personalBestEntry.at = Date()
            PersistentDataManager.saveFeeshDataToFileAsync()
        }
    }
}
