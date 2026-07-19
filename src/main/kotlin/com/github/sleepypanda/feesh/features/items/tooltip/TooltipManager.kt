package com.github.sleepypanda.feesh.features.items.tooltip

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.GuiClosedEvent
import com.github.sleepypanda.feesh.events.models.ItemTooltipRenderedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.WorldUtils

/**
 * Coordinates all [BaseTooltip] instances:
 * - keeps all registered tooltip adders;
 * - tracks enabled tooltip features (refreshed on init and settings change);
 * - manages per-screen caches;
 * - invokes enabled tooltip adders on tooltip render event.
 */
object TooltipManager {

    private val tooltipFeatures: MutableList<BaseTooltip> = mutableListOf()
    private val enabledFeatures: MutableList<BaseTooltip> = mutableListOf()

    fun init() {
        EventBus.subscribe(ItemTooltipRenderedEvent::class, ::onItemTooltipRendered)
        EventBus.subscribe(GuiClosedEvent::class, ::onGuiClosed)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        refreshEnabledAdders()
    }

    fun register(tooltip: BaseTooltip) {
        tooltipFeatures.add(tooltip)
    }

    fun refreshEnabledAdders() {
        enabledFeatures.clear()
        enabledFeatures.addAll(tooltipFeatures.filter { it.isEnabled() })
    }

    private fun onItemTooltipRendered(event: ItemTooltipRenderedEvent) {
        if (!WorldUtils.isInSkyblock()) return
        if (event.stack.isEmpty) return
        if (enabledFeatures.isEmpty()) return

        for (tooltipFeature in enabledFeatures) {
            tooltipFeature.modifyTooltip(event.stack, event.lines)
        }
    }

    private fun onGuiClosed(@Suppress("UNUSED_PARAMETER") event: GuiClosedEvent) {
        tooltipFeatures.forEach { it.clearCache() }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        tooltipFeatures.forEach { it.clearCache() }
    }
}
