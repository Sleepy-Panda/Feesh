package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ArmorStandDetailsLoadedEvent
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.PlayerInteractEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.enums.DeployableTypes
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.constants.Sounds
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import com.github.sleepypanda.feesh.events.models.InteractActionType
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.projectile.FireworkRocketEntity
import java.util.*
import java.util.Timer
import java.util.TimerTask

object DeployablesTimer {
    private const val SECONDS_BEFORE_EXPIRATION = 10
    private const val TICKS_PER_CHECK = 20

    private val FLARE_DISAPPEARED_PATTERN = Regex("^Your flare disappeared because you were too far away\\!$")
    private val PREVIOUS_DEPLOYABLE_REMOVED_PATTERN = Regex("^Your previous (.*) was removed\\!$")

    private open class BaseDeployableData {
        var remainingTime: String? = null
        var lastAlertAt: Date? = null
    }

    private class TotemData : BaseDeployableData()

    private class BlackHoleData : BaseDeployableData()

    private class UmberellaData : BaseDeployableData() {
        var id: Int? = null
    }

    private class FlareData : BaseDeployableData() {
        var remainingSeconds: Int? = null
        var lastPlacedAt: Date? = null
        var itemDisplayName: String? = null
    }

    private class DwarvenLanternData : BaseDeployableData() {
        var id: Int? = null
        var itemDisplayName: String? = null
    }

    private val DWARVEN_LANTERN_NAME_PREFIXES = listOf(
        "Dwarven Lantern",
        "Mithril Lantern",
        "Titanium Lantern",
        "Glacite Lantern",
        "Will-o'-wisp",
    )

    private fun isHeldItemDwarvenLantern(heldItemName: String): Boolean =
        DWARVEN_LANTERN_NAME_PREFIXES.any { prefix -> heldItemName == prefix }

    private var totemData = TotemData()
    private var blackHoleData = BlackHoleData()
    private var umberellaData = UmberellaData()
    private var flareData = FlareData()
    private var dwarvenLanternData = DwarvenLanternData()

    private var tickCounter = 0

    // We can't know owner for those items, so we track when there was an interaction, to try ignoring spawns from other players.
    private var lastDwarvenLanternInteractTimeMs: Long = 0L
    private var lastUmberellaInteractTimeMs: Long = 0L

    private val gui = FeeshGui()
        .setCoordsDataKey("deployablesTimer")
        .setClickable(false)
        .setSampleLines(listOf(
            "${DARK_PURPLE}Totem of Corruption: ${WHITE}01m 02s",
            "${DARK_PURPLE}Black Hole: ${WHITE}50s",
            "${BLUE}Umberella: ${WHITE}30s",
            "${DARK_PURPLE}SOS Flare: ${WHITE}02m 58s",
            "${GOLD}Will-o'-wisp: ${WHITE}04m 30s",
        ))
        .setSettingsKey { Overlays.deployablesTimerOverlay }
        .setApplyCustomStyleKey { Overlays.deployablesTimerCustomStyle }

    fun init() {
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(PlayerInteractEvent::class, ::onPlayerInteract)
        EventBus.subscribe(ArmorStandDetailsLoadedEvent::class, ::onArmorStandDetailsLoaded)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        resetTotem()
        resetBlackHole()
        resetUmberella()
        resetFlare()
        resetDwarvenLantern()
        lastUmberellaInteractTimeMs = 0L
        lastDwarvenLanternInteractTimeMs = 0L
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock()) return

        if (FLARE_DISAPPEARED_PATTERN.matches(event.unformattedText)) {
            resetFlare()
        } else if (PREVIOUS_DEPLOYABLE_REMOVED_PATTERN.matches(event.unformattedText)) {
            val matchResult = PREVIOUS_DEPLOYABLE_REMOVED_PATTERN.find(event.unformattedText) ?: return
            val captured: String = matchResult.groupValues.getOrNull(1) ?: ""
            if (captured.contains("flare", ignoreCase = true)) resetFlare()
        }
    }

    private fun onPlayerInteract(event: PlayerInteractEvent) {
        CommonUtils.runWithCatching("Failed to handle deployable interaction") {
            if (!WorldUtils.isInSkyblock()) return
            if (!event.isMainHand || (event.actionType != InteractActionType.USE_ITEM && event.actionType != InteractActionType.USE_BLOCK)) return

            val heldItem = FeeshMod.mc.player?.mainHandItem
            if (heldItem == null || heldItem.isEmpty) return

            val heldItemName = heldItem.hoverName.string
            val heldItemDisplayName = heldItem.hoverName.getFormattedString()
            
            if (isUmberellaTrackingEnabled() && heldItemName == "Umberella") {
                lastUmberellaInteractTimeMs = System.currentTimeMillis()
            }

            if (isFlareTrackingEnabled() && heldItemName.endsWith("Flare")) {
                // Prevent multiple clicks
                if (flareData.lastPlacedAt != null && Date().time - flareData.lastPlacedAt!!.time < 500) return

                // Give time for a firework rocket to appear after click
                Timer(true).schedule(object : TimerTask() {
                    override fun run() {
                        trackFlareRocketNearby(heldItemDisplayName)
                    }
                }, 500)
            }

            if (isDwarvenLanternTrackingEnabled() && isHeldItemDwarvenLantern(heldItemName)) {
                lastDwarvenLanternInteractTimeMs = System.currentTimeMillis()
            }
        }
    }

    private fun onArmorStandDetailsLoaded(event: ArmorStandDetailsLoadedEvent) {
        CommonUtils.runWithCatching("Failed to handle deployable armor stand spawn") {
            if (!WorldUtils.isInSkyblock()) return
            if (!isDwarvenLanternTrackingEnabled() && !isUmberellaTrackingEnabled()) return

            val armorStand = event.entity
            val player = FeeshMod.mc.player ?: return
            if (EntityUtils.getDistance(player, armorStand) > 5.0) return

            val nowMs = System.currentTimeMillis()
            if (nowMs - lastDwarvenLanternInteractTimeMs > 1000L && nowMs - lastUmberellaInteractTimeMs > 1000L) return

            val name = event.customNameUnformatted

            if (isDwarvenLanternTrackingEnabled() &&
                isDwarvenLanternArmorStandName(name) &&
                (name.endsWith("300s") || name.endsWith("600s")) &&
                nowMs - lastDwarvenLanternInteractTimeMs <= 1000L
            ) {
                dwarvenLanternData.id = armorStand.id
                val formattedName = event.customNameFormatted
                dwarvenLanternData.itemDisplayName = formattedName.replace(Regex(" §.+\\d+s"), "").replace(BOLD.code, "").trim().ifBlank { "Dwarven Lantern" }
            } else if (isUmberellaTrackingEnabled() &&
                (name == "Umberella 300s" || name == "Umberella 600s") &&
                nowMs - lastUmberellaInteractTimeMs <= 1000L
            ) {
                umberellaData.id = armorStand.id
            }
        }
    }

    private fun trackFlareRocketNearby(heldItemName: String) {
        CommonUtils.runWithCatching("Failed to track Flare rocket nearby") {
            val player = FeeshMod.mc.player ?: return
            val world = FeeshMod.mc.level ?: return

            val flareRockets = world.entitiesForRendering()
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

    private fun isDwarvenLanternTrackingEnabled(): Boolean {
        return (Alerts.alertOnDeployableExpiresSoon && Alerts.alertOnDeployableTypes.contains(DeployableTypes.DWARVEN_LANTERN)) ||
               (Overlays.deployablesTimerOverlay && Overlays.deployablesOverlayTypes.contains(DeployableTypes.DWARVEN_LANTERN))
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

    private fun resetDwarvenLantern() {
        dwarvenLanternData = DwarvenLanternData()
    }

    private fun trackDeployablesStatus() {
        if (!WorldUtils.isInSkyblock()) return

        val world = FeeshMod.mc.level ?: return
        val entities = world.entitiesForRendering().filterIsInstance<ArmorStand>()

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
        if (isDwarvenLanternTrackingEnabled()) {
            trackDwarvenLanternStatus(entities)
        }
    }

    private fun trackTotemStatus(entities: List<ArmorStand>) {
        CommonUtils.runWithCatching("Failed to track Totem status") {
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
                (totemData.lastAlertAt == null || Date().time - totemData.lastAlertAt!!.time >= 5000)
            ) {
                playAlert("${DARK_PURPLE}Totem of Corruption", totemData)
            }
        }
    }

    private fun trackBlackHoleStatus(entities: List<ArmorStand>) {
        CommonUtils.runWithCatching("Failed to track Black Hole status") {
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
                (blackHoleData.lastAlertAt == null || Date().time - blackHoleData.lastAlertAt!!.time >= 5000)
            ) {
                playAlert("${DARK_PURPLE}Black Hole", blackHoleData)
            }
        }
    }

    private fun trackUmberellaStatus(entities: List<ArmorStand>) {
        CommonUtils.runWithCatching("Failed to track Umberella status") {
            if (!WorldUtils.isInSkyblock() || entities.isEmpty() || !isUmberellaTrackingEnabled()) {
                resetUmberella()
                return
            }

            val umberellaArmorStand = entities.find { entity ->
                entity.customName?.string?.startsWith("Umberella ") == true &&
                entity.id == umberellaData.id
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
                (umberellaData.lastAlertAt == null || Date().time - umberellaData.lastAlertAt!!.time >= 5000)
            ) {
                playAlert("${BLUE}Umberella", umberellaData)
            }
        }
    }

    private fun isDwarvenLanternArmorStandName(name: String): Boolean =
        DWARVEN_LANTERN_NAME_PREFIXES.any { name.startsWith(it) }

    private fun trackDwarvenLanternStatus(entities: List<ArmorStand>) {
        CommonUtils.runWithCatching("Failed to track Dwarven Lantern status") {
            if (!WorldUtils.isInSkyblock() || entities.isEmpty() || !isDwarvenLanternTrackingEnabled()) {
                resetDwarvenLantern()
                return
            }

            val lanternArmorStand = entities.find { entity ->
                entity.customName?.string?.let { isDwarvenLanternArmorStandName(it) } == true &&
                entity.id == dwarvenLanternData.id
            }

            if (lanternArmorStand == null) {
                resetDwarvenLantern()
                return
            }

            val name = lanternArmorStand.customName?.string ?: ""
            val seconds = name.split(" ").lastOrNull()?.replace("s", "")?.toIntOrNull() ?: return
            dwarvenLanternData.remainingTime = fromSecondsToTimeString(seconds)

            if (Alerts.alertOnDeployableExpiresSoon &&
                Alerts.alertOnDeployableTypes.contains(DeployableTypes.DWARVEN_LANTERN) &&
                dwarvenLanternData.remainingTime == "${SECONDS_BEFORE_EXPIRATION}s" &&
                (dwarvenLanternData.lastAlertAt == null || Date().time - dwarvenLanternData.lastAlertAt!!.time >= 5000)
            ) {
                playAlert(dwarvenLanternData.itemDisplayName!!, dwarvenLanternData)
            }
        }
    }

    private fun trackFlareStatus() {
        CommonUtils.runWithCatching("Failed to track Flare status") {
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
                (flareData.lastAlertAt == null || Date().time - flareData.lastAlertAt!!.time >= 5000)
            ) {
                playAlert(flareData.itemDisplayName ?: "Flare", flareData)
            }
        }
    }

    private fun playAlert(itemDisplayName: String, data: BaseDeployableData) {
        data.lastAlertAt = Date()
        CommonUtils.showTitle("$itemDisplayName ${RED}expires soon")
        ChatUtils.sendLocalChat("${WHITE}Your $itemDisplayName ${WHITE}expires soon.", true)
        if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(Sounds.FEESH_NOTIFICATION_BELL)
        else SoundUtils.playSound()
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

        if (Overlays.deployablesOverlayTypes.contains(DeployableTypes.DWARVEN_LANTERN) && !dwarvenLanternData.remainingTime.isNullOrEmpty() && dwarvenLanternData.remainingTime != "00s") {
            val timerColor = fromTimeStringToSeconds(dwarvenLanternData.remainingTime!!) <= SECONDS_BEFORE_EXPIRATION
            val colorCode = if (timerColor) RED.code else WHITE.code
            lines.add("${dwarvenLanternData.itemDisplayName!!}: $colorCode${dwarvenLanternData.remainingTime}")
        }

        if (lines.isNotEmpty()) {
            gui.setLines(lines.map { LineInfo(it) })
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
