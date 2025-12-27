package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.ScreenRenderEvent
import com.github.sleepypanda.feesh.events.ScreenPostRenderEvent
import com.github.sleepypanda.feesh.events.GameRenderEvent
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import java.awt.Color

object JerryWorkshopTracker {
    data class CatchCounterData(var catchesSinceLast: Int = 0, var lastCatchTime: Date = Date(), var averageCatches: Int = 0, var catchesHistory: List<Int> = emptyList())
    data class JerryWorkshopTrackerData(val yeti: CatchCounterData = CatchCounterData(), val reindrake: CatchCounterData = CatchCounterData())

    private var data = JerryWorkshopTrackerData()

    val yeti = SeaCreatures.allSeaCreatures.find { it.name === "Yeti" }!!
    val reindrake = SeaCreatures.allSeaCreatures.find { it.name === "Reindrake" }!!

    fun init() {
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreature)
        EventBus.subscribe(ScreenRenderEvent::class, ::testRender)
        EventBus.subscribe(ScreenPostRenderEvent::class, ::testPostRender)
        EventBus.subscribe(GameRenderEvent::class, ::testRenderHud)
        //EventBus.subscribe(ScreenRenderEvent::class, ::onRender)
    }

    private fun testRender(event: ScreenRenderEvent) {
        if (!WorldUtils.isInSkyblock()) return
        if (System.currentTimeMillis() % 1000 == 0L) FeeshMod.LOGGER.info("FEESH Rendering test overlay")

        event.drawContext.matrices.pushMatrix()
        event.drawContext.matrices.scale(1.0f, 1.0f)

        val textRenderer = event.textRenderer
        event.drawContext.drawText(textRenderer, Text.literal("HELLO FROM FEESH"), 20, 20, Color(255, 255, 255, 200).rgb, true)

        event.drawContext.matrices.popMatrix()
    }

    private fun testPostRender(event: ScreenPostRenderEvent) {
        if (!WorldUtils.isInSkyblock()) return
        if (System.currentTimeMillis() % 1000 == 0L) FeeshMod.LOGGER.info("FEESH POST Rendering test overlay")

        // TODO if in chat or inventory
        event.drawContext.matrices.pushMatrix()
        event.drawContext.matrices.scale(1.0f, 1.0f)

        val textRenderer = event.textRenderer
        event.drawContext.drawText(textRenderer, Text.literal("HELLO FROM FEESH POSTRENDER"), 20, 30, Color(255, 255, 255, 200).rgb, true)

        event.drawContext.matrices.popMatrix()
    }

    private fun testRenderHud(event: GameRenderEvent) {
        if (!WorldUtils.isInSkyblock()) return
        if (System.currentTimeMillis() % 1000 == 0L) FeeshMod.LOGGER.info("FEESH InGameHud Rendering test overlay")

        // TODO if in chat or inventory
        event.drawContext.matrices.pushMatrix()
        event.drawContext.matrices.scale(1.0f, 1.0f)

        val textRenderer = event.textRenderer
        event.drawContext.drawText(textRenderer, Text.literal("HELLO FROM FEESH HUDRENDER"), 20, 40, Color(255, 255, 255, 200).rgb, true)

        event.drawContext.matrices.popMatrix()
    }

    private fun onSeaCreature(event: OwnSeaCreatureCaughtEvent) {
        if (!WorldUtils.isInSkyblock()) return // TODO check if in Jerry's Workshop

        val seaCreatureName = event.seaCreatureName

        if (seaCreatureName == yeti.name) onYeti()
        else if (seaCreatureName == reindrake.name) onReindrake()
        else onOtherSeaCreature()
    }

    private fun onYeti() {
        if (!WorldUtils.isInSkyblock()) return
        
        data.yeti.catchesSinceLast++
        val catchesSinceLast = data.yeti.catchesSinceLast
        ChatUtils.sendLocalChat(getMessage(catchesSinceLast, yeti.name, yeti.rarityColorCode), true)

        data.yeti.lastCatchTime = Date()
        data.yeti.catchesHistory = (listOf(catchesSinceLast) + data.yeti.catchesHistory).take(5)
        data.yeti.averageCatches = data.yeti.catchesHistory.average().toInt()
        data.yeti.catchesSinceLast = 0
        data.reindrake.catchesSinceLast++
    }

    private fun onReindrake() {
        if (!WorldUtils.isInSkyblock()) return

        data.reindrake.catchesSinceLast++
        val catchesSinceLast = data.reindrake.catchesSinceLast
        ChatUtils.sendLocalChat(getMessage(catchesSinceLast, reindrake.name, reindrake.rarityColorCode), true)

        data.reindrake.lastCatchTime = Date()
        data.reindrake.catchesHistory = (listOf(catchesSinceLast) + data.reindrake.catchesHistory).take(5)
        data.reindrake.averageCatches = data.yeti.catchesHistory.average().toInt()
        data.reindrake.catchesSinceLast = 0
        data.yeti.catchesSinceLast++
    }

    private fun onOtherSeaCreature() {
        if (!WorldUtils.isInSkyblock()) return
        // If on Jerry
        data.yeti.catchesSinceLast++
        data.reindrake.catchesSinceLast++
    }

    private fun onRender(event: ScreenRenderEvent) {
        if (!WorldUtils.isInSkyblock()) return
        if (!hasData()) return
        
        if (System.currentTimeMillis() % 1000 == 0L) FeeshMod.LOGGER.info("FEESH Rendering overlay")

        event.drawContext.matrices.pushMatrix()
        event.drawContext.matrices.scale(1.0f, 1.0f)

        val textRenderer = event.mcClient.textRenderer
        var y = 10

        val title = "${AQUA}${BOLD}Jerry Workshop tracker"
        event.drawContext.drawText(textRenderer, Text.literal(title), 10, y, 0xFFFFFFF, true)
        y += textRenderer.fontHeight + 2

        if (hasYetiData()) {
            val yeti = SeaCreatures.allSeaCreatures.find { it.name == "Yeti" }!!
            val yetiLine = "${GOLD}${yeti.name}: ${WHITE}${data.yeti.catchesSinceLast} ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}${data.yeti.averageCatches}${DARK_GRAY})"
            event.drawContext.drawText(textRenderer, Text.literal(yetiLine), 10, y, 0xFFFFFFF, true)
            y += textRenderer.fontHeight + 2

            if (data.yeti.catchesHistory.isNotEmpty()) {
                val yetiLastOn = formatTimeElapsed(data.yeti.lastCatchTime)
                val yetiLastOnDate = formatDate(data.yeti.lastCatchTime)
                val yetiLastOnLine = "${GRAY}Last on: ${WHITE}${yetiLastOn} ${GRAY}(${WHITE}${yetiLastOnDate}${GRAY})"
                event.drawContext.drawText(textRenderer, Text.literal(yetiLastOnLine), 10, y, 0xFFFFFFF, true)
                y += textRenderer.fontHeight + 2
            }
        }

        if (hasReindrakeData()) {
            val reindrake = SeaCreatures.allSeaCreatures.find { it.name == "Reindrake" }!!
            val reindrakeLine = "${LIGHT_PURPLE}${reindrake.name}: ${WHITE}${data.reindrake.catchesSinceLast} ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}${data.reindrake.averageCatches}${DARK_GRAY})"
            event.drawContext.drawText(textRenderer, Text.literal(reindrakeLine), 10, y, 0xFFFFFFF, true)
            y += textRenderer.fontHeight + 2

            if (data.reindrake.catchesHistory.isNotEmpty()) {
                val reindrakeLastOn = formatTimeElapsed(data.reindrake.lastCatchTime)
                val reindrakeLastOnDate = formatDate(data.reindrake.lastCatchTime)
                val reindrakeLastOnLine = "${GRAY}Last on: ${WHITE}${reindrakeLastOn} ${GRAY}(${WHITE}${reindrakeLastOnDate}${GRAY})"
                event.drawContext.drawText(textRenderer, Text.literal(reindrakeLastOnLine), 10, y, 0xFFFFFFF, true)
            }
        }

        event.drawContext.matrices.popMatrix()
    }

    private fun hasData(): Boolean {
        return true // TODO remove
        return hasYetiData() || hasReindrakeData()
    }

    private fun hasYetiData(): Boolean {
        return data.yeti.catchesSinceLast > 0 || data.yeti.catchesHistory.isNotEmpty()
    }

    private fun hasReindrakeData(): Boolean {
        return data.reindrake.catchesSinceLast > 0 || data.reindrake.catchesHistory.isNotEmpty()
    }

    private fun formatTimeElapsed(lastCatchTime: Date): String {
        val now = Date()
        val diffMillis = now.time - lastCatchTime.time
        val diffSeconds = diffMillis / 1000

        val days = TimeUnit.SECONDS.toDays(diffSeconds)
        val hours = TimeUnit.SECONDS.toHours(diffSeconds) % 24
        val minutes = TimeUnit.SECONDS.toMinutes(diffSeconds) % 60

        return when {
            days > 0 -> "${days}d ${hours}h ${minutes}m"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }

    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return formatter.format(date)
    }

    private fun getMessage(catchesSinceLast: Int, seaCreatureName: String, rarityCode: String): String {
        val b2bText = if (catchesSinceLast == 1) "${RED}B2B! " else "";
        //val catchesText = "${WHITE}${catchesSinceLast} ${GRAY}${catchesSinceLast == 1 ? 'catch' : 'catches'}";
        val seaCreatureDisplayName = "${rarityCode}${seaCreatureName}";
        return "${b2bText}${GRAY}It took ${WHITE}${data.yeti.catchesSinceLast} ${GRAY}catches to get the ${seaCreatureDisplayName}${GRAY}."
    }
}