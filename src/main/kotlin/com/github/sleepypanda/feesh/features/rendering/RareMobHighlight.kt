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
import com.github.sleepypanda.feesh.utils.enums.ColorCodes
import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.JvmField
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.mob.GuardianEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.mob.SlimeEntity

// TODO Fire Eel, other debug creatures to remove
// TODO Do not parse all nametags after world swap
// TODO Observable setting to cleanup on disable
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

                val debugEntities = listOf("Baby Magma Slug", "Magma Slug", "Moogma", "Taurus", "Lava Leech", "Pyroclastic Worm", "Lava Flame", "Fire Eel");
                val extraEntities = listOf("Wiki Tiki Laser Totem", "Jawbus Follower");

                if (info != null || cleanName in extraEntities || cleanName in debugEntities) {
                    FeeshMod.LOGGER.info(cleanName + " found / ${entity.customName?.string}")
                    val mobEntity = entity.entityWorld.getEntityById(entity.id - 1) as? LivingEntity
                    FeeshMod.LOGGER.info("Mob entity by ID: ${mobEntity?.id} ${mobEntity?.type?.name} ${mobEntity?.isAlive}")
                    val vehicle = mobEntity?.vehicle
                    FeeshMod.LOGGER.info("Vehicle: ${vehicle?.id} ${vehicle?.type?.name}")
                    val passengers = mobEntity?.firstPassenger
                    FeeshMod.LOGGER.info("Passengers: ${passengers?.id} ${passengers?.type?.name} ${passengers?.customName?.string}")
                    if (cleanName.contains("Titanoboa")) {
                        for (i in 1..48) {
                            val testEntity = entity.entityWorld.getEntityById(entity.id - i)
                            FeeshMod.LOGGER.info("Titanoboa tail checked at ${i} times, entity at ${entity.id - i} is ${testEntity?.type?.name}")
                        }
                    }
                    if (cleanName.contains("Fire Eel")) {
                        for (i in 1..20) {
                            val testEntity = entity.entityWorld.getEntityById(entity.id - i)
                            FeeshMod.LOGGER.info("Fire Eel tail checked at ${i} times, entity at ${entity.id - i} is ${testEntity?.type?.name}")
                        }
                    }
                    if (cleanName.contains("Wiki Tiki")) {
                        for (i in 1..10) {
                            val testEntity = entity.entityWorld.getEntityById(entity.id - i)
                            FeeshMod.LOGGER.info("Wiki TIki bottom checked at ${i} times, entity at ${entity.id - i} is ${testEntity?.type?.name}")
                        }
                    }
                    if (cleanName.contains("Ragnarok")) {
                        val testEntity = entity.entityWorld.getEntityById(entity.id - 2)
                        FeeshMod.LOGGER.info("Ragnarok entity - 2 is ${entity.id - 2}, ${testEntity?.type?.name}")
                    }
                }

                if (info == null && cleanName !in extraEntities && cleanName !in debugEntities) return@execute

                if (cleanName.contains("Jawbus Follower")) FeeshMod.LOGGER.info("Follower checks started")

                val shift = when {
                    cleanName.contains("Fire Eel") -> 11 // TODO Remove, It has 6 visible segments but 12 entities (don't ask me)
                    cleanName.contains("Titanoboa") -> 43 // It consists of chain of mixed slimes and armor stands, and zombie on 45th position
                    else -> 1
                }

                var mobEntity = entity.entityWorld.getEntityById(entity.id - shift) as? LivingEntity ?: return@execute
                if (cleanName.contains("Jawbus Follower") && mobEntity is SlimeEntity) { // Fire Eel
                    mobEntity = entity.entityWorld.getEntityById(entity.id - 11) as? LivingEntity ?: return@execute
                }

                if (!mobEntity.isAlive) return@execute
                if (mobEntity is PlayerEntity && (mobEntity.uuid.version() == 4 || mobEntity.uuid.version() == 1)) return@execute



                if (cleanName.contains("Jawbus Follower")) FeeshMod.LOGGER.info("Follower still here")
                // TODO: Add rider for ragnarok
                // TODO: Highlight full wiki tiki
       
                val color = when {
                    info?.rarityColorCode == ColorCodes.COMMON.code -> 0xFFFFFF
                    info?.rarityColorCode == ColorCodes.UNCOMMON.code -> 0x55FF55
                    info?.rarityColorCode == ColorCodes.RARE.code -> 0x8AA1FF
                    info?.rarityColorCode == ColorCodes.EPIC.code -> 0xAA00AA
                    info?.rarityColorCode == ColorCodes.LEGENDARY.code -> 0xFFAA00
                    info?.rarityColorCode == ColorCodes.MYTHIC.code -> 0xFF55FF
                    info?.rarityColorCode == ColorCodes.DIVINE.code -> 0x55FFFF
                    info?.rarityColorCode == ColorCodes.SPECIAL.code -> 0xFF5555
                    cleanName in extraEntities -> 0xF01616
                    cleanName in debugEntities -> 0xF01616 // TODO Remove
                    else -> 0x00FFFF
                }

                applyGlow(mobEntity, color)

                if (mobEntity.vehicle != null && mobEntity.vehicle is LivingEntity) {
                    applyGlow(mobEntity.vehicle as LivingEntity, color)
                }
            }
        }
    }

    private fun onClientTick(event: ClientTickEvent) {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
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

    private fun applyGlow(target: LivingEntity, color: Int) {
        highlightedEntities[target.id] = color
    }

    private fun worldChange(event: WorldChangedEvent) {
        if (highlightedEntities.isNotEmpty()) highlightedEntities.clear()
    }
}
