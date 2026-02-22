package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.constants.SeaCreatures
import kotlin.jvm.JvmField
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity

object RareMobHighlight {
    @JvmField
    val highlightedEntities = mutableMapOf<Int, Int>()

    fun init() {

        ClientEntityEvents.ENTITY_LOAD.register { entity, _ ->
            if (!WorldRendering.highlightSeaCreatures) return@register

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
                                    //Tester:
                                    ChatUtils.sendLocalChat("§4[GLOW] §fGlow applied to §b${info.name}")
                                }
                            }
                        }
                    }
                }.start()
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
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
}