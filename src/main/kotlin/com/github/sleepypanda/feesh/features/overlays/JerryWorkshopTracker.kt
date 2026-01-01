package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.TabListUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.ScreenRenderEvent
import com.github.sleepypanda.feesh.events.GameRenderEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import java.awt.Color

object JerryWorkshopTracker {
    data class CatchCounterData(var catchesSinceLast: Int = 0, var lastCatchTime: Date? = null, var averageCatches: Int = 0, var catchesHistory: List<Int> = emptyList())
    data class JerryWorkshopTrackerData(val yeti: CatchCounterData = CatchCounterData(), val reindrake: CatchCounterData = CatchCounterData())

    private var data = JerryWorkshopTrackerData()

    private val yeti = SeaCreatures.allSeaCreatures.find { it.name === "Yeti" }!!
    private val reindrake = SeaCreatures.allSeaCreatures.find { it.name === "Reindrake" }!!

    private val color = Color(255, 255, 255, 255).rgb

    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
        EventBus.subscribe(GameRenderEvent::class, ::onRender)
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!Overlays.jerryWorkshopTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return

        val seaCreatureName = event.seaCreatureName

        if (seaCreatureName == yeti.name) onYeti()
        else if (seaCreatureName == reindrake.name) onReindrake()
        else onOtherSeaCreature()
    }

    private fun onYeti() {
        if (!Overlays.jerryWorkshopTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return
        
        data.yeti.catchesSinceLast++
        val catchesSinceLast = data.yeti.catchesSinceLast
        ChatUtils.sendLocalChat(getMessage(catchesSinceLast, data.yeti.lastCatchTime, yeti.name, yeti.rarityColorCode), true)

        data.yeti.lastCatchTime = Date()
        data.yeti.catchesHistory = (listOf(catchesSinceLast) + data.yeti.catchesHistory).take(5)
        data.yeti.averageCatches = data.yeti.catchesHistory.average().toInt()
        data.yeti.catchesSinceLast = 0
        data.reindrake.catchesSinceLast++
    }

    private fun onReindrake() {
        if (!Overlays.jerryWorkshopTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return

        data.reindrake.catchesSinceLast++
        val catchesSinceLast = data.reindrake.catchesSinceLast
        ChatUtils.sendLocalChat(getMessage(catchesSinceLast, data.reindrake.lastCatchTime, reindrake.name, reindrake.rarityColorCode), true)

        data.reindrake.lastCatchTime = Date()
        data.reindrake.catchesHistory = (listOf(catchesSinceLast) + data.reindrake.catchesHistory).take(5)
        data.reindrake.averageCatches = data.yeti.catchesHistory.average().toInt()
        data.reindrake.catchesSinceLast = 0
        data.yeti.catchesSinceLast++
    }

    private fun onOtherSeaCreature() {
        if (!Overlays.jerryWorkshopTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return

        data.yeti.catchesSinceLast++
        data.reindrake.catchesSinceLast++
    }

    private fun onRender(event: GameRenderEvent) {
        if (!Overlays.jerryWorkshopTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.JERRY_WORKSHOP) return
        if (!hasData()) return
        if (!PlayerUtils.isFishingHookSeenMinutesAgo(5)) return
        // TODO (new Date() - getLastFishingHookSeenAt() > 10 * 60 * 1000) ||
        
        event.drawContext.matrices.pushMatrix()
        event.drawContext.matrices.scale(1.0f, 1.0f)

        val textRenderer = event.mcClient.textRenderer
        var y = 10

        val title = "${AQUA}${BOLD}Jerry Workshop tracker"
        event.drawContext.drawText(textRenderer, Text.literal(title), 10, y, color, true)
        y += textRenderer.fontHeight + 2

        val yetiLine = "${GOLD}${yeti.name}: ${WHITE}${data.yeti.catchesSinceLast} ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}${data.yeti.averageCatches}${DARK_GRAY})"
        event.drawContext.drawText(textRenderer, Text.literal(yetiLine), 10, y, color, true)
        y += textRenderer.fontHeight + 2

        var yetiLastOnLine: String
        if (data.yeti.lastCatchTime != null) {
            val lastOn = formatDate(data.yeti.lastCatchTime)
            val since = formatTimeElapsed(data.yeti.lastCatchTime)
            yetiLastOnLine = "${GRAY}Last on: ${WHITE}${lastOn} ${GRAY}(${WHITE}${since} ago${GRAY})"
        } else {
            yetiLastOnLine = "${GRAY}Last on: ${WHITE}N/A${GRAY}"
        }
        event.drawContext.drawText(textRenderer, Text.literal(yetiLastOnLine), 10, y, color, true)
        y += textRenderer.fontHeight + 2

        val reindrakeLine = "${LIGHT_PURPLE}${reindrake.name}: ${WHITE}${data.reindrake.catchesSinceLast} ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}${data.reindrake.averageCatches}${DARK_GRAY})"
        event.drawContext.drawText(textRenderer, Text.literal(reindrakeLine), 10, y, color, true)
        y += textRenderer.fontHeight + 2

        var reindrakeLastOnLine: String
        if (data.reindrake.lastCatchTime != null) {
            val lastOn = formatDate(data.reindrake.lastCatchTime)
            val since = formatTimeElapsed(data.reindrake.lastCatchTime)
            reindrakeLastOnLine = "${GRAY}Last on: ${WHITE}${lastOn} ${GRAY}(${WHITE}${since} ago${GRAY})"
        } else {
            reindrakeLastOnLine = "${GRAY}Last on: ${WHITE}N/A${GRAY}"
        }
        event.drawContext.drawText(textRenderer, Text.literal(reindrakeLastOnLine), 10, y, color, true)
        y += textRenderer.fontHeight + 2

        // TODO do not check every render event, check once per second in separate function executed on timer
        val islandOpen = TabListUtils.getLineAfter("Island open:")
        val islandClosesIn = TabListUtils.getLineAfter("Island closes in:")
        if (!islandOpen.isNullOrEmpty()) {
            val islandOpenLine = "${GRAY}Island open: ${WHITE}${islandOpen}"
            event.drawContext.drawText(textRenderer, Text.literal(islandOpenLine), 10, y, color, true)
            y += textRenderer.fontHeight + 2
        } else if (!islandClosesIn.isNullOrEmpty()) {
            val islandClosesInLine = "${GRAY}Island closes in: ${WHITE}${islandClosesIn}"
            event.drawContext.drawText(textRenderer, Text.literal(islandClosesInLine), 10, y, color, true)
            y += textRenderer.fontHeight + 2
        }

        event.drawContext.matrices.popMatrix()
    }

    private fun hasData(): Boolean {
        return hasYetiData() || hasReindrakeData()
    }

    private fun hasYetiData(): Boolean {
        return data.yeti.catchesSinceLast > 0 || data.yeti.catchesHistory.isNotEmpty() || data.yeti.lastCatchTime != null
    }

    private fun hasReindrakeData(): Boolean {
        return data.reindrake.catchesSinceLast > 0 || data.reindrake.catchesHistory.isNotEmpty() || data.reindrake.lastCatchTime != null
    }

    private fun formatTimeElapsed(lastCatchTime: Date?): String {
        if (lastCatchTime == null) return ""

        val now = Date()
        val diffMillis = now.time - lastCatchTime.time
        val diffSeconds = diffMillis / 1000

        val days = TimeUnit.SECONDS.toDays(diffSeconds)
        val hours = TimeUnit.SECONDS.toHours(diffSeconds) % 24
        val minutes = TimeUnit.SECONDS.toMinutes(diffSeconds) % 60

        return when {
            days > 0 -> "${days}d ${hours}h ${minutes}m"
            hours > 0 -> "${hours}h ${minutes}m"
            minutes < 1 -> "less than 1m"
            else -> "${minutes}m"
        }
    }

    private fun formatDate(date: Date?): String {
        if (date == null) return ""
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return formatter.format(date)
    }

    private fun getMessage(catchesSinceLast: Int, lastCatchTime: Date?, seaCreatureName: String, rarityCode: String): String {
        val b2bText = if (catchesSinceLast == 1) "${RED}B2B! " else ""
        val elapsedTimeText = if (lastCatchTime != null) " ${GRAY}(${WHITE}${formatTimeElapsed(lastCatchTime)}${GRAY})" else ""
        val catchesText = "${WHITE}${catchesSinceLast} ${GRAY}${if (catchesSinceLast == 1) "catch" else "catches"}"
        val seaCreatureDisplayName = "${rarityCode}${seaCreatureName}"
        return "${b2bText}${GRAY}It took ${catchesText}${elapsedTimeText} to get the ${seaCreatureDisplayName}${GRAY}."
    }
}