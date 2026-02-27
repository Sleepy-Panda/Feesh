package com.github.sleepypanda.feesh.features.rendering

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.ArmorStandLoadedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.WorldRendering
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.JvmField
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.mob.SlimeEntity

// TODO Fire Eel
// TODO Do not parse all nametags after world swap
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
        if (!WorldRendering.highlightSeaCreatures || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        val entity = event.entity

        modScope.launch {
            delay(250)
            MinecraftClient.getInstance().execute {
                // TODO: Pass parameter for allowed sc names to not parse all nametags?
                val cleanName = EntityUtils.parseSeaCreatureNametag(entity)?.baseMobName ?: return@execute
                val info = SeaCreatures.rareSeaCreatures.find { it.name == cleanName }

                // Titanoboa
                //[21:04:00] [Render thread/INFO]: Mob entity by ID: 102114 translation{key='entity.minecraft.slime', args=[]}
                //[21:04:00] [Render thread/INFO]: Vehicle: null null
                //[21:04:00] [Render thread/INFO]: Passengers: null null null

                // Alligator: Mob entity by ID: 164758 translation{key='entity.minecraft.player', args=[]}

                // Eel: tail highlighted
                //[23:11:04] [Render thread/INFO]: Mob entity by ID: 281131 translation{key='entity.minecraft.slime', args=[]}
                //[23:11:04] [Render thread/INFO]: Vehicle: null null
                //[23:11:04] [Render thread/INFO]: Passengers: null null null
                if (info != null || cleanName.contains("Wiki Tiki Laser Totem") || cleanName.contains("Jawbus Follower") || cleanName.contains("Fire Eel")) {
                    FeeshMod.LOGGER.info(cleanName + " found")
                    val mobEntity = entity.entityWorld.getEntityById(entity.id - 1) as? LivingEntity
                    FeeshMod.LOGGER.info("Mob entity by ID: ${mobEntity?.id} ${mobEntity?.type?.name}")
                    val vehicle = mobEntity?.vehicle
                    FeeshMod.LOGGER.info("Vehicle: ${vehicle?.id} ${vehicle?.type?.name}")
                    val passengers = mobEntity?.firstPassenger
                    FeeshMod.LOGGER.info("Passengers: ${passengers?.id} ${passengers?.type?.name} ${passengers?.customName?.string}")
                    var other = entity.entityWorld.getOtherEntities(
                            entity,
                            entity.boundingBox.expand(1.0, 2.0, 1.0)
                        ) {
                            it is LivingEntity
                        }.firstOrNull() as? LivingEntity
                    FeeshMod.LOGGER.info("Other: ${other?.id} ${other?.type?.name} ${other?.customName?.string}")
                    if (cleanName.contains("Titanoboa")) {
                        for (i in 1..50) {
                            val testEntity = entity.entityWorld.getEntityById(entity.id - i)
                            FeeshMod.LOGGER.info("Titanoboa tail checked at ${i} times, entity at ${entity.id - i} is ${testEntity?.type?.name}")

                        }
                    }
                    if (cleanName.contains("Ragnarok")) {
                        val testEntity = entity.entityWorld.getEntityById(entity.id - 2)
                        FeeshMod.LOGGER.info("Ragnarok entity - 2 is ${entity.id - 2}, ${testEntity?.type?.name}")
                    }
                    if (cleanName.contains("Magma Slug") || cleanName.contains("Moogma" ) || cleanName.contains("Taurus" ) || cleanName.contains("Lava Leech") || cleanName.contains("Pyroclastic Worm") || cleanName.contains("Lava Flame") || cleanName.contains("Fire Eel")) {
                        val testEntity = entity.entityWorld.getEntityById(entity.id - 1)
                        FeeshMod.LOGGER.info("Followers entity is ${entity.id - 1}, ${testEntity?.type?.name}")
                    }
                }

                if (info != null || cleanName.contains("Wiki Tiki Laser Totem") || cleanName.contains("Jawbus Follower") || cleanName.contains("Fire Eel")) {
                    val shift = when {
                        cleanName.contains("Fire Eel") -> 12 // It has 6 visible segments but 12 entities (don't ask me)
                        cleanName.contains("Titanoboa") -> 43 // It consists of chain of mixed slimes and armor stands (don't ask me again)
                        else -> 1
                    }
                    var mobEntity = entity.entityWorld.getEntityById(entity.id - shift) as? LivingEntity ?: return@execute
                    if (!mobEntity.isAlive) return@execute

                    if (cleanName.contains("Jawbus Follower") && mobEntity is SlimeEntity) {
                        // Find head for Fire Eel - TODO check if SlimeEntity fits to detect Fire Eel follower
                        mobEntity = entity.entityWorld.getEntityById(entity.id - 12) as? LivingEntity ?: return@execute
                    }

                    // TODO: Add rider for ragnarok
                    if (mobEntity is PlayerEntity) FeeshMod.LOGGER.info("Player ${mobEntity.uuid.version()} ${mobEntity.name.string}")
                    if (mobEntity is PlayerEntity && (mobEntity.uuid.version() == 4 || mobEntity.uuid.version() == 1)) return@execute

                    applyGlow(mobEntity, info?.name ?: cleanName)
                }
            }
        }
    }

    private fun onClientTick(event: ClientTickEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        val world = event.mc.world ?: return

        if (!WorldRendering.highlightSeaCreatures) { // TODO Observable setting
            if (highlightedEntities.isNotEmpty()) {
                highlightedEntities.forEach { (id, _) -> // TODO enough to clear list?
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
        if (cleanName.contains("Jawbus Follower") || cleanName.contains("Wiki Tiki Laser Totem")) highlightedEntities[target.id] = 0xFF0000
        else highlightedEntities[target.id] = 0x00FFFF
    }

    private fun worldChange(event: WorldChangedEvent) {
        if (highlightedEntities.isNotEmpty()) highlightedEntities.clear()
    }
}
