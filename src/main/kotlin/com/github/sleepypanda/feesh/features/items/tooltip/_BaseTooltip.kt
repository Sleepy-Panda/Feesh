package com.github.sleepypanda.feesh.features.items.tooltip

import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.ItemUtils
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

/**
 * Base class for item tooltip features that append or modify tooltip lines on hover.
 * - Registers every tooltip feature in [TooltipManager].
 * - Optionally caches values parsed from item NBT.
 */
abstract class BaseTooltip {

    private data class TooltipCacheEntry<T>(
        val customDataHash: Int,
        val value: T?,
    )

    private val cache = mutableMapOf<String, TooltipCacheEntry<*>>() // item stack identifier -> cached value for tooltip rendering

    init {
        TooltipManager.register(this)
    }

    abstract fun isEnabled(): Boolean

    abstract fun modifyTooltip(stack: ItemStack, lines: MutableList<Component>)

    protected fun <T> getCachedValue(
        stack: ItemStack,
        getValueFn: (ItemStack) -> T?,
    ): T? {
        val key = getStackIdentifier(stack)
        val customDataHash = getCustomDataHash(stack)

        val cached = cache[key] as? TooltipCacheEntry<T>
        if (cached != null && cached.customDataHash == customDataHash) {
            return cached.value
        }

        val value = getValueFn(stack)
        cache[key] = TooltipCacheEntry(customDataHash, value)
        return value
    }

    protected fun getStackIdentifier(stack: ItemStack): String {
        return (stack.hoverName.getUnformattedString()) + System.identityHashCode(stack)
    }

    protected fun getCustomDataHash(stack: ItemStack): Int {
        val customData = ItemUtils.getCustomData(stack) ?: return 0
        return customData.copyTag()?.hashCode() ?: 0
    }

    fun clearCache() {
        if (cache.isNotEmpty()) cache.clear()
    }
}
