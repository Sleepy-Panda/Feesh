package com.github.sleepypanda.feesh.features.sounds

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatCancellableEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.resources.Identifier

object MuteReindrakeGifts {
    private const val REINDRAKE_NAME = "Reindrake"
    private const val POST_DEATH_GIFTS_PICKUP_MS = 5_000L // Gifts are still dropping after Reindrake is defeated
    // WOAH! [MVP+] MoonTheSadFisher summoned a Reindrake from the depths!
    private val REINDRAKE_SUMMONED_PATTERN = Regex("^WOAH! (?<playerAndRank>.+?) summoned a Reindrake from the depths!$")
    // WOAH! [MVP+] MoonTheSadFisher summoned TWO Reindrakes from the depths!
    private val REINDRAKE_SUMMONED_DH_PATTERN = Regex("^WOAH! (?<playerAndRank>.+?) summoned TWO Reindrakes from the depths!$")
    private val REINDRAKE_DEFEATED_PATTERN = Regex("^DEFEATED! A Reindrake was slain and dropped all its loot!$")
    private val mutedSoundPaths = setOf<String>("minecraft:item.totem.use")

    private var isReindrakeAlive = false
    private var remainingReindrakeDeaths = 0
    private var reindrakeDeadAtMs: Long? = null

    fun init() {
        EventBus.subscribe(ChatCancellableEvent::class, ::onChat)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    @JvmStatic
    fun shouldCancel(soundId: Identifier?): Boolean {
        if (!WorldRendering.muteReindrakeGifts) return false
        if (!isInJerryWorkshop()) return false
        if (!isReindrakeAlive && !isWithinPostDeathMute()) return false
        if (soundId == null || soundId.namespace != "minecraft") return false

        val fullSoundPath = "${soundId.namespace}:${soundId.path}"
        return mutedSoundPaths.contains(fullSoundPath)
    }

    private fun onChat(event: ChatCancellableEvent) {
        CommonUtils.runWithCatching("Failed to handle Reindrake death chat") {
            onReindrakeSummoned(event)
            onReindrakeDeath(event)
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        reindrakeDeadAtMs = null
        isReindrakeAlive = false
        remainingReindrakeDeaths = 0
    }

    private fun onReindrakeSummoned(event: ChatCancellableEvent) {
        if (!isInJerryWorkshop()) return

        if (REINDRAKE_SUMMONED_PATTERN.matches(event.unformattedText)) {
            isReindrakeAlive = true
            remainingReindrakeDeaths = 1
            reindrakeDeadAtMs = null
        } else if (REINDRAKE_SUMMONED_DH_PATTERN.matches(event.unformattedText)) {
            isReindrakeAlive = true
            remainingReindrakeDeaths = 2
            reindrakeDeadAtMs = null
        }
    }

    private fun onReindrakeDeath(event: ChatCancellableEvent) {
        if (!isReindrakeAlive || !isInJerryWorkshop()) return
        if (!REINDRAKE_DEFEATED_PATTERN.matches(event.unformattedText)) return

        reindrakeDeadAtMs = System.currentTimeMillis()

        if (remainingReindrakeDeaths > 0) {
            remainingReindrakeDeaths--
        }

        if (remainingReindrakeDeaths <= 0) {
            isReindrakeAlive = false
            remainingReindrakeDeaths = 0
        }
    }

    private fun isWithinPostDeathMute(): Boolean {
        val deadAt = reindrakeDeadAtMs ?: return false
        return System.currentTimeMillis() - deadAt <= POST_DEATH_GIFTS_PICKUP_MS
    }

    private fun isInJerryWorkshop(): Boolean {
        return WorldUtils.isInSkyblock() && WorldUtils.getWorldName() == WorldUtils.JERRY_WORKSHOP
    }
}
