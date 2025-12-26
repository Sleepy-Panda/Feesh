package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

object JerryWorkshopTracker {
    data class CatchCounterData(var catchesSinceLast: Int = 0, var lastCatchTime: Date = Date(), var averageCatches: Int = 0, var catchesHistory: List<Int> = emptyList())
    data class JerryWorkshopTrackerData(val yeti: CatchCounterData = CatchCounterData(), val reindrake: CatchCounterData = CatchCounterData())

    private var data = JerryWorkshopTrackerData()

    val yeti = SeaCreatures.allSeaCreatures.find { it.name === "Yeti" }!!
    val reindrake = SeaCreatures.allSeaCreatures.find { it.name === "Reindrake" }!!

    fun init() {
        RegisterUtils.chat(Regex(yeti.pattern)) { _, _ -> onYeti() }

        RegisterUtils.chat(Regex(reindrake.pattern)) { _, _ -> onReindrake() }

        SeaCreatures.allSeaCreatures
            .filter { it.name != yeti.name && it.name != reindrake.name }
            .forEach { sc -> RegisterUtils.chat(Regex(sc.pattern)) { _, _ -> onOtherSeaCreature() }
        }

        HudRenderCallback.EVENT.register { drawContext, tickDelta ->
            render(drawContext)
        }
    }

    fun onYeti() {
        if (!WorldUtils.isInSkyblock()) return
        val catchesSinceLast = data.yeti.catchesSinceLast
        data.yeti.catchesSinceLast = 0
        data.yeti.lastCatchTime = Date()
        data.reindrake.catchesSinceLast++
        data.yeti.catchesHistory = (listOf(data.yeti.catchesSinceLast) + data.yeti.catchesHistory).take(5)
        data.yeti.averageCatches = data.yeti.catchesHistory.average().toInt()
        ChatUtils.sendLocalChat(getMessage(catchesSinceLast, yeti.name, yeti.rarityColorCode), true)
    }

    fun onReindrake() {
        if (!WorldUtils.isInSkyblock()) return

        val catchesSinceLast = data.reindrake.catchesSinceLast
        data.reindrake.catchesSinceLast = 0
        data.reindrake.lastCatchTime = Date()
        data.yeti.catchesSinceLast++
        data.reindrake.catchesHistory = (listOf(data.reindrake.catchesSinceLast) + data.reindrake.catchesHistory).take(5)
        data.reindrake.averageCatches = data.yeti.catchesHistory.average().toInt()
        ChatUtils.sendLocalChat(getMessage(catchesSinceLast, reindrake.name, reindrake.rarityColorCode), true)
    }

    fun onOtherSeaCreature() {
        if (!WorldUtils.isInSkyblock()) return
        // If on Jerry
        data.yeti.catchesSinceLast++
        data.reindrake.catchesSinceLast++
    }

    fun render(drawContext: DrawContext) {
        if (!hasData()) return

        val textRenderer = FeeshMod.mc.textRenderer
        var y = 10

        val title = "${AQUA}${BOLD}Jerry Workshop tracker"
        drawContext.drawText(textRenderer, Text.literal(title), 10, y, 0xFFFFFF, true)
        y += textRenderer.fontHeight + 2

        if (hasYetiData()) {
            val yeti = SeaCreatures.allSeaCreatures.find { it.name == "Yeti" }!!
            val yetiLine = "${GOLD}${yeti.name}: ${WHITE}${data.yeti.catchesSinceLast} ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}${data.yeti.averageCatches}${DARK_GRAY})"
            drawContext.drawText(textRenderer, Text.literal(yetiLine), 10, y, 0xFFFFFF, true)
            y += textRenderer.fontHeight + 2

            if (data.yeti.catchesHistory.isNotEmpty()) {
                val yetiLastOn = formatTimeElapsed(data.yeti.lastCatchTime)
                val yetiLastOnDate = formatDate(data.yeti.lastCatchTime)
                val yetiLastOnLine = "${GRAY}Last on: ${WHITE}${yetiLastOn} ${GRAY}(${WHITE}${yetiLastOnDate}${GRAY})"
                drawContext.drawText(textRenderer, Text.literal(yetiLastOnLine), 10, y, 0xFFFFFF, true)
                y += textRenderer.fontHeight + 2
            }
        }

        if (hasReindrakeData()) {
            val reindrake = SeaCreatures.allSeaCreatures.find { it.name == "Reindrake" }!!
            val reindrakeLine = "${LIGHT_PURPLE}${reindrake.name}: ${WHITE}${data.reindrake.catchesSinceLast} ${GRAY}catches ago ${DARK_GRAY}(${GRAY}avg: ${WHITE}${data.reindrake.averageCatches}${DARK_GRAY})"
            drawContext.drawText(textRenderer, Text.literal(reindrakeLine), 10, y, 0xFFFFFF, true)
            y += textRenderer.fontHeight + 2

            if (data.reindrake.catchesHistory.isNotEmpty()) {
                val reindrakeLastOn = formatTimeElapsed(data.reindrake.lastCatchTime)
                val reindrakeLastOnDate = formatDate(data.reindrake.lastCatchTime)
                val reindrakeLastOnLine = "${GRAY}Last on: ${WHITE}${reindrakeLastOn} ${GRAY}(${WHITE}${reindrakeLastOnDate}${GRAY})"
                drawContext.drawText(textRenderer, Text.literal(reindrakeLastOnLine), 10, y, 0xFFFFFF, true)
            }
        }
    }

    private fun hasData(): Boolean {
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