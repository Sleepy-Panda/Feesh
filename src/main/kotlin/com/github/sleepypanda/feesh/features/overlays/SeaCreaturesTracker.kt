package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.ClientTickEvent
import com.github.sleepypanda.feesh.events.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.GameClosedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.SeaCreaturesTrackerDisplayMode
import com.github.sleepypanda.feesh.settings.categories.SeaCreaturesTrackerSorting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import java.text.DecimalFormat

object SeaCreaturesTracker {
    enum class ViewMode {
        SESSION,
        TOTAL
    }
    
    data class SeaCreatureCatchData(
        var amount: Int = 0,
        var doubleHookAmount: Int = 0
    )
    
    data class SeaCreaturesData(
        val catches: MutableMap<String, SeaCreatureCatchData> = mutableMapOf(),
        var totalCount: Int = 0
    )
    
    data class SeaCreaturesTrackerData(
        var session: SeaCreaturesData = SeaCreaturesData(),
        var total: SeaCreaturesData = SeaCreaturesData(),
        var viewMode: String = ViewMode.SESSION.name
    )
    
    const val RESET_SESSION = "feeshResetSeaCreatures"
    const val RESET_TOTAL = "feeshResetSeaCreaturesTotal"

    private const val TICKS_PER_UPDATE = 20

    private var data = PersistentDataManager.feeshData.seaCreatures
    private val decimalFormat = DecimalFormat("#.#")
    private var tickCounter = 0
    private val baseTitle = "${AQUA}${BOLD}Sea creatures tracker"

    private val gui = FeeshGui()
        .setCoordsDataKey("seaCreaturesTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${GRAY}- ${GOLD}Yeti: ${WHITE}10 ${GRAY}1% | DH: ${WHITE}1 ${GRAY}20%",
            "${GRAY}- ${LIGHT_PURPLE}Reindrake: ${WHITE}1 ${GRAY}0.1%",
            "${GRAY}Total: ${WHITE}11 ${GRAY}rare out of ${WHITE}1000",
        ))
        .setSettingsKey { Overlays.seaCreaturesTrackerOverlay }
        .setCondition {
            WorldUtils.isInFishingWorld() &&
            PlayerUtils.isFishingHookSeenMinutesAgo(5)
        }

    fun init() {
        registerCommands()

        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreatureCaught)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }

    private fun saveData() {
        PersistentDataManager.saveFeeshDataToFileAsync()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        updateGuiLines()
    }

    private fun onSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        if (!Overlays.seaCreaturesTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

        val seaCreatureName = event.seaCreatureName
        val isDoubleHook = event.isDoubleHook
        val valueToAdd = if (isDoubleHook) 2 else 1

        trackSeaCreatureCatch(data.session, seaCreatureName, valueToAdd, isDoubleHook)
        trackSeaCreatureCatch(data.total, seaCreatureName, valueToAdd, isDoubleHook)

        updateGuiLines()
        saveData()
    }

    private fun trackSeaCreatureCatch(sourceObj: SeaCreaturesData, seaCreatureName: String, valueToAdd: Int, isDoubleHook: Boolean) {
        val key = seaCreatureName.uppercase()
        val catchData = sourceObj.catches.getOrPut(key) { SeaCreatureCatchData() }
        catchData.amount += valueToAdd
        if (isDoubleHook) {
            catchData.doubleHookAmount++
        }
        sourceObj.totalCount = sourceObj.catches.values.sumOf { it.amount }
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

    private fun getSourceObject(viewMode: ViewMode): SeaCreaturesData {
        return when (viewMode) {
            ViewMode.SESSION -> data.session
            ViewMode.TOTAL -> data.total
        }
    }

    private fun getViewModeDisplayText(viewMode: ViewMode): String {
        return when (viewMode) {
            ViewMode.SESSION -> "${GRAY}[${GREEN}Session${GRAY}]"
            ViewMode.TOTAL -> "${GRAY}[${GREEN}Total${GRAY}]"
        }
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetSeaCreaturesTrackerSessionOnGameClosed && 
            Overlays.seaCreaturesTrackerOverlay && 
            data.session.totalCount > 0) {
            resetSession()
            FeeshMod.LOGGER.info("[Feesh] Automatically reset Sea creatures tracker [Session] on game closed.")
        }
    }

    private fun resetSession() {
        data.session = SeaCreaturesData()
        saveData()
    }

    private fun resetTotal() {
        data.total = SeaCreaturesData()
        saveData()
    }

    private fun resetSeaCreaturesTracker(isConfirmed: Boolean, resetViewMode: ViewMode) {
        try {
            val viewModeText = getViewModeDisplayText(resetViewMode)

            if (!isConfirmed) {
                val resetAction = when (resetViewMode) {
                    ViewMode.SESSION -> "$RESET_SESSION noconfirm"
                    ViewMode.TOTAL -> "$RESET_TOTAL noconfirm"
                }
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to reset Sea creatures tracker ${viewModeText}${WHITE}? ${RED}${BOLD}[Click to confirm]",
                    resetAction,
                    true
                )
                return
            }

            when (resetViewMode) {
                ViewMode.SESSION -> resetSession()
                ViewMode.TOTAL -> resetTotal()
            }

            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Sea creatures tracker ${viewModeText} ${WHITE}was reset.", true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to reset Sea creatures tracker.", e)
        }
    }

    private fun registerCommands() {
        RegisterUtils.command(RESET_SESSION) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetSeaCreaturesTracker(isConfirmed, ViewMode.SESSION)
        }

        RegisterUtils.command(RESET_TOTAL) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetSeaCreaturesTracker(isConfirmed, ViewMode.TOTAL)
        }

        // TODO delete
        RegisterUtils.command("feeshToggleSeaCreaturesViewMode") {
            toggleViewMode()
        }
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!Overlays.seaCreaturesTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld() || !PlayerUtils.isFishingHookSeenMinutesAgo(5)) return

        val viewMode = getCurrentViewMode()
        val sourceObj = getSourceObject(viewMode)

        if (sourceObj.catches.isEmpty()) {
            gui.clearLines()
            return
        }

        val displayMode = Overlays.seaCreaturesTrackerMode
        val showPercentage = Overlays.showSeaCreaturesPercentage && displayMode == SeaCreaturesTrackerDisplayMode.ALL
        val showDoubleHook = Overlays.showSeaCreaturesDoubleHookStatistics
        val sorting = Overlays.seaCreaturesTrackerSorting
        val entries = sourceObj.catches.mapNotNull { (key, value) ->
            val seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name.uppercase() == key }
            if (seaCreatureInfo == null) return@mapNotNull null
            if (displayMode == SeaCreaturesTrackerDisplayMode.ONLY_RARE && !seaCreatureInfo.isRare) return@mapNotNull null

            val percent = if (sourceObj.totalCount > 0) {
                ((value.amount.toDouble() / sourceObj.totalCount) * 100.0)
            } else 0.0

            val doubleHookPercent = if (value.amount > 0 && value.doubleHookAmount > 0) {
                ((value.doubleHookAmount.toDouble() / (value.amount - value.doubleHookAmount)) * 100.0)
            } else 0.0

            TrackerLineEntry(
                seaCreature = key,
                seaCreatureInfo = seaCreatureInfo,
                amount = value.amount,
                percent = percent,
                doubleHookAmount = value.doubleHookAmount,
                doubleHookPercent = doubleHookPercent
            )
        }

        if (entries.isEmpty()) {
            gui.clearLines()
            return
        }

        val sortedEntries = when (sorting) {
            SeaCreaturesTrackerSorting.CATCHES_COUNT_DESC -> entries.sortedByDescending { it.amount }
            SeaCreaturesTrackerSorting.CATCHES_COUNT_ASC -> entries.sortedBy { it.amount }
            SeaCreaturesTrackerSorting.RARITY_ASC -> entries.sortedWith(
                compareBy<TrackerLineEntry> { CommonUtils.getRarityNumericCode(it.seaCreatureInfo.rarityColorCode) }
                .thenByDescending { it.amount }
            )
            SeaCreaturesTrackerSorting.RARITY_DESC -> entries.sortedWith(
                compareByDescending<TrackerLineEntry> { CommonUtils.getRarityNumericCode(it.seaCreatureInfo.rarityColorCode) }
                .thenByDescending { it.amount }
            )
            else -> entries.sortedByDescending { it.amount }
        }

        val lines = mutableListOf<String>()

        val nextMode = if (viewMode == ViewMode.SESSION) ViewMode.TOTAL else ViewMode.SESSION
        val nextModeText = getViewModeDisplayText(nextMode)
        lines.add("${GRAY}[Click to show $nextModeText${GRAY}] ${DARK_GRAY}(/feeshToggleSeaCreaturesViewMode)")

        val resetCommand = when (viewMode) {
            ViewMode.SESSION -> "/$RESET_SESSION"
            ViewMode.TOTAL -> "/$RESET_TOTAL"
        }
        lines.add("${GRAY}[${RED}Click to reset${GRAY}] ${DARK_GRAY}($resetCommand)")
      
        val viewModeText = getViewModeDisplayText(viewMode)
        lines.add("${baseTitle} ${viewModeText}")

        sortedEntries.forEach { entry ->
            val seaCreatureText = if (entry.seaCreatureInfo.isRare) "${entry.seaCreatureInfo.boldDisplayName}" else "${entry.seaCreatureInfo.displayName}"
            val countText = "${WHITE}${CommonUtils.formatNumberWithSpaces(entry.amount)}"
            val percentText = if (showPercentage) " ${GRAY}${decimalFormat.format(entry.percent)}%" else ""
            
            val doubleHookText = if (showDoubleHook && entry.seaCreatureInfo.name != "Vanquisher" && entry.seaCreatureInfo.name != "Reindrake") {
                val dhAmount = CommonUtils.formatNumberWithSpaces(entry.doubleHookAmount)
                val dhPercent = decimalFormat.format(entry.doubleHookPercent)
                " ${DARK_GRAY}| ${GRAY}DH: ${WHITE}$dhAmount ${GRAY}$dhPercent%"
            } else ""

            lines.add("${GRAY}- $seaCreatureText${GRAY}: $countText$percentText$doubleHookText")
        }

        val totalCount = if (displayMode == SeaCreaturesTrackerDisplayMode.ALL) {
            sourceObj.totalCount
        } else {
            sortedEntries.sumOf { it.amount }
        }

        val totalText = if (displayMode == SeaCreaturesTrackerDisplayMode.ALL) {
            "${GRAY}Total: ${WHITE}${CommonUtils.formatNumberWithSpaces(totalCount)}"
        } else {
            val rareTotal = sortedEntries.sumOf { it.amount }
            "${GRAY}Total: ${WHITE}${CommonUtils.formatNumberWithSpaces(rareTotal)} ${GRAY}rare out of ${WHITE}${CommonUtils.formatNumberWithSpaces(sourceObj.totalCount)}"
        }
        lines.add(totalText)

        gui.setLines(lines)
    }

    private data class TrackerLineEntry(
        val seaCreature: String,
        val seaCreatureInfo: SeaCreatures.Companion.SeaCreatureInfo,
        val amount: Int,
        val percent: Double,
        val doubleHookAmount: Int,
        val doubleHookPercent: Double
    )
}

