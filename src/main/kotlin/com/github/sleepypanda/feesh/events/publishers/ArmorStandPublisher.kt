package com.github.sleepypanda.feesh.events.publishers

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.events.models.ArmorStandCustomNameChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.CommonUtils
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.decoration.ArmorStand

object ArmorStandPublisher {

    fun init() {

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
            val isFirstLoaded = previousCustomName == null || previousUnformatted.isEmpty()
            
            EventBus.publish(
                ArmorStandCustomNameChangedEvent(
                    entityId = armorStand.id,
                    isFirstLoaded = isFirstLoaded,
                    previousCustomName = ArmorStandCustomNameChangedEvent.CustomName(
                        formatted = previousFormatted,
                        unformatted = previousUnformatted,
                    ),
                    customName = ArmorStandCustomNameChangedEvent.CustomName(
                        formatted = formatted,
                        unformatted = unformatted,
                    ),
                    position = ArmorStandCustomNameChangedEvent.Position(
                        x = armorStand.x,
                        y = armorStand.y,
                        z = armorStand.z,
                    ),
                )
            )
        }
    }
}
