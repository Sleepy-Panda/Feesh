package com.github.sleepypanda.feesh.features.alerts

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.passive.SnifferEntity
import java.util.Date

object NessieDestinationAlert {
    private const val NESSIE_NAME = "Nessie"
    private const val SCAN_DISTANCE = 30.0
    private const val TICKS_PER_MOBS_SCAN = 10
    private const val CLEANUP_DELAY_TICKS = 30 * 20
    private const val EXPIRATION_TIME_MS = 6 * 60 * 1000L
    private const val DESTINATION_ENTRANCE_RADIUS = 5.0

    private val driptoadDelveEntranceCoords = listOf(
        Triple(-663.0, 71.0, 12.0),
        Triple(-665.0, 71.0, 20.0),
        Triple(-666.0, 69.0, 17.0),
        Triple(-675.0, 63.0, 40.0),
        Triple(-674.0, 81.0, 44.0),
    )
    private val jadeDragonEntranceCoords = listOf(
        Triple(-660.0, 71.0, 0.0)
    )

    private val trackedNessieMobIds = mutableMapOf<Int, Long>()
    private val lastNessieMessageTimes = mutableMapOf<Int, Long>()

    private var tickCounter = 0
    private var cleanupTickCounter = 0

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        trackedNessieMobIds.clear()
        lastNessieMessageTimes.clear()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        onTickMobsScan()
        onTickCleanup()
    }

    private fun onTickMobsScan() {
        tickCounter++
        if (tickCounter < TICKS_PER_MOBS_SCAN) return
        tickCounter = 0

        if (!Alerts.alertOnNessieDestination || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.GALATEA) return

        CommonUtils.runWithCatching("Failed to scan for Nessie armor stands") {
            trackNessiesNearby()
            checkNessiesChosenDestinations()
        }
    }
   
    private fun onTickCleanup() {
        cleanupTickCounter++
        if (cleanupTickCounter < CLEANUP_DELAY_TICKS) return
        cleanupTickCounter = 0

        if (!Alerts.alertOnNessieDestination || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.GALATEA) return

        cleanupOutdatedTrackedIds()
        cleanupOutdatedNessieMessageTimes()
    }

    private fun trackNessiesNearby() {
        val player = FeeshMod.mc.player ?: return
        val world = FeeshMod.mc.world ?: return
        val now = Date().time

        world.entities.filterIsInstance<ArmorStandEntity>()
            .filter { EntityUtils.getDistance(player, it) <= SCAN_DISTANCE }
            .forEach { stand ->
                val name = stand.customName?.string?.removeFormatting() ?: return@forEach
                if (!name.contains(NESSIE_NAME)) return@forEach
                val scInfo = EntityUtils.parseSeaCreatureNametag(stand, listOf(NESSIE_NAME)) ?: return@forEach
                val mobId = stand.id - 1
                val existing = trackedNessieMobIds[mobId]
                if (existing == null) {
                    trackedNessieMobIds[mobId] = now
                }
            }
    }

    private fun checkNessiesChosenDestinations() {
        if (trackedNessieMobIds.isEmpty()) return

        val ids = trackedNessieMobIds.keys.toList()
        ids.forEach { mobId -> checkNessieChosenDestination(mobId) }
    }

    private fun cleanupOutdatedTrackedIds() {
        if (trackedNessieMobIds.isEmpty()) return
        val now = Date().time
        val expiredIds = trackedNessieMobIds.filter { (_, timestamp) -> now - timestamp > EXPIRATION_TIME_MS }.keys
        expiredIds.forEach { id -> trackedNessieMobIds.remove(id) }
    }

    private fun cleanupOutdatedNessieMessageTimes() {
        if (lastNessieMessageTimes.isEmpty()) return
        val now = Date().time
        val expiredIds = lastNessieMessageTimes.filter { (_, timestamp) -> now - timestamp > 1000L * 30 }.keys // To not announce Nessie moving through some point a few times
        expiredIds.forEach { id -> lastNessieMessageTimes.remove(id) }
    }

    private fun checkNessieChosenDestination(nessieEntityId: Int) {
        val mobEntity = EntityUtils.getMcEntityById(nessieEntityId) ?: return
        if (mobEntity !is SnifferEntity) return

        FeeshMod.LOGGER.info("Nessie destination alert: ${mobEntity.x}, ${mobEntity.y}, ${mobEntity.z}")
        if (driptoadDelveEntranceCoords.any { (x, y, z) -> EntityUtils.getDistance(mobEntity, x, y, z) <= DESTINATION_ENTRANCE_RADIUS }) {
            alertOnNessieDestinationChosen(nessieEntityId, "Driptoad Delve")
        } else if (jadeDragonEntranceCoords.any { (x, y, z) -> EntityUtils.getDistance(mobEntity, x, y, z) <= DESTINATION_ENTRANCE_RADIUS }) {
            alertOnNessieDestinationChosen(nessieEntityId, "Jade Dragon")
        }
    }

    private fun alertOnNessieDestinationChosen(nessieEntityId: Int, destination: String) {
        val lastMessageTime = lastNessieMessageTimes[nessieEntityId]
        if (lastMessageTime == null) {
            lastNessieMessageTimes[nessieEntityId] = Date().time
            CommonUtils.showTitle("", "${LIGHT_PURPLE}Nessie ${WHITE}goes to ${GREEN}${BOLD}${destination}${WHITE} cave!")
            SoundUtils.playSound()
            ChatUtils.sendLocalChat("${LIGHT_PURPLE}${BOLD}Nessie ${WHITE}is swimming to the ${GREEN}${BOLD}${destination}${WHITE} cave. Meet it there!", true)
        }
    }
}
