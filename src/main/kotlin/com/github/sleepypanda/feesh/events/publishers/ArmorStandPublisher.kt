package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.events.models.ArmorStandCustomNameChangedEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.CommonUtils
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.decoration.ArmorStand

object ArmorStandPublisher {

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    @JvmStatic
    fun onArmorStandCustomNameChanged(armorStand: ArmorStand, previousCustomName: Component?) {
        if (!WorldUtils.isInSkyblock()) return
        if (!armorStand.isAlive) return

        CommonUtils.runWithCatching("Failed to publish armor stand custom name changed") {
            val name = armorStand.customName ?: return
            val unformatted = name.getUnformattedString()
            if (unformatted.isEmpty()) return

            val formatted = name.getFormattedString()
            val previousFormatted = previousCustomName.getFormattedString()
            val previousUnformatted = previousCustomName.getUnformattedString()
            EventBus.publish(
                ArmorStandCustomNameChangedEvent(
                    armorStand,
                    armorStand.id,
                    previousCustomName == null || previousUnformatted.isEmpty(),
                    previousFormatted,
                    previousUnformatted,
                    formatted,
                    unformatted,
                )
            )
        }
    }
}
