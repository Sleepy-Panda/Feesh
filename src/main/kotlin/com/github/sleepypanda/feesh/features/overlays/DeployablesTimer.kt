package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.ClientTickEvent
import com.github.sleepypanda.feesh.events.WorldChangedEvent
import com.github.sleepypanda.feesh.events.PlayerInteractEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.enums.DeployableTypes
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.constants.Sounds
import com.github.sleepypanda.feesh.events.InteractActionType
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.projectile.FireworkRocketEntity
import java.util.*
import java.util.Timer
import java.util.TimerTask

object DeployablesTimer {
    private const val SECONDS_BEFORE_EXPIRATION = 10
    private const val TICKS_PER_CHECK = 20

    private open class BaseDeployableData {
        var remainingTime: String? = null
        var lastAlertAt: Date? = null
    }

    private class TotemData : BaseDeployableData()

    private class BlackHoleData : BaseDeployableData()

    private class UmberellaData : BaseDeployableData() {
        var id: UUID? = null
    }

    private class FlareData : BaseDeployableData() {
        var remainingSeconds: Int? = null
        var lastPlacedAt: Date? = null
        var itemDisplayName: String? = null
    }

    private var totemData = TotemData()
    private var blackHoleData = BlackHoleData()
    private var umberellaData = UmberellaData()
    private var flareData = FlareData()

    private var tickCounter = 0

    private val gui = FeeshGui()
        .setCoordsDataKey("deployablesTimer")
        .setClickable(false)
        .setSampleLines(listOf(
            "${DARK_PURPLE}Totem of Corruption: ${WHITE}01m 02s",
            "${DARK_PURPLE}Black Hole: ${WHITE}50s",
            "${BLUE}Umberella: ${WHITE}30s",
            "${DARK_PURPLE}SOS Flare: ${WHITE}180s"
        ))
        .setSettingsKey { Overlays.deployablesTimerOverlay }
        .setCondition {
            WorldUtils.isInSkyblock()
        }

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(PlayerInteractEvent::class, ::onPlayerInteract)

        RegisterUtils.chat(Regex("^Your flare disappeared because you were too far away\\!$")) { _, _ ->
            if (WorldUtils.isInSkyblock()) {
                resetFlare()
            }
        }
        RegisterUtils.chat(Regex("^Your previous (.*) was removed\\!$")) { _, _ ->
            if (WorldUtils.isInSkyblock()) {
                resetFlare()
            }
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        resetTotem()
        resetBlackHole()
        resetUmberella()
        resetFlare()
    }

    private fun onPlayerInteract(event: PlayerInteractEvent) {
        try {
            if (!WorldUtils.isInSkyblock()) return
            if (!event.isMainHand || (event.actionType != InteractActionType.USE_ITEM && event.actionType != InteractActionType.USE_BLOCK)) return

            val heldItem = FeeshMod.mc.player?.mainHandStack
            if (heldItem == null || heldItem.isEmpty) return

            val heldItemName = heldItem.name.getFormattedString()

            if (isUmberellaTrackingEnabled() && heldItemName.contains("Umberella")) {
                // Give time for an Umberella to appear after click
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        trackUmberellaNearby()
                    }
                }, 250)
            }

            if (isFlareTrackingEnabled() && heldItemName.contains("Flare")) {
                // Prevent multiple clicks
                if (flareData.lastPlacedAt != null && Date().time - flareData.lastPlacedAt!!.time < 500) return

                // Give time for a firework rocket to appear after click
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        trackFlareRocketNearby(heldItemName)
                    }
                }, 500)
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to handle deployable interaction", e)
        }
    }

    private fun trackUmberellaNearby() {
        try {
            val player = FeeshMod.mc.player ?: return
            val world = FeeshMod.mc.world ?: return
            val entities = world.entities.filterIsInstance<ArmorStandEntity>()

            val umberellaArmorStand = entities.find { entity ->
                val distance = EntityUtils.getDistance(player, entity)
                distance <= 5.0 && entity.customName?.string == "Umberella 300s"
            }

            if (umberellaArmorStand != null) {
                umberellaData.id = umberellaArmorStand.uuid
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to track Umberella nearby", e)
        }
    }

    private fun trackFlareRocketNearby(heldItemName: String) {
        try {
            val player = FeeshMod.mc.player ?: return
            val world = FeeshMod.mc.world ?: return

            val flareRockets = world.entities
                .filterIsInstance<FireworkRocketEntity>()
                .filter { rocket ->
                    val distance = EntityUtils.getDistance(player, rocket)
                    distance <= 10.0
                }

            if (flareRockets.isNotEmpty()) {
                flareData.remainingSeconds = 180
                flareData.remainingTime = fromSecondsToTimeString(flareData.remainingSeconds!!)
                flareData.lastPlacedAt = Date()
                flareData.itemDisplayName = heldItemName
            }

            // Future notes: flare itself appears on slightly different coords than the initial rocket
            // e.g. rocket is at 62.01113596669814 -160.09375 and flare (armor stand) is at 62.125 -160.09375
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to track Flare rocket nearby", e)
        }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        if (isAnyAlertEnabled() || isOverlayEnabled()) {
            trackDeployablesStatus()
        }

        if (isOverlayEnabled()) {
            updateGuiLines()
        }
    }

    private fun isOverlayEnabled(): Boolean {
        return Overlays.deployablesTimerOverlay && Overlays.deployablesOverlayTypes.isNotEmpty()
    }

    private fun isAnyAlertEnabled(): Boolean {
        return Alerts.alertOnDeployableExpiresSoon && Alerts.alertOnDeployableTypes.isNotEmpty()
    }

    private fun isTotemTrackingEnabled(): Boolean {
        return (Alerts.alertOnDeployableExpiresSoon && Alerts.alertOnDeployableTypes.contains(DeployableTypes.TOTEM_OF_CORRUPTION)) ||
               (Overlays.deployablesTimerOverlay && Overlays.deployablesOverlayTypes.contains(DeployableTypes.TOTEM_OF_CORRUPTION))
    }

    private fun isBlackHoleTrackingEnabled(): Boolean {
        return (Alerts.alertOnDeployableExpiresSoon && Alerts.alertOnDeployableTypes.contains(DeployableTypes.BLACK_HOLE)) ||
               (Overlays.deployablesTimerOverlay && Overlays.deployablesOverlayTypes.contains(DeployableTypes.BLACK_HOLE))
    }

    private fun isUmberellaTrackingEnabled(): Boolean {
        return (Alerts.alertOnDeployableExpiresSoon && Alerts.alertOnDeployableTypes.contains(DeployableTypes.UMBERELLA)) ||
               (Overlays.deployablesTimerOverlay && Overlays.deployablesOverlayTypes.contains(DeployableTypes.UMBERELLA))
    }

    private fun isFlareTrackingEnabled(): Boolean {
        return (Alerts.alertOnDeployableExpiresSoon && Alerts.alertOnDeployableTypes.contains(DeployableTypes.FLARE)) ||
               (Overlays.deployablesTimerOverlay && Overlays.deployablesOverlayTypes.contains(DeployableTypes.FLARE))
    }

    private fun resetTotem() {
        totemData = TotemData()
    }

    private fun resetBlackHole() {
        blackHoleData = BlackHoleData()
    }

    private fun resetUmberella() {
        umberellaData = UmberellaData()
    }

    private fun resetFlare() {
        flareData = FlareData()
    }

    private fun trackDeployablesStatus() {
        if (!WorldUtils.isInSkyblock()) return

        val world = FeeshMod.mc.world ?: return
        val entities = world.entities.filterIsInstance<ArmorStandEntity>()

        if (isTotemTrackingEnabled()) {
            trackTotemStatus(entities)
        }
        if (isBlackHoleTrackingEnabled()) {
            trackBlackHoleStatus(entities)
        }
        if (isUmberellaTrackingEnabled()) {
            trackUmberellaStatus(entities)
        }
        if (isFlareTrackingEnabled()) {
            trackFlareStatus()
        }
    }

    private fun trackTotemStatus(entities: List<ArmorStandEntity>) {
        try {
            if (!WorldUtils.isInSkyblock() || entities.isEmpty()) {
                resetTotem()
                return
            }

            val playerName = PlayerUtils.getName()
            if (playerName.isNullOrEmpty()) return

            val ownerArmorStand = entities.find { entity ->
                val name = entity.customName?.string ?: return@find false
                name.contains("Owner:") && name.contains(playerName)
            }

            if (ownerArmorStand == null) {
                resetTotem()
                return
            }

            val ownerArmorStandId = ownerArmorStand.id
            val totemArmorStand = entities.find { it.id == ownerArmorStandId - 2 }
            if (totemArmorStand == null) return

            val totemArmorStandName = totemArmorStand.customName?.string ?: ""
            if (totemArmorStandName != "Totem of Corruption") {
                resetTotem()
                return
            }

            val remainingArmorStand = entities.find { it.id == ownerArmorStandId - 1 }
            if (remainingArmorStand == null) return

            val remainingArmorStandName = remainingArmorStand.customName?.string ?: ""
            if (!remainingArmorStandName.contains("Remaining: ")) {
                resetTotem()
                return
            }

            totemData.remainingTime = remainingArmorStandName.split("Remaining: ").lastOrNull()

            if (Alerts.alertOnDeployableExpiresSoon &&
                Alerts.alertOnDeployableTypes.contains(DeployableTypes.TOTEM_OF_CORRUPTION) &&
                totemData.remainingTime == "${SECONDS_BEFORE_EXPIRATION}s" &&
                (totemData.lastAlertAt == null || Date().time - totemData.lastAlertAt!!.time >= 1000)
            ) {
                playAlert("${DARK_PURPLE}Totem of Corruption", totemData)
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to track Totem status", e)
        }
    }

    private fun trackBlackHoleStatus(entities: List<ArmorStandEntity>) {
        try {
            if (!WorldUtils.isInSkyblock() || entities.isEmpty()) {
                resetBlackHole()
                return
            }

            val playerName = PlayerUtils.getName()
            if (playerName.isNullOrEmpty()) return

            val ownerArmorStand = entities.find { entity ->
                val name = entity.customName?.string ?: ""
                name.contains("Spawned by:") && name.contains(playerName)
            }

            if (ownerArmorStand == null) {
                resetBlackHole()
                return
            }

            val ownerArmorStandId = ownerArmorStand.id
            val blackHoleArmorStand = entities.find { it.id == ownerArmorStandId + 1 }
            if (blackHoleArmorStand == null) return

            val blackHoleArmorStandName = blackHoleArmorStand.customName?.string ?: ""
            if (!blackHoleArmorStandName.startsWith("Black Hole")) {
                resetBlackHole()
                return
            }

            // When a Black Hole is placed, it has no timer for a second
            val timer = blackHoleArmorStandName.substringAfter("Black Hole").trim()
            val seconds = if (timer.isNotEmpty()) {
                timer.replace("s", "").toIntOrNull() ?: 180
            } else 180
            blackHoleData.remainingTime = fromSecondsToTimeString(seconds)

            if (Alerts.alertOnDeployableExpiresSoon &&
                Alerts.alertOnDeployableTypes.contains(DeployableTypes.BLACK_HOLE) &&
                blackHoleData.remainingTime == "${SECONDS_BEFORE_EXPIRATION}s" &&
                (blackHoleData.lastAlertAt == null || Date().time - blackHoleData.lastAlertAt!!.time >= 1000)
            ) {
                playAlert("${DARK_PURPLE}Black Hole", blackHoleData)
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to track Black Hole status", e)
        }
    }

    private fun trackUmberellaStatus(entities: List<ArmorStandEntity>) {
        try {
            if (!WorldUtils.isInSkyblock() || entities.isEmpty() || !isUmberellaTrackingEnabled()) {
                resetUmberella()
                return
            }

            val umberellaArmorStand = entities.find { entity ->
                entity.customName?.string?.startsWith("Umberella ") == true &&
                entity.uuid == umberellaData.id
            }

            if (umberellaArmorStand == null) {
                resetUmberella()
                return
            }

            val name = umberellaArmorStand.customName?.string ?: ""
            val seconds = name.split("Umberella ").lastOrNull()?.replace("s", "")?.toIntOrNull() ?: return
            umberellaData.remainingTime = fromSecondsToTimeString(seconds)

            if (Alerts.alertOnDeployableExpiresSoon &&
                Alerts.alertOnDeployableTypes.contains(DeployableTypes.UMBERELLA) &&
                umberellaData.remainingTime == "${SECONDS_BEFORE_EXPIRATION}s" &&
                (umberellaData.lastAlertAt == null || Date().time - umberellaData.lastAlertAt!!.time >= 1000)
            ) {
                playAlert("${BLUE}Umberella", umberellaData)
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to track Umberella status", e)
        }
    }

    private fun trackFlareStatus() {
        try {
            if (!WorldUtils.isInSkyblock() || !isFlareTrackingEnabled()) {
                resetFlare()
                return
            }

            val remainingSeconds = flareData.remainingSeconds ?: 0
            if (remainingSeconds <= 0) {
                resetFlare()
                return
            }

            flareData.remainingSeconds = remainingSeconds - 1
            flareData.remainingTime = fromSecondsToTimeString(flareData.remainingSeconds!!)

            if (Alerts.alertOnDeployableExpiresSoon &&
                Alerts.alertOnDeployableTypes.contains(DeployableTypes.FLARE) &&
                remainingSeconds == SECONDS_BEFORE_EXPIRATION &&
                (flareData.lastAlertAt == null || Date().time - flareData.lastAlertAt!!.time >= 1000)
            ) {
                playAlert(flareData.itemDisplayName ?: "Flare", flareData)
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to track Flare status", e)
        }
    }

    private fun playAlert(itemDisplayName: String, data: BaseDeployableData) {
        CommonUtils.showTitle("$itemDisplayName ${RED}expires soon")
        ChatUtils.sendLocalChat("${WHITE}Your $itemDisplayName ${WHITE}expires soon.", true)
        data.lastAlertAt = Date()
        SoundUtils.playCustomSound(Sounds.FEESH_NOTIFICATION_BELL)
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!Overlays.deployablesTimerOverlay || !WorldUtils.isInSkyblock()) return

        val lines = mutableListOf<String>()

        if (Overlays.deployablesOverlayTypes.contains(DeployableTypes.UMBERELLA) && !umberellaData.remainingTime.isNullOrEmpty() && umberellaData.remainingTime != "00s") {
            val timerColor = fromTimeStringToSeconds(umberellaData.remainingTime!!) <= SECONDS_BEFORE_EXPIRATION
            val colorCode = if (timerColor) RED.code else WHITE.code
            lines.add("${BLUE.code}Umberella: $colorCode${umberellaData.remainingTime}")
        }

        if (Overlays.deployablesOverlayTypes.contains(DeployableTypes.FLARE) && !flareData.remainingTime.isNullOrEmpty() && flareData.remainingTime != "00s") {
            val timerColor = (flareData.remainingSeconds ?: 0) <= SECONDS_BEFORE_EXPIRATION
            val colorCode = if (timerColor) RED.code else WHITE.code
            lines.add("${flareData.itemDisplayName}: $colorCode${flareData.remainingTime}")
        }

        if (Overlays.deployablesOverlayTypes.contains(DeployableTypes.BLACK_HOLE) && !blackHoleData.remainingTime.isNullOrEmpty() && blackHoleData.remainingTime != "00s") {
            val timerColor = fromTimeStringToSeconds(blackHoleData.remainingTime!!) <= SECONDS_BEFORE_EXPIRATION
            val colorCode = if (timerColor) RED.code else WHITE.code
            lines.add("${DARK_PURPLE.code}Black Hole: $colorCode${blackHoleData.remainingTime}")
        }

        if (Overlays.deployablesOverlayTypes.contains(DeployableTypes.TOTEM_OF_CORRUPTION) && !totemData.remainingTime.isNullOrEmpty() && totemData.remainingTime != "00s") {
            val remainingSeconds = fromTimeStringToSeconds(totemData.remainingTime!!)
            val timerColor = remainingSeconds > 0 && remainingSeconds <= SECONDS_BEFORE_EXPIRATION && !totemData.remainingTime!!.contains("m")
            val colorCode = if (timerColor) RED.code else WHITE.code
            lines.add("${DARK_PURPLE.code}Totem of Corruption: $colorCode${totemData.remainingTime}")
        }

        if (lines.isNotEmpty()) {
            gui.setLines(lines)
        }
    }

    private fun fromSecondsToTimeString(totalSeconds: Int): String {
        if (totalSeconds <= 0) return ""
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (minutes > 0) {
            "${minutes.toString().padStart(2, '0')}m ${seconds.toString().padStart(2, '0')}s"
        } else {
            "${seconds.toString().padStart(2, '0')}s"
        }
    }

    private fun fromTimeStringToSeconds(timeStr: String): Int {
        return try {
            if (timeStr.contains("m")) {
                val parts = timeStr.split("m")
                val minutes = parts[0].toIntOrNull() ?: 0
                val seconds = parts.getOrNull(1)?.trim()?.replace("s", "")?.toIntOrNull() ?: 0
                minutes * 60 + seconds
            } else {
                timeStr.replace("s", "").toIntOrNull() ?: 0
            }
        } catch (e: Exception) {
            0
        }
    }
}
