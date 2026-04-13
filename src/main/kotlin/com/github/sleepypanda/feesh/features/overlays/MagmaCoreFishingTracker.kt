package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.RareDropTypes
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.GameClosedEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.RareDropEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.PriceUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.GuiButton
import java.util.Date

object MagmaCoreFishingTracker {
    enum class ViewMode {
        SESSION,
        TOTAL
    }

    data class MagmaCoreFishingSourceData(
        var magmaCoresCount: Int = 0,
        var lastAddedMagmaCoresCount: Int = 0,
        var seaCreaturesCaughtCount: Int = 0,
        var elapsedSeconds: Int = 0
    )

    data class MagmaCoreFishingData(
        var session: MagmaCoreFishingSourceData = MagmaCoreFishingSourceData(),
        var total: MagmaCoreFishingSourceData = MagmaCoreFishingSourceData(),
        var viewMode: String = ViewMode.SESSION.name
    )

    const val RESET_COMMAND = "feeshResetMagmaCoreFishing"
    const val RESET_TOTAL_COMMAND = "feeshResetMagmaCoreFishingTotal"
    const val PAUSE_COMMAND = "feeshPauseMagmaCoreFishing"
    private const val TRACKER_NAME = "Magma Core fishing tracker"
    private const val TOGGLE_VIEW_MODE_COMMAND = "feeshToggleMagmaCoreFishingViewMode"

    private const val TICKS_PER_UPDATE = 20
    private const val MAX_SECONDS_SINCE_LAST_CATCH = 60
    private const val HIDE_OVERLAY_AFTER_HOOK_MINUTES = 5
    private const val DEDUPE_CORES_MILLISECONDS = 10_000L // To aggregate multiple drops in a short period of time (e.g. clearing cap)

    private val data: MagmaCoreFishingData
        get() = PersistentDataManager.feeshData.magmaCoreFishing

    private var isSessionActive = false
    private var tickCounter = 0
    private var lastSeaCreatureCaughtAt: Date? = null
    private var lastMagmaCoreDroppedAt: Date? = null

    private val baseTitle = "${YELLOW}${BOLD}$TRACKER_NAME"

    private val gui = FeeshGui()
        .setCoordsDataKey("magmaCoreFishingTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            "$baseTitle ${GRAY}[${GREEN}Session${GRAY}]",
            "${GREEN}Total sea creatures caught: ${WHITE}13 245",
            "${BLUE}Total magma cores: ${WHITE}156 ${GRAY}[+2 last added]",
            "${GOLD}Total coins (sell offer): ${WHITE}43.6M",
            "${GOLD}Total coins (insta-sell): ${WHITE}41.5M",
            "",
            "${GREEN}Sea creatures caught/h: ${WHITE}221",
            "${BLUE}Magma cores/h: ${WHITE}4",
            "${GOLD}Coins/h (sell offer): ${WHITE}1.2M",
            "${GOLD}Coins/h (insta-sell): ${WHITE}1.1M",
            "",
            "${AQUA}Elapsed time: ${WHITE}59h 54m 10s"
        ))
        .setSettingsKey { Overlays.magmaCoreFishingTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.magmaCoreFishingTrackerCustomStyle }
        .setCondition { isTrackerVisible() }

    fun init() {
        registerCommands()
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onOwnSeaCreatureCaught)
        EventBus.subscribe(RareDropEvent::class, ::onRareDrop)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }

    private fun registerCommands() {
        RegisterUtils.command(RESET_COMMAND) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetMagmaCoreFishingTracker(isConfirmed, getCurrentViewMode())
        }
        RegisterUtils.command(RESET_TOTAL_COMMAND) { args ->
            val isConfirmed = args.isNotEmpty() && args[0] == "noconfirm"
            resetMagmaCoreFishingTracker(isConfirmed, ViewMode.TOTAL)
        }
        RegisterUtils.command(PAUSE_COMMAND) {
            pauseMagmaCoreFishingTracker()
        }
        RegisterUtils.command(TOGGLE_VIEW_MODE_COMMAND) {
            toggleViewMode()
        }
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        if (!Overlays.magmaCoreFishingTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRYSTAL_HOLLOWS) {
            pause()
            return
        }

        refreshElapsedTime()
        updateGuiLines()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        pause()
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (!Overlays.resetMagmaCoreFishingTrackerSessionOnGameClosed) return

        val session = data.session
        if (session.magmaCoresCount > 0 || session.seaCreaturesCaughtCount > 0 || session.elapsedSeconds > 0) {
            resetSession(force = true)
            FeeshMod.LOGGER.info("[Feesh] Automatically reset $TRACKER_NAME [Session] on game closed.")
        }
    }

    private fun onOwnSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        CommonUtils.runWithCatching("Failed to track Magma Fields sea creature catch") {
            if (!Overlays.magmaCoreFishingTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRYSTAL_HOLLOWS) return
            if (!event.seaCreatureTypes.contains(SeaCreatureTypes.TYPE_MAGMA_FIELDS)) return

            isSessionActive = true
            lastSeaCreatureCaughtAt = Date()

            val diff = if (event.isDoubleHook) 2 else 1
            data.session.seaCreaturesCaughtCount += diff
            data.total.seaCreaturesCaughtCount += diff
            activateTimerInMode(ViewMode.SESSION)
            activateTimerInMode(ViewMode.TOTAL)

            saveData()
            updateGuiLines()
        }
    }

    private fun onRareDrop(event: RareDropEvent) {
        CommonUtils.runWithCatching("Failed to track Magma Core drop") {
            if (!isSessionActive || !Overlays.magmaCoreFishingTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRYSTAL_HOLLOWS) return
            if (event.itemName != RareDropTypes.MAGMA_CORE.displayName) return

            data.session.magmaCoresCount += 1
            data.total.magmaCoresCount += 1

            val now = Date()
            val previousDrop = lastMagmaCoreDroppedAt
            val isNearPrevious = previousDrop != null && now.time - previousDrop.time < DEDUPE_CORES_MILLISECONDS

            data.session.lastAddedMagmaCoresCount = if (isNearPrevious) data.session.lastAddedMagmaCoresCount + 1 else 1
            data.total.lastAddedMagmaCoresCount = if (isNearPrevious) data.total.lastAddedMagmaCoresCount + 1 else 1
            lastMagmaCoreDroppedAt = now

            saveData()
            updateGuiLines()
        }
    }

    private fun refreshElapsedTime() {
        val lastCatch = lastSeaCreatureCaughtAt ?: run {
            pause()
            return
        }

        val elapsedSinceCatch = (Date().time - lastCatch.time) / 1000
        if (elapsedSinceCatch < MAX_SECONDS_SINCE_LAST_CATCH) {
            isSessionActive = true
            data.session.elapsedSeconds += 1
            data.total.elapsedSeconds += 1
            saveData()
        } else {
            pause()
        }
    }

    private fun isTrackerVisible(): Boolean {
        if (!Overlays.magmaCoreFishingTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRYSTAL_HOLLOWS) return false
        if (!FishingHookUtils.wasFishingHookActiveMinutesAgo(HIDE_OVERLAY_AFTER_HOOK_MINUTES)) return false

        val sourceObj = getSourceObject(getCurrentViewMode())
        return sourceObj.magmaCoresCount > 0 || sourceObj.seaCreaturesCaughtCount > 0
    }

    private fun pause() {
        isSessionActive = false
    }

    fun pauseMagmaCoreFishingTracker() {
        CommonUtils.runWithCatching("Failed to pause $TRACKER_NAME") {
            if (!isSessionActive || !isTrackerVisible()) return

            pause()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}$TRACKER_NAME is paused. Continue fishing to resume it.", true)
        }
    }

    fun resetMagmaCoreFishingTracker(isConfirmed: Boolean, resetViewMode: ViewMode) {
        CommonUtils.runWithCatching("Failed to reset $TRACKER_NAME") {
            val viewModeText = getViewModeDisplayText(resetViewMode)
            if (!isConfirmed) {
                val resetAction = when (resetViewMode) {
                    ViewMode.SESSION -> "$RESET_COMMAND noconfirm"
                    ViewMode.TOTAL -> "$RESET_TOTAL_COMMAND noconfirm"
                }
                ChatUtils.sendLocalChatWithCommand(
                    "${WHITE}Do you want to reset $TRACKER_NAME ${viewModeText}${WHITE}? ${RED}${BOLD}[Click to confirm]",
                    resetAction,
                    true
                )
                return
            }

            pause()
            when (resetViewMode) {
                ViewMode.SESSION -> resetSession()
                ViewMode.TOTAL -> resetTotal()
            }
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}$TRACKER_NAME ${viewModeText} ${WHITE}was reset.", true)
        }
    }

    private fun activateTimerInMode(viewMode: ViewMode) {
        val sourceObj = getSourceObject(viewMode)
        if (sourceObj.elapsedSeconds == 0) {
            sourceObj.elapsedSeconds = 1
        }
    }

    private fun getCurrentViewMode(): ViewMode {
        return try {
            ViewMode.valueOf(data.viewMode)
        } catch (_: Exception) {
            ViewMode.SESSION
        }
    }

    private fun toggleViewMode() {
        val newMode = if (getCurrentViewMode() == ViewMode.SESSION) ViewMode.TOTAL else ViewMode.SESSION
        data.viewMode = newMode.name
        saveData()
        updateGuiLines()
    }

    private fun getSourceObject(viewMode: ViewMode): MagmaCoreFishingSourceData {
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

    private fun resetSession(force: Boolean = false) {
        data.session = MagmaCoreFishingSourceData()
        lastSeaCreatureCaughtAt = null
        lastMagmaCoreDroppedAt = null
        saveData(force)
    }

    private fun resetTotal() {
        data.total = MagmaCoreFishingSourceData()
        lastSeaCreatureCaughtAt = null
        lastMagmaCoreDroppedAt = null
        saveData()
    }

    private fun updateGuiLines() {
        CommonUtils.runWithCatching("Failed to update $TRACKER_NAME GUI lines") {
            gui.clearLines()
            if (!isTrackerVisible()) {
                pause()
                return
            }

            val viewMode = getCurrentViewMode()
            val sourceObj = getSourceObject(viewMode)
            val viewModeText = getViewModeDisplayText(viewMode)
            val nextMode = if (viewMode == ViewMode.SESSION) ViewMode.TOTAL else ViewMode.SESSION
            val nextModeText = getViewModeDisplayText(nextMode)

            val elapsedHours = sourceObj.elapsedSeconds / 3600.0
            val magmaCorePrice = PriceUtils.getBazaarItemPrices("MAGMA_CORE")
            val sellOffer = magmaCorePrice?.sellOffer?.toLong() ?: 0L
            val instaSell = magmaCorePrice?.instaSell?.toLong() ?: 0L

            val totalSellOffer = sourceObj.magmaCoresCount * sellOffer
            val totalInstaSell = sourceObj.magmaCoresCount * instaSell
            val magmaCoresPerHour = if (elapsedHours > 0) (sourceObj.magmaCoresCount / elapsedHours).toInt() else 0
            val seaCreaturesPerHour = if (elapsedHours > 0) (sourceObj.seaCreaturesCaughtCount / elapsedHours).toInt() else 0
            val sellOfferPerHour = magmaCoresPerHour * sellOffer
            val instaSellPerHour = magmaCoresPerHour * instaSell
            val pausedText = if (isSessionActive) "" else " ${GRAY}[Paused]"

            val lines = mutableListOf<String>()
            lines.add("$baseTitle $viewModeText")
            lines.add("${GREEN}Total sea creatures caught: ${WHITE}${CommonUtils.formatNumberWithSpaces(sourceObj.seaCreaturesCaughtCount)}")
            lines.add("${BLUE}Total magma cores: ${WHITE}${CommonUtils.formatNumberWithSpaces(sourceObj.magmaCoresCount)} ${GRAY}[+${CommonUtils.formatNumberWithSpaces(sourceObj.lastAddedMagmaCoresCount)} last added]")
            lines.add("${GOLD}Total coins (sell offer): ${WHITE}${CommonUtils.toShortNumber(totalSellOffer.toDouble()) ?: "0"}")
            lines.add("${GOLD}Total coins (insta-sell): ${WHITE}${CommonUtils.toShortNumber(totalInstaSell.toDouble()) ?: "0"}")
            lines.add("")
            lines.add("${GREEN}Sea creatures caught/h: ${WHITE}${CommonUtils.formatNumberWithSpaces(seaCreaturesPerHour)}")
            lines.add("${BLUE}Magma cores/h: ${WHITE}${CommonUtils.formatNumberWithSpaces(magmaCoresPerHour)}")
            lines.add("${GOLD}Coins/h (sell offer): ${WHITE}${CommonUtils.toShortNumber(sellOfferPerHour.toDouble()) ?: "0"}")
            lines.add("${GOLD}Coins/h (insta-sell): ${WHITE}${CommonUtils.toShortNumber(instaSellPerHour.toDouble()) ?: "0"}")
            lines.add("")
            lines.add("${AQUA}Elapsed time: ${WHITE}${CommonUtils.formatTimeElapsed(sourceObj.elapsedSeconds)}$pausedText")

            gui.setLines(lines)
            gui.setButtons(listOf(
                GuiButton(0, "${GRAY}[Click to show $nextModeText${GRAY}]") { toggleViewMode() },
                GuiButton(1, "${GRAY}[${YELLOW}Click to pause${GRAY}]") { pauseMagmaCoreFishingTracker() },
                GuiButton(2, "${GRAY}[${RED}Click to reset${GRAY}]") { resetMagmaCoreFishingTracker(false, getCurrentViewMode()) }
            ))
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
