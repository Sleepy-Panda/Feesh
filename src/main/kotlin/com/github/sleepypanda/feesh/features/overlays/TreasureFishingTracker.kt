package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.RareDrops
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
import java.util.Date

object TreasureFishingTracker {
    enum class ViewMode {
        SESSION,
        TOTAL
    }

    data class TreasureCatchesData(
        var good: Int = 0,
        var great: Int = 0,
        var outstanding: Int = 0
    ) {
        fun totalCatches(): Int = good + great + outstanding
    }

    data class TreasureFishingSessionData(
        var catches: TreasureCatchesData = TreasureCatchesData()
    )

    data class TreasureFishingTotalData(
        var catches: TreasureCatchesData = TreasureCatchesData(),
        var treasureDyes: DropCounterData = DropCounterData()
    )

    data class TreasureFishingData(
        var session: TreasureFishingSessionData = TreasureFishingSessionData(),
        var total: TreasureFishingTotalData = TreasureFishingTotalData(),
        var viewMode: String = ViewMode.SESSION.name
    )

    const val RESET_SESSION_COMMAND = "feeshResetTreasureFishing"
    const val RESET_TOTAL_COMMAND = "feeshResetTreasureFishingTotal"
    private const val TOGGLE_VIEW_MODE_COMMAND = "feeshToggleTreasureFishingViewMode"

    private val PATTERN_TREASURE_CATCH = Regex("^⛃ (GOOD|GOOD JUNK|GREAT|GREAT JUNK|OUTSTANDING|OUTSTANDING JUNK) CATCH!")

    private var data = PersistentDataManager.feeshData.treasureFishing
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
            "${GRAY}Last on: ${WHITE}5 000 ${GRAY}Treasures ago"
        ))
        .setSettingsKey { Overlays.treasureFishingTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.treasureFishingTrackerCustomStyle }
        .setCondition {
            WorldUtils.isInFishingWorld()
        }

    fun init() {
        registerCommands()
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(RareDropEvent::class, ::onRareDrop)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }

    private fun registerCommands() {
        RegisterUtils.command(RESET_SESSION_COMMAND) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetTreasureFishingTracker(isConfirmed, ViewMode.SESSION)
        }
        RegisterUtils.command(RESET_TOTAL_COMMAND) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetTreasureFishingTracker(isConfirmed, ViewMode.TOTAL)
        }
        RegisterUtils.command(TOGGLE_VIEW_MODE_COMMAND) {
            toggleViewMode()
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
        if (Overlays.resetTreasureFishingTrackerSessionOnGameClosed &&
            Overlays.treasureFishingTrackerOverlay &&
            data.session.catches.totalCatches() > 0) {
            resetSession(force = true)
            FeeshMod.LOGGER.info("[Feesh] Automatically reset Treasure fishing tracker [Session] on game closed.")
        }
    }

    private fun onRareDrop(event: RareDropEvent) {
        if (!Overlays.treasureFishingTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        if (event.itemName == "Treasure Dye") {
            trackTreasureDyeDrop()
        }
    }

    fun setTreasureDyes(count: Int, lastOn: Date?) {
        CommonUtils.runWithCatching(
            message = "Failed to set Treasure Dyes.",
            onError = {
                ChatUtils.sendLocalChat("${RED}Failed to set Treasure Dyes.", true)
            }
        ) {
            if (!WorldUtils.isInSkyblock()) return
            
            data.total.treasureDyes.initDropCount(count, lastOn)         
            saveData()
            ChatUtils.sendLocalChat("${GRAY}Successfully changed Treasure Dyes count to ${count} for the Treasure fishing tracker.", true)
        }
    }

    private fun getCurrentViewMode(): ViewMode {
        return try {
            ViewMode.valueOf(data.viewMode)
        } catch (e: Exception) {
            ViewMode.SESSION
        }
    }

    private fun toggleViewMode() {
        val currentMode = getCurrentViewMode()
        val newMode = if (currentMode == ViewMode.SESSION) ViewMode.TOTAL else ViewMode.SESSION
        data.viewMode = newMode.name
        updateGuiLines()
        saveData()
    }

    private fun getSourceCatches(viewMode: ViewMode): TreasureCatchesData {
        return when (viewMode) {
            ViewMode.SESSION -> data.session.catches
            ViewMode.TOTAL -> data.total.catches
        }
    }

    private fun getViewModeDisplayText(viewMode: ViewMode): String {
        return when (viewMode) {
            ViewMode.SESSION -> "${GRAY}[${GREEN}Session${GRAY}]"
            ViewMode.TOTAL -> "${GRAY}[${GREEN}Total${GRAY}]"
        }
    }

    private fun resetSession(force: Boolean = false) {
        data.session = TreasureFishingSessionData()
        saveData(force)
    }

    private fun resetTotal() {
        data.total = TreasureFishingTotalData()
        saveData()
    }

    private fun resetTreasureFishingTracker(isConfirmed: Boolean, resetViewMode: ViewMode) {
        CommonUtils.runWithCatching("Failed to reset Treasure fishing tracker") {
            val viewModeText = getViewModeDisplayText(resetViewMode)

            if (!isConfirmed) {
                val resetCommand = when (resetViewMode) {
                    ViewMode.SESSION -> "$RESET_SESSION_COMMAND noconfirm"
                    ViewMode.TOTAL -> "$RESET_TOTAL_COMMAND noconfirm"
                }
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to reset Treasure fishing tracker ${viewModeText}${WHITE}? ${RED}${BOLD}[Click to confirm]",
                    resetCommand,
                    true
                )
                return
            }

            when (resetViewMode) {
                ViewMode.SESSION -> resetSession()
                ViewMode.TOTAL -> resetTotal()
            }

            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Treasure fishing tracker ${viewModeText} ${WHITE}was reset.", true)
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
                }
                "great", "great junk" -> {
                    data.total.catches.great++
                    data.session.catches.great++
                }
                "outstanding", "outstanding junk" -> {
                    data.total.catches.outstanding++
                    data.session.catches.outstanding++
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

            data.total.treasureDyes.updateAfterDrop(treasureDye.boldDisplayName, "treasure", null)
            updateGuiLines()
            saveData()
        }
    }

    private fun hasAnyData(): Boolean {
        return data.session.catches.totalCatches() > 0 ||
            data.total.catches.totalCatches() > 0 ||
            data.total.treasureDyes.hasData()
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
        val nextMode = if (viewMode == ViewMode.SESSION) ViewMode.TOTAL else ViewMode.SESSION
        val nextModeText = getViewModeDisplayText(nextMode)

        val lines = mutableListOf<LineInfo>()
        lines.add(LineInfo("$baseTitle $viewModeText"))
        lines.add(LineInfo("${GRAY}- ${DARK_PURPLE}Good catch${GRAY}: ${WHITE}${CommonUtils.formatNumberWithSpaces(catches.good)}"))
        lines.add(LineInfo("${GRAY}- ${GOLD}Great catch${GRAY}: ${WHITE}${CommonUtils.formatNumberWithSpaces(catches.great)}"))
        lines.add(LineInfo("${GRAY}- ${LIGHT_PURPLE}Outstanding catch${GRAY}: ${WHITE}${CommonUtils.formatNumberWithSpaces(catches.outstanding)}"))
        lines.add(LineInfo("${GRAY}Total Treasures: ${WHITE}${CommonUtils.formatNumberWithSpaces(catches.totalCatches())}"))
        lines.add(LineInfo(""))
        lines.addAll(data.total.treasureDyes.getOverlayLines(treasureDye.displayName, "treasure"))

        gui.setLines(lines)
        gui.setButtons(listOf(
            GuiButton(0, "${GRAY}[Click to show $nextModeText${GRAY}]", { toggleViewMode() }),
            GuiButton(1, "${GRAY}[${RED}Click to reset${GRAY}]", { resetTreasureFishingTracker(false, getCurrentViewMode()) })
        ))
    }

    private fun saveData(force: Boolean = false) {
        if (force) {
            PersistentDataManager.forceSaveFeeshDataToFileSync()
        } else {
            PersistentDataManager.saveFeeshDataToFileAsync()
        }
    }
}
