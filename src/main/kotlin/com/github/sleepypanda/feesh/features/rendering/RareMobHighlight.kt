package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import kotlin.jvm.JvmField
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.WorldUtils
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.block.entity.VaultBlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.world.World

object RareMobHighlight {
    @JvmField
    val highlightedEntities = mutableMapOf<Int, Int>()

    fun init() {
        ClientEntityEvents.ENTITY_LOAD.register { entity, _ ->
            if (!WorldRendering.highlightSeaCreatures || !WorldUtils.isInFishingWorld()) return@register

            if (entity is LivingEntity) {
                Thread {
                    Thread.sleep(500)
                    val plainName = entity.customName?.string?.removeFormatting()

                    if (plainName?.contains("[Lv") == true && plainName.contains("❤")) {
                        val info = SeaCreatures.allSeaCreatures.find { creatureInfo ->
                            val cleanDbName = creatureInfo.name.removeFormatting()
                            plainName.contains(cleanDbName, true)
                        }

                        if (info != null && info.isRare) {
                            net.minecraft.client.MinecraftClient.getInstance().execute {
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
                }.start()
            }
        }
        if (highlightedEntities.isNotEmpty()) EventBus.subscribe(WorldChangedEvent::class, ::worldChange)

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if(!WorldUtils.isInFishingWorld()) return@register
            val world = client.world ?: return@register
            if (!WorldRendering.highlightSeaCreatures) {
                if (highlightedEntities.isNotEmpty()) {
                    highlightedEntities.forEach { (id, _) ->
                        world.getEntityById(id)?.isGlowing = false
                    }
                    highlightedEntities.clear()
                }
                return@register
            }
            if (highlightedEntities.isNotEmpty()) {
                highlightedEntities.keys.removeIf { id ->
                    val entity = world.getEntityById(id)
                    entity == null || !entity.isAlive
                }
            }
        }
    }

    //can use cleanName for later ;)
    private fun applyGlow(target: LivingEntity, cleanName: String) {
        highlightedEntities[target.id] = 0x00FFFF
        target.isGlowing = true
    }

    private fun worldChange(event: WorldChangedEvent) {
        highlightedEntities.clear()
    }
}
