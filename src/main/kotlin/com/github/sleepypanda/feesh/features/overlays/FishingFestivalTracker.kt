package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.SeaCreatureMessages
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.data.PersonalBestData
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.features.overlays.base.IResettableTracker
import java.util.Date
import net.minecraft.sounds.SoundEvents

object FishingFestivalTracker : IResettableTracker {
    private const val FESTIVAL_DURATION_MS = 61 * 60 * 1000L // 1 hour 1 minute - how long festival usually lasts, + some extra time to be safe
    private const val TICKS_PER_UPDATE = 20

    private val FESTIVAL_ENDED_PATTERN = Regex("^FISHING FESTIVAL The festival has concluded! Time to dry off and repair your rods!$")

    const val RESET_COMMAND = "feeshResetFishingFestivalTracker"

    override val trackerName = "Fishing Festival tracker"
    override val resetCommand = RESET_COMMAND

    private val sharkInfos = run {
        val byMessage = SeaCreatures.allSeaCreatures.associateBy { it.pattern.pattern }
        listOfNotNull(
            byMessage[SeaCreatureMessages.GREAT_WHITE_SHARK_MESSAGE],
            byMessage[SeaCreatureMessages.TIGER_SHARK_MESSAGE],
            byMessage[SeaCreatureMessages.BLUE_SHARK_MESSAGE],
            byMessage[SeaCreatureMessages.NURSE_SHARK_MESSAGE],
        )
    }

    private val SHARK_NAMES = sharkInfos.map { it.name }

    private val sharksCaught = mutableMapOf<String, Int>().apply {
        SHARK_NAMES.forEach { put(it, 0) }
    }

    private val personalBest: PersonalBestData
        get() = PersistentDataManager.feeshData.personalBest

    private var festivalStartedAt: Long? = null
    private var tickCounter = 0

    private val gui = FeeshGui()
        .setCoordsDataKey("fishingFestivalTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            "${AQUA}${BOLD}Sharks: ${WHITE}500 ${GRAY}(${LEGENDARY}50 ${EPIC}100 ${RARE}150 ${UNCOMMON}200${GRAY})",
        ))
        .setSettingsKey { Overlays.fishingFestivalTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.fishingFestivalTrackerCustomStyle }
        .setCondition {
            isTrackerVisible()
        }

    fun init() {
        registerResetCommand()
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreatureCaught)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    override fun hasData(): Boolean {
        return sharksCaught.values.any { it > 0 }
    }

    override fun resetData(force: Boolean) {
        SHARK_NAMES.forEach { sharksCaught[it] = 0 }
        festivalStartedAt = null
    }

    override fun refreshGui() {
        updateGuiLines()
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock()) return
        if (!FESTIVAL_ENDED_PATTERN.matches(event.unformattedText)) return

        alertOnFestivalResults()
        announcePersonalBest()
        resetData()
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        if (isInPast()) {
            resetData()
        }
    }

    private fun onSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        if (!shouldTrackCatch(event.seaCreatureName)) return

        if (festivalStartedAt == null || isInPast()) {
            resetData()
            festivalStartedAt = System.currentTimeMillis()
        }

        val valueToAdd = if (event.isDoubleHook) 2 else 1
        sharksCaught[event.seaCreatureName] = (sharksCaught[event.seaCreatureName] ?: 0) + valueToAdd
        updateGuiLines()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_UPDATE) return
        tickCounter = 0

        updateGuiLines()
    }

    private fun shouldTrackCatch(seaCreatureName: String): Boolean {
        return (Overlays.fishingFestivalTrackerOverlay || Alerts.alertOnFishingFestivalEnded || Alerts.trackPersonalBestFishingFestival) &&
            WorldUtils.isInSkyblock() &&
            WorldUtils.isInFishingWorld() &&
            seaCreatureName in SHARK_NAMES
    }

    private fun isTrackerVisible(): Boolean {
        return Overlays.fishingFestivalTrackerOverlay && 
            WorldUtils.isInSkyblock() &&
            WorldUtils.isInFishingWorld() && 
            hasData() &&
            FishingHookUtils.wasFishingHookSubmergedMinutesAgo(5)
    }

    private fun isInPast(): Boolean {
        val started = festivalStartedAt ?: return false
        return System.currentTimeMillis() - started > FESTIVAL_DURATION_MS
    }

    private fun getTotalSharks(): Int = sharksCaught.values.sum()

    private fun updateGuiLines() {
        gui.clearLines()

        if (!isTrackerVisible()) return

        val total = getTotalSharks()
        if (total == 0) return

        val sharkCounts = SHARK_NAMES.map { sharksCaught[it] ?: 0 }

        val countsText = sharkInfos.zip(sharkCounts).joinToString(" ") { (info, count) ->
            "${info.rarityColorCode}$count"
        }.trimIndent()

        val sharksLine = "${AQUA}${BOLD}Sharks: ${WHITE}$total ${GRAY}($countsText${GRAY})"
        gui.setLines(listOf(LineInfo(sharksLine)))
        gui.setButtons(listOf(getResetGuiButton { requestReset(false) }))
    }

    private fun alertOnFestivalResults() {
        if (!Alerts.alertOnFishingFestivalEnded ||
            !WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld()) return

        val total = getTotalSharks()
        if (total == 0) return

        CommonUtils.showTitle("${YELLOW}Fishing Festival ended")

        if (General.soundMode != SoundMode.OFF) {
            SoundUtils.playSound()
        }

        val countsText = sharkInfos.zip(SHARK_NAMES.map { sharksCaught[it] ?: 0 }).joinToString(" ") { (info, count) ->
            "${RESET}${info.rarityColorCode}${CommonUtils.formatNumberWithSpaces(count)}"
        }

        val message = "${WHITE}You caught ${BOLD}$total ${RESET}($countsText${WHITE}) sharks during the Fishing Festival."
        ChatUtils.sendLocalChat(message, true)
    }

    private fun announcePersonalBest() {
        if (!Alerts.trackPersonalBestFishingFestival ||
            !WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld()) return

        val total = getTotalSharks()
        if (total == 0) return

        val greatWhiteName = SHARK_NAMES.firstOrNull()
        val greatWhiteCount = if (greatWhiteName != null) sharksCaught[greatWhiteName] ?: 0 else 0

        var isNewTotalPb = false
        var isNewGwPb = false
        val previousTotalPb = personalBest.sharksCaught.amount
        val previousGwPb = personalBest.greatWhiteSharksCaught.amount

        if (total > previousTotalPb) {
            personalBest.sharksCaught.amount = total
            personalBest.sharksCaught.at = Date()
            isNewTotalPb = true
            ChatUtils.sendLocalChat(
                "${LIGHT_PURPLE}${BOLD}PERSONAL BEST!${RESET} You caught ${WHITE}${BOLD}${previousTotalPb} ${GRAY}-> ${GREEN}${BOLD}$total${RESET} sharks during the Fishing Festival!",
                true
            )
            CommonUtils.showTitle("${LIGHT_PURPLE}${BOLD}PERSONAL BEST!", "Sharks: ${GREEN}$total")
        }

        if (greatWhiteCount > previousGwPb) {
            personalBest.greatWhiteSharksCaught.amount = greatWhiteCount
            personalBest.greatWhiteSharksCaught.at = Date()
            isNewGwPb = true
            ChatUtils.sendLocalChat(
                "${LIGHT_PURPLE}${BOLD}PERSONAL BEST!${RESET} You caught ${WHITE}${BOLD}${previousGwPb} ${GRAY}-> ${GREEN}${BOLD}$greatWhiteCount${RESET} Great White Sharks during the Fishing Festival!",
                true
            )
            CommonUtils.showTitle("${LIGHT_PURPLE}${BOLD}PERSONAL BEST!", "Great White Sharks: ${GREEN}$greatWhiteCount")
        }

        if (isNewTotalPb || isNewGwPb) {
            SoundUtils.playSound(SoundEvents.PLAYER_LEVELUP)
            PersistentDataManager.saveFeeshDataToFileAsync()
        }
    }
}
