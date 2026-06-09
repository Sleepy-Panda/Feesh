package com.github.sleepypanda.feesh.features.items.tooltip

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ItemTooltipRenderedEvent
import com.github.sleepypanda.feesh.events.models.ScreenBeforeInitEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Items
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import net.minecraft.world.item.ItemStack

object ExpertiseTooltip {

    private const val MAX_EXPERTISE_KILLS = 15_000
    private const val DEBUG_CACHE = true

    private data class CacheEntry(
        val customDataHash: Int,
        val kills: Int?,
    )

    private val cache = mutableMapOf<String, CacheEntry>()

    fun init() {
        EventBus.subscribe(ItemTooltipRenderedEvent::class, ::onItemTooltipRendered)
        EventBus.subscribe(ScreenBeforeInitEvent::class, ::onScreenBeforeInit)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onItemTooltipRendered(event: ItemTooltipRenderedEvent) {
        if (!WorldUtils.isInSkyblock() || !Items.showExpertiseKillsTooltip) return
        if (event.stack.isEmpty) return

        val kills = getCachedExpertiseKills(event.stack) ?: return
        val killsFormatted = CommonUtils.formatNumberWithSpaces(kills)
        val maxKillsFormatted = CommonUtils.formatNumberWithSpaces(MAX_EXPERTISE_KILLS)
        val line = if (kills >= MAX_EXPERTISE_KILLS) "${GRAY}Expertise: ${WHITE}${killsFormatted} ${GRAY}kills ${GREEN}(Maxed)"
            else "${GRAY}Expertise: ${WHITE}${killsFormatted} ${GRAY}/ ${WHITE}${maxKillsFormatted} ${GRAY}kills"
        CommonUtils.appendTooltipLine(event.lines, line)
    }

    private fun onScreenBeforeInit(@Suppress("UNUSED_PARAMETER") event: ScreenBeforeInitEvent) {
        clearCache("screen opened")
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        clearCache("world changed")
    }

    private fun getCachedExpertiseKills(stack: ItemStack): Int? {
        val key = getStackIdentifier(stack)
        val customDataHash = getCustomDataHash(stack)
        val cached = cache[key]
        if (cached != null && cached.customDataHash == customDataHash) {
            return cached.kills
        }

        val kills = getExpertiseKills(stack)
        cache[key] = CacheEntry(customDataHash, kills)
        logCacheAdd(stack, kills, customDataHash, cached != null)
        return kills
    }

    private fun getExpertiseKills(stack: ItemStack): Int? {
        if (!ItemUtils.isFishingRod(stack)) return null

        val customData = ItemUtils.getCustomData(stack) ?: return null
        val obj = ItemUtils.customDataToJsonObject(customData) ?: return null
        return obj.get("expertise_kills")
            ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }
            ?.asInt
    }

    private fun getStackIdentifier(stack: ItemStack): String {
        return stack.hoverName.string + System.identityHashCode(stack)
    }

    private fun getCustomDataHash(stack: ItemStack): Int {
        val customData = ItemUtils.getCustomData(stack) ?: return 0
        return customData.copyTag().hashCode()
    }

    private fun clearCache(reason: String) {
        if (cache.isEmpty()) return
        if (DEBUG_CACHE) {
            ChatUtils.sendLocalChat(
                "${GRAY}[ExpertiseTooltip] Cache cleared (${reason}): ${WHITE}${cache.size} ${GRAY}entries",
                addModPrefix = true
            )
        }
        cache.clear()
    }

    private fun logCacheAdd(stack: ItemStack, kills: Int?, customDataHash: Int, isUpdate: Boolean) {
        if (!DEBUG_CACHE) return
        val itemName = ItemUtils.getCleanItemName(stack.hoverName.getFormattedString())
        val action = if (isUpdate) "updated" else "added"
        val cacheKey = getStackIdentifier(stack)
        ChatUtils.sendLocalChat(
            "${GRAY}[ExpertiseTooltip] Cache $action: ${WHITE}$itemName ${GRAY}kills=${kills ?: "n/a"} " +
                "${GRAY}hash=$customDataHash ${GRAY}id=${System.identityHashCode(stack)} ${GRAY}size=${cache.size}",
            addModPrefix = true
        )
    }
}
