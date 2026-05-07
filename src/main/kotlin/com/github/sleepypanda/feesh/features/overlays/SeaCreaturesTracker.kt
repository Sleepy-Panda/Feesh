package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GameClosedEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.SeaCreaturesTrackerDisplayMode
import com.github.sleepypanda.feesh.settings.categories.SeaCreaturesTrackerSorting
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.GuiButton
import com.github.sleepypanda.feesh.utils.gui.LineAction
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.FishingHookUtils
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
    const val TOGGLE_VIEW_MODE = "feeshToggleSeaCreaturesViewMode"
    const val SET_SEA_CREATURE_COUNT_COMMAND = "feeshSetSeaCreatureCount"
    const val SET_SEA_CREATURE_COUNT_TOTAL_COMMAND = "feeshSetSeaCreatureCountTotal"
    const val DELETE_SEA_CREATURE_COMMAND = "feeshDeleteSeaCreature"
    const val DELETE_SEA_CREATURE_TOTAL_COMMAND = "feeshDeleteSeaCreatureTotal"

    private const val TICKS_PER_UPDATE = 20

    private var data = PersistentDataManager.feeshData.seaCreatures
    private val decimalFormat = DecimalFormat("#.##")
    private var tickCounter = 0
    private val baseTitle = "${AQUA}${BOLD}Sea creatures tracker"

    private val gui = FeeshGui()
        .setCoordsDataKey("seaCreaturesTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${GRAY}- ${GOLD}Yeti: ${WHITE}10 ${GRAY}1% | DH: ${WHITE}1 ${GRAY}20%",
            "${GRAY}- ${LIGHT_PURPLE}Reindrake: ${WHITE}1 ${GRAY}0.1%",
            "",
            "${AQUA}Total: ${WHITE}11 ${GRAY}rare out of ${WHITE}1000",
        ))
        .setSettingsKey { Overlays.seaCreaturesTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.seaCreaturesTrackerCustomStyle }
        .setCondition {
            WorldUtils.isInFishingWorld() &&
            FishingHookUtils.wasFishingHookActiveMinutesAgo(5)
        }

    fun init() {
        registerCommands()

        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreatureCaught)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }

    private fun saveData(force: Boolean = false) {
        if (force) {
            PersistentDataManager.forceSaveFeeshDataToFileSync()
        } else {
            PersistentDataManager.saveFeeshDataToFileAsync()
        }
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

        // Do not track Vanquishers if not fishing
        if (seaCreatureName == "Vanquisher" && !FishingHookUtils.wasFishingHookActiveMinutesAgo(5)) return

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
        recalculateTotalCount(sourceObj)
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
            resetSession(force = true)
            FeeshMod.LOGGER.info("[Feesh] Automatically reset Sea creatures tracker [Session] on game closed.")
        }
    }

    private fun resetSession(force: Boolean = false) {
        data.session = SeaCreaturesData()
        saveData(force)
    }

    private fun resetTotal() {
        data.total = SeaCreaturesData()
        saveData()
    }

    private fun resetSeaCreaturesTracker(isConfirmed: Boolean, resetViewMode: ViewMode) {
        CommonUtils.runWithCatching("Failed to reset Sea creatures tracker") {
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

        RegisterUtils.command(TOGGLE_VIEW_MODE) {
            toggleViewMode()
        }

        RegisterUtils.command(SET_SEA_CREATURE_COUNT_COMMAND) { args ->
            onSetSeaCreatureCountCommand(args, ViewMode.SESSION)
        }

        RegisterUtils.command(SET_SEA_CREATURE_COUNT_TOTAL_COMMAND) { args ->
            onSetSeaCreatureCountCommand(args, ViewMode.TOTAL)
        }

        RegisterUtils.command(DELETE_SEA_CREATURE_COMMAND) { args ->
            onDeleteSeaCreatureCommand(args, ViewMode.SESSION)
        }

        RegisterUtils.command(DELETE_SEA_CREATURE_TOTAL_COMMAND) { args ->
            onDeleteSeaCreatureCommand(args, ViewMode.TOTAL)
        }
    }

    private fun onSetSeaCreatureCountCommand(args: Array<String>, viewMode: ViewMode) {
        CommonUtils.runWithCatching("Failed to change sea creature count in Sea creatures tracker") {
            if (args.size < 2) {
                ChatUtils.sendLocalChat("${RED}Usage: /$SET_SEA_CREATURE_COUNT_COMMAND <SEA_CREATURE_NAME> <count>", true)
                return
            }

            val count = args.last().toIntOrNull()
            if (count == null || count <= 0) {
                ChatUtils.sendLocalChat("${RED}Invalid count, should be a positive number: ${args.last()}", true)
                return
            }

            val seaCreatureName = args.dropLast(1).joinToString(" ").trim()
            if (seaCreatureName.isBlank()) {
                ChatUtils.sendLocalChat("${RED}Sea creature name is required.", true)
                return
            }

            val seaCreatureInfo = findSeaCreatureInfoByName(seaCreatureName)
            if (seaCreatureInfo == null) {
                ChatUtils.sendLocalChat("${RED}Sea creature not found by name: $seaCreatureName", true)
                return
            }

            val sourceObj = getSourceObject(viewMode)
            val key = seaCreatureInfo.name.uppercase()
            val existing = sourceObj.catches[key]

            if (existing == null) {
                sourceObj.catches[key] = SeaCreatureCatchData(amount = count, doubleHookAmount = 0)
            } else {
                existing.amount = count
                existing.doubleHookAmount = existing.doubleHookAmount.coerceAtMost(existing.amount / 2)
            }

            recalculateTotalCount(sourceObj)
            saveData()
            updateGuiLines()

            val viewModeText = getViewModeDisplayText(viewMode)
            ChatUtils.sendLocalChat("${WHITE}Changed count of ${seaCreatureInfo.displayName} ${WHITE}to ${count} in Sea creatures tracker ${viewModeText}${WHITE}.", true)
        }
    }

    private fun onDeleteSeaCreatureCommand(args: Array<String>, viewMode: ViewMode) {
        CommonUtils.runWithCatching("Failed to delete sea creature from Sea creatures tracker") {
            if (args.isEmpty()) {
                ChatUtils.sendLocalChat("${RED}Usage: /$DELETE_SEA_CREATURE_COMMAND <SEA_CREATURE_NAME>", true)
                return
            }

            val sourceObj = getSourceObject(viewMode)
            val isConfirmed = args.isNotEmpty() && args.last() == "noconfirm"
            val seaCreatureName = if (isConfirmed) args.dropLast(1).joinToString(" ") else args.joinToString(" ")
            val normalizedName = seaCreatureName.trim()
            if (normalizedName.isBlank()) {
                ChatUtils.sendLocalChat("${RED}Sea creature name is required.", true)
                return
            }

            val key = sourceObj.catches.keys.find { it.equals(normalizedName, ignoreCase = true) }
                ?: SeaCreatures.allSeaCreatures.find { it.name.equals(normalizedName, ignoreCase = true) }?.name?.uppercase()
            if (key == null || !sourceObj.catches.containsKey(key)) {
                ChatUtils.sendLocalChat("${RED}Sea creature is not found in the tracker, nothing to delete: $normalizedName", true)
                return
            }

            val entry = sourceObj.catches[key] ?: return
            val seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name.uppercase() == key }
            val displayName = seaCreatureInfo?.displayName ?: key
            val viewModeText = getViewModeDisplayText(viewMode)
            if (!isConfirmed) {
                val command = when (viewMode) {
                    ViewMode.SESSION -> "$DELETE_SEA_CREATURE_COMMAND ${seaCreatureInfo?.name ?: key} noconfirm"
                    ViewMode.TOTAL -> "$DELETE_SEA_CREATURE_TOTAL_COMMAND ${seaCreatureInfo?.name ?: key} noconfirm"
                }
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to delete ${WHITE}${entry.amount}x ${displayName}${WHITE} from the Sea creatures tracker ${viewModeText}${WHITE}? ${RED}${BOLD}[Click to confirm]",
                    command,
                    true
                )
                return
            }

            sourceObj.catches.remove(key)
            recalculateTotalCount(sourceObj)
            saveData()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Deleted ${WHITE}${entry.amount}x ${displayName}${WHITE} from the Sea creatures tracker ${viewModeText}${WHITE}.", true)
        }
    }

    private fun findSeaCreatureInfoByName(name: String): SeaCreatures.SeaCreatureInfo? {
        return SeaCreatures.allSeaCreatures.find { it.name.equals(name.trim(), ignoreCase = true) }
    }

    private fun recalculateTotalCount(sourceObj: SeaCreaturesData) {
        sourceObj.totalCount = sourceObj.catches.values.sumOf { it.amount }
    }

    private fun onLineSeaCreatureIncrease(seaCreatureKey: String) {
        CommonUtils.runWithCatching("Failed to change sea creature amount in Sea creatures tracker") {
            val viewMode = getCurrentViewMode()
            val sourceObj = getSourceObject(viewMode)
            val entry = sourceObj.catches[seaCreatureKey] ?: return
            val seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name.uppercase() == seaCreatureKey } ?: return

            entry.amount += 1
            recalculateTotalCount(sourceObj)
            saveData()
            updateGuiLines()

            val viewModeText = getViewModeDisplayText(viewMode)
            ChatUtils.sendLocalChat("${WHITE}Changed count of ${seaCreatureInfo.displayName} ${WHITE}to ${GRAY}${entry.amount}x ${WHITE}in the Sea creatures tracker ${viewModeText}${WHITE}.", true)
        }
    }

    private fun onLineSeaCreatureDecrease(seaCreatureKey: String) {
        CommonUtils.runWithCatching("Failed to change sea creature amount in Sea creatures tracker") {
            val viewMode = getCurrentViewMode()
            val sourceObj = getSourceObject(viewMode)
            val entry = sourceObj.catches[seaCreatureKey] ?: return
            val seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name.uppercase() == seaCreatureKey } ?: return
            if (entry.amount <= 1) return

            entry.amount -= 1
            entry.doubleHookAmount = entry.doubleHookAmount.coerceAtMost(entry.amount / 2)
            recalculateTotalCount(sourceObj)
            saveData()
            updateGuiLines()

            val viewModeText = getViewModeDisplayText(viewMode)
            ChatUtils.sendLocalChat("${WHITE}Changed count of ${seaCreatureInfo.displayName} ${WHITE}to ${GRAY}${entry.amount}x ${WHITE}in the Sea creatures tracker ${viewModeText}${WHITE}.", true)
        }
    }

    private fun onLineSeaCreatureDelete(seaCreatureKey: String) {
        CommonUtils.runWithCatching("Failed to delete sea creature from Sea creatures tracker") {
            val viewMode = getCurrentViewMode()
            val seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name.uppercase() == seaCreatureKey } ?: return
            onDeleteSeaCreatureCommand(arrayOf(seaCreatureInfo.name), viewMode)
        }
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!Overlays.seaCreaturesTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld() || !FishingHookUtils.wasFishingHookActiveMinutesAgo(5)) return

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
                val regularHookCount = value.amount - value.doubleHookAmount
                if (regularHookCount > 0) {
                    ((value.doubleHookAmount.toDouble() / regularHookCount) * 100.0)
                } else {
                    0.0
                }
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
        }

        val maxLines = Overlays.seaCreaturesTrackerShowTop.coerceIn(1, 100)
        val entriesToShow = sortedEntries.take(maxLines)
        val hiddenEntries = sortedEntries.drop(maxLines)

        val lines = mutableListOf<String>()
        val nextMode = if (viewMode == ViewMode.SESSION) ViewMode.TOTAL else ViewMode.SESSION
        val nextModeText = getViewModeDisplayText(nextMode)

        val viewModeText = getViewModeDisplayText(viewMode)
        lines.add("${baseTitle} ${viewModeText}")

        entriesToShow.forEach { entry ->
            val seaCreatureText = if (entry.seaCreatureInfo.isRare) "${entry.seaCreatureInfo.boldDisplayName}" else "${entry.seaCreatureInfo.displayName}"
            val countText = "${WHITE}${CommonUtils.formatNumberWithSpaces(entry.amount)}"
            val percentText = if (showPercentage) " ${GRAY}${decimalFormat.format(entry.percent)}%" else ""
            
            val doubleHookText = if (showDoubleHook && entry.seaCreatureInfo.canBeDoubleHooked) {
                val dhAmount = CommonUtils.formatNumberWithSpaces(entry.doubleHookAmount)
                val dhPercent = decimalFormat.format(entry.doubleHookPercent)
                " ${DARK_GRAY}| ${GRAY}DH: ${WHITE}$dhAmount ${GRAY}$dhPercent%"
            } else ""

            lines.add("${GRAY}- $seaCreatureText${GRAY}: $countText$percentText$doubleHookText")
        }

        if (hiddenEntries.isNotEmpty()) {
            val otherSeaCreaturesCount = CommonUtils.formatNumberWithSpaces(hiddenEntries.sumOf { it.amount })
            val otherSeaCreaturesTypes = CommonUtils.formatNumberWithSpaces(hiddenEntries.size)
            lines.add("${GRAY}- Other sea creatures of ${WHITE}$otherSeaCreaturesTypes ${GRAY}types: ${WHITE}$otherSeaCreaturesCount")
        }

        val totalCount = if (displayMode == SeaCreaturesTrackerDisplayMode.ALL) {
            sourceObj.totalCount
        } else {
            sortedEntries.sumOf { it.amount }
        }

        val totalText = if (displayMode == SeaCreaturesTrackerDisplayMode.ALL) {
            "${AQUA}Total: ${WHITE}${CommonUtils.formatNumberWithSpaces(totalCount)}"
        } else {
            val rareTotal = sortedEntries.sumOf { it.amount }
            "${AQUA}Total: ${WHITE}${CommonUtils.formatNumberWithSpaces(rareTotal)} ${GRAY}rare out of ${WHITE}${CommonUtils.formatNumberWithSpaces(sourceObj.totalCount)}"
        }
        lines.add("")
        lines.add(totalText)

        gui.setLines(lines)
        val lineIndexToActions = mutableMapOf<Int, List<LineAction>>()
        val buttonLinesCount = 2 // Buttons count (view mode, reset)
        val titleLineIndex = buttonLinesCount
        entriesToShow.forEachIndexed { index, entry ->
            lineIndexToActions[titleLineIndex + 1 + index] = listOf(
                LineAction("${GRAY}[${GREEN}+${GRAY}]") { onLineSeaCreatureIncrease(entry.seaCreature) },
                LineAction("${GRAY}[${RED}-${GRAY}]") { onLineSeaCreatureDecrease(entry.seaCreature) },
                LineAction("${GRAY}[${RED}x${GRAY}]") { onLineSeaCreatureDelete(entry.seaCreature) }
            )
        }
        gui.setLineActions(lineIndexToActions)

        gui.setButtons(listOf(
            GuiButton(0, "${GRAY}[Click to show ${nextModeText}${GRAY}]", { toggleViewMode() }),
            GuiButton(1, "${GRAY}[${RED}Click to reset${GRAY}]", { resetSeaCreaturesTracker(false, getCurrentViewMode()) })
        ))
    }

    private data class TrackerLineEntry(
        val seaCreature: String,
        val seaCreatureInfo: SeaCreatures.SeaCreatureInfo,
        val amount: Int,
        val percent: Double,
        val doubleHookAmount: Int,
        val doubleHookPercent: Double
    )
}

