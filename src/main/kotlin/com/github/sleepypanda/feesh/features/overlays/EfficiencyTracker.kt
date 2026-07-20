package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.InteractActionType
import com.github.sleepypanda.feesh.events.models.OwnSeaCreatureCaughtEvent
import com.github.sleepypanda.feesh.events.models.PlayerInteractEvent
import com.github.sleepypanda.feesh.events.models.SeaCreatureCocoonedByYouEvent
import com.github.sleepypanda.feesh.events.models.SoundPlayedEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.models.EfficiencyStatTypes
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.ItemUtils
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.FishingHookUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.gui.GuiButton
import com.github.sleepypanda.feesh.utils.gui.Table
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.features.overlays.base.IResettableTracker
import com.github.sleepypanda.feesh.utils.EntityUtils
import java.util.Date

// Just timer when all stats are 0 - shown 0 stats for now
// Drone pet
// Stuck in kelp = submerged more than a second ago
// Tracker starts when fishing hook is submerged, like other overlays
// Tracker is paused when fishing hook is not submerged for 5 minutes
// If in trophy armor, we track only Casts/hour but do not track SC catches/hour, SC/hour (+ DH), and SC/hour (+ DH and BS) - they are hidden
// Tracker is hidden if all its stats disabled in settings or hidden
// How to not count sc related stats if treasure fishing
// Check dirt rod
// Widget reappearing when started fishing after 5 minutes (while its hidden)
// Build sample lines depending on enabled stats
// Test that cocoons do not appear in not visible trackers
// Fix settings descriptions and default values

object EfficiencyTracker : IResettableTracker {
    const val RESET_COMMAND = "feeshResetEfficiencyTracker"
    const val PAUSE_COMMAND = "feeshPauseEfficiencyTracker"

    override val trackerName = "Efficiency tracker"
    override val resetCommand = RESET_COMMAND

    private const val TICKS_PER_UPDATE = 20
    private const val HIDE_OVERLAY_MINUTES = 5
    private const val CATCH_XP_ORB_SOUND_PATH = "entity.experience_orb.pickup" // Some trash items caught also play sound(s) with volume 0.5 + 0.1 or just 0.1
    // ♪ MUSICAL CATCH! You caught a Music Disc - Cat!
    private val TREASURE_OR_JUNK_CATCH_PATTERN = Regex("^. (GOOD|GOOD JUNK|GREAT|GREAT JUNK|OUTSTANDING|OUTSTANDING JUNK|MUSICAL) CATCH!")
    private val TROPHY_CATCH_PATTERN = Regex("^. (TROPHY FISH|TROPHY FROG)!")

    private var catchesCount = 0
    private var seaCreatureCatchesCount = 0
    private var seaCreatureCountWithDh = 0
    private var seaCreatureCountWithDhAndBs = 0
    private var elapsedSeconds = 0

    private var isSessionActive = false
    private var lastIsFishingHookActive = false
    private var lastRodRightClickedAt: Date? = null
    private var lastSeaCreatureCaughtAt: Date? = null
    private var lastTreasureOrJunkCaughtAt: Date? = null
    private var lastTrophyCaughtAt: Date? = null
    private var lastCatchXpOrbSoundAt: Date? = null

    private var tickCounter = 0
    private val baseTitle = "${AQUA}${BOLD}${trackerName}"

    private val gui = FeeshGui()
        .setCoordsDataKey("efficiencyTracker")
        .setClickable(true)
        .setSampleLines(listOf(
            baseTitle,
            "${WHITE}500 ${GRAY}Catches/h (${WHITE}1000 ${GRAY}total)",
            "${WHITE}600 ${GRAY}SC/h (${WHITE}1200 ${GRAY}total)",
            "",
            "${AQUA}Elapsed time: ${WHITE}30m",
        ))
        .setSettingsKey { Overlays.efficiencyTrackerOverlay }
        .setApplyCustomStyleKey { Overlays.efficiencyTrackerCustomStyle }
        .setCondition {
            WorldUtils.isInFishingWorld() &&
            FishingHookUtils.wasFishingHookSubmergedMinutesAgo(HIDE_OVERLAY_MINUTES)
        }

    fun init() {
        registerResetCommand()
        RegisterUtils.command(PAUSE_COMMAND) {
            pause()
        }
        EventBus.subscribe(OwnSeaCreatureCaughtEvent::class, ::onSeaCreatureCaught)
        EventBus.subscribe(SeaCreatureCocoonedByYouEvent::class, ::onSeaCreatureCocooned)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(PlayerInteractEvent::class, ::onPlayerInteract)
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(SoundPlayedEvent::class, ::onSoundPlayed)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    override fun hasData(): Boolean {
        return catchesCount > 0 || seaCreatureCatchesCount > 0 || seaCreatureCountWithDh > 0 || seaCreatureCountWithDhAndBs > 0 || elapsedSeconds > 0
    }

    override fun resetData(force: Boolean) {
        catchesCount = 0
        seaCreatureCatchesCount = 0
        seaCreatureCountWithDh = 0
        seaCreatureCountWithDhAndBs = 0
        isSessionActive = false
        elapsedSeconds = 0

        lastIsFishingHookActive = false
        lastRodRightClickedAt = null
        lastSeaCreatureCaughtAt = null
        lastTreasureOrJunkCaughtAt = null
        lastTrophyCaughtAt = null
        lastCatchXpOrbSoundAt = null
    }

    override fun refreshGui() {
        updateGuiLines()
    }

    fun pause() {
        CommonUtils.runWithCatching("Failed to pause $trackerName") {
            if (!Overlays.efficiencyTrackerOverlay || !WorldUtils.isInSkyblock() || !isSessionActive) return

            pauseInternal()
            updateGuiLines()
            ChatUtils.sendLocalChat("${WHITE}$trackerName is paused. Continue fishing to resume it.", true)
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        pauseInternal()
        lastRodRightClickedAt = null
        lastSeaCreatureCaughtAt = null
        lastTreasureOrJunkCaughtAt = null
        lastTrophyCaughtAt = null
        lastCatchXpOrbSoundAt = null
        lastIsFishingHookActive = false
        gui.clearLines()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        CommonUtils.runWithCatching("Failed to handle tick in $trackerName") {
            val currentIsFishingHookActive = FishingHookUtils.getActiveFishingHook() != null
            if (!currentIsFishingHookActive && lastIsFishingHookActive) onRodReeledIn()
            lastIsFishingHookActive = currentIsFishingHookActive
    
            tickCounter++
            if (tickCounter < TICKS_PER_UPDATE) return
            tickCounter = 0
    
            refreshElapsedTimeOrPause() // Once per second!
            updateGuiLines()
        }
    }

    private fun onSeaCreatureCaught(event: OwnSeaCreatureCaughtEvent) {
        CommonUtils.runWithCatching("Failed to track sea creature catch in $trackerName") {
            if (event.seaCreatureName == "Vanquisher") return
            lastSeaCreatureCaughtAt = Date()

            if (!isTrackerActive()) return

            val isDoubleHooked = event.isDoubleHook
            val dhValue = if (isDoubleHooked) 2 else 1

            seaCreatureCatchesCount += 1
            seaCreatureCountWithDh += dhValue
            seaCreatureCountWithDhAndBs += dhValue         
            updateGuiLines()
        }
    }

    private fun onSeaCreatureCocooned(event: SeaCreatureCocoonedByYouEvent) {
        CommonUtils.runWithCatching("Failed to track cocooned sea creature in $trackerName") {
            if (!isTrackerVisible()) return
            if (event.seaCreatureName == "Vanquisher") return

            seaCreatureCountWithDhAndBs += 1
            updateGuiLines()
        }
    }

    private fun onPlayerInteract(event: PlayerInteractEvent) {
        CommonUtils.runWithCatching("Failed to handle fishing rod interaction in $trackerName") {
            if (!isTrackerActive()) return
            if (!isStatEnabled(EfficiencyStatTypes.CATCHES_PER_HOUR)) return
            if (!event.isMainHand || (event.actionType != InteractActionType.USE_ITEM && event.actionType != InteractActionType.USE_BLOCK)) return
            if (FishingHookUtils.getActiveFishingHook() == null) return

            val heldItem = FeeshMod.mc.player?.mainHandItem
            if (heldItem == null || heldItem.isEmpty) return
            if (!ItemUtils.isFishingRod(heldItem)) return

            lastRodRightClickedAt = Date()
        }
    }

    private fun onChat(event: ChatEvent) {
        CommonUtils.runWithCatching("Failed to track catch chat message in $trackerName") {
            if (!isTrackerActive()) return
            if (!isStatEnabled(EfficiencyStatTypes.CATCHES_PER_HOUR)) return
            if (TREASURE_OR_JUNK_CATCH_PATTERN.containsMatchIn(event.unformattedText)) {
                lastTreasureOrJunkCaughtAt = Date()
            }
            if (TROPHY_CATCH_PATTERN.containsMatchIn(event.unformattedText)) {
                lastTrophyCaughtAt = Date()
            }
        }
    }

    // Trash catches like raw fish are usually caught with XP orb sound (volume 0.5 + 0.1 or just 0.1)
    // However it's not too precise because other players' catches play this sound too
    private fun onSoundPlayed(event: SoundPlayedEvent) {
        CommonUtils.runWithCatching("Failed to track catch XP orb sound in $trackerName") {
            if (!isTrackerActive()) return
            if (!isStatEnabled(EfficiencyStatTypes.CATCHES_PER_HOUR)) return
            if (event.soundId.path != CATCH_XP_ORB_SOUND_PATH) return
            if (event.volume != 0.1f && event.volume != 0.5f) return

            val player = FeeshMod.mc.player ?: return
            val distanceSqr = EntityUtils.getDistanceSqr(player.x, player.y, player.z, event.x, event.y, event.z)
            if (distanceSqr > 1.0) return // Usually sounds played with distance (non-sqr) 0.1 and 0.7

            lastCatchXpOrbSoundAt = Date()
        }
    }


    private fun onRodReeledIn() {
        CommonUtils.runWithCatching("Failed to track rod reeled in in $trackerName") {
            if (!isTrackerActive()) return
            if (!isStatEnabled(EfficiencyStatTypes.CATCHES_PER_HOUR)) return
            if (!isSuccessfulCatch()) return
            catchesCount += 1
            updateGuiLines()
            ChatUtils.sendLocalChat("Added catch " + catchesCount, true)
        }
    }

    private fun isSuccessfulCatch(): Boolean {
        if (!isTrackerActive()) return false
        if (!isStatEnabled(EfficiencyStatTypes.CATCHES_PER_HOUR)) return false

        val now = Date().time
        fun ageMs(timestamp: Date?): Long? = timestamp?.let { now - it.time }
        fun wasWithinMs(timestamp: Date?, windowMs: Long): Boolean = ageMs(timestamp)?.let { it < windowMs } == true
        fun formatMs(label: String, ageMs: Long?): String {
            if (ageMs == null) return "${GRAY}$label:${DARK_GRAY} n/a"
            val color = if (ageMs > 250L) RED else WHITE
            return "${GRAY}$label:${color}${ageMs}ms"
        }

        val rodAgeMs = ageMs(lastRodRightClickedAt)
        val submergedAgeMs = FishingHookUtils.lastSubmergedFishingHookSeenAt()?.let { now - it.time }
        val seaCreatureAgeMs = ageMs(lastSeaCreatureCaughtAt)
        val treasureAgeMs = ageMs(lastTreasureOrJunkCaughtAt)
        val trophyAgeMs = ageMs(lastTrophyCaughtAt)
        val xpAgeMs = ageMs(lastCatchXpOrbSoundAt)

        ChatUtils.sendLocalChat(
            "${GRAY}Catch debug ${DARK_GRAY}| " +
                formatMs("rod", rodAgeMs) + " ${DARK_GRAY}| " +
                formatMs("sub", submergedAgeMs) + " ${DARK_GRAY}| " +
                formatMs("sc", seaCreatureAgeMs) + " ${DARK_GRAY}| " +
                formatMs("tr", treasureAgeMs) + " ${DARK_GRAY}| " +
                formatMs("tf", trophyAgeMs) + " ${DARK_GRAY}| " +
                formatMs("xp", xpAgeMs),
            true
        )

        if (!wasWithinMs(lastRodRightClickedAt, 500L)) return false
        if (!FishingHookUtils.wasFishingHookSubmergedMillisecondsAgo(500)) return false
        
        return wasWithinMs(lastSeaCreatureCaughtAt, 300L) ||
            wasWithinMs(lastTreasureOrJunkCaughtAt, 300L) ||
            wasWithinMs(lastTrophyCaughtAt, 300L) ||
            wasWithinMs(lastCatchXpOrbSoundAt, 300L)
    }

    private fun isTrackerEnabledInWorld(): Boolean {
        if (!Overlays.efficiencyTrackerOverlay || !WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return false
        return true
    }

    private fun isTrackerVisible(): Boolean {
        if (!isTrackerEnabledInWorld()) return false
        if (!FishingHookUtils.wasFishingHookSubmergedMinutesAgo(HIDE_OVERLAY_MINUTES)) return false
        return true
    }

    private fun isTrackerActive(): Boolean {
        return isTrackerVisible() && isSessionActive
    }

    private fun isStatEnabled(stat: EfficiencyStatTypes): Boolean {
        if (!Overlays.efficiencyTrackerOverlay || !Overlays.efficiencyTrackerStats.contains(stat)) return false
        if (stat != EfficiencyStatTypes.CATCHES_PER_HOUR && PlayerUtils.isInTrophyArmor()) return false
        return true
    }

    private fun refreshElapsedTimeOrPause() {
        CommonUtils.runWithCatching("Failed to refresh elapsed time in $trackerName") {
            if (!isTrackerVisible()) {
                pauseInternal()
                return
            }

            val isHookActive = FishingHookUtils.isFishingHookSubmerged()

            // Start fishing timer after pause or when tracker was empty
            if (isHookActive) {
                isSessionActive = true
            }
    
            if (!isTrackerActive()) return

            val lastHookSeenAt = FishingHookUtils.lastSubmergedFishingHookSeenAt() ?: return
            val elapsedSinceHook = (Date().time - lastHookSeenAt.time) / 1000

            if (elapsedSinceHook < Overlays.trackersAutoPauseSeconds) {
                elapsedSeconds += 1
            } else {
                pause()
            }
        }
    }

    private fun pauseInternal() {
        isSessionActive = false
    }

    private fun getTotalForStat(stat: EfficiencyStatTypes): Int {
        return when (stat) {
            EfficiencyStatTypes.CATCHES_PER_HOUR -> catchesCount
            EfficiencyStatTypes.SC_CATCHES_PER_HOUR -> seaCreatureCatchesCount
            EfficiencyStatTypes.SC_PER_HOUR -> seaCreatureCountWithDh
            EfficiencyStatTypes.SC_PER_HOUR_WITH_BS -> seaCreatureCountWithDhAndBs
        }
    }

    private fun calculatePerHour(total: Int): Int {
        val elapsedHours = elapsedSeconds / 3600.0
        return if (elapsedHours > 0) (total / elapsedHours).toInt() else 0
    }

    private data class StatLineColumns(val perHour: String, val total: String) {
        fun toCells(): List<String> = listOf(perHour, total)
    }

    private fun getColumnsSeparator(): String = " ${DARK_GRAY}| "

    private fun getStatLineColumns(stat: EfficiencyStatTypes): StatLineColumns {
        val total = getTotalForStat(stat)
        val perHour = calculatePerHour(total)
        val label = when (stat) {
            EfficiencyStatTypes.CATCHES_PER_HOUR -> "Catches/h"
            EfficiencyStatTypes.SC_CATCHES_PER_HOUR -> "SC catches/h"
            EfficiencyStatTypes.SC_PER_HOUR -> "SC/h"
            EfficiencyStatTypes.SC_PER_HOUR_WITH_BS -> "SC/h with BS"
        }
        return StatLineColumns(
            perHour = "${GRAY}$label: ${WHITE}${CommonUtils.formatNumberWithSpaces(perHour)}",
            total = "${WHITE}${CommonUtils.formatNumberWithSpaces(total)} ${GRAY}total",
        )
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!isTrackerVisible() || !hasData()) return

        val pausedText = if (isSessionActive) "" else " ${GRAY}[Paused]"
        val lines = mutableListOf<LineInfo>()
        lines.add(LineInfo(baseTitle))

        val statRows = EfficiencyStatTypes.entries
            .filter { isStatEnabled(it) }
            .map { getStatLineColumns(it).toCells() }

        if (statRows.isNotEmpty()) {
            val tableLayout = Table.layout(FeeshMod.mc.font, statRows, getColumnsSeparator())
            statRows.indices.forEach { index ->
                lines.add(
                    LineInfo.withCells(
                        cells = tableLayout.rows[index],
                        tableWidth = tableLayout.tableWidth,
                    )
                )
            }
        }

        lines.add(LineInfo(""))
        lines.add(LineInfo("${AQUA}Elapsed time: ${WHITE}${CommonUtils.formatTimeElapsed(elapsedSeconds)}${pausedText}"))

        gui.setLines(lines)
        gui.setButtons(listOf(
            GuiButton(0, "${GRAY}[${YELLOW}Click to pause${GRAY}]", { pause() }),
            getResetGuiButton(1) { requestReset() }
        ))
    }
}
