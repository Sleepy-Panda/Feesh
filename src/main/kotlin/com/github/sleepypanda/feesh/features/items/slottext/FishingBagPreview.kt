package com.github.sleepypanda.feesh.features.items.slottext

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.AfterHotbarSlotRenderedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Items
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import java.util.Locale


object FishingBagPreview {
    private const val CACHE_MS = 1000L
    private const val TEXT_COLOR = 0xFF55FFFF.toInt()
    private const val LAST_HOTBAR_INDEX = 8

    // Example: "Bait Remaining: 1,234"
    private val baitRemainingPattern = Regex("(?i)Bait\\s+Remaining:\\s*([\\d,]+)")

    private var lastCacheAt = 0L
    private var cachedSlotText: String? = null

    fun init() {
        EventBus.subscribe(AfterHotbarSlotRenderedEvent::class, ::onAfterHotbarSlot)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        cachedSlotText = null
        lastCacheAt = 0L
    }

    private fun onAfterHotbarSlot(event: AfterHotbarSlotRenderedEvent) {
        if (!Items.showFishingBagPreview) return
        if (event.hotbarSlotIndex != LAST_HOTBAR_INDEX) return

        val mc = FeeshMod.mc
        if (mc.screen != null) return
        if (mc.options.hideGui) return
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

        val stack = event.stack
        if (stack.isEmpty) return
        val name = ItemUtils.getCleanItemName(stack.hoverName.string)
        if (name.isEmpty() || !name.endsWith("Bait")) return

        val now = System.currentTimeMillis()
        if (now - lastCacheAt >= CACHE_MS) {
            lastCacheAt = now
            cachedSlotText = computeCachedText(mc)
        }

        val text = cachedSlotText ?: return

        SlotTextRendererManager.drawHudSlotTextBottomLeft(
            event.drawContext,
            event.textRenderer,
            event.slotX,
            event.slotY,
            text,
            TEXT_COLOR,
            true
        )
    }

    private fun computeCachedText(mc: Minecraft): String? {
        if (mc.screen != null || mc.options.hideGui) return null
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return null

        val stack = mc.player?.inventory?.getItem(LAST_HOTBAR_INDEX) ?: return null
        if (stack.isEmpty) return null

        val name = ItemUtils.getCleanItemName(stack.hoverName.string)
        if (name.isEmpty() || !name.endsWith("Bait")) return null

        val loreLines = stack.get(DataComponents.LORE)?.lines()?.map { it.string.removeFormatting() } ?: return null
        for (line in loreLines) {
            val match = baitRemainingPattern.find(line) ?: continue
            val rawNumber = match.groupValues[1].replace(",", "")
            val amount = rawNumber.toIntOrNull() ?: continue
            ChatUtils.sendLocalChat("Bait remaining: $amount")
            return CommonUtils.toShortNumber(amount.toDouble())
        }
        return null
    }
}
