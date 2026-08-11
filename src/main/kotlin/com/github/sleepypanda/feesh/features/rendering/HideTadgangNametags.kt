package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ArmorStandCustomNameChangedEvent
import com.github.sleepypanda.feesh.events.models.ArmorStandDespawnedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils

object HideTadgangNametags {

    private val hiddenNametagIds = mutableSetOf<Int>()

    fun init() {
        EventBus.subscribe(ArmorStandCustomNameChangedEvent::class, ::onArmorStandCustomNameChanged)
        EventBus.subscribe(ArmorStandDespawnedEvent::class, ::onArmorStandDespawned)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    @JvmStatic
    fun shouldHide(entityId: Int): Boolean {
        if (!WorldRendering.hideTadgangNametags) return false
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.MOONGLADE_MARSH) return false
        return entityId in hiddenNametagIds
    }

    private fun onArmorStandCustomNameChanged(event: ArmorStandCustomNameChangedEvent) {
        if (!event.isFirstLoaded) return
        if (!WorldRendering.hideTadgangNametags) return
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.MOONGLADE_MARSH) return

        CommonUtils.runWithCatching("Failed to check Tadgang nametag") {
            val name = event.customName.unformatted
            // Tadpoles are level 8, frogs are level 10
            if (!(name.contains("[Lv8]") && name.contains(SeaCreatureNames.TADGANG))) return
            hiddenNametagIds.add(event.entityId)
        }
    }

    private fun onArmorStandDespawned(event: ArmorStandDespawnedEvent) {
        hiddenNametagIds.remove(event.armorStand.id)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        hiddenNametagIds.clear()
    }
}
