package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GameClosedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.PriceUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.GuiButton
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import java.util.Date

object ArchfiendDiceProfitTracker {
    enum class ViewMode {
        SESSION,
        TOTAL
    }

    enum class DiceType {
        ARCHFIEND,
        HIGH_CLASS
    }

    data class DiceData(
        var rollsCount: Int = 0,
        var rollsCost: Long = 0,
        var count6: Int = 0,
        var count7: Int = 0,
        var lostDicesCost: Long = 0,
        var earnedCost: Long = 0,
        var profit: Long = 0
    )

    data class ArchfiendDiceData(
        var archfiend: DiceData = DiceData(),
        var highClass: DiceData = DiceData(),
        var profit: Long = 0
    )

    data class ArchfiendDiceProfitData(
        var session: ArchfiendDiceData = ArchfiendDiceData(),
        var total: ArchfiendDiceData = ArchfiendDiceData(),
        var viewMode: String = ViewMode.SESSION.name
    )

    const val RESET_COMMAND = "feeshResetArchfiendDiceProfit"
    const val RESET_TOTAL_COMMAND = "feeshResetArchfiendDiceProfitTotal"
    private const val TOGGLE_VIEW_MODE_COMMAND = "feeshToggleArchfiendDiceViewMode"
    private const val ARCHFIEND_DICE_ID = "ARCHFIEND_DICE"
    private const val HIGH_CLASS_ARCHFIEND_DICE_ID = "HIGH_CLASS_ARCHFIEND_DICE"
    private const val ARCHFIEND_DYE_ID = "DYE_ARCHFIEND"
    
    private const val ARCHFIEND_DICE_ROLL_COST = 666_000L
    private const val ARCHFIEND_DICE_WIN_COST = 15_000_000L
    private const val HIGH_CLASS_ARCHFIEND_DICE_ROLL_COST = 6_600_000L
    private const val HIGH_CLASS_ARCHFIEND_DICE_WIN_COST = 100_000_000L

    private val ARCHFIEND_DICE_ROLL_MESSAGE = Regex("^Your Archfiend Dice rolled a (\\d+)!")
    private val HIGH_CLASS_ARCHFIEND_DICE_ROLL_MESSAGE = Regex("^Your High Class Archfiend Dice rolled a (\\d+)!")

    private var data = PersistentDataManager.feeshData.archfiendDiceProfit
    private var lastDiceRolledAt: Date? = null
    private var tickCounter = 0
    private const val TICKS_PER_UPDATE = 20

    private val baseTitle = "${AQUA}${BOLD}Archfiend Dice profit tracker"

    private val gui = FeeshGui()
        .setCoordsDataKey("archfiendDiceProfitTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            "${baseTitle} ${GRAY}[${GREEN}Session${GRAY}]",
            "",
            "${DARK_PURPLE}${BOLD}Archfiend Dice",
            "${WHITE}1 000${GRAY}x rolls | ${WHITE}10${GRAY}x ${DARK_PURPLE}6 ${GRAY}| ${WHITE}5${GRAY}x ${DARK_PURPLE}7",
            "${AQUA}Profit: ${GREEN}1.5M",
            "",
            "${GOLD}${BOLD}High Class Archfiend Dice",
            "${WHITE}100${GRAY}x rolls | ${WHITE}2${GRAY}x ${DARK_PURPLE}6 ${GRAY}| ${WHITE}1${GRAY}x ${DARK_PURPLE}7",
            "${AQUA}Profit: ${RED}-500k",
            "",
            "${AQUA}${BOLD}Total profit: ${GREEN}1M"
        ))
        .setSettingsKey { Overlays.archfiendDiceProfitTrackerOverlay }
        .setCondition {
            isRolledRecently()
        }

    fun init() {
        registerChatHandlers()
        registerCommands()
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }

    private fun registerChatHandlers() {
        RegisterUtils.chat(ARCHFIEND_DICE_ROLL_MESSAGE) { _, matchResult ->
            val number = matchResult.groupValues[1].toIntOrNull() ?: return@chat
            trackArchfiendDiceRoll(data.session, DiceType.ARCHFIEND, number, true)
            trackArchfiendDiceRoll(data.total, DiceType.ARCHFIEND, number, false)
        }

        RegisterUtils.chat(HIGH_CLASS_ARCHFIEND_DICE_ROLL_MESSAGE) { _, matchResult ->
            val number = matchResult.groupValues[1].toIntOrNull() ?: return@chat
            trackArchfiendDiceRoll(data.session, DiceType.HIGH_CLASS, number, true)
            trackArchfiendDiceRoll(data.total, DiceType.HIGH_CLASS, number, false)
        }
    }

    private fun registerCommands() {
        RegisterUtils.command(RESET_COMMAND) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            val viewMode = getCurrentViewMode()
            resetArchfiendDiceProfitTracker(isConfirmed, viewMode)
        }

        RegisterUtils.command(RESET_TOTAL_COMMAND) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetArchfiendDiceProfitTracker(isConfirmed, ViewMode.TOTAL)
        }

        RegisterUtils.command(TOGGLE_VIEW_MODE_COMMAND) {
            toggleViewMode()
        }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        updateGuiLines()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        lastDiceRolledAt = null
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetArchfiendDiceProfitTrackerSessionOnGameClosed &&
            Overlays.archfiendDiceProfitTrackerOverlay &&
            (data.session.archfiend.rollsCount > 0 || data.session.highClass.rollsCount > 0)) {
            resetSession()
            FeeshMod.LOGGER.info("[Feesh] Automatically reset Archfiend Dice profit tracker [Session] on game closed.")
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

    private fun getSourceObject(viewMode: ViewMode): ArchfiendDiceData {
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
        data.session = ArchfiendDiceData()
        saveData()
    }

    private fun resetTotal() {
        data.total = ArchfiendDiceData()
        saveData()
    }

    private fun resetArchfiendDiceProfitTracker(isConfirmed: Boolean, resetViewMode: ViewMode) {
        CommonUtils.runWithCatching("Failed to reset Archfiend Dice profit tracker") {
            val viewModeText = getViewModeDisplayText(resetViewMode)

            if (!isConfirmed) {
                val resetAction = when (resetViewMode) {
                    ViewMode.SESSION -> "$RESET_COMMAND noconfirm"
                    ViewMode.TOTAL -> "$RESET_TOTAL_COMMAND noconfirm"
                }
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to reset Archfiend Dice profit tracker ${viewModeText}${WHITE}? ${RED}${BOLD}[Click to confirm]",
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
            ChatUtils.sendLocalChat("${WHITE}Archfiend Dice profit tracker ${viewModeText} ${WHITE}was reset.", true)
        }
    }

    private fun trackArchfiendDiceRoll(
        sourceObj: ArchfiendDiceData,
        diceType: DiceType,
        number: Int,
        announceCost: Boolean
    ) {
        CommonUtils.runWithCatching("Failed to track Archfiend Dice roll for Archfiend Dice profit tracker") {
            if (!Overlays.archfiendDiceProfitTrackerOverlay || !WorldUtils.isInSkyblock() || number < 1 || number > 7) return

            lastDiceRolledAt = Date()

            val diceData = when (diceType) {
                DiceType.ARCHFIEND -> sourceObj.archfiend
                DiceType.HIGH_CLASS -> sourceObj.highClass
            }

            val rollCost = when (diceType) {
                DiceType.ARCHFIEND -> ARCHFIEND_DICE_ROLL_COST
                DiceType.HIGH_CLASS -> HIGH_CLASS_ARCHFIEND_DICE_ROLL_COST
            }

            val winCost = when (diceType) {
                DiceType.ARCHFIEND -> ARCHFIEND_DICE_WIN_COST
                DiceType.HIGH_CLASS -> HIGH_CLASS_ARCHFIEND_DICE_WIN_COST
            }

            val diceId = when (diceType) {
                DiceType.ARCHFIEND -> ARCHFIEND_DICE_ID
                DiceType.HIGH_CLASS -> HIGH_CLASS_ARCHFIEND_DICE_ID
            }

            val displayName = when (diceType) {
                DiceType.ARCHFIEND -> "${EPIC}Archfiend Dice"
                DiceType.HIGH_CLASS -> "${LEGENDARY}High Class Archfiend Dice"
            }

            diceData.rollsCount++
            diceData.rollsCost -= rollCost
            diceData.profit -= rollCost
            sourceObj.profit -= rollCost

            if (number == 6) {
                val dicePrice = PriceUtils.getAuctionItemPrice(diceId)?.lbin?.toLong() ?: 0L
                diceData.count6 += 1
                diceData.lostDicesCost -= dicePrice
                diceData.earnedCost += winCost
                diceData.profit -= dicePrice
                diceData.profit += winCost
                sourceObj.profit -= dicePrice
                sourceObj.profit += winCost

                if (announceCost) {
                    val winCostShort = CommonUtils.toShortNumber(winCost.toDouble()) ?: "0"
                    val dicePriceShort = CommonUtils.toShortNumber(dicePrice.toDouble()) ?: "0"
                    ChatUtils.sendLocalChat(
                        "${WHITE}You gained ${GOLD}${winCostShort} ${WHITE}coins for rolling 6, ${WHITE}but lost ${displayName} ${WHITE}costing ${GOLD}${dicePriceShort} ${WHITE}coins.",
                        true
                    )
                }
            }

            if (number == 7) {
                val dicePrice = PriceUtils.getAuctionItemPrice(diceId)?.lbin?.toLong() ?: 0L
                val dyePrice = PriceUtils.getAuctionItemPrice(ARCHFIEND_DYE_ID)?.lbin?.toLong() ?: 0L
                diceData.count7 += 1
                diceData.lostDicesCost -= dicePrice
                diceData.earnedCost += dyePrice
                diceData.profit -= dicePrice
                diceData.profit += dyePrice
                sourceObj.profit -= dicePrice
                sourceObj.profit += dyePrice

                if (announceCost) {
                    val dyePriceShort = CommonUtils.toShortNumber(dyePrice.toDouble()) ?: "0"
                    val dicePriceShort = CommonUtils.toShortNumber(dicePrice.toDouble()) ?: "0"
                    ChatUtils.sendLocalChat(
                        "${WHITE}You gained ${DARK_RED}Archfiend Dye ${WHITE}costing ${GOLD}${dyePriceShort} ${WHITE}coins for rolling 7, but lost ${displayName} ${WHITE}costing ${GOLD}${dicePriceShort} ${WHITE}coins.",
                        true
                    )
                }
            }

            saveData()
            updateGuiLines()
        }
    }

    private fun updateGuiLines() {
        CommonUtils.runWithCatching("Failed to refresh tracker data for Archfiend Dice profit tracker") {
            gui.clearLines()

            val viewMode = getCurrentViewMode()

            if (!Overlays.archfiendDiceProfitTrackerOverlay ||
                !WorldUtils.isInSkyblock() ||
                !isRolledRecently() ||
                (viewMode == ViewMode.SESSION && data.session.archfiend.rollsCount == 0 && data.session.highClass.rollsCount == 0) ||
                (viewMode == ViewMode.TOTAL && data.total.archfiend.rollsCount == 0 && data.total.highClass.rollsCount == 0)
            ) return

            val sourceObj = getSourceObject(viewMode)
            val viewModeText = getViewModeDisplayText(viewMode)
            val lines = mutableListOf<String>()
            val nextMode = if (viewMode == ViewMode.SESSION) ViewMode.TOTAL else ViewMode.SESSION
            val nextModeText = getViewModeDisplayText(nextMode)

            lines.add("${baseTitle} ${viewModeText}")
            lines.add("")

            lines.add("${DARK_PURPLE}${BOLD}Archfiend Dice")
            lines.add("${WHITE}${CommonUtils.formatNumberWithSpaces(sourceObj.archfiend.rollsCount)}${GRAY}x rolls | ${WHITE}${CommonUtils.formatNumberWithSpaces(sourceObj.archfiend.count6)}${GRAY}x ${DARK_PURPLE}6 ${GRAY}| ${WHITE}${CommonUtils.formatNumberWithSpaces(sourceObj.archfiend.count7)}${GRAY}x ${DARK_PURPLE}7")
            val archfiendProfitColor = if (sourceObj.archfiend.profit > 0) GREEN else if (sourceObj.archfiend.profit < 0) RED else WHITE
            val archfiendProfitShort = CommonUtils.toShortNumber(sourceObj.archfiend.profit.toDouble()) ?: "0"
            lines.add("${AQUA}Profit: ${archfiendProfitColor}${archfiendProfitShort}")

            lines.add("")
            lines.add("${GOLD}${BOLD}High Class Archfiend Dice")
            lines.add("${WHITE}${CommonUtils.formatNumberWithSpaces(sourceObj.highClass.rollsCount)}${GRAY}x rolls | ${WHITE}${CommonUtils.formatNumberWithSpaces(sourceObj.highClass.count6)}${GRAY}x ${DARK_PURPLE}6 ${GRAY}| ${WHITE}${CommonUtils.formatNumberWithSpaces(sourceObj.highClass.count7)}${GRAY}x ${DARK_PURPLE}7")
            val highClassProfitColor = if (sourceObj.highClass.profit > 0) GREEN else if (sourceObj.highClass.profit < 0) RED else WHITE
            val highClassProfitShort = CommonUtils.toShortNumber(sourceObj.highClass.profit.toDouble()) ?: "0"
            lines.add("${AQUA}Profit: ${highClassProfitColor}${highClassProfitShort}")

            val profitColor = if (sourceObj.profit > 0) GREEN else if (sourceObj.profit < 0) RED else WHITE
            val profitShort = CommonUtils.toShortNumber(sourceObj.profit.toDouble()) ?: "0"
            lines.add("")
            lines.add("${AQUA}${BOLD}Total profit: ${profitColor}${profitShort}")

            gui.setLines(lines)
            gui.setButtons(listOf(
                GuiButton(0, "${GRAY}[Click to show $nextModeText${GRAY}]", { toggleViewMode() }),
                GuiButton(1, "${GRAY}[${RED}Click to reset${GRAY}]", { resetArchfiendDiceProfitTracker(false, getCurrentViewMode()) })
            ))
        }
    }

    private fun isRolledRecently(): Boolean {
        return lastDiceRolledAt != null && Date().time - lastDiceRolledAt!!.time < 60_000
    }

    private fun saveData() {
        PersistentDataManager.saveFeeshDataToFileAsync()
    }
}
