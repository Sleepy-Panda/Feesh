package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import net.minecraft.client.resources.sounds.SoundInstance

object WormholeGoneAlert {
    val PATTERN = Regex("^Your Wormhole closed up\\.\\.\\.$")

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
    }

    @JvmStatic
    fun shouldLog(sound: SoundInstance?) {
        if (!WorldUtils.isInSkyblock() || (WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL && WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE)) return
        val s = sound ?: return
        //#if MC >= 1.21.11
        //$$ val soundId = s.identifier
        //#else
        val soundId = s.location
        //#endif
        if (soundId.namespace != "minecraft" || soundId.path != "block.portal.travel") return

        // Todo check for froggles
        ChatUtils.sendLocalChat(
            "${CommonUtils.getFormattedLocation(s.x, s.y, s.z)} | ${s.source.name} | Wormhole",
            true
        )
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock() || (WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL && WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) || !Alerts.alertOnWormholeGone) return
        if (!PATTERN.matches(event.unformattedText)) return

        CommonUtils.showTitle("${LIGHT_PURPLE}Wormhole ${RED}is gone")
        SoundUtils.playSound()
    }
}
