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
import com.github.sleepypanda.feesh.utils.ChatUtils.getUnformattedString
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
    private const val TICKS_PER_CHECK = 20

    private val FLARE_DISAPPEARED_PATTERN = Regex("^Your flare disappeared because you were too far away\\!$")
    private val PREVIOUS_DEPLOYABLE_REMOVED_PATTERN = Regex("^Your previous (.*) was removed\\!$")

    private open class BaseDeployableData(
        val isShortLiving: Boolean = false,
    ) {
        var remainingTimeStr: String? = null
        var remainingSeconds: Int? = null
        var isAlerted: Boolean = false
        var lastAlertAt: Date? = null
    }

    private class TotemData : BaseDeployableData()

    private class BlackHoleData : BaseDeployableData()

    private class UmberellaData : BaseDeployableData() {
        var id: Int? = null
    }

    private class FlareData : BaseDeployableData() {
        var lastPlacedAt: Date? = null
        var itemDisplayName: String? = null
    }

    private class DwarvenLanternData : BaseDeployableData() {
        var id: Int? = null
        var itemDisplayName: String? = null
    }

    private class FluxData : BaseDeployableData(isShortLiving = true) {
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
    private val FLUX_NAME_PREFIXES = listOf(
        "Overflux",
        "Plasmaflux",
        "Mana Flux",
    )

    private fun isHeldItemDwarvenLantern(heldItemName: String): Boolean =
        DWARVEN_LANTERN_NAME_PREFIXES.any { prefix -> heldItemName == prefix }

    private fun isHeldItemFlux(heldItemName: String): Boolean =
        FLUX_NAME_PREFIXES.any { prefix -> heldItemName.contains(prefix + " Power Orb") }

    private var totemData = TotemData()
    private var blackHoleData = BlackHoleData()
    private var umberellaData = UmberellaData()
    private var flareData = FlareData()
    private var dwarvenLanternData = DwarvenLanternData()
    private var fluxData = FluxData()

    private var tickCounter = 0

    // We can't know owner for those items, so we track when there was an interaction, to try ignoring spawns from other players.
    private var lastDwarvenLanternInteractTimeMs: Long = 0L
    private var lastUmberellaInteractTimeMs: Long = 0L
    private var lastFluxInteractTimeMs: Long = 0L

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
        resetFlux()
        lastUmberellaInteractTimeMs = 0L
        lastDwarvenLanternInteractTimeMs = 0L
        lastFluxInteractTimeMs = 0L
    }

    private fun onChat(event: ChatEvent) {
        CommonUtils.runWithCatching("Failed to handle chat event") {
            if (!WorldUtils.isInSkyblock()) return

            if (FLARE_DISAPPEARED_PATTERN.matches(event.unformattedText)) {
                resetFlare()
            } else if (PREVIOUS_DEPLOYABLE_REMOVED_PATTERN.matches(event.unformattedText)) {
                val matchResult = PREVIOUS_DEPLOYABLE_REMOVED_PATTERN.find(event.unformattedText) ?: return
                val captured: String = matchResult.groupValues.getOrNull(1) ?: ""
                if (captured.contains("flare", ignoreCase = true)) resetFlare()
            }
        }
    }

    private fun onPlayerInteract(event: PlayerInteractEvent) {
        CommonUtils.runWithCatching("Failed to handle deployable interaction") {
            if (!WorldUtils.isInSkyblock()) return
            if (!event.isMainHand || (event.actionType != InteractActionType.USE_ITEM && event.actionType != InteractActionType.USE_BLOCK)) return

            val heldItem = FeeshMod.mc.player?.mainHandItem
            if (heldItem == null || heldItem.isEmpty) return

            val heldItemName = heldItem.hoverName.getUnformattedString()
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

            if (isFluxTrackingEnabled() && isHeldItemFlux(heldItemName)) {
                lastFluxInteractTimeMs = System.currentTimeMillis()
            }
        }
    }

    private fun onArmorStandDetailsLoaded(event: ArmorStandDetailsLoadedEvent) {
        CommonUtils.runWithCatching("Failed to handle deployable armor stand spawn") {
            if (!WorldUtils.isInSkyblock()) return
            if (!isDwarvenLanternTrackingEnabled() && !isUmberellaTrackingEnabled() && !isFluxTrackingEnabled()) return

            val armorStand = event.entity
            val player = FeeshMod.mc.player ?: return
            if (EntityUtils.getDistance(player, armorStand) > 5.0) return

            val nowMs = System.currentTimeMillis()
            if (nowMs - lastDwarvenLanternInteractTimeMs > 1000L &&
                nowMs - lastUmberellaInteractTimeMs > 1000L &&
                nowMs - lastFluxInteractTimeMs > 1000L
            ) return

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
            } else if (isFluxTrackingEnabled() &&
                isFluxArmorStandName(name) &&
                (name.endsWith("30s") || name.endsWith("60s") || name.endsWith("120s")) &&
                nowMs - lastFluxInteractTimeMs <= 1000L
            ) {
                fluxData.id = armorStand.id
                val formattedName = event.customNameFormatted
                fluxData.itemDisplayName = formattedName.replace(Regex(" §.+\\d+s"), "").replace(BOLD.code, "").trim().ifBlank { "Flux" }
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
                setRemaining(flareData, 180) // TODO how to track if pet has Bubblegum which makes timer x2
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

    private fun isFluxTrackingEnabled(): Boolean {
        return (Alerts.alertOnDeployableExpiresSoon && Alerts.alertOnDeployableTypes.contains(DeployableTypes.FLUX)) ||
               (Overlays.deployablesTimerOverlay && Overlays.deployablesOverlayTypes.contains(DeployableTypes.FLUX))
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

    private fun resetFlux() {
        fluxData = FluxData()
    }

    private fun trackDeployablesStatus() {
        CommonUtils.runWithCatching("Failed to track deployables status") {
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
            if (isFluxTrackingEnabled()) {
                trackFluxStatus(entities)
            }
        }
    }

    private fun trackTotemStatus(entities: List<ArmorStand>) {
        CommonUtils.runWithCatching("Failed to track Totem status") {
            if (!WorldUtils.isInSkyblock() || entities.isEmpty()) {
                resetTotem()
                return
            }

            val playerName = PlayerUtils.getUnformattedName()
            if (playerName.isNullOrEmpty()) return

            val ownerArmorStand = entities.find { entity ->
                val name = entity.customName.getUnformattedString()
                name.contains("Owner:") && name.contains(playerName)
            }

            if (ownerArmorStand == null) {
                resetTotem()
                return
            }

            val ownerArmorStandId = ownerArmorStand.id
            val totemArmorStand = entities.find { it.id == ownerArmorStandId - 2 }
            if (totemArmorStand == null) return

            val totemArmorStandName = totemArmorStand.customName.getUnformattedString()
            if (totemArmorStandName != "Totem of Corruption") {
                resetTotem()
                return
            }

            val remainingArmorStand = entities.find { it.id == ownerArmorStandId - 1 }
            if (remainingArmorStand == null) return

            val remainingArmorStandName = remainingArmorStand.customName.getUnformattedString()
            if (!remainingArmorStandName.contains("Remaining: ")) {
                resetTotem()
                return
            }

            val remainingTimeStr = remainingArmorStandName.split("Remaining: ").lastOrNull() ?: ""
            setRemaining(totemData, fromTimeStringToSeconds(remainingTimeStr))
            maybeAlertExpiresSoon(
                totemData,
                totemData.remainingSeconds!!,
                DeployableTypes.TOTEM_OF_CORRUPTION,
                "${DARK_PURPLE}Totem of Corruption",
            )
        }
    }

    private fun trackBlackHoleStatus(entities: List<ArmorStand>) {
        CommonUtils.runWithCatching("Failed to track Black Hole status") {
            if (!WorldUtils.isInSkyblock() || entities.isEmpty()) {
                resetBlackHole()
                return
            }

            val playerName = PlayerUtils.getUnformattedName()
            if (playerName.isNullOrEmpty()) return

            val ownerArmorStand = entities.find { entity ->
                val name = entity.customName.getUnformattedString()
                name.contains("Spawned by:") && name.contains(playerName)
            }

            if (ownerArmorStand == null) {
                resetBlackHole()
                return
            }

            val ownerArmorStandId = ownerArmorStand.id
            val blackHoleArmorStand = entities.find { it.id == ownerArmorStandId + 1 }
            if (blackHoleArmorStand == null) return

            val blackHoleArmorStandName = blackHoleArmorStand.customName.getUnformattedString()
            if (!blackHoleArmorStandName.startsWith("Black Hole")) {
                resetBlackHole()
                return
            }

            // When a Black Hole is placed, it has no timer for a second
            val timer = blackHoleArmorStandName.substringAfter("Black Hole").trim()
            val seconds = if (timer.isNotEmpty()) {
                timer.replace("s", "").toIntOrNull() ?: 180
            } else 180
            setRemaining(blackHoleData, seconds)
            maybeAlertExpiresSoon(blackHoleData, seconds, DeployableTypes.BLACK_HOLE, "${DARK_PURPLE}Black Hole")
        }
    }

    private fun trackUmberellaStatus(entities: List<ArmorStand>) {
        CommonUtils.runWithCatching("Failed to track Umberella status") {
            if (!WorldUtils.isInSkyblock() || entities.isEmpty() || !isUmberellaTrackingEnabled()) {
                resetUmberella()
                return
            }

            val umberellaArmorStand = entities.find { entity ->
                entity.customName.getUnformattedString().startsWith("Umberella ") &&
                entity.id == umberellaData.id
            }

            if (umberellaArmorStand == null) {
                resetUmberella()
                return
            }

            val name = umberellaArmorStand.customName.getUnformattedString()
            val seconds = name.split("Umberella ").lastOrNull()?.replace("s", "")?.toIntOrNull() ?: return
            setRemaining(umberellaData, seconds)
            maybeAlertExpiresSoon(umberellaData, seconds, DeployableTypes.UMBERELLA, "${BLUE}Umberella")
        }
    }

    private fun isDwarvenLanternArmorStandName(name: String): Boolean =
        DWARVEN_LANTERN_NAME_PREFIXES.any { name.startsWith(it) }

    private fun isFluxArmorStandName(name: String): Boolean =
        FLUX_NAME_PREFIXES.any { name.startsWith(it) }

    private fun trackDwarvenLanternStatus(entities: List<ArmorStand>) {
        CommonUtils.runWithCatching("Failed to track Dwarven Lantern status") {
            if (!WorldUtils.isInSkyblock() || entities.isEmpty() || !isDwarvenLanternTrackingEnabled()) {
                resetDwarvenLantern()
                return
            }

            val lanternArmorStand = entities.find { entity ->
                entity.customName.getUnformattedString().let { isDwarvenLanternArmorStandName(it) } &&
                entity.id == dwarvenLanternData.id
            }

            if (lanternArmorStand == null) {
                resetDwarvenLantern()
                return
            }

            val name = lanternArmorStand.customName.getUnformattedString()
            val seconds = name.split(" ").lastOrNull()?.replace("s", "")?.toIntOrNull() ?: return
            setRemaining(dwarvenLanternData, seconds)
            maybeAlertExpiresSoon(
                dwarvenLanternData,
                seconds,
                DeployableTypes.DWARVEN_LANTERN,
                dwarvenLanternData.itemDisplayName!!,
            )
        }
    }

    private fun trackFluxStatus(entities: List<ArmorStand>) {
        CommonUtils.runWithCatching("Failed to track Flux status") {
            if (!WorldUtils.isInSkyblock() || entities.isEmpty() || !isFluxTrackingEnabled()) {
                resetFlux()
                return
            }

            val fluxArmorStand = entities.find { entity ->
                entity.customName.getUnformattedString().let { isFluxArmorStandName(it) } &&
                entity.id == fluxData.id
            }

            if (fluxArmorStand == null) {
                resetFlux()
                return
            }

            val name = fluxArmorStand.customName.getUnformattedString()
            val seconds = name.split(" ").lastOrNull()?.replace("s", "")?.toIntOrNull() ?: return
            setRemaining(fluxData, seconds)
            maybeAlertExpiresSoon(
                fluxData,
                seconds,
                DeployableTypes.FLUX,
                fluxData.itemDisplayName!!,
            )
        }
    }

    private fun trackFlareStatus() {
        CommonUtils.runWithCatching("Failed to track Flare status") {
            if (!WorldUtils.isInSkyblock() || !isFlareTrackingEnabled()) {
                resetFlare()
                return
            }

            val remainingSeconds = flareData.remainingSeconds
            if (remainingSeconds == null || remainingSeconds <= 0) {
                resetFlare()
                return
            }

            setRemaining(flareData, remainingSeconds - 1)
            maybeAlertExpiresSoon(
                flareData,
                flareData.remainingSeconds!!,
                DeployableTypes.FLARE,
                flareData.itemDisplayName ?: "Flare",
            )
        }
    }

    private fun expiresSoonSecondsFor(data: BaseDeployableData): Int =
        return (if (data.isShortLiving) Alerts.shortLivingDeployableExpiresSoonSeconds else Alerts.deployableExpiresSoonSeconds)
            .coerceAtLeast(1)

    private fun maybeAlertExpiresSoon(
        data: BaseDeployableData,
        remainingSeconds: Int,
        type: DeployableTypes,
        itemDisplayName: String,
    ) {
        val expiresSoonSeconds = expiresSoonSecondsFor(data)
        if (remainingSeconds > expiresSoonSeconds) {
            data.isAlerted = false
            data.lastAlertAt = null
            return
        }
        if (!Alerts.alertOnDeployableExpiresSoon || !Alerts.alertOnDeployableTypes.contains(type) || data.isAlerted) return

        data.isAlerted = true
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

        if (Overlays.deployablesOverlayTypes.contains(DeployableTypes.UMBERELLA)) {
            val seconds = umberellaData.remainingSeconds
            if (seconds != null && seconds > 0) {
                val colorCode = if (seconds <= expiresSoonSecondsFor(umberellaData)) RED.code else WHITE.code
                lines.add("${BLUE.code}Umberella: $colorCode${umberellaData.remainingTimeStr}")
            }
        }

        if (Overlays.deployablesOverlayTypes.contains(DeployableTypes.FLARE)) {
            val seconds = flareData.remainingSeconds
            if (seconds != null && seconds > 0) {
                val colorCode = if (seconds <= expiresSoonSecondsFor(flareData)) RED.code else WHITE.code
                lines.add("${flareData.itemDisplayName}: $colorCode${flareData.remainingTimeStr}")
            }
        }

        if (Overlays.deployablesOverlayTypes.contains(DeployableTypes.BLACK_HOLE)) {
            val seconds = blackHoleData.remainingSeconds
            if (seconds != null && seconds > 0) {
                val colorCode = if (seconds <= expiresSoonSecondsFor(blackHoleData)) RED.code else WHITE.code
                lines.add("${DARK_PURPLE.code}Black Hole: $colorCode${blackHoleData.remainingTimeStr}")
            }
        }

        if (Overlays.deployablesOverlayTypes.contains(DeployableTypes.TOTEM_OF_CORRUPTION)) {
            val seconds = totemData.remainingSeconds
            if (seconds != null && seconds > 0) {
                val colorCode = if (seconds <= expiresSoonSecondsFor(totemData)) RED.code else WHITE.code
                lines.add("${DARK_PURPLE.code}Totem of Corruption: $colorCode${totemData.remainingTimeStr}")
            }
        }

        if (Overlays.deployablesOverlayTypes.contains(DeployableTypes.DWARVEN_LANTERN)) {
            val seconds = dwarvenLanternData.remainingSeconds
            if (seconds != null && seconds > 0) {
                val colorCode = if (seconds <= expiresSoonSecondsFor(dwarvenLanternData)) RED.code else WHITE.code
                lines.add("${dwarvenLanternData.itemDisplayName!!}: $colorCode${dwarvenLanternData.remainingTimeStr}")
            }
        }

        if (Overlays.deployablesOverlayTypes.contains(DeployableTypes.FLUX)) {
            val seconds = fluxData.remainingSeconds
            if (seconds != null && seconds > 0) {
                val colorCode = if (seconds <= expiresSoonSecondsFor(fluxData)) RED.code else WHITE.code
                lines.add("${fluxData.itemDisplayName!!}: $colorCode${fluxData.remainingTimeStr}")
            }
        }

        if (lines.isNotEmpty()) {
            gui.setLines(lines.map { LineInfo(it) })
        }
    }

    private fun setRemaining(data: BaseDeployableData, seconds: Int) {
        if (seconds <= 0) {
            data.remainingSeconds = 0
            data.remainingTimeStr = null
            return
        }
        data.remainingSeconds = seconds
        data.remainingTimeStr = fromSecondsToTimeString(seconds)
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
            if (timeStr.isEmpty()) return 0
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
