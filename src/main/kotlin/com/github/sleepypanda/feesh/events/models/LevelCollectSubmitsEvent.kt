package com.github.sleepypanda.feesh.events.models

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext

/*
 * Called from LevelRenderEvents.COLLECT_SUBMITS, while world features are submitted.
 * @param context The level render context for this frame.
 */
data class LevelCollectSubmitsEvent(val context: LevelRenderContext)
