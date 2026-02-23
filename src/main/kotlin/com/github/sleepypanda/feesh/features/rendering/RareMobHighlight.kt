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
import net.minecraft.client.MinecraftClient
import kotlin.jvm.JvmField
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.player.PlayerEntity

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

                    if (info != null && (info.name.contains("Jawbus") || info.name.contains("Wiki Tiki") || info.isRare)) {
                        //finds the head of all entities.
                        var root: net.minecraft.entity.Entity = entity
                        if (info.name.contains("Titanoboa")) root = root.vehicle!!
                        else {
                            while (root.vehicle != null) {
                                root = root.vehicle!!
                            }
                        }

                        // Identify if the stack actually contains a mob (besides the armorStand)
                        val hasMobInStack = hasLivingPassenger(root)

                        if (hasMobInStack) {
                            if (root is LivingEntity && root !is ArmorStandEntity && root !is PlayerEntity) {
                                applyGlow(root, info.name)
                            } else {
                                // mob searcher since it didn't find a mob attached to the armorStand
                                val nearbyMob =
                                    entity.entityWorld.getOtherEntities(entity, entity.boundingBox.expand(1.0)) {
                                        it is LivingEntity && it !is ArmorStandEntity
                                    }.firstOrNull() as? LivingEntity

                                if (nearbyMob != null) {
                                    applyGlow(nearbyMob, info.name)
                                }
                            }
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

    private fun applyGlow(target: LivingEntity, cleanName: String) {
        if (cleanName.contains("Follower") || cleanName.contains("Laser Totem")) highlightedEntities[target.id] =
            0xFF0000
        else highlightedEntities[target.id] = 0x00FFFF
        target.isGlowing = true
    }

    private fun worldChange(event: WorldChangedEvent) {
        if (highlightedEntities.isNotEmpty()) highlightedEntities.clear()
    }

    private fun hasLivingPassenger(base: net.minecraft.entity.Entity): Boolean {
        if (base is LivingEntity && base !is ArmorStandEntity) return true
        return base.passengerList.any { hasLivingPassenger(it) }
    }
}
