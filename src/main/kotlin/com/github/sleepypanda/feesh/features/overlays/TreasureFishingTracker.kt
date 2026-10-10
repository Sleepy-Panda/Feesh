package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.constants.RareDrops
import com.github.sleepypanda.feesh.features.overlays.base.IResettableViewModeTracker
import com.github.sleepypanda.feesh.features.overlays.base.TrackerViewMode
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GameClosedEvent
import com.github.sleepypanda.feesh.events.models.RareDropEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.gui.GuiButton
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import net.minecraft.network.chat.Component
import java.util.Date

object TreasureFishingTracker : IResettableViewModeTracker {
    data class TreasureCatchesData(
        var good: Int = 0,
        var great: Int = 0,
        var outstanding: Int = 0
    ) {
        fun totalCatches(): Int = good + great + outstanding

        fun rngMeterPercent(): Double =
            (outstanding / 10_000.0 + great / 100_000.0 + good / 1_000_000.0) * 100.0
    }

    data class TreasureFishingSessionData(
        var catches: TreasureCatchesData = TreasureCatchesData()
    )

    data class TreasureFishingTotalData(
        var catches: TreasureCatchesData = TreasureCatchesData(),
        var treasureDyes: TreasureDyesData = TreasureDyesData()
    )

    data class TreasureDyesData(
        var catchesBreakdown: TreasureCatchesData = TreasureCatchesData(),
    ) : DropCounterData()

    data class TreasureFishingData(
        var session: TreasureFishingSessionData = TreasureFishingSessionData(),
        var total: TreasureFishingTotalData = TreasureFishingTotalData(),
        var viewMode: String = TrackerViewMode.SESSION.name
    )

    override val trackerName = "Treasure fishing tracker"

    const val RESET_SESSION_COMMAND = "feeshResetTreasureFishingTracker"
    override val resetSessionCommand = RESET_SESSION_COMMAND
    const val RESET_TOTAL_COMMAND = "feeshResetTreasureFishingTrackerTotal"
    override val resetTotalCommand = RESET_TOTAL_COMMAND
    const val SET_CATCHES_COMMAND = "feeshSetTreasureCatches"
    const val SET_CATCHES_TOTAL_COMMAND = "feeshSetTreasureCatchesTotal"
    const val GUIDE_URL = "https://github.com/Sleepy-Panda/Feesh/blob/develop/docs/Editing%20treasure%20fishing%20tracker.md"
    private const val TOGGLE_VIEW_MODE_COMMAND = "feeshToggleTreasureFishingTrackerViewMode"

    //  GOOD JUNK CATCH! You caught a Rusty Coin!
    private val PATTERN_TREASURE_CATCH = Regex("^. (GOOD|GOOD JUNK|GREAT|GREAT JUNK|OUTSTANDING|OUTSTANDING JUNK) CATCH!")

    private val data: TreasureFishingData
        get() = PersistentDataManager.feeshData.treasureFishing
    private var lastTreasureCaughtAt: Date? = null
    private var tickCounter = 0
    private const val TICKS_PER_UPDATE = 20
    private val baseTitle = "${AQUA}${BOLD}Treasure fishing tracker"
    private val treasureDye = RareDrops.rareDrops.find { it.itemName == "Treasure Dye" }!!

    private val gui = FeeshGui()
        .setCoordsDataKey("treasureFishingTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            "$baseTitle ${GRAY}[${GREEN}Session${GRAY}]",
            "${GRAY}- ${DARK_PURPLE}Good catch${GRAY}: ${WHITE}100",
            "${GRAY}- ${GOLD}Great catch${GRAY}: ${WHITE}50",
            "${GRAY}- ${LIGHT_PURPLE}Outstanding catch${GRAY}: ${WHITE}10",
            "${GRAY}Total Treasures: ${WHITE}160",
            "",
            "${GOLD}Treasure Dyes${GRAY}: ${WHITE}2",
            "${GRAY}Last on: ${WHITE}7h 15m ago",
            "${GRAY}Last on: ${WHITE}5 000 ${GRAY}Treasures ago",
            "${GRAY}RNG meter: ${WHITE}15.67%"
        ))
        .setSettingsKey { Overlays.treasureFishingTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.treasureFishingTrackerCustomStyle }
        .setCondition {
            WorldUtils.isInFishingWorld()
        }

    fun init() {
        registerViewModeResetCommands()
        registerCommands()
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(RareDropEvent::class, ::onRareDrop)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }

    override fun getCurrentViewMode(): TrackerViewMode {
        return try {
            TrackerViewMode.valueOf(data.viewMode)
        } catch (e: Exception) {
            TrackerViewMode.SESSION
        }
    }

    override fun hasSessionData(): Boolean {
        return data.session.catches.totalCatches() > 0
    }

    override fun hasTotalData(): Boolean {
        return data.total.catches.totalCatches() > 0 || data.total.treasureDyes.hasData()
    }

    override fun resetSessionData(force: Boolean) {
        data.session = TreasureFishingSessionData()
        saveData(force)
    }

    override fun resetTotalData(force: Boolean) {
        data.total = TreasureFishingTotalData()
        saveData(force)
    }

    override fun refreshGui() {
        updateGuiLines()
    }

    private fun registerCommands() {
        RegisterUtils.command(TOGGLE_VIEW_MODE_COMMAND) {
            toggleViewMode()
        }
        RegisterUtils.command(SET_CATCHES_COMMAND) { args ->
            onSetTreasureCatchesCommand(args, TrackerViewMode.SESSION)
        }
        RegisterUtils.command(SET_CATCHES_TOTAL_COMMAND) { args ->
            onSetTreasureCatchesCommand(args, TrackerViewMode.TOTAL)
        }
    }

    private fun onChat(event: ChatEvent) {
        if (!Overlays.treasureFishingTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

        CommonUtils.runWithCatching("Failed to track treasure catch") {
            val matchResult = PATTERN_TREASURE_CATCH.find(event.unformattedText) ?: return@onChat
            trackTreasureCatch(matchResult.groupValues[1].lowercase())
        }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        updateGuiLines()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        lastTreasureCaughtAt = null
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetTreasureFishingTrackerSessionOnGameClosed) {
            resetOnGameClosed()
        }
    }

    private fun onRareDrop(event: RareDropEvent) {
        if (!Overlays.treasureFishingTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        if (event.itemName == "Treasure Dye") {
            trackTreasureDyeDrop()
        }
    }
  
    private fun onSetTreasureCatchesCommand(args: Array<String>, viewMode: TrackerViewMode) {

        fun sendGuideMessage() {
            ChatUtils.sendLocalChatWithUrl("${GRAY}Edit Treasure Fishing Tracker guide:", "${GREEN}${UNDERLINE}View in browser", GUIDE_URL, true)
        }

        CommonUtils.runWithCatching(
            message = "Failed to set treasure catches.",
            onError = {
                ChatUtils.sendLocalChat("${RED}Failed to set treasure catches in the ${trackerName}.", true)
            }
        ) {
            if (!WorldUtils.isInSkyblock()) {
                ChatUtils.sendLocalChat("${RED}You need to be in Skyblock to use this command.", true)
                return
            }

            val commandName = when (viewMode) {
                TrackerViewMode.SESSION -> SET_CATCHES_COMMAND
                TrackerViewMode.TOTAL -> SET_CATCHES_TOTAL_COMMAND
            }

            if (args.isEmpty()) {
                ChatUtils.sendLocalChat("${RED}Usage: /$commandName <GOOD>/<GREAT>/<OUTSTANDING>${GRAY}, e.g. 100/50/10", true)
                sendGuideMessage()
                return
            }

            val parts = args[0].split("/")
            val good = parts.getOrNull(0)?.toIntOrNull()
            val great = parts.getOrNull(1)?.toIntOrNull()
            val outstanding = parts.getOrNull(2)?.toIntOrNull()
            if (parts.size != 3 || good == null || great == null || outstanding == null || good < 0 || great < 0 || outstanding < 0) {
                ChatUtils.sendLocalChat("${RED}Please specify treasure catches in format <GOOD>/<GREAT>/<OUTSTANDING>, e.g. 100/50/10. Each number must be >= 0.", true)
                sendGuideMessage()
                return
            }

            setTreasureCatches(viewMode, TreasureCatchesData(good, great, outstanding))
        }
    }

    private fun setTreasureCatches(viewMode: TrackerViewMode, catches: TreasureCatchesData) {
        val target = getSourceCatches(viewMode)
        val previous = TreasureCatchesData(good = target.good, great = target.great, outstanding = target.outstanding)
        target.good = catches.good
        target.great = catches.great
        target.outstanding = catches.outstanding
        saveData()
        updateGuiLines()

        val viewModeText = getViewModeDisplayText(viewMode)
        ChatUtils.sendLocalChat(
            "Successfully changed treasure catches for the ${trackerName} $viewModeText.\n" +
                "Good: ${previous.good} -> ${catches.good}, Great: ${previous.great} -> ${catches.great}, Outstanding: ${previous.outstanding} -> ${catches.outstanding}.",
            true
        )
    }

    fun setTreasureDyes(count: Int, catchesBreakdown: TreasureCatchesData, lastOn: Date?) {
        CommonUtils.runWithCatching(
            message = "Failed to set Treasure Dyes.",
            onError = {
                ChatUtils.sendLocalChat("${RED}Failed to set Treasure Dyes in the ${trackerName}.", true)
            }
        ) {
            if (!WorldUtils.isInSkyblock()) return

            val treasuresSinceLast = catchesBreakdown.totalCatches()
            data.total.treasureDyes.initDropCount(count, lastOn, treasuresSinceLast)
            data.total.treasureDyes.catchesBreakdown = catchesBreakdown
            saveData()
            updateGuiLines()
            ChatUtils.sendLocalChat("Successfully changed Treasure Dyes for the ${trackerName}.\nCount = ${count}, treasures since last = ${treasuresSinceLast} (${catchesBreakdown.good} Good/${catchesBreakdown.great} Great/${catchesBreakdown.outstanding} Outstanding), last on = ${lastOn}.", true)
        }
    }

    private fun toggleViewMode() {
        val currentMode = getCurrentViewMode()
        val newMode = if (currentMode == TrackerViewMode.SESSION) TrackerViewMode.TOTAL else TrackerViewMode.SESSION
        data.viewMode = newMode.name
        updateGuiLines()
        saveData()
    }

    private fun getSourceCatches(viewMode: TrackerViewMode): TreasureCatchesData {
        return when (viewMode) {
            TrackerViewMode.SESSION -> data.session.catches
            TrackerViewMode.TOTAL -> data.total.catches
        }
    }

    private fun trackTreasureCatch(treasureType: String) {
        CommonUtils.runWithCatching("Failed to track treasure catch") {
            if (!Overlays.treasureFishingTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

            lastTreasureCaughtAt = Date()
            when (treasureType.lowercase()) {
                "good", "good junk" -> {
                    data.total.catches.good++
                    data.session.catches.good++
                    data.total.treasureDyes.catchesBreakdown.good++
                }
                "great", "great junk" -> {
                    data.total.catches.great++
                    data.session.catches.great++
                    data.total.treasureDyes.catchesBreakdown.great++
                }
                "outstanding", "outstanding junk" -> {
                    data.total.catches.outstanding++
                    data.session.catches.outstanding++
                    data.total.treasureDyes.catchesBreakdown.outstanding++
                }
            }
            data.total.treasureDyes.updateAfterCatch(false)
            updateGuiLines()
            saveData()
        }
    }

    private fun trackTreasureDyeDrop() {
        CommonUtils.runWithCatching("Failed to track Treasure Dye drop") {
            if (!Overlays.treasureFishingTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

            val catchesSinceLastDye = data.total.treasureDyes.catchesBreakdown
            data.total.treasureDyes.updateAfterDrop(treasureDye.boldDisplayName, "treasure", null)
            ChatUtils.sendLocalChat("${GRAY}RNG meter dropped at: ${formatRngMeterPercent(catchesSinceLastDye)}%", true)
            data.total.treasureDyes.catchesBreakdown = TreasureCatchesData()
            updateGuiLines()
            saveData()
        }
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!Overlays.treasureFishingTrackerOverlay ||
            !WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld() ||
            lastTreasureCaughtAt == null ||
            (Date().time - lastTreasureCaughtAt!!.time > 2 * 60 * 1000) // 2 minutes ago
        ) return

        val viewMode = getCurrentViewMode()
        val catches = getSourceCatches(viewMode)

        if (catches.totalCatches() == 0) return
        
        val viewModeText = getViewModeDisplayText(viewMode)
        val nextMode = if (viewMode == TrackerViewMode.SESSION) TrackerViewMode.TOTAL else TrackerViewMode.SESSION
        val nextModeText = getViewModeDisplayText(nextMode)

        val lines = mutableListOf<LineInfo>()
        lines.add(LineInfo("$baseTitle $viewModeText"))
        lines.add(LineInfo("${GRAY}- ${DARK_PURPLE}Good catch${GRAY}: ${WHITE}${CommonUtils.formatNumberWithSpaces(catches.good)}"))
        lines.add(LineInfo("${GRAY}- ${GOLD}Great catch${GRAY}: ${WHITE}${CommonUtils.formatNumberWithSpaces(catches.great)}"))
        lines.add(LineInfo("${GRAY}- ${LIGHT_PURPLE}Outstanding catch${GRAY}: ${WHITE}${CommonUtils.formatNumberWithSpaces(catches.outstanding)}"))
        lines.add(LineInfo("${GRAY}Total Treasures: ${WHITE}${CommonUtils.formatNumberWithSpaces(catches.totalCatches())}"))
        lines.add(LineInfo(""))
        lines.addAll(data.total.treasureDyes.getOverlayLines(treasureDye.displayName, "treasure"))
        val catchesSinceLastDye = data.total.treasureDyes.catchesBreakdown
        lines.add(LineInfo(
            "${GRAY}RNG meter: ${formatRngMeterPercent(catchesSinceLastDye)}%",
            tooltip = getRngMeterTooltip(catchesSinceLastDye)
        ))

        gui.setLines(lines)
        gui.setButtons(listOf(
            GuiButton(0, "${GRAY}[Click to show $nextModeText${GRAY}]", { toggleViewMode() }),
            getResetGuiButton(1) { requestReset() }
        ))
    }

    private fun getRngMeterTooltip(catches: TreasureCatchesData): List<Component> {
        val lines = listOf(
            "${AQUA}RNG meter",
            "${GRAY}Progress towards next Treasure Dye drop.",
            "",
            "${GRAY}Each treasure catch adds pity:",
            "  ${LIGHT_PURPLE}Outstanding${GRAY}: 1 in 10,000 (${WHITE}0.01%${GRAY})",
            "  ${GOLD}Great${GRAY}: 1 in 100,000 (${WHITE}0.001%${GRAY})",
            "  ${DARK_PURPLE}Good${GRAY}: 1 in 1,000,000 (${WHITE}0.0001%${GRAY})",
            "",
            "${GRAY}Progress:",
            "  ${LIGHT_PURPLE}Outstanding${GRAY}: ${WHITE}${CommonUtils.formatNumberWithSpaces(catches.outstanding)} ${GRAY}-> ${WHITE}${formatTooltipPercent(catches.outstanding / 10_000.0 * 100.0)}",
            "  ${GOLD}Great${GRAY}: ${WHITE}${CommonUtils.formatNumberWithSpaces(catches.great)} ${GRAY}-> ${WHITE}${formatTooltipPercent(catches.great / 100_000.0 * 100.0)}",
            "  ${DARK_PURPLE}Good${GRAY}: ${WHITE}${CommonUtils.formatNumberWithSpaces(catches.good)} ${GRAY}-> ${WHITE}${formatTooltipPercent(catches.good / 1_000_000.0 * 100.0)}",
        )
        return lines.map { Component.literal(it) }
    }

    private fun formatTooltipPercent(percent: Double): String {
        return when {
            percent >= 0.01 -> "${String.format("%.2f", percent)}%"
            percent > 0 -> "${String.format("%.4f", percent)}%"
            else -> "0%"
        }
    }

    private fun formatRngMeterPercent(catches: TreasureCatchesData): String {
        val percent = catches.rngMeterPercent()
        return when {
            percent >= 90 -> "${RED}${String.format("%.2f", percent)}"
            percent >= 50 -> "${YELLOW}${String.format("%.2f", percent)}"
            percent >= 0.01 -> "${WHITE}${String.format("%.2f", percent)}"
            percent > 0 -> "${WHITE}${String.format("%.4f", percent)}"
            else -> "${WHITE}0"
        }
    }

    private fun saveData(force: Boolean = false) {
        if (force) {
            PersistentDataManager.forceSaveFeeshDataToFileSync()
        } else {
            PersistentDataManager.saveFeeshDataToFileAsync()
        }
    }
}
