package com.github.sleepypanda.feesh.features.sounds

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.resources.ResourceLocation

object MuteReindrakeGifts {
    private const val REINDRAKE_NAME = "Reindrake"
    private const val POST_DEATH_GIFTS_PICKUP_MS = 5_000L // Gifts are still dropping after Reindrake is defeated
    private val REINDRAKE_DEFEATED_PATTERN = Regex("^DEFEATED! A Reindrake was slain and dropped all its loot!$")
    private val mutedSoundPaths = setOf<String>("minecraft:item.totem.use")

    private var isOwnReindrakeAlive = false
    private var remainingReindrakeDeaths = 0
    private var ownReindrakeDeadAtMs: Long? = null

    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    @JvmStatic
    fun shouldCancel(soundId: ResourceLocation?): Boolean {
        if (!WorldRendering.muteReindrakeGifts) return false
        if (!isInJerryWorkshop()) return false
        if (!isOwnReindrakeAlive && !isWithinPostDeathMute()) return false
        if (soundId == null || soundId.namespace != "minecraft") return false

        val fullSoundPath = "${soundId.namespace}:${soundId.path}"
        return mutedSoundPaths.contains(fullSoundPath)
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        CommonUtils.runWithCatching("Failed to handle own Reindrake catch") {
            if (!isInJerryWorkshop()) return@onSeaCreature
            if (event.seaCreatureName != REINDRAKE_NAME) return@onSeaCreature

            isOwnReindrakeAlive = true
            remainingReindrakeDeaths = if (event.isDoubleHook) 2 else 1
            ownReindrakeDeadAtMs = null
        }
    }

    private fun onChat(event: ChatEvent) {
        CommonUtils.runWithCatching("Failed to handle Reindrake death chat") {
            if (!isOwnReindrakeAlive || !isInJerryWorkshop()) return@onChat
            if (!REINDRAKE_DEFEATED_PATTERN.matches(event.unformattedText)) return@onChat
    
            ownReindrakeDeadAtMs = System.currentTimeMillis()
    
            if (remainingReindrakeDeaths > 0) {
                remainingReindrakeDeaths--
            }
    
            if (remainingReindrakeDeaths <= 0) {
                isOwnReindrakeAlive = false
                remainingReindrakeDeaths = 0
            }
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        ownReindrakeDeadAtMs = null
        isOwnReindrakeAlive = false
        remainingReindrakeDeaths = 0
    }

    private fun isWithinPostDeathMute(): Boolean {
        val deadAt = ownReindrakeDeadAtMs ?: return false
        return System.currentTimeMillis() - deadAt <= POST_DEATH_GIFTS_PICKUP_MS
    }

    private fun isInJerryWorkshop(): Boolean {
        return WorldUtils.isInSkyblock() && WorldUtils.getWorldName() == WorldUtils.JERRY_WORKSHOP
    }
}
