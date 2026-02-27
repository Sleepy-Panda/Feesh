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
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.mob.MagmaCubeEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.mob.SlimeEntity

// TODO Fire Eel, other debug creatures to remove
// TODO Do not parse nametags after world swap
// TODO Observable setting to cleanup on disable
// TODO: Slugs and Pyro not highlighted when they are followers, highlighted when normal mobs
// TODO: Highlight full wiki tiki


object RareMobHighlight {
    @JvmField
    val highlightedEntities = mutableMapOf<Int, Int>()
    private val modScope = CoroutineScope(Dispatchers.Default)

    private val debugEntities = listOf("Baby Magma Slug", "Magma Slug", "Moogma", "Taurus", "Lava Leech", "Pyroclastic Worm", "Lava Flame", "Fire Eel");
    private val extraEntities = listOf("Wiki Tiki Laser Totem", "Jawbus Follower");

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
                val mobNames = SeaCreatures.rareSeaCreatures.map { it.name } + debugEntities + extraEntities
                val cleanName = EntityUtils.parseSeaCreatureNametag(entity, mobNames)?.baseMobName ?: return@execute
                val rareScInfo = SeaCreatures.rareSeaCreatures.find { it.name == cleanName }

                if (rareScInfo != null || cleanName in extraEntities || cleanName in debugEntities) {
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

                if (rareScInfo == null && cleanName !in extraEntities && cleanName !in debugEntities) return@execute

                if (cleanName.contains("Jawbus Follower")) FeeshMod.LOGGER.info("Follower checks started")

                val shift = when {
                    cleanName.contains("Fire Eel") -> 11 // TODO Remove, It has 6 visible segments but 12 entities (don't ask me)
                    cleanName.contains("Titanoboa") -> 43 // It consists of chain of mixed slimes and armor stands, and zombie on 45th position
                    else -> 1
                }

                var mobEntity = entity.entityWorld.getEntityById(entity.id - shift) as? LivingEntity ?: return@execute
                if (cleanName.contains("Jawbus Follower") && mobEntity is SlimeEntity && mobEntity !is MagmaCubeEntity) { // Fire Eel
                    mobEntity = entity.entityWorld.getEntityById(entity.id - 11) as? LivingEntity ?: return@execute // Find its head
                }

                if (!mobEntity.isAlive) return@execute
                if (mobEntity is PlayerEntity && (mobEntity.uuid.version() == 4 || mobEntity.uuid.version() == 1)) return@execute

                if (cleanName.contains("Jawbus Follower")) FeeshMod.LOGGER.info("Follower still here")
       
                val color = when {
                    rareScInfo?.rarityColorCode == ColorCodes.COMMON.code -> 0xFFFFFF
                    rareScInfo?.rarityColorCode == ColorCodes.UNCOMMON.code -> 0x55FF55
                    rareScInfo?.rarityColorCode == ColorCodes.RARE.code -> 0x8AA1FF
                    rareScInfo?.rarityColorCode == ColorCodes.EPIC.code -> 0xAA00AA
                    rareScInfo?.rarityColorCode == ColorCodes.LEGENDARY.code -> 0xFFAA00
                    rareScInfo?.rarityColorCode == ColorCodes.MYTHIC.code -> 0xFF55FF
                    rareScInfo?.rarityColorCode == ColorCodes.DIVINE.code -> 0x55FFFF
                    rareScInfo?.rarityColorCode == ColorCodes.SPECIAL.code -> 0xFF5555
                    cleanName in extraEntities -> 0xF01616
                    cleanName in debugEntities -> 0xF01616 // TODO Remove
                    else -> 0x00FFFF
                }

                applyGlow(mobEntity, color)

                if (mobEntity.vehicle != null && mobEntity.vehicle is LivingEntity) { // The Loch Emperor's guardian, etc
                    applyGlow(mobEntity.vehicle as LivingEntity, color)
                }

                if (mobEntity.firstPassenger != null && mobEntity.firstPassenger is LivingEntity) { // Ragnarok's rider
                    applyGlow(mobEntity.firstPassenger as LivingEntity, color)
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
