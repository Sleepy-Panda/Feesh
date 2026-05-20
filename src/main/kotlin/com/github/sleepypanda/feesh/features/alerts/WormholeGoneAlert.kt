package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.SoundPlayEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.rendering.TextWaypoint
import com.github.sleepypanda.feesh.utils.rendering.WaypointUtils
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.world.entity.EquipmentSlot

object WormholeGoneAlert {
    val PATTERN = Regex("^Your Wormhole closed up\\.\\.\\.$")

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(SoundPlayEvent::class, ::onSoundPlay)
    }

    private fun onSoundPlay(event: SoundPlayEvent) {
        CommonUtils.runWithCatching("Failed to handle sound play event") {

            fun hasFrogglesEquipped(): Boolean {
                val player = FeeshMod.mc.player ?: return false
                val helmet = player.getItemBySlot(EquipmentSlot.HEAD)
                if (helmet == null || helmet.isEmpty) return false
                val helmetName = helmet.hoverName?.string ?: return false
                return (helmetName.contains("Froggles"))
            }

            if (!WorldUtils.isInSkyblock() || (WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL && WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE)) return
            if (event.soundPath != "block.portal.travel") return
            if (!hasFrogglesEquipped()) return

            val location = CommonUtils.getFormattedLocation(event.x, event.y, event.z)
            ChatUtils.sendLocalChat(
                "New Wormhole is located at ${location}.",
                true
            )
            WaypointUtils.add(TextWaypoint(x = event.x.toInt(), y = event.y.toInt(), z = event.z.toInt(), durationSeconds = 30, text = "New Wormhole is located at ${location}."))
        }
    }

    private fun onChat(event: ChatEvent) {

        // TODO: REMOVE
        if (event.unformattedText.contains("TEST WAYPOINT")) {
            val player = FeeshMod.mc.player ?: return
            WaypointUtils.add(TextWaypoint(x = player.getX().toInt(), y = player.getY().toInt(), z = player.getZ().toInt(), durationSeconds = 30, text = "${LIGHT_PURPLE}New Wormhole!"))
        }

        if (!WorldUtils.isInSkyblock() || (WorldUtils.getWorldName() != WorldUtils.LOTUS_ATOLL && WorldUtils.getWorldName() != WorldUtils.CRIMSON_ISLE) || !Alerts.alertOnWormholeGone) return
        if (!PATTERN.matches(event.unformattedText)) return

        CommonUtils.showTitle("${LIGHT_PURPLE}Wormhole ${RED}is gone")
        SoundUtils.playSound()
    }
}
