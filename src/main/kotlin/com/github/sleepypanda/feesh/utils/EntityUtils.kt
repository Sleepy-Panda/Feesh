package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import net.minecraft.world.phys.Vec3
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.projectile.FishingHook
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
     * Get the squared distance between two points in the world. Used for faster distance checks.
     * @param xa The x coordinate of the first point.
     * @param ya The y coordinate of the first point.
     * @param za The z coordinate of the first point.
     * @param xb The x coordinate of the second point.
     * @param yb The y coordinate of the second point.
     * @param zb The z coordinate of the second point.
     * @returns {Double} The squared distance between the two points.
     */
    fun getDistanceSqr(xa: Double, ya: Double, za: Double, xb: Double, yb: Double, zb: Double): Double {
        val dx = xb - xa
        val dy = yb - ya
        val dz = zb - za
        return dx * dx + dy * dy + dz * dz
    }

    /**
     * Get the player's fishing hook if it is active.
     * @returns The player's fishing hook.
     */
    fun getPlayersFishingHookEntity(): FishingHook? {
        val player = FeeshMod.mc.player ?: return null
        val world = FeeshMod.mc.level ?: return null
        val fishingHook = player.fishing
        return if (fishingHook == null) world.entitiesForRendering().filterIsInstance<FishingHook>().firstOrNull { it.owner == player } 
            else world.getEntity(player.fishing!!.id) as? FishingHook
    }

    /**
     * Get all ArmorStandEntities within the specified range from the specified entity position.
     * @param entityPosition The position to search from.
     * @param distance The maximum distance to search.
     * @returns List of ArmorStand
     */
    fun getArmorStandsInRange(entityPosition: Vec3, distance: Double): List<ArmorStand> {
        val world = FeeshMod.mc.level ?: return emptyList()
        val armorStands = world.entitiesForRendering()
            .filterIsInstance<ArmorStand>()
            .filter { asEntity ->
                EntityUtils.getDistance(asEntity, entityPosition.x, entityPosition.y, entityPosition.z) <= distance
            }

        return armorStands
    }

    /**
     * Get all ArmorStandEntities with the specified unformattedname within the specified range from the specified position.
     * @param entityPosition The position to search from.
     * @param distance The maximum distance to search.
     * @param name The unformatted name of the ArmorStand.
     * @param allowContains If true, the entity's custom name can contain the specified name. If false, the entity's custom name must be exactly the specified name.
     * @returns List of ArmorStand
     */
    fun getArmorStandsInRange(entityPosition: Vec3, distance: Double, name: String, allowContains: Boolean = false): List<ArmorStand> {
        val armorStands = getArmorStandsInRange(entityPosition, distance)
            .filter { asEntity ->
                if (allowContains) {
                    asEntity.customName.getUnformattedString().contains(name)
                } else {
                    asEntity.customName.getUnformattedString() == name
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
        val world = FeeshMod.mc.level ?: return null
        return world.getEntity(entityId)
    }

    data class SeaCreatureParsedNametagInfo(
        val mcEntityId: Int,
        val baseMobName: String,
        val currentHpNumber: Double,
        val maxHpNumber: Double,
        val renderPos: Triple<Double, Double, Double>,
        val formattedHp: String,
        val isCorrupted: Boolean,
        val hasShuriken: Boolean,
    )

    /**
     * Parses an ArmorStand nametag and returns a SeaCreatureParsedNametagInfo object.
     * @param entity The ArmorStand to parse.
     * @param includedSeaCreatureNames The list of sea creatures names to include into result (using "contains" check). If null, no filtering is done and all nametags returned.
     * @returns The SeaCreatureParsedNametagInfo object if the nametag is a valid sea creature nametag, null otherwise.
     */
    fun parseSeaCreatureNametag(entity: ArmorStand, includedSeaCreatureNames: List<String>? = null): SeaCreatureParsedNametagInfo? {
        val customName = entity.customName ?: return null
        return parseSeaCreatureNametag(
            entityId = entity.id,
            customNameFormatted = customName.getFormattedString(),
            customNameUnformatted = customName.getUnformattedString(),
            x = entity.x,
            y = entity.y,
            z = entity.z,
            includedSeaCreatureNames = includedSeaCreatureNames,
        )
    }

    fun parseSeaCreatureNametag(
        entityId: Int,
        customNameFormatted: String,
        customNameUnformatted: String,
        x: Double,
        y: Double,
        z: Double,
        includedSeaCreatureNames: List<String>? = null,
    ): SeaCreatureParsedNametagInfo? {
        if (customNameFormatted.isEmpty() ||
            customNameUnformatted.isEmpty() ||
            !customNameUnformatted.contains("[Lv") ||
            (!customNameUnformatted.contains("❤") && !customNameUnformatted.contains(SeaCreatureNames.PUDDLE_JUMPER)) ||
            (includedSeaCreatureNames != null && !includedSeaCreatureNames.any { customNameUnformatted.contains(it) })
        ) return null

        val match = SEA_CREATURE_NAMETAG.matchEntire(customNameFormatted) ?: return null
        val baseMobName = match.groups["name"]?.value
            ?.removeFormatting()
            ?.replace(Regex("[^a-zA-Z\\s'-]"), "")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        return SeaCreatureParsedNametagInfo(
            mcEntityId = entityId,
            baseMobName = baseMobName,
            currentHpNumber = parseHpGroup(match, "currentHp"),
            maxHpNumber = parseHpGroup(match, "maxHp"),
            renderPos = Triple(x, y, z),
            formattedHp = match.groups["hp"]?.value.orEmpty(),
            isCorrupted = match.groups["corrupted"]?.value != null,
            hasShuriken = match.groups["trailingIcons"]?.value?.contains("✯") == true,
        )
    }

    private fun parseHpGroup(match: MatchResult, group: String): Double {
        val value = match.groups[group]?.value?.removeFormatting() ?: return 0.0
        return CommonUtils.parseShortNumber(value)
    }

    // Original nametag samples:
    // §r§8[§r§7Lv1§r§8] §r§9⚓§r§a☮ §r§cSquid§r §r§a100§r§f/§r§a100§r§c❤
	// §r§8[§r§7Lv1§r§8] §r§9⚓§r§a☮ §r§k§5a§r§5Corrupted Squid§r§k§5a§r §r§a300§r§f/§r§a300§r§c❤
    // §8[§7Lv600§8] §c♆§7⚙§d♣ §c§lLord Jawbus§r§r §a69M§f/§a100M§c❤
    // §8[§7Lv600§8] §c♆§7⚙§d♣ §c§lLord Jawbus§r§r §e6.3M§f/§a100M§c❤ §b✯
    // §8[§7Lv600§8] §c♆§7⚙§d♣ §c§lLord Jawbus§r§r §e6.3M§f/§a100M§c❤ §b✯§b✯§aЖ
    // §8[§7Lv250§8] §c♆§e✰§a☮ §cJawbus Follower§r §a3M§f/§a3M§c❤

    private const val FORMAT_CODES = "(?:§.)*"
    private const val NON_OBFUSCATED_FORMAT_CODES = "(?:§[^k])*"
    private const val OBFUSCATED_CHARACTER = "(?:§.)*§k(?:§.)*a(?:§.)*" // §r§k§5a
    private const val MOB_NAME_WORD = "[A-Za-z][A-Za-z'-]*"
    private const val MOB_TYPE_ICON = "${NON_OBFUSCATED_FORMAT_CODES}[^§\\p{L}\\d\\s]" // §r§9⚓ or §r§a☮
    private const val HP_NUMBER = "[0-9]+(?:[.,][0-9]+)*[kKmMbB]?"
    private const val CURRENT_AND_MAX_HP = "${FORMAT_CODES}(?<currentHp>${HP_NUMBER})${FORMAT_CODES}/${FORMAT_CODES}(?<maxHp>${HP_NUMBER})${FORMAT_CODES}\\s*${FORMAT_CODES}❤"
    private val SEA_CREATURE_NAMETAG = Regex(
        "^${FORMAT_CODES}\\[${FORMAT_CODES}Lv\\d+${FORMAT_CODES}]" +
            "(?:\\s*${MOB_TYPE_ICON})*\\s*" +
            "(?:${OBFUSCATED_CHARACTER})?" +
            "(?<corrupted>${FORMAT_CODES}Corrupted${FORMAT_CODES}\\s+)?" +
            "(?<name>${NON_OBFUSCATED_FORMAT_CODES}${MOB_NAME_WORD}(?:${NON_OBFUSCATED_FORMAT_CODES}\\s+${NON_OBFUSCATED_FORMAT_CODES}${MOB_NAME_WORD})*)${NON_OBFUSCATED_FORMAT_CODES}" +
            "(?:${OBFUSCATED_CHARACTER})?" +
            "(?:\\s+(?<hp>${CURRENT_AND_MAX_HP}))?" +
            "(?<trailingIcons>(?:\\s*${FORMAT_CODES}[^\\s])*)" +
            "${FORMAT_CODES}\\s*$"
    )
}