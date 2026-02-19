package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.WorldUtils

object LavaRendering {
    @JvmStatic
    fun shouldReplaceLavaWithWater(): Boolean {
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return false
        if (!WorldRendering.replaceLavaWithWater) return false

        return true
    }

    @JvmStatic
    fun shouldTintLava(): Boolean {
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return false
        if (!WorldRendering.replaceLavaWithTinted) return false

        return true
    }

    @JvmStatic
    fun getLavaTintColor(): Int {
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return 0
        if (!WorldRendering.replaceLavaWithTinted) return 0

        return WorldRendering.lavaTintColor
    }

    /**
     * Schedules chunks rebuild around the player so lava/tinted water updates.
     */
    @JvmStatic
    fun reloadRenderedLava() {
        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return

        scheduleChunkRebuildAroundPlayer()
    }

    @JvmStatic
    private fun scheduleChunkRebuildAroundPlayer() {
        FeeshMod.mc.execute {
            if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) return@execute
            val world = FeeshMod.mc.world ?: return@execute
            val player = FeeshMod.mc.player ?: return@execute
            val blockRadius = FeeshMod.mc.options.viewDistance.value * 16
            val x = player.blockPos.x
            val y = player.blockPos.y
            val z = player.blockPos.z
            val minX = x - blockRadius
            val minY = world.bottomY.coerceAtLeast(y - blockRadius)
            val minZ = z - blockRadius
            val maxX = x + blockRadius
            val maxY = (world.bottomY + world.height - 1).coerceAtMost(y + blockRadius)
            val maxZ = z + blockRadius
            FeeshMod.mc.worldRenderer.scheduleBlockRenders(minX, minY, minZ, maxX, maxY, maxZ)
        }
    }
}
