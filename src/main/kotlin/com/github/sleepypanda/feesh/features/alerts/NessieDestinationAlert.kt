package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.ArmorStandDetailsLoadedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import net.minecraft.world.entity.animal.sniffer.Sniffer
import java.util.Date

object NessieDestinationAlert {
    private data class TrackedNessieInfo(
        val detectedTime: Long,
        val isDestinationSent: Boolean = false
    )

    private const val NESSIE_NAME = "Nessie"
    private const val TICKS_PER_MOBS_SCAN = 10
    private const val CLEANUP_DELAY_TICKS = 30 * 20
    private const val EXPIRATION_TIME_MS = 6 * 60 * 1000L
    private const val DESTINATION_ENTRANCE_RADIUS = 5.0

    private val driptoadDelveEntranceCoords = listOf(
        Triple(-663.0, 71.0, 12.0),
        Triple(-665.0, 71.0, 20.0),
        // Some checkpoints when Nessie goes down through blocks instead of entering through a tunnel entrance
        Triple(-666.0, 69.0, 17.0),
        Triple(-675.0, 63.0, 40.0),
        Triple(-674.0, 81.0, 44.0),
        Triple(-680.0, 70.0, 51.0),
        Triple(-674.0, 59.0, 30.0),
        Triple(-675.0, 62.0, 39.0),
        Triple(-678.0, 65.0, 47.0),
        Triple(-682.0, 68.0, 54.0),
        Triple(-686.0, 74.0, 60.0),
    )

    private val jadeDragonEntranceCoords = listOf(
        Triple(-660.0, 71.0, 0.0),
        // Some checkpoints when Nessie goes down through blocks instead of entering through a tunnel entrance
        Triple(-637.0, 60.0, -7.0),
        Triple(-660.0, 68.0, -2.0),
        Triple(-656.0, 55.0, -12.0),
        Triple(-648.0, 54.0, -13.0),
        Triple(-640.0, 53.0, -10.0),
        Triple(-638.0, 51.0, -4.0),
        Triple(-634.0, 50.0, -10.0),
        Triple(-627.0, 50.0, -5.0),
        Triple(-620.0, 48.0, 5.0),
        Triple(-620.0, 47.0, 13.0),
        Triple(-626.0, 47.0, 19.0),
        Triple(-636.0, 47.0, 7.0),
        Triple(-640.0, 45.0, 13.0),
        Triple(-644.0, 44.0, 15.0),
    )

    private val trackedNessieMobIds = mutableMapOf<Int, TrackedNessieInfo>()

    private var tickCounter = 0
    private var cleanupTickCounter = 0

    fun init() {
        EventBus.subscribe(ArmorStandDetailsLoadedEvent::class, ::onArmorStandLoaded)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        trackedNessieMobIds.clear()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        onTickMobsScan()
        onTickCleanup()
    }

    private fun onArmorStandLoaded(event: ArmorStandDetailsLoadedEvent) {
        if (!Alerts.alertOnNessieDestination || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.GALATEA) return
        if (!event.customNameUnformatted.contains(NESSIE_NAME)) return

        EntityUtils.parseSeaCreatureNametag(event.entity, listOf(NESSIE_NAME)) ?: return

        val mobId = event.entityId - 1
        val existing = trackedNessieMobIds[mobId]
        if (existing == null) {
            trackedNessieMobIds[mobId] = TrackedNessieInfo(detectedTime = Date().time)
        }
    }

    private fun onTickMobsScan() {
        tickCounter++
        if (tickCounter < TICKS_PER_MOBS_SCAN) return
        tickCounter = 0

        if (!Alerts.alertOnNessieDestination || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.GALATEA) return

        CommonUtils.runWithCatching("Failed to check Nessies nearby") {
            checkNessiesChosenDestinations()
        }
    }
   
    private fun onTickCleanup() {
        cleanupTickCounter++
        if (cleanupTickCounter < CLEANUP_DELAY_TICKS) return
        cleanupTickCounter = 0

        if (!WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.GALATEA) return

        if (trackedNessieMobIds.isEmpty()) return
        val now = Date().time
        val expiredIds = trackedNessieMobIds.filter { (_, info) -> now - info.detectedTime > EXPIRATION_TIME_MS }.keys
        expiredIds.forEach { id -> trackedNessieMobIds.remove(id) }
    }

    private fun checkNessiesChosenDestinations() {
        if (trackedNessieMobIds.isEmpty()) return

        val ids = trackedNessieMobIds.keys.toList()
        ids.forEach { mobId -> checkNessieChosenDestination(mobId) }
    }

    private fun checkNessieChosenDestination(nessieEntityId: Int) {
        val mobEntity = EntityUtils.getMcEntityById(nessieEntityId) ?: return
        if (mobEntity !is Sniffer) return

        if (driptoadDelveEntranceCoords.any { (x, y, z) -> EntityUtils.getDistance(mobEntity, x, y, z) <= DESTINATION_ENTRANCE_RADIUS }) {
            alertOnNessieDestinationChosen(nessieEntityId, "Driptoad Delve")
        } else if (jadeDragonEntranceCoords.any { (x, y, z) -> EntityUtils.getDistance(mobEntity, x, y, z) <= DESTINATION_ENTRANCE_RADIUS }) {
            alertOnNessieDestinationChosen(nessieEntityId, "Jade Dragon")
        }
    }

    private fun alertOnNessieDestinationChosen(nessieEntityId: Int, destination: String) {
        val trackedInfo = trackedNessieMobIds[nessieEntityId] ?: return
        if (trackedInfo.isDestinationSent) return

        trackedNessieMobIds[nessieEntityId] = trackedInfo.copy(isDestinationSent = true)
        val alertMessage = "${LIGHT_PURPLE}${BOLD}Nessie ${WHITE}is swimming to the ${GREEN}${BOLD}${destination}${WHITE} cave."

        CommonUtils.showTitle("", "${LIGHT_PURPLE}Nessie ${WHITE}goes to ${GREEN}${BOLD}${destination}${WHITE} cave!")
        SoundUtils.playSound()
        ChatUtils.sendLocalChat(alertMessage, true)

        if (Alerts.autoShareNessieDestination) {
            ChatUtils.sendPartyChat(alertMessage.removeFormatting())
        }
    }
}
