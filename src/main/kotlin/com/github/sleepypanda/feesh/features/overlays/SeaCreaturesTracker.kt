package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GameClosedEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.SeaCreatureCocoonedByYouEvent
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
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import net.minecraft.network.chat.Component
import java.text.DecimalFormat

object SeaCreaturesTracker {
    enum class ViewMode {
        SESSION,
        TOTAL
    }
    
    data class SeaCreatureCatchData(
        var amount: Int = 0, // Caught amount
        var cocoonedAmount: Int = 0,
        var doubleHookAmount: Int = 0 // Applies only to caught amount
    )
    
    data class SeaCreaturesData(
        val catches: MutableMap<String, SeaCreatureCatchData> = mutableMapOf(),
        var totalCount: Int = 0, // Total caught amount
        var totalCocoonedCount: Int = 0
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
            "${GRAY}- ${GOLD}Yeti: ${WHITE}10 ${GRAY}1% ${DARK_GRAY}| ${GRAY}DH: ${WHITE}1 ${GRAY}20%",
            "${GRAY}- ${LIGHT_PURPLE}Reindrake: ${WHITE}1 ${GRAY}0.1% ${DARK_GRAY}| ${GRAY}DH: ${WHITE}0 ${GRAY}0%",
            "",
            "${AQUA}Total: ${WHITE}11 ${GRAY}rare out of ${WHITE}1000 ${DARK_GRAY}| ${GRAY}DH: ${WHITE}1 ${GRAY}20% ${DARK_GRAY}| ${GRAY}BS: ${WHITE}1 ${GRAY}10%",
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
        EventBus.subscribe(SeaCreatureCocoonedByYouEvent::class, ::onSeaCreatureCocooned)
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

    private fun onSeaCreatureCocooned(event: SeaCreatureCocoonedByYouEvent) {
        if (!Overlays.seaCreaturesTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return
        if (!Overlays.countCocoonedSeaCreatures) return

        val seaCreatureName = event.seaCreatureName

        if (seaCreatureName == "Vanquisher" && !FishingHookUtils.wasFishingHookActiveMinutesAgo(5)) return

        trackSeaCreatureCocoon(data.session, seaCreatureName)
        trackSeaCreatureCocoon(data.total, seaCreatureName)

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
        recalculateTotalCaughtCount(sourceObj)
    }

    private fun trackSeaCreatureCocoon(sourceObj: SeaCreaturesData, seaCreatureName: String) {
        val key = seaCreatureName.uppercase()
        val catchData = sourceObj.catches.getOrPut(key) { SeaCreatureCatchData() }
        catchData.cocoonedAmount += 1
        recalculateTotalCocoonedCount(sourceObj)
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
            (data.session.totalCount > 0 || data.session.totalCocoonedCount > 0)) {
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

        fun isArgValidCount(value: String): Boolean {
            val trimmed = value.trim()
            if (trimmed.isEmpty()) return false
            return when {
                trimmed.startsWith("+") -> trimmed.drop(1).toIntOrNull() != null
                trimmed.startsWith("-") -> trimmed.drop(1).toIntOrNull() != null
                else -> trimmed.toIntOrNull() != null && trimmed.toInt() >= 0
            }
        }

        fun applyChangeToCurrentValue(value: String, currentCount: Int): Int? {
            val trimmed = value.trim()
            if (trimmed.isEmpty()) return null
    
            val newCount = when {
                trimmed.startsWith("+") -> {
                    val delta = trimmed.drop(1).toIntOrNull() ?: return null
                    if (delta < 0) return null
                    currentCount + delta
                }
                trimmed.startsWith("-") -> {
                    val delta = trimmed.drop(1).toIntOrNull() ?: return null
                    if (delta < 0) return null
                    currentCount - delta
                }
                else -> trimmed.toIntOrNull()
            } ?: return null
    
            if (newCount < 0) return null
            return newCount
        }

        fun findSeaCreatureInfoByName(name: String): SeaCreatures.SeaCreatureInfo? {
            return SeaCreatures.allSeaCreatures.find { it.name.equals(name.trim(), ignoreCase = true) }
        }

        CommonUtils.runWithCatching("Failed to change sea creature count in Sea creatures tracker") {
            val commandName = when (viewMode) {
                ViewMode.SESSION -> SET_SEA_CREATURE_COUNT_COMMAND
                ViewMode.TOTAL -> SET_SEA_CREATURE_COUNT_TOTAL_COMMAND
            }

            if (args.isEmpty()) {
                ChatUtils.sendLocalChat(
                    "${RED}Usage: /$commandName <SEA_CREATURE_NAME> <TOTAL_COUNT> [DH_COUNT] [BS_COUNT] ${GRAY}(e.g. 100, +1, -10)",
                    true
                )
                return
            }

            val countArgTokens = mutableListOf<String>()
            var nameEndIndex = args.size
            while (nameEndIndex > 0 && countArgTokens.size < 3 && isArgValidCount(args[nameEndIndex - 1])) {
                countArgTokens.add(0, args[nameEndIndex - 1])
                nameEndIndex--
            }

            if (countArgTokens.isEmpty()) {
                ChatUtils.sendLocalChat(
                    "${RED}Usage: /$commandName <SEA_CREATURE_NAME> <TOTAL_COUNT> [DH_COUNT] [BS_COUNT] ${GRAY}(e.g. 100, +1, -10)",
                    true
                )
                return
            }

            val seaCreatureName = args.take(nameEndIndex).joinToString(" ").trim()
            if (seaCreatureName.isBlank()) {
                ChatUtils.sendLocalChat("${RED}Sea creature name is required.", true)
                return
            }

            val seaCreatureInfo = findSeaCreatureInfoByName(seaCreatureName)
            if (seaCreatureInfo == null) {
                ChatUtils.sendLocalChat("${RED}Sea creature name is not supported: $seaCreatureName", true)
                return
            }

            val sourceObj = getSourceObject(viewMode)
            val key = seaCreatureInfo.name.uppercase()
            val existing = sourceObj.catches[key]

            val previousCaught = existing?.amount ?: 0
            val previousDoubleHook = existing?.doubleHookAmount ?: 0
            val previousCocooned = existing?.cocoonedAmount ?: 0
            val previousTotal = previousCaught + previousCocooned

            val newTotal = applyChangeToCurrentValue(countArgTokens[0], previousTotal)
            if (newTotal == null || newTotal <= 0) {
                ChatUtils.sendLocalChat(
                    "${RED}Invalid TOTAL_COUNT. Use an integer > 0, or +N / -N to adjust (e.g. 100, +1, -10).",
                    true
                )
                return
            }

            var newDoubleHook = previousDoubleHook
            if (countArgTokens.size >= 2) {
                newDoubleHook = applyChangeToCurrentValue(countArgTokens[1], previousDoubleHook) ?: run {
                    ChatUtils.sendLocalChat(
                        "${RED}Invalid DH_COUNT. Use an integer >= 0, or +N / -N to adjust (e.g. 10, +1, -10).",
                        true
                    )
                    return
                }
            }

            var newCocooned = previousCocooned
            if (countArgTokens.size >= 3) {
                newCocooned = applyChangeToCurrentValue(countArgTokens[2], previousCocooned) ?: run {
                    ChatUtils.sendLocalChat(
                        "${RED}Invalid BS_COUNT. Use an integer >= 0, or +N / -N to adjust (e.g. 1, +1, -10).",
                        true
                    )
                    return
                }
            }

            val newCaught = newTotal - newCocooned
            if (newCaught < 0) {
                ChatUtils.sendLocalChat(
                    "${RED}Cocooned count ($newCocooned) cannot exceed total count ($newTotal).",
                    true
                )
                return
            }
            if (newDoubleHook < 0 || newDoubleHook > newCaught / 2) {
                ChatUtils.sendLocalChat(
                    "${RED}Double hook count must be between 0 and ${newCaught / 2} (half of caught count).",
                    true
                )
                return
            }

            if (existing != null) {
                existing.amount = newCaught
                existing.doubleHookAmount = newDoubleHook
                existing.cocoonedAmount = newCocooned
            } else {
                sourceObj.catches[key] = SeaCreatureCatchData(
                    amount = newCaught,
                    cocoonedAmount = newCocooned,
                    doubleHookAmount = newDoubleHook,
                )
            }

            recalculateTotalCaughtCount(sourceObj)
            recalculateTotalCocoonedCount(sourceObj)
            saveData()
            updateGuiLines()

            val viewModeText = getViewModeDisplayText(viewMode)
            val changes = buildList {
                if (newTotal != previousTotal) {
                    add("${GRAY}Total: ${AQUA}$previousTotal ${WHITE}-> ${AQUA}$newTotal")
                }
                if (countArgTokens.size >= 2 && newDoubleHook != previousDoubleHook) {
                    add("${GRAY}DH: ${AQUA}$previousDoubleHook ${WHITE}-> ${AQUA}$newDoubleHook")
                }
                if (countArgTokens.size >= 3 && newCocooned != previousCocooned) {
                    add("${GRAY}BS: ${AQUA}$previousCocooned ${WHITE}-> ${AQUA}$newCocooned")
                }
            }
            val changesText = if (changes.isEmpty()) {
                "${GRAY}(no changes)"
            } else {
                changes.joinToString("${WHITE}, ")
            }
            ChatUtils.sendLocalChat(
                "${WHITE}Changed ${seaCreatureInfo.displayName} ${WHITE}in Sea creatures tracker $viewModeText${WHITE}: $changesText${WHITE}.",
                true
            )
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
                ChatUtils.sendLocalChat("${RED}Sea creature is not found in Sea creatures tracker, nothing to delete: $normalizedName", true)
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
                    "${WHITE}Do you want to delete ${displayName}${WHITE} from Sea creatures tracker ${viewModeText}${WHITE}? ${RED}${BOLD}[Click to confirm]",
                    command,
                    true
                )
                return
            }

            sourceObj.catches.remove(key)
            recalculateTotalCaughtCount(sourceObj)
            recalculateTotalCocoonedCount(sourceObj)
            saveData()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Deleted ${displayName}${WHITE} from the Sea creatures tracker ${viewModeText}${WHITE}.", true)
        }
    }

    private fun recalculateTotalCaughtCount(sourceObj: SeaCreaturesData) {
        sourceObj.totalCount = sourceObj.catches.values.sumOf { it.amount }
    }

    private fun recalculateTotalCocoonedCount(sourceObj: SeaCreaturesData) {
        sourceObj.totalCocoonedCount = sourceObj.catches.values.sumOf { it.cocoonedAmount }
    }

    private fun onDeleteSeaCreatureInline(seaCreatureKey: String) {
        CommonUtils.runWithCatching("Failed to delete sea creature from Sea creatures tracker") {
            val viewMode = getCurrentViewMode()
            val seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name.uppercase() == seaCreatureKey } ?: return
            onDeleteSeaCreatureCommand(arrayOf(seaCreatureInfo.name), viewMode)
        }
    }

    private fun updateGuiLines() {
        CommonUtils.runWithCatching("Failed to update GUI lines in Sea creatures tracker") {
            gui.clearLines()

            if (!Overlays.seaCreaturesTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld() || !FishingHookUtils.wasFishingHookActiveMinutesAgo(5)) return

            val viewMode = getCurrentViewMode()
            val sourceObj = getSourceObject(viewMode)

            if (sourceObj.catches.isEmpty()) {
                gui.clearLines()
                return
            }

            val displayMode = Overlays.seaCreaturesTrackerMode

            val sorting = Overlays.seaCreaturesTrackerSorting

            val entries = sourceObj.catches.mapNotNull { (key, value) ->
                val seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name.uppercase() == key }
                if (seaCreatureInfo == null) return@mapNotNull null
                if (displayMode == SeaCreaturesTrackerDisplayMode.ONLY_RARE && !seaCreatureInfo.isRare) return@mapNotNull null

                val caughtAmount = value.amount
                val totalAmount = caughtAmount + value.cocoonedAmount
                val percent = getPercentage(sourceObj, totalAmount)
                val doubleHookPercent = getDoubleHookPercent(caughtAmount, value.doubleHookAmount)
                val cocoonedPercent = getCocoonedPercent(totalAmount, value.cocoonedAmount)

                SeaCreatureTrackerEntry(
                    seaCreature = key,
                    seaCreatureInfo = seaCreatureInfo,
                    totalAmount = totalAmount,
                    totalAmountFormatted = CommonUtils.formatNumberWithSpaces(totalAmount),
                    caughtAmount = caughtAmount,
                    caughtAmountFormatted = CommonUtils.formatNumberWithSpaces(caughtAmount),
                    cocoonedAmount = value.cocoonedAmount,
                    cocoonedAmountFormatted = CommonUtils.formatNumberWithSpaces(value.cocoonedAmount),
                    percent = percent,
                    percentFormatted = formatPercent(percent),
                    cocoonedPercent = cocoonedPercent,
                    cocoonedPercentFormatted = formatPercent(cocoonedPercent),
                    doubleHookAmount = value.doubleHookAmount,
                    doubleHookAmountFormatted = CommonUtils.formatNumberWithSpaces(value.doubleHookAmount),
                    doubleHookPercent = doubleHookPercent,
                    doubleHookPercentFormatted = formatPercent(doubleHookPercent),
                )
            }

            if (entries.isEmpty()) {
                gui.clearLines()
                return
            }

            val sortedEntries = getSortedSeaCreatureEntries(entries, sorting)
            val maxLines = Overlays.seaCreaturesTrackerShowTop.coerceIn(1, 100)
            val entriesToShow = sortedEntries.take(maxLines)
            val hiddenEntries = sortedEntries.drop(maxLines)

            val nextMode = if (viewMode == ViewMode.SESSION) ViewMode.TOTAL else ViewMode.SESSION
            val nextModeText = getViewModeDisplayText(nextMode)

            val viewModeText = getViewModeDisplayText(viewMode)
            val lines = mutableListOf<LineInfo>()
            lines.add(LineInfo("$baseTitle $viewModeText"))

            entriesToShow.forEach { entry ->
                val seaCreatureText = getSeaCreatureLineText(entry, displayMode)
                lines.add(
                    LineInfo(
                        text = seaCreatureText,
                        tooltip = getSeaCreatureLineTooltip(entry),
                        actions = listOf(
                            LineAction("${GRAY}[${RED}x${GRAY}]") { onDeleteSeaCreatureInline(entry.seaCreature) }
                        )
                    )
                )
            }

            if (hiddenEntries.isNotEmpty()) {
                val otherSeaCreaturesCount = CommonUtils.formatNumberWithSpaces(hiddenEntries.sumOf { it.totalAmount })
                val otherSeaCreaturesTypes = CommonUtils.formatNumberWithSpaces(hiddenEntries.size)
                lines.add(LineInfo("${GRAY}- Other sea creatures of ${WHITE}$otherSeaCreaturesTypes ${GRAY}types: ${WHITE}$otherSeaCreaturesCount"))
            }

            val totalLineInfo = getTotalLineInfo(sourceObj)
            val totalLineText = getTotalLineText(displayMode, totalLineInfo)
            val totalLineTooltip = getTotalLineTooltip(totalLineInfo)
            lines.add(LineInfo(""))
            lines.add(LineInfo(totalLineText, tooltip = totalLineTooltip))

            gui.setLines(lines)

            gui.setButtons(listOf(
                GuiButton(0, "${GRAY}[Click to show ${nextModeText}${GRAY}]", { toggleViewMode() }),
                GuiButton(1, "${GRAY}[${RED}Click to reset${GRAY}]", { resetSeaCreaturesTracker(false, getCurrentViewMode()) })
            ))
        }
    }

    private fun formatPercent(percent: Double): String = "${decimalFormat.format(percent)}%"

    private fun getSortedSeaCreatureEntries(entries: List<SeaCreatureTrackerEntry>, sorting: SeaCreaturesTrackerSorting): List<SeaCreatureTrackerEntry> {
        val sorted = when (sorting) {
            SeaCreaturesTrackerSorting.CATCHES_COUNT_DESC -> entries.sortedByDescending { it.totalAmount }
            SeaCreaturesTrackerSorting.CATCHES_COUNT_ASC -> entries.sortedBy { it.totalAmount }
            SeaCreaturesTrackerSorting.RARITY_ASC -> entries.sortedWith(
                compareBy<SeaCreatureTrackerEntry> { CommonUtils.getRarityNumericCode(it.seaCreatureInfo.rarityColorCode) }
                .thenByDescending { it.totalAmount }
            )
            SeaCreaturesTrackerSorting.RARITY_DESC -> entries.sortedWith(
                compareByDescending<SeaCreatureTrackerEntry> { CommonUtils.getRarityNumericCode(it.seaCreatureInfo.rarityColorCode) }
                .thenByDescending { it.totalAmount }
            )
        }
        return sorted
    }

    private fun getSeaCreatureLineText(entry: SeaCreatureTrackerEntry, displayMode: SeaCreaturesTrackerDisplayMode): String {
        val showPercentage = Overlays.showSeaCreaturesPercentage
        val showDoubleHook = Overlays.showSeaCreaturesDoubleHookStatistics
        val showCocooned = Overlays.showCocoonedStatistics

        val seaCreatureText = if (entry.seaCreatureInfo.isRare) entry.seaCreatureInfo.boldDisplayName else entry.seaCreatureInfo.displayName
        val countText = "${WHITE}${entry.totalAmountFormatted}"
        val percentText = if (showPercentage) " ${GRAY}${entry.percentFormatted}" else ""
            
        val doubleHookText = if (showDoubleHook && entry.seaCreatureInfo.canBeDoubleHooked) {
            " ${DARK_GRAY}| ${GRAY}DH: ${WHITE}${entry.doubleHookAmountFormatted} ${GRAY}${entry.doubleHookPercentFormatted}"
        } else ""
        val cocoonedText = if (showCocooned) {
            " ${DARK_GRAY}| ${GRAY}BS: ${WHITE}${entry.cocoonedAmountFormatted} ${GRAY}${entry.cocoonedPercentFormatted}"
        } else ""

        return "${GRAY}- $seaCreatureText${GRAY}: $countText$percentText$doubleHookText$cocoonedText"
    }

    private fun getSeaCreatureLineTooltip(entry: SeaCreatureTrackerEntry): List<Component> {
        val doubleHookText = if (entry.seaCreatureInfo.canBeDoubleHooked) {
            "${GRAY}Double hook: ${WHITE}${entry.doubleHookAmountFormatted} ${GRAY}(${WHITE}${entry.doubleHookPercentFormatted} ${GRAY}of catches)"
        } else null
        val lines = listOfNotNull(
            entry.seaCreatureInfo.displayName,
            "${WHITE}${entry.totalAmountFormatted} ${GRAY}(${WHITE}${entry.percentFormatted} ${GRAY}out of all SC)",
            "${WHITE}${entry.caughtAmountFormatted} ${GRAY}caught, ${WHITE}${entry.cocoonedAmountFormatted} ${GRAY}cocooned",
            doubleHookText,
            "${GRAY}Bloodshot: ${WHITE}${entry.cocoonedAmountFormatted} ${GRAY}(${WHITE}${entry.cocoonedPercentFormatted}${GRAY})",
        )
        return lines.map { Component.literal(it) }
    }

    private fun getDoubleHookPercent(amount: Int, doubleHook: Int): Double {
        if (amount <= 0 || doubleHook <= 0) return 0.0

        val regularHookAmount = amount - doubleHook

        return if (regularHookAmount > 0) {
            (doubleHook.toDouble() / regularHookAmount) * 100.0
        } else 0.0
    }

    private fun getCocoonedPercent(totalAmount: Int, cocoonedAmount: Int): Double {
        if (totalAmount <= 0 || cocoonedAmount <= 0) return 0.0
        return (cocoonedAmount.toDouble() / totalAmount.toDouble()) * 100.0
    }

    private fun getPercentage(sourceObj: SeaCreaturesData, totalAmount: Int): Double {
        val percent = if (sourceObj.totalCount + sourceObj.totalCocoonedCount > 0) {
            ((totalAmount.toDouble() / (sourceObj.totalCount + sourceObj.totalCocoonedCount)) * 100.0)
        } else 0.0
        return percent
    }

    private fun getTotalLineInfo(sourceObj: SeaCreaturesData): TotalTrackerEntry {

        fun getDoubleHookCatches(catches: MutableMap<String, SeaCreatureCatchData>): Int {
            var doubleHookSum = 0
            for ((key, catchData) in catches) {
                val info = SeaCreatures.allSeaCreatures.find { it.name.uppercase() == key } ?: continue
                if (!info.canBeDoubleHooked) continue
                doubleHookSum += catchData.doubleHookAmount
            }
            return doubleHookSum
        }

        fun getRareCatches(sourceObj: SeaCreaturesData): MutableMap<String, SeaCreatureCatchData>  {
            return sourceObj.catches.filter { (k, v) -> SeaCreatures.allSeaCreatures.find { it.name.uppercase() == k }?.isRare == true }.toMutableMap()
        }

        val totalCaughtAmount = sourceObj.totalCount
        val totalCocoonedAmount = sourceObj.totalCocoonedCount
        val totalAmount = totalCaughtAmount + totalCocoonedAmount
        val totalDoubleHookAmount = getDoubleHookCatches(sourceObj.catches)
        val totalDoubleHookPercent = getDoubleHookPercent(totalCaughtAmount, totalDoubleHookAmount)
        val totalCocoonedPercent = getCocoonedPercent(totalAmount, totalCocoonedAmount)

        val rareCatches = getRareCatches(sourceObj)
        val rareCaughtAmount = rareCatches.values.sumOf { it.amount }
        val rareCocoonedAmount = rareCatches.values.sumOf { it.cocoonedAmount }
        val rareTotalAmount = rareCaughtAmount + rareCocoonedAmount
        val rareDoubleHookAmount = getDoubleHookCatches(rareCatches)
        val rareDoubleHookPercent = getDoubleHookPercent(rareTotalAmount, rareDoubleHookAmount)
        val rareCocoonedPercent = getCocoonedPercent(rareTotalAmount, rareCocoonedAmount)

        return TotalTrackerEntry(
            allTotalAmount = totalAmount,
            allTotalAmountFormatted = CommonUtils.formatNumberWithSpaces(totalAmount),
            allCaughtAmount = totalCaughtAmount,
            allCaughtAmountFormatted = CommonUtils.formatNumberWithSpaces(totalCaughtAmount),
            allDoubleHookAmount = totalDoubleHookAmount,
            allDoubleHookAmountFormatted = CommonUtils.formatNumberWithSpaces(totalDoubleHookAmount),
            allDoubleHookPercent = totalDoubleHookPercent,
            allDoubleHookPercentFormatted = formatPercent(totalDoubleHookPercent),
            allCocoonedAmount = totalCocoonedAmount,
            allCocoonedAmountFormatted = CommonUtils.formatNumberWithSpaces(totalCocoonedAmount),
            allCocoonedPercent = totalCocoonedPercent,
            allCocoonedPercentFormatted = formatPercent(totalCocoonedPercent),
            rareTotalAmount = rareTotalAmount,
            rareTotalAmountFormatted = CommonUtils.formatNumberWithSpaces(rareTotalAmount),
            rareCaughtAmount = rareCaughtAmount,
            rareCaughtAmountFormatted = CommonUtils.formatNumberWithSpaces(rareCaughtAmount),
            rareDoubleHookAmount = rareDoubleHookAmount,
            rareDoubleHookAmountFormatted = CommonUtils.formatNumberWithSpaces(rareDoubleHookAmount),
            rareDoubleHookPercent = rareDoubleHookPercent,
            rareDoubleHookPercentFormatted = formatPercent(rareDoubleHookPercent),
            rareCocoonedAmount = rareCocoonedAmount,
            rareCocoonedAmountFormatted = CommonUtils.formatNumberWithSpaces(rareCocoonedAmount),
            rareCocoonedPercent = rareCocoonedPercent,
            rareCocoonedPercentFormatted = formatPercent(rareCocoonedPercent),
        )
    }
    
    private fun getTotalLineText(displayMode: SeaCreaturesTrackerDisplayMode, totalInfo: TotalTrackerEntry): String {
        when (displayMode) {
            SeaCreaturesTrackerDisplayMode.ALL -> {
                val doubleHookText = " ${DARK_GRAY}| ${GRAY}DH: ${WHITE}${totalInfo.allDoubleHookAmountFormatted} ${GRAY}${totalInfo.allDoubleHookPercentFormatted}"
                val cocoonedText = " ${DARK_GRAY}| ${GRAY}BS: ${WHITE}${totalInfo.allCocoonedAmountFormatted} ${GRAY}${totalInfo.allCocoonedPercentFormatted}"
                return "${AQUA}Total: ${WHITE}${totalInfo.allTotalAmountFormatted}${doubleHookText}${cocoonedText}"
            }
            SeaCreaturesTrackerDisplayMode.ONLY_RARE -> {
                val doubleHookText = " ${DARK_GRAY}| ${GRAY}DH: ${WHITE}${totalInfo.rareDoubleHookAmountFormatted} ${GRAY}${totalInfo.rareDoubleHookPercentFormatted}"
                val cocoonedText = " ${DARK_GRAY}| ${GRAY}BS: ${WHITE}${totalInfo.rareCocoonedAmountFormatted} ${GRAY}${totalInfo.rareCocoonedPercentFormatted}"
                return "${AQUA}Total: ${WHITE}${totalInfo.rareTotalAmountFormatted} ${GRAY}rare out of ${WHITE}${totalInfo.allTotalAmountFormatted}${doubleHookText}${cocoonedText}"
            }
        }
    }

    private fun getTotalLineTooltip(totalInfo: TotalTrackerEntry): List<Component> {
        val lines = listOfNotNull(
            "${AQUA}Total",
            "${GRAY}All SC: ${WHITE}${totalInfo.allTotalAmountFormatted}",
            "  ${WHITE}${totalInfo.allCaughtAmountFormatted} ${GRAY}caught, ${WHITE}${totalInfo.allCocoonedAmountFormatted} ${GRAY}cocooned",
            "  ${GRAY}Double hook: ${WHITE}${totalInfo.allDoubleHookAmountFormatted} ${GRAY}(${WHITE}${totalInfo.allDoubleHookPercentFormatted} ${GRAY}of catches)",
            "  ${GRAY}Bloodshot: ${WHITE}${totalInfo.allCocoonedAmountFormatted} ${GRAY}(${WHITE}${totalInfo.allCocoonedPercentFormatted}${GRAY})",
            "",
            "${GRAY}Rare SC: ${WHITE}${totalInfo.rareCaughtAmountFormatted}",
            "  ${WHITE}${totalInfo.rareCaughtAmountFormatted} ${GRAY}caught, ${WHITE}${totalInfo.rareCocoonedAmountFormatted} ${GRAY}cocooned",
            "  ${GRAY}Double hook: ${WHITE}${totalInfo.rareDoubleHookAmountFormatted} ${GRAY}(${WHITE}${totalInfo.rareDoubleHookPercentFormatted} ${GRAY}of catches)",
            "  ${GRAY}Bloodshot: ${WHITE}${totalInfo.rareCocoonedAmountFormatted} ${GRAY}(${WHITE}${totalInfo.rareCocoonedPercentFormatted}${GRAY})",
        )
        return lines.map { Component.literal(it) }
    }

    private data class SeaCreatureTrackerEntry(
        val seaCreature: String,
        val seaCreatureInfo: SeaCreatures.SeaCreatureInfo,
        val totalAmount: Int,
        val totalAmountFormatted: String,
        val caughtAmount: Int,
        val caughtAmountFormatted: String,
        val percent: Double,
        val percentFormatted: String,
        val cocoonedAmount: Int,
        val cocoonedAmountFormatted: String,
        val cocoonedPercent: Double,
        val cocoonedPercentFormatted: String,
        val doubleHookAmount: Int,
        val doubleHookAmountFormatted: String,
        val doubleHookPercent: Double,
        val doubleHookPercentFormatted: String
    )

    private data class TotalTrackerEntry(
        // All sea creatures without rare filtering
        val allTotalAmount: Int,
        val allTotalAmountFormatted: String,
        val allCaughtAmount: Int,
        val allCaughtAmountFormatted: String,
        val allDoubleHookAmount: Int,
        val allDoubleHookAmountFormatted: String,
        val allDoubleHookPercent: Double,
        val allDoubleHookPercentFormatted: String,
        val allCocoonedAmount: Int,
        val allCocoonedAmountFormatted: String,
        val allCocoonedPercent: Double,
        val allCocoonedPercentFormatted: String,
        // Rare sea creatures
        val rareTotalAmount: Int,
        val rareTotalAmountFormatted: String,
        val rareCaughtAmount: Int,
        val rareCaughtAmountFormatted: String,
        val rareDoubleHookAmount: Int,
        val rareDoubleHookAmountFormatted: String,
        val rareDoubleHookPercent: Double,
        val rareDoubleHookPercentFormatted: String,
        val rareCocoonedAmount: Int,
        val rareCocoonedAmountFormatted: String,
        val rareCocoonedPercent: Double,
        val rareCocoonedPercentFormatted: String,
    )
}
