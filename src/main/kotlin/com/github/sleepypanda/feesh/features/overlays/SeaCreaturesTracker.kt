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

// TODO [Feesh] Changed count of Sea Guardian to 1x in the Sea creatures tracker [Session]. While its 2x due to 1 bloodshot
// [Feesh] Do you want to delete 1x Sea Guardian from the Sea creatures tracker [Session]? [Click to confirm] - should be 2 due to 1 bs
// Remove +- buttons as they are not intuitive, from what number to substract
// Adjust set command to set the count of caught and cocooned and DH?
// Add guidance on how to use the commands
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
            "${GRAY}- ${GOLD}Yeti: ${WHITE}10 ${GRAY}1% ${DARK_GRAY}| ${GRAY}DH: ${WHITE}1 ${GRAY}20% | ${GRAY}BS: ${WHITE}1 ${GRAY}10%",
            "${GRAY}- ${LIGHT_PURPLE}Reindrake: ${WHITE}1 ${GRAY}0.1% ${DARK_GRAY}| ${GRAY}DH: ${WHITE}0 ${GRAY}0% | ${GRAY}BS: ${WHITE}0 ${GRAY}0%",
            "",
            "${AQUA}Total: ${WHITE}11 ${GRAY}rare out of ${WHITE}1000 ${DARK_GRAY}| ${GRAY}DH: ${WHITE}1 ${GRAY}20% | ${GRAY}BS: ${WHITE}1 ${GRAY}10%",
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
        if (!Overlays.seaCreaturesTrackerOverlay || !Overlays.countCocoonedSeaCreatures || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

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

            recalculateTotalCaughtCount(sourceObj)
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
            recalculateTotalCaughtCount(sourceObj)
            saveData()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}Deleted ${WHITE}${entry.amount}x ${displayName}${WHITE} from the Sea creatures tracker ${viewModeText}${WHITE}.", true)
        }
    }

    private fun findSeaCreatureInfoByName(name: String): SeaCreatures.SeaCreatureInfo? {
        return SeaCreatures.allSeaCreatures.find { it.name.equals(name.trim(), ignoreCase = true) }
    }

    private fun recalculateTotalCaughtCount(sourceObj: SeaCreaturesData) {
        sourceObj.totalCount = sourceObj.catches.values.sumOf { it.amount }
    }

    private fun recalculateTotalCocoonedCount(sourceObj: SeaCreaturesData) {
        sourceObj.totalCocoonedCount = sourceObj.catches.values.sumOf { it.cocoonedAmount }
    }

    private fun onLineSeaCreatureIncrease(seaCreatureKey: String) {
        CommonUtils.runWithCatching("Failed to change sea creature amount in Sea creatures tracker") {
            val viewMode = getCurrentViewMode()
            val sourceObj = getSourceObject(viewMode)
            val entry = sourceObj.catches[seaCreatureKey] ?: return
            val seaCreatureInfo = SeaCreatures.allSeaCreatures.find { it.name.uppercase() == seaCreatureKey } ?: return

            entry.amount += 1
            recalculateTotalCaughtCount(sourceObj)
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
            recalculateTotalCaughtCount(sourceObj)
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
                    caughtAmount = caughtAmount,
                    cocoonedAmount = value.cocoonedAmount,
                    formattedCocoonedAmount = CommonUtils.formatNumberWithSpaces(value.cocoonedAmount),
                    totalAmount = totalAmount,
                    formattedTotalAmount = CommonUtils.formatNumberWithSpaces(totalAmount),
                    percent = percent,
                    formattedPercent = "${decimalFormat.format(percent)}%",
                    cocoonedPercent = cocoonedPercent,
                    formattedCocoonedPercent = "${decimalFormat.format(cocoonedPercent)}%",
                    doubleHookAmount = value.doubleHookAmount,
                    formattedDoubleHookAmount = CommonUtils.formatNumberWithSpaces(value.doubleHookAmount),
                    doubleHookPercent = doubleHookPercent,
                    formattedDoubleHookPercent = "${decimalFormat.format(doubleHookPercent)}%",
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
                            LineAction("${GRAY}[${GREEN}+${GRAY}]") { onLineSeaCreatureIncrease(entry.seaCreature) },
                            LineAction("${GRAY}[${RED}-${GRAY}]") { onLineSeaCreatureDecrease(entry.seaCreature) },
                            LineAction("${GRAY}[${RED}x${GRAY}]") { onLineSeaCreatureDelete(entry.seaCreature) }
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
        val countText = "${WHITE}${entry.formattedTotalAmount}"
        val percentText = if (showPercentage) " ${GRAY}${entry.formattedPercent}" else ""
            
        val doubleHookText = if (showDoubleHook && entry.seaCreatureInfo.canBeDoubleHooked) {
            " ${DARK_GRAY}| ${GRAY}DH: ${WHITE}${entry.formattedDoubleHookAmount} ${GRAY}${entry.formattedDoubleHookPercent}"
        } else ""
        val cocoonedText = if (showCocooned) {
            " ${DARK_GRAY}| ${GRAY}BS: ${WHITE}${entry.formattedCocoonedAmount} ${GRAY}${entry.formattedCocoonedPercent}"
        } else ""

        return "${GRAY}- $seaCreatureText${GRAY}: $countText$percentText$doubleHookText$cocoonedText"
    }

    private fun getSeaCreatureLineTooltip(entry: SeaCreatureTrackerEntry): List<Component> {
        val doubleHookText = if (entry.seaCreatureInfo.canBeDoubleHooked) {
            "${GRAY}Double hook: ${WHITE}${entry.formattedDoubleHookAmount} ${GRAY}(${WHITE}${entry.formattedDoubleHookPercent} ${GRAY}of catches)"
        } else null
        val lines = listOfNotNull(
            entry.seaCreatureInfo.displayName,
            "${WHITE}${CommonUtils.formatNumberWithSpaces(entry.totalAmount)} ${GRAY}(caught + cocooned)",
            "${WHITE}${entry.formattedPercent} ${GRAY}out of total SC",
            doubleHookText,
            "${GRAY}Cocooned: ${WHITE}${entry.formattedCocoonedAmount} ${GRAY}(${WHITE}${entry.formattedCocoonedPercent}${GRAY})",
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
            allDoubleHookPercentFormatted = "${decimalFormat.format(totalDoubleHookPercent)}%",
            allCocoonedAmount = totalCocoonedAmount,
            allCocoonedAmountFormatted = CommonUtils.formatNumberWithSpaces(totalCocoonedAmount),
            allCocoonedPercent = totalCocoonedPercent,
            allCocoonedPercentFormatted = "${decimalFormat.format(totalCocoonedPercent)}%",
            rareTotalAmount = rareTotalAmount,
            rareTotalAmountFormatted = CommonUtils.formatNumberWithSpaces(rareTotalAmount),
            rareCaughtAmount = rareCaughtAmount,
            rareCaughtAmountFormatted = CommonUtils.formatNumberWithSpaces(rareCaughtAmount),
            rareDoubleHookAmount = rareDoubleHookAmount,
            rareDoubleHookAmountFormatted = CommonUtils.formatNumberWithSpaces(rareDoubleHookAmount),
            rareDoubleHookPercent = rareDoubleHookPercent,
            rareDoubleHookPercentFormatted = "${decimalFormat.format(rareDoubleHookPercent)}%",
            rareCocoonedAmount = rareCocoonedAmount,
            rareCocoonedAmountFormatted = CommonUtils.formatNumberWithSpaces(rareCocoonedAmount),
            rareCocoonedPercent = rareCocoonedPercent,
            rareCocoonedPercentFormatted = "${decimalFormat.format(rareCocoonedPercent)}%",
        )
    }
    
    private fun getTotalLineText(displayMode: SeaCreaturesTrackerDisplayMode, totalInfo: TotalTrackerEntry): String {
        val showDoubleHook = Overlays.showSeaCreaturesDoubleHookStatistics
        val showCocooned = Overlays.showCocoonedStatistics

        when (displayMode) {
            SeaCreaturesTrackerDisplayMode.ALL -> {
                val doubleHookText = if (showDoubleHook) " ${DARK_GRAY}| ${GRAY}DH: ${WHITE}${totalInfo.allDoubleHookAmountFormatted} ${GRAY}${totalInfo.allDoubleHookPercentFormatted}" else ""
                val cocoonedText = if (showCocooned) " ${DARK_GRAY}| ${GRAY}BS: ${WHITE}${totalInfo.allCocoonedAmountFormatted} ${GRAY}${totalInfo.allCocoonedPercentFormatted}" else ""
                return "${AQUA}Total: ${WHITE}${totalInfo.allTotalAmountFormatted}${doubleHookText}${cocoonedText}"
            }
            SeaCreaturesTrackerDisplayMode.ONLY_RARE -> {
                val doubleHookText = if (showDoubleHook) " ${DARK_GRAY}| ${GRAY}DH: ${WHITE}${totalInfo.rareDoubleHookAmountFormatted} ${GRAY}${totalInfo.rareDoubleHookPercentFormatted}" else ""
                val cocoonedText = if (showCocooned) " ${DARK_GRAY}| ${GRAY}BS: ${WHITE}${totalInfo.rareCocoonedAmountFormatted} ${GRAY}${totalInfo.rareCocoonedPercentFormatted}" else ""
                return "${AQUA}Total: ${WHITE}${totalInfo.rareTotalAmountFormatted} ${GRAY}rare out of ${WHITE}${totalInfo.allTotalAmountFormatted}${doubleHookText}${cocoonedText}"
            }
        }
    }

    private fun getTotalLineTooltip(totalInfo: TotalTrackerEntry): List<Component> {
        val lines = listOfNotNull(
            "${AQUA}Total",
            "${GRAY}All SC: ${WHITE}${totalInfo.allTotalAmountFormatted} ${GRAY}(caught + cocooned)",
            "${GRAY}Double hook: ${WHITE}${totalInfo.allDoubleHookAmountFormatted} ${GRAY}(${WHITE}${totalInfo.allDoubleHookPercentFormatted} ${GRAY}of catches)",
            "${GRAY}Cocooned: ${WHITE}${totalInfo.allCocoonedAmountFormatted} ${GRAY}(${WHITE}${totalInfo.allCocoonedPercentFormatted}${GRAY})",
            "",
            "${GRAY}Rare SC: ${WHITE}${totalInfo.rareCaughtAmountFormatted} ${GRAY}(caught + cocooned)",
            "${GRAY}Double hook: ${WHITE}${totalInfo.rareDoubleHookAmountFormatted} ${GRAY}(${WHITE}${totalInfo.rareDoubleHookPercentFormatted} ${GRAY}of catches)",
            "${GRAY}Cocooned: ${WHITE}${totalInfo.rareCocoonedAmountFormatted} ${GRAY}(${WHITE}${totalInfo.rareCocoonedPercentFormatted}${GRAY})",
        )
        return lines.map { Component.literal(it) }
    }

    private data class SeaCreatureTrackerEntry(
        val seaCreature: String,
        val seaCreatureInfo: SeaCreatures.SeaCreatureInfo,
        val caughtAmount: Int,
        val cocoonedAmount: Int,
        val totalAmount: Int,
        val formattedTotalAmount: String,
        val percent: Double,
        val formattedPercent: String,
        val cocoonedPercent: Double,
        val formattedCocoonedAmount: String,
        val formattedCocoonedPercent: String,
        val doubleHookAmount: Int,
        val formattedDoubleHookAmount: String,
        val doubleHookPercent: Double,
        val formattedDoubleHookPercent: String
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
