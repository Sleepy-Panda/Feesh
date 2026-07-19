package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.constants.FishingProfitDrops
import com.github.sleepypanda.feesh.features.overlays.base.IResettableViewModeTracker
import com.github.sleepypanda.feesh.features.overlays.base.TrackerViewMode
import com.github.sleepypanda.feesh.constants.RareDropTypes
import com.github.sleepypanda.feesh.constants.SeaCreatures
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
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.enums.PricingModeWithNpc
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.gui.GuiButton
import java.util.Date

object MagmaCoreFishingTracker : IResettableViewModeTracker {

    data class MagmaCoreFishingSourceData(
        var magmaCoresCount: Int = 0,
        var lastAddedMagmaCoresCount: Int = 0,
        var seaCreaturesCaughtCount: Int = 0,
        var elapsedSeconds: Int = 0
    )

    data class MagmaCoreFishingData(
        var session: MagmaCoreFishingSourceData = MagmaCoreFishingSourceData(),
        var total: MagmaCoreFishingSourceData = MagmaCoreFishingSourceData(),
        var viewMode: String = TrackerViewMode.SESSION.name
    )

    override val trackerName = "Magma Core fishing tracker"

    const val RESET_COMMAND = "feeshResetMagmaCoreFishingTracker"
    override val resetSessionCommand = RESET_COMMAND
    const val RESET_TOTAL_COMMAND = "feeshResetMagmaCoreFishingTrackerTotal"
    override val resetTotalCommand = RESET_TOTAL_COMMAND
    const val PAUSE_COMMAND = "feeshPauseMagmaCoreFishingTracker"
    private const val MAGMA_CORE_ID = "MAGMA_CORE"
    private const val TOGGLE_VIEW_MODE_COMMAND = "feeshToggleMagmaCoreFishingTrackerViewMode"

    private const val TICKS_PER_UPDATE = 20
    private const val HIDE_OVERLAY_MINUTES = 5
    private const val DEDUPE_CORES_MILLISECONDS = 10_000L // To aggregate multiple drops in a short period of time (e.g. clearing cap)

    private val data: MagmaCoreFishingData
        get() = PersistentDataManager.feeshData.magmaCoreFishing

    private var isSessionActive = false
    private var tickCounter = 0
    private var lastSeaCreatureCaughtAt: Date? = null
    private var lastMagmaCoreDroppedAt: Date? = null

    private val baseTitle = "${AQUA}${BOLD}$trackerName"

    private val gui = FeeshGui()
        .setCoordsDataKey("magmaCoreFishingTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            "$baseTitle ${GRAY}[${GREEN}Session${GRAY}]",
            "${BLUE}Sea creatures: ${WHITE}22 ${GRAY}(${WHITE}990${GRAY}/h)",
            "${BLUE}Magma Cores: ${WHITE}3 ${GRAY}(${WHITE}135${GRAY}/h) ${GRAY}[${WHITE}+1 ${GRAY}last added]",
            "",
            "${AQUA}Total: ${GOLD}${BOLD}2.2M ${GRAY}(${GOLD}101.2M${GRAY}/h) ${DARK_GRAY}[sell offer]",
            "${AQUA}Elapsed time: ${WHITE}1m 10s"
        ))
        .setSettingsKey { Overlays.magmaCoreFishingTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.magmaCoreFishingTrackerCustomStyle }
        .setCondition { isTrackerVisible() }

    fun init() {
        registerViewModeResetCommands()
        registerCommands()
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onOwnSeaCreatureCaught)
        EventBus.subscribe(RareDropEvent::class, ::onRareDrop)
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }

    override fun onBeforeReset() {
        pause()
    }

    override fun getCurrentViewMode(): TrackerViewMode {
        return try {
            TrackerViewMode.valueOf(data.viewMode)
        } catch (_: Exception) {
            TrackerViewMode.SESSION
        }
    }

    override fun hasSessionData(): Boolean {
        val session = data.session
        return session.magmaCoresCount > 0 || session.seaCreaturesCaughtCount > 0 || session.elapsedSeconds > 0
    }

    override fun hasTotalData(): Boolean {
        val total = data.total
        return total.magmaCoresCount > 0 || total.seaCreaturesCaughtCount > 0 || total.elapsedSeconds > 0
    }

    override fun resetSessionData(force: Boolean) {
        data.session = MagmaCoreFishingSourceData()
        lastSeaCreatureCaughtAt = null
        lastMagmaCoreDroppedAt = null
        saveData(force)
    }

    override fun resetTotalData(force: Boolean) {
        data.total = MagmaCoreFishingSourceData()
        lastSeaCreatureCaughtAt = null
        lastMagmaCoreDroppedAt = null
        saveData(force)
    }

    override fun refreshGui() {
        updateGuiLines()
    }

    fun pauseMagmaCoreFishingTracker() {
        CommonUtils.runWithCatching("Failed to pause $trackerName") {
            if (!isSessionActive || !isTrackerVisible()) return

            pause()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}$trackerName is paused. Continue fishing to resume it.", true)
        }
    }

    private fun registerCommands() {
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

        if (!Overlays.magmaCoreFishingTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRYSTAL_HOLLOWS || PlayerUtils.isInTrophyArmor()) {
            pause()
            return
        }

        refreshElapsedTime()
        updateGuiLines()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        pause()
        lastSeaCreatureCaughtAt = null
        lastMagmaCoreDroppedAt = null
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        if (Overlays.resetMagmaCoreFishingTrackerSessionOnGameClosed) {
            resetOnGameClosed()
        }
    }

    private fun onOwnSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        CommonUtils.runWithCatching("Failed to track Magma Fields sea creature catch") {
            if (!Overlays.magmaCoreFishingTrackerOverlay || !WorldUtils.isInSkyblock() || WorldUtils.getWorldName() != WorldUtils.CRYSTAL_HOLLOWS) return

            val seaCreatureInfo = event.seaCreatureInfo
            if (!seaCreatureInfo.types.contains(SeaCreatures.TYPE_MAGMA_FIELDS)) return

            isSessionActive = true
            lastSeaCreatureCaughtAt = Date()

            val diff = if (event.isDoubleHook) 2 else 1
            data.session.seaCreaturesCaughtCount += diff
            data.total.seaCreaturesCaughtCount += diff
            activateTimerInMode(TrackerViewMode.SESSION)
            activateTimerInMode(TrackerViewMode.TOTAL)

            saveData()
            updateGuiLines()
        }
    }

    private fun onRareDrop(event: RareDropEvent) {
        CommonUtils.runWithCatching("Failed to track Magma Core drop") {
            if (!isTrackerVisible()) return // Allow counting with paused tracker while it's visible
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
        if (!isSessionActive || !isTrackerVisible()) {
            pause()
            return
        }

        val lastCatch = lastSeaCreatureCaughtAt ?: run {
            pause()
            return
        }

        val elapsedSinceCatch = (Date().time - lastCatch.time) / 1000
        if (elapsedSinceCatch < Overlays.trackersAutoPauseSeconds) {
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
        if (!FishingHookUtils.wasFishingHookSubmergedMinutesAgo(HIDE_OVERLAY_MINUTES)) return false
        if (PlayerUtils.isInTrophyArmor()) return false
        if (lastSeaCreatureCaughtAt == null) return false

        val elapsedSinceCatch = (Date().time - lastSeaCreatureCaughtAt!!.time) / 1000
        if (elapsedSinceCatch > HIDE_OVERLAY_MINUTES * 60) return false

        val sourceObj = getSourceObject(getCurrentViewMode())
        return sourceObj.magmaCoresCount > 0 || sourceObj.seaCreaturesCaughtCount > 0
    }

    private fun pause() {
        isSessionActive = false
    }

    private fun activateTimerInMode(viewMode: TrackerViewMode) {
        val sourceObj = getSourceObject(viewMode)
        if (sourceObj.elapsedSeconds == 0) {
            sourceObj.elapsedSeconds = 1
        }
    }

    private fun toggleViewMode() {
        val newMode = if (getCurrentViewMode() == TrackerViewMode.SESSION) TrackerViewMode.TOTAL else TrackerViewMode.SESSION
        data.viewMode = newMode.name
        saveData()
        updateGuiLines()
    }

    private fun getSourceObject(viewMode: TrackerViewMode): MagmaCoreFishingSourceData {
        return when (viewMode) {
            TrackerViewMode.SESSION -> data.session
            TrackerViewMode.TOTAL -> data.total
        }
    }

    private fun updateGuiLines() {
        CommonUtils.runWithCatching("Failed to update $trackerName GUI lines") {
            gui.clearLines()
            
            if (!isTrackerVisible()) {
                pause()
                return
            }

            val viewMode = getCurrentViewMode()
            val sourceObj = getSourceObject(viewMode)
            val viewModeText = getViewModeDisplayText(viewMode)
            val nextMode = if (viewMode == TrackerViewMode.SESSION) TrackerViewMode.TOTAL else TrackerViewMode.SESSION
            val nextModeText = getViewModeDisplayText(nextMode)

            val elapsedHours = sourceObj.elapsedSeconds / 3600.0
            val magmaCorePrice = PriceUtils.getBazaarItemPrices(MAGMA_CORE_ID)
            val priceMode = Overlays.magmaCoreFishingTrackerPriceMode
            val npcSell = (FishingProfitDrops.items.find { it.itemId == MAGMA_CORE_ID }?.npcPrice ?: 0.0).toLong()
            val unitPrice = when (priceMode) {
                PricingModeWithNpc.SELL_OFFER -> magmaCorePrice?.sellOffer?.toLong() ?: 0L
                PricingModeWithNpc.INSTA_SELL -> magmaCorePrice?.instaSell?.toLong() ?: 0L
                PricingModeWithNpc.NPC_SELL -> npcSell
            }
            val priceModeStr = when (Overlays.magmaCoreFishingTrackerPriceMode) {
                PricingModeWithNpc.SELL_OFFER -> "${DARK_GRAY}[sell offer]"
                PricingModeWithNpc.INSTA_SELL -> "${DARK_GRAY}[insta-sell]"
                PricingModeWithNpc.NPC_SELL -> "${DARK_GRAY}[NPC sell]"
            }

            val totalCoins = sourceObj.magmaCoresCount * unitPrice
            val magmaCoresPerHour = if (elapsedHours > 0) (sourceObj.magmaCoresCount / elapsedHours).toInt() else 0
            val seaCreaturesPerHour = if (elapsedHours > 0) (sourceObj.seaCreaturesCaughtCount / elapsedHours).toInt() else 0
            val coinsPerHour = magmaCoresPerHour * unitPrice
            val pausedText = if (isSessionActive) "" else " ${GRAY}[Paused]"
            val lastAddedText = if (sourceObj.lastAddedMagmaCoresCount > 0) " ${GRAY}[${WHITE}+${CommonUtils.formatNumberWithSpaces(sourceObj.lastAddedMagmaCoresCount)} ${GRAY}last added]" else ""

            val lines = mutableListOf<String>()
            lines.add("$baseTitle $viewModeText")
            lines.add("${BLUE}Sea creatures: ${WHITE}${CommonUtils.formatNumberWithSpaces(sourceObj.seaCreaturesCaughtCount)} ${GRAY}(${WHITE}${seaCreaturesPerHour}${GRAY}/h)")
            lines.add("${BLUE}Magma Cores: ${WHITE}${CommonUtils.formatNumberWithSpaces(sourceObj.magmaCoresCount)} ${GRAY}(${WHITE}${magmaCoresPerHour}${GRAY}/h)${lastAddedText}")
            lines.add("")
            lines.add("${AQUA}Total: ${GOLD}${BOLD}${CommonUtils.toShortNumber(totalCoins.toDouble()) ?: "0"} ${GRAY}(${GOLD}${CommonUtils.toShortNumber(coinsPerHour.toDouble()) ?: "0"}${GRAY}/h) ${priceModeStr}")
            lines.add("${AQUA}Elapsed time: ${WHITE}${CommonUtils.formatTimeElapsed(sourceObj.elapsedSeconds)}$pausedText")

            gui.setLines(lines.map { LineInfo(it) })
            gui.setButtons(listOf(
                GuiButton(0, "${GRAY}[Click to show $nextModeText${GRAY}]") { toggleViewMode() },
                GuiButton(1, "${GRAY}[${YELLOW}Click to pause${GRAY}]") { pauseMagmaCoreFishingTracker() },
                getResetGuiButton(2) { requestReset() }
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
