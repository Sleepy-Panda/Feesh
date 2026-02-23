package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.ArmorStandLoadedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.WorldUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.JvmField
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.client.MinecraftClient

object RareMobHighlight {
    @JvmField
    val highlightedEntities = mutableMapOf<Int, Int>()
    private val modScope = CoroutineScope(Dispatchers.Default)

    fun init() {
        EventBus.subscribe(WorldChangedEvent::class, ::worldChange)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(ArmorStandLoadedEvent::class, ::onArmorStandLoaded)
    }

    private fun onArmorStandLoaded(event: ArmorStandLoadedEvent) {
        if (!WorldRendering.highlightSeaCreatures || !WorldUtils.isInFishingWorld()) return
        val entity = event.entity

        modScope.launch {
            delay(500)
            MinecraftClient.getInstance().execute {
                val plainName = entity.customName?.string?.removeFormatting()

                if (plainName?.contains("[Lv") == true && plainName.contains("❤")) {
                    val info = SeaCreatures.allSeaCreatures.find { creatureInfo ->
                        val cleanDbName = creatureInfo.name.removeFormatting()
                        plainName.contains(cleanDbName, true)
                    }

                    if (info != null && info.isRare) {
                        val mobEntity = entity.entityWorld.getEntityById(entity.id - 1) as? LivingEntity
                            ?: entity.entityWorld.getOtherEntities(
                                entity,
                                entity.boundingBox.expand(1.0, 2.0, 1.0)
                            ) {
                                it is LivingEntity && it !is ArmorStandEntity
                            }.firstOrNull() as? LivingEntity

                        if (mobEntity != null) {
                            applyGlow(mobEntity, info.name)
                        }
                    }
                }
            }
        }
    }

    private fun onClientTick(event: ClientTickEvent) {
        if (!WorldUtils.isInFishingWorld()) return
        val world = event.mc.world ?: return

        if (!WorldRendering.highlightSeaCreatures) {
            if (highlightedEntities.isNotEmpty()) {
                highlightedEntities.forEach { (id, _) ->
                    world.getEntityById(id)?.isGlowing = false
                }
                highlightedEntities.clear()
            }
            return
        }

        if (highlightedEntities.isNotEmpty()) {
            highlightedEntities.keys.removeIf { id ->
                val entity = world.getEntityById(id)
                entity == null || !entity.isAlive
            }
        }
    }

    //can use cleanName for later ;)
    private fun applyGlow(target: LivingEntity, cleanName: String) {
        highlightedEntities[target.id] = 0x00FFFF
        target.isGlowing = true
    }

    private fun worldChange(event: WorldChangedEvent) {
        if (highlightedEntities.isNotEmpty()) highlightedEntities.clear()
    }
}
