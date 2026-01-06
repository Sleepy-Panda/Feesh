package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.ClientTickEvent
import com.github.sleepypanda.feesh.events.OwnSeaCreatureCaughtEvent
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
import java.text.DecimalFormat

enum class ViewMode {
    SESSION,
    TOTAL
}

//@Serializable
data class SeaCreatureCatchData(
    var amount: Int = 0,
    var doubleHookAmount: Int = 0
)

//@Serializable
data class SeaCreaturesData(
    val catches: MutableMap<String, SeaCreatureCatchData> = mutableMapOf(),
    var totalCount: Int = 0
)

//@Serializable
data class SeaCreaturesTrackerData(
    var session: SeaCreaturesData = SeaCreaturesData(),
    var total: SeaCreaturesData = SeaCreaturesData(),
    var viewMode: String = ViewMode.SESSION.name
)

object SeaCreaturesTracker {
    private var data = SeaCreaturesTrackerData()
    private val decimalFormat = DecimalFormat("#.#")
    private var tickCounter = 0
    private val baseTitle = "${AQUA}${BOLD}Sea creatures tracker"

    private const val TICKS_PER_UPDATE = 20
    private const val RESET_SESSION = "feeshResetSeaCreatures"
    private const val RESET_TOTAL = "feeshResetSeaCreaturesTotal"

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
    }

    //private fun loadData() {
    //    try {
    //        if (dataFile.exists() && dataFile.canRead()) {
    //            val content = dataFile.readText()
    //            if (content.isNotEmpty()) {
    //                data = json.decodeFromString<SeaCreaturesTrackerData>(content)
    //            }
    //        }
    //    } catch (e: Exception) {
    //        FeeshMod.LOGGER.error("[Feesh] Failed to load Sea creatures tracker data.", e)
    //    }
    //}
//
    private fun saveData() {
    //    try {
    //        dataFile.parentFile?.mkdirs()
    //        val content = json.encodeToString(data)
    //        Files.write(dataFile.toPath(), content.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
    //    } catch (e: Exception) {
    //        FeeshMod.LOGGER.error("[Feesh] Failed to save Sea creatures tracker data.", e)
    //    }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        if (!Overlays.seaCreaturesTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld() || !PlayerUtils.isFishingHookSeenMinutesAgo(5)) {
            gui.clearLines()
            return
        }

        updateGuiLines()
    }

    private fun onSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        if (!Overlays.seaCreaturesTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

        val seaCreatureName = event.seaCreatureName
        val isDoubleHook = event.isDoubleHook
        val valueToAdd = if (isDoubleHook) 2 else 1

        trackSeaCreatureCatch(data.session, seaCreatureName, valueToAdd, isDoubleHook)
        trackSeaCreatureCatch(data.total, seaCreatureName, valueToAdd, isDoubleHook)

        saveData()
        updateGuiLines()
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
            val viewMode = getCurrentViewMode()
            val viewModeText = getViewModeDisplayText(viewMode)
            ChatUtils.sendLocalChat("${WHITE}Switched to ${viewModeText}${WHITE} view mode.", true)
        }
    }

    private fun updateGuiLines() {
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
                .thenBy { it.seaCreatureInfo.name }
            )
            SeaCreaturesTrackerSorting.RARITY_DESC -> entries.sortedWith(
                compareByDescending<TrackerLineEntry> { CommonUtils.getRarityNumericCode(it.seaCreatureInfo.rarityColorCode) }
                .thenBy { it.seaCreatureInfo.name }
            )
            else -> entries.sortedByDescending { it.amount }
        }

        val lines = mutableListOf<String>()
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

        val nextMode = if (viewMode == ViewMode.SESSION) ViewMode.TOTAL else ViewMode.SESSION
        val nextModeText = getViewModeDisplayText(nextMode)
        lines.add("${GRAY}[${AQUA}Switch to $nextModeText${GRAY}] ${DARK_GRAY}(/feeshToggleSeaCreaturesViewMode)")

        val resetCommand = when (viewMode) {
            ViewMode.SESSION -> "/$RESET_SESSION"
            ViewMode.TOTAL -> "/$RESET_TOTAL"
        }
        lines.add("${RED}${BOLD}[Click to reset] ${DARK_GRAY}($resetCommand)")

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

