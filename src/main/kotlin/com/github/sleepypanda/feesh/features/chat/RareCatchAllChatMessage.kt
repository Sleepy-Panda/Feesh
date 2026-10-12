package com.github.sleepypanda.feesh.features.chat

import com.github.sleepypanda.feesh.settings.models.RareSeaCreatureTypesAllChat
import com.github.sleepypanda.feesh.settings.categories.Chat
import com.github.sleepypanda.feesh.constants.SeaCreatureNames
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.WorldUnloadEvent
import net.minecraft.world.entity.decoration.ArmorStand

object RareCatchAllChatMessage {
    // Chat is handled mid-tick, before the isopod nametag exists. Skip this tick's END and search on the next one.
    // A double hook sends two catch messages. One share is enough — coords of a single isopod.
    private var pendingIsopodDoubleHook: Boolean? = null
    private var isopodSearchDelayTicks = 0
    private var isopodShareLockTicks = 0
    private const val ISOPOD_SHARE_LOCK_TICKS = 20

    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldUnloadEvent::class, ::onWorldUnload)
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!WorldUtils.isInSkyblock() || !Chat.shareRareSeaCreaturesAllChat) return

        val seaCreatureName = event.seaCreatureName

        val type = try {
            RareSeaCreatureTypesAllChat.valueOf(seaCreatureName.uppercase().replace(" ", "_"))
        } catch (_: IllegalArgumentException) {
            return
        }
        if (!Chat.shareRareSeaCreaturesTypesAllChat.contains(type)) return

        val isDoubleHook = event.isDoubleHook
        if (seaCreatureName.equals(SeaCreatureNames.GIANT_ISOPOD, ignoreCase = true)) {
            scheduleIsopodShare(isDoubleHook)
            return
        }

        sendShare(seaCreatureName, isDoubleHook)
    }

    private fun scheduleIsopodShare(isDoubleHook: Boolean) {
        if (pendingIsopodDoubleHook != null) {
            pendingIsopodDoubleHook = pendingIsopodDoubleHook == true || isDoubleHook
            isopodSearchDelayTicks = 1
            debugIsopod("${YELLOW}another isopod catch, still sending one share")
            return
        }
        if (isopodShareLockTicks > 0) {
            debugIsopod("${YELLOW}skipped extra isopod share (${isopodShareLockTicks} ticks left on lock)")
            return
        }

        pendingIsopodDoubleHook = isDoubleHook
        isopodSearchDelayTicks = 1
        isopodShareLockTicks = ISOPOD_SHARE_LOCK_TICKS
        debugIsopod("${YELLOW}chat received, searching next tick")
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        if (isopodShareLockTicks > 0) isopodShareLockTicks--

        if (pendingIsopodDoubleHook == null) return
        if (isopodSearchDelayTicks > 0) {
            isopodSearchDelayTicks--
            return
        }

        val isDoubleHook = pendingIsopodDoubleHook ?: return
        pendingIsopodDoubleHook = null
        sendShare(SeaCreatureNames.GIANT_ISOPOD, isDoubleHook)
    }

    private fun onWorldUnload(@Suppress("UNUSED_PARAMETER") event: WorldUnloadEvent) {
        pendingIsopodDoubleHook = null
        isopodSearchDelayTicks = 0
        isopodShareLockTicks = 0
    }

    private fun sendShare(seaCreatureName: String, isDoubleHook: Boolean) {
        val message = getAllChatMessage(seaCreatureName, isDoubleHook)
        //ChatUtils.sendAllChat(message) // TODO: Uncomment this when we are ready to send the message to all chat
        ChatUtils.sendLocalChat(message)
    }

    private fun getAllChatMessage(seaCreatureName: String, isDoubleHooked: Boolean): String {
        val player = FeeshMod.mc.player ?: return ""
        val isGiantIsopod = seaCreatureName.equals(SeaCreatureNames.GIANT_ISOPOD, ignoreCase = true)
        val (x, y, z) = when {
            isGiantIsopod -> findGiantIsopodPosition() // It spawns not on player but at the top of Torrhus Springs
            else -> Triple(player.getX(), player.getY(), player.getZ())
        }
        val formattedCoordinates = CommonUtils.getFormattedLocation(x, y, z)
        val scMessage = if (isDoubleHooked) "${seaCreatureName} x2" else "${seaCreatureName}"
        val zone = when {
            isGiantIsopod -> WorldUtils.TORRHUS_SPRINGS
            WorldUtils.getWorldName() == WorldUtils.BACKWATER_BAYOU -> null // Bayou has single zone so no need to show it
            else -> WorldUtils.getZoneName()
        }
        val zoneText = if (!zone.isNullOrEmpty()) " at $zone" else ""
        val messageId = CommonUtils.getMessageId()

        var message = "${formattedCoordinates} | ${scMessage}${zoneText} | ${messageId}"
        return message
    }

    private fun findGiantIsopodPosition(): Triple<Double, Double, Double> {
        val fallback = Triple(-642.0, 157.0, 183.0) // Top of Torrhus Springs
        val world = FeeshMod.mc.level
        if (world == null) {
            debugIsopod("${RED}no world, using fallback")
            return fallback
        }

        val armorStands = world.entitiesForRendering().filterIsInstance<ArmorStand>().toList()
        val namedIsopods = armorStands.mapNotNull { stand ->
            val name = stand.customName?.getUnformattedString()?.trim().orEmpty()
            if (!name.contains("isopod", ignoreCase = true)) return@mapNotNull null
            stand to name
        }

        debugIsopod(
            "${YELLOW}armor stands=${armorStands.size}, " +
                "ticks<=20=${armorStands.count { it.tickCount <= 20 }}, " +
                "nametag contains isopod=${namedIsopods.size}"
        )

        if (namedIsopods.isEmpty()) {
            val recent = armorStands
                .filter { it.customName != null }
                .sortedBy { it.tickCount }
                .take(8)
            if (recent.isEmpty()) {
                debugIsopod("${RED}no named armor stands. using fallback")
            } else {
                debugIsopod("${RED}no nametag contains \"isopod\". youngest named stands:")
                recent.forEach { stand ->
                    val name = stand.customName?.getUnformattedString()?.take(90) ?: "?"
                    debugIsopod("${GRAY}  ticks=${stand.tickCount} \"$name\"")
                }
            }
            return fallback
        }

        namedIsopods.sortedBy { it.first.tickCount }.forEach { (stand, name) ->
            val shortName = name.take(90)
            val pos = "(${stand.x.toInt()}, ${stand.y.toInt()}, ${stand.z.toInt()})"
            if (stand.tickCount > 20) {
                debugIsopod("${RED}WARN found but ticks=${stand.tickCount} > 20 at $pos \"$shortName\"")
                return@forEach
            }
            val info = EntityUtils.parseSeaCreatureNametag(stand, listOf(SeaCreatureNames.GIANT_ISOPOD))
            val detail = when {
                info == null -> "${RED}parse returned null"
                info.baseMobName != SeaCreatureNames.GIANT_ISOPOD ->
                    "${RED}baseMobName=\"${info.baseMobName}\""
                else -> "${GREEN}match"
            }
            debugIsopod("${GRAY}ticks=${stand.tickCount} at $pos $detail \"$shortName\"")
        }

        val closest = namedIsopods
            .map { it.first }
            .filter { it.tickCount <= 20 }
            .mapNotNull { stand ->
                val info = EntityUtils.parseSeaCreatureNametag(stand, listOf(SeaCreatureNames.GIANT_ISOPOD)) ?: return@mapNotNull null
                if (info.baseMobName != SeaCreatureNames.GIANT_ISOPOD) return@mapNotNull null
                stand
            }
            .minByOrNull { it.tickCount }

        if (closest == null) {
            debugIsopod("${RED}no usable stand (ticks<=20 + parsed Giant Isopod). using fallback $fallback")
            return fallback
        }

        debugIsopod("${GREEN}using stand ticks=${closest.tickCount} at (${closest.x}, ${closest.y}, ${closest.z})")
        return Triple(closest.x, closest.y, closest.z)
    }

    private fun debugIsopod(message: String) {
        ChatUtils.sendLocalChat("${GRAY}[Isopod] $message")
    }
}
