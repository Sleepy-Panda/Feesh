package com.github.sleepypanda.feesh.features.personalbests

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.MobyDuckConsumedEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import java.util.Date

object MobyDuckPersonalBest {

    fun init() {
        EventBus.subscribe(MobyDuckConsumedEvent::class, ::onMobyDuckConsumed)
    }

    private fun onMobyDuckConsumed(@Suppress("UNUSED_PARAMETER") event: MobyDuckConsumedEvent) {
        if (!WorldUtils.isInSkyblock()) return

        updatePersonalBest()
    }

    private fun updatePersonalBest() {
        CommonUtils.runWithCatching("Failed to update Moby-Duck PB") {
            val personalBestEntry = PersistentDataManager.feeshData.personalBest.totalMobyDucksConsumed
            personalBestEntry.amount++
            personalBestEntry.at = Date()
            PersistentDataManager.saveFeeshDataToFileAsync()
        }
    }
}
