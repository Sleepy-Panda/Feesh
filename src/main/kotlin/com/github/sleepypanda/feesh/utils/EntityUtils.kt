package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import net.minecraft.util.math.Vec3d
import kotlin.math.sqrt

object EntityUtils {
    /*
     * Get the distance between two entities.
     * @param entityA The first entity.
     * @param entityB The second entity.
     * @returns {Double} The distance between the two entities.
     */
    fun getDistance(entityA: Entity, entityB: Entity): Double {
        return getDistance(entityA.x, entityA.y, entityA.z, entityB.x, entityB.y, entityB.z)
    }

    /**
     * Get the distance between an entity and a point in the world.
     * @param entityA The entity.
     * @param x The x coordinate of the point.
     * @param y The y coordinate of the point.
     * @param z The z coordinate of the point.
     * @returns {Double} The distance between the entity and the point.
     */
    fun getDistance(entityA: Entity, x: Double, y: Double, z: Double): Double {
        return getDistance(entityA.x, entityA.y, entityA.z, x, y, z)
    }

    /**
     * Get the distance between two points in the world.
     * @param xa The x coordinate of the first point.
     * @param ya The y coordinate of the first point.
     * @param za The z coordinate of the first point.
     * @param xb The x coordinate of the second point.
     * @param yb The y coordinate of the second point.
     * @param zb The z coordinate of the second point.
     * @returns {Double} The distance between the two points.
     */
    fun getDistance(xa: Double, ya: Double, za: Double, xb: Double, yb: Double, zb: Double): Double {
        val dx = xb - xa
        val dy = yb - ya
        val dz = zb - za
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    /**
     * Get the player's fishing hook if it is active.
     * @returns The player's fishing hook.
     */
    fun getPlayersFishingHookEntity(): FishingBobberEntity? {
        val player = FeeshMod.mc.player ?: return null
        val world = FeeshMod.mc.world ?: return null
        return world.entities.filterIsInstance<FishingBobberEntity>().firstOrNull { it.owner == player }
    }

    /**
     * Get all ArmorStandEntities within the specified range from the specified entity position.
     * @param entityPosition The position to search from.
     * @param distance The maximum distance to search.
     * @returns List of ArmorStandEntity
     */
    fun getArmorStandsInRange(entityPosition: Vec3d, distance: Double): List<ArmorStandEntity> {
        val world = FeeshMod.mc.world ?: return emptyList()
        val armorStands = world.entities
            .filterIsInstance<ArmorStandEntity>()
            .filter { asEntity ->
                EntityUtils.getDistance(asEntity, entityPosition.x, entityPosition.y, entityPosition.z) <= distance
            }

        return armorStands
    }

    /**
     * Get all ArmorStandEntities with the specified unformattedname within the specified range from the specified position.
     * @param entityPosition The position to search from.
     * @param distance The maximum distance to search.
     * @param name The unformatted name of the ArmorStandEntity.
     * @param allowContains If true, the entity's custom name can contain the specified name. If false, the entity's custom name must be exactly the specified name.
     * @returns List of ArmorStandEntity
     */
    fun getArmorStandsInRange(entityPosition: Vec3d, distance: Double, name: String, allowContains: Boolean = false): List<ArmorStandEntity> {
        val armorStands = getArmorStandsInRange(entityPosition, distance)
            .filter { asEntity ->
                if (allowContains) {
                    asEntity.customName?.string?.contains(name) == true
                } else {
                    asEntity.customName?.string == name
                }
            }

        return armorStands
    }

    /**
     * Get an entity by its numeric ID from the world.
     * @param entityId The numeric ID of the entity.
     * @return The entity if found, null otherwise.
     */
    fun getMcEntityById(entityId: Int): Entity? {
        val world = FeeshMod.mc.world ?: return null
        return world.getEntityById(entityId)
    }

    data class SeaCreatureParsedNametagInfo(
        val mcEntityId: Int,
        val baseMobName: String,
        val shortNametag: String,
        val currentHpNumber: Double,
        val renderPos: Triple<Double, Double, Double>
    )

    // Original nametag samples:
    // §r§8[§r§7Lv1§r§8] §r§9⚓§r§a☮ §r§cSquid§r §r§a100§r§f/§r§a100§r§c❤
	// §r§8[§r§7Lv1§r§8] §r§9⚓§r§a☮ §r§k§5a§r§5Corrupted Squid§r§k§5a§r §r§a300§r§f/§r§a300§r§c❤
    // §e﴾ §8[§7Lv600§8] §c♆§7⚙§d♣ §c§lLord Jawbus§r§r §a69M§f/§a100M§c❤ §e﴿
    // §e﴾ §8[§7Lv600§8] §c♆§7⚙§d♣ §c§lLord Jawbus§r§r §e6.3M§f/§a100M§c❤ §e﴿ §b✯
    // §8[§7Lv250§8] §c♆§e✰§a☮ §cJawbus Follower§r §a3M§f/§a3M§c❤
	// MC 1.21.5: §r§8[§r§7Lv150§r§8] §r§9⚓§r§f🦴§r§5♃ §r§5§ka§r§5Corrupted The Loch Emperor§r§5§ka§r §r§e521.8k§r§f/§r§a2.4M§r§c❤ §r§b✯
	// MC 1.21.5: §r§8[§r§7Lv14§r§8] §r§2⸙§r§9⚓ §r§5§ka§r§5Corrupted Ent§r§5§ka§r §r§e1§r§f/§r§a75,000§r§c❤
    /**
     * Parses an ArmorStandEntity nametag and returns a SeaCreatureParsedNametagInfo object.
     * @param entity The ArmorStandEntity to parse.
     * @param includedSeaCreatureNames The list of sea creatures names to include into result. If null, no filtering is done and all nametags returned.
     * @returns The SeaCreatureParsedNametagInfo object if the nametag is a valid sea creature nametag, null otherwise.
     */
    fun parseSeaCreatureNametag(entity: ArmorStandEntity, includedSeaCreatureNames: List<String>? = null): SeaCreatureParsedNametagInfo? {
        val customName = entity.customName ?: return null
        val plainName = customName.string.removeFormatting()

        if (plainName.isEmpty() ||
            !plainName.contains("[Lv") ||
            !plainName.contains("]") ||
            !plainName.contains("❤") ||
            (includedSeaCreatureNames != null && !includedSeaCreatureNames.any { plainName.contains(it) })
        ) return null

        val formattedText = customName.getFormattedString()
        var name = formattedText
            .replace("§e﴾ ", "")
            .replace(" §e﴿", "")
            .replace("§5§ka", "")
            .trim()

        val shortName = name.split("] ").getOrNull(1)?.replace("Corrupted ", "") ?: return null
        
        val nameParts = shortName.split(" ")
        val namePartIndex = nameParts.indexOfFirst { it.contains("/") }
        val baseMobNameParts = if (namePartIndex >= 0) {
            nameParts.take(namePartIndex)
        } else {
            nameParts
        }
        
        val baseMobName = baseMobNameParts.joinToString(" ")
            .removeFormatting()
            .replace(Regex("[^a-zA-Z\\s'-]"), "")
            .trim()

        val hpPart = shortName.split("§f/").getOrNull(0) ?: return null
        val currentHp = hpPart.split(" ").lastOrNull() ?: return null
        val currentHpNumber = CommonUtils.parseShortNumber(currentHp.removeFormatting())

        return SeaCreatureParsedNametagInfo(
            mcEntityId = entity.id,
            baseMobName = baseMobName, // "Lord Jawbus" or "Squid"
            shortNametag = shortName, // §c♆§7⚙§d♣ §c§lLord Jawbus§r§r §a69M§f/§a100M§c❤ §b✯
            currentHpNumber = currentHpNumber,
            renderPos = Triple(entity.x, entity.y, entity.z)
        )
    }
}