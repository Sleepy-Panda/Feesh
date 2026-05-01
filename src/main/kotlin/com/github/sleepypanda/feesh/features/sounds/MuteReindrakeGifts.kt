package com.github.sleepypanda.feesh.features.sounds

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.minecraft.resources.ResourceLocation

object MuteReindrakeGifts {
    private const val REINDRAKE_NAME = "Reindrake"
    private val REINDRAKE_DEFEATED_PATTERN = Regex("^DEFEATED! A Reindrake was slain and dropped all its loot!$")
    private val mutedSoundPaths = setOf<String>("minecraft:item.totem.use")
    private val loggedSoundPaths = mutableSetOf<String>()

    private var isOwnReindrakeAlive = false
    private var remainingReindrakeDeaths = 0

    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onOwnSeaCreatureCaught)
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    @JvmStatic
    fun shouldCancel(soundId: ResourceLocation?): Boolean {
        if (!WorldRendering.muteReindrakeGifts) return false
        if (!isInJerryWorkshop() || !isOwnReindrakeAlive) return false
        if (soundId == null || soundId.namespace != "minecraft") return false

        val fullSoundPath = "${soundId.namespace}:${soundId.path}"
        if (loggedSoundPaths.add(fullSoundPath)) {
            ChatUtils.sendLocalChat("${ChatUtils.MOD_PREFIX} Reindrake sound heard: $fullSoundPath")
        }

        return mutedSoundPaths.contains(fullSoundPath)
    }

    private fun onOwnSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        if (!isInJerryWorkshop()) return
        if (event.seaCreatureName != REINDRAKE_NAME) return

        isOwnReindrakeAlive = true
        remainingReindrakeDeaths = if (event.isDoubleHook) 2 else 1
    }

    private fun onChat(event: ChatEvent) {
        if (!isOwnReindrakeAlive || !isInJerryWorkshop()) return
        if (!REINDRAKE_DEFEATED_PATTERN.matches(event.unformattedText)) return

        if (remainingReindrakeDeaths > 0) {
            remainingReindrakeDeaths--
        }

        if (remainingReindrakeDeaths <= 0) {
            clearState()
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        clearState()
    }

    private fun clearState() {
        isOwnReindrakeAlive = false
        remainingReindrakeDeaths = 0
        loggedSoundPaths.clear()
    }

    private fun isInJerryWorkshop(): Boolean {
        return WorldUtils.isInSkyblock() && WorldUtils.getWorldName() == WorldUtils.JERRY_WORKSHOP
    }
}
