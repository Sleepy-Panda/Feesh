package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.ClientTickEvent
import com.github.sleepypanda.feesh.events.models.ChatEvent
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.LineInfo
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.Sounds
import com.github.sleepypanda.feesh.settings.categories.General
import com.github.sleepypanda.feesh.settings.categories.SoundMode
import com.github.sleepypanda.feesh.features.overlays.base.IResettableTracker
import com.github.sleepypanda.feesh.features.overlays.base.TrackerResetUtils
import net.minecraft.world.entity.decoration.ArmorStand
import java.util.Date

object BarnFishingTimer : IResettableTracker {
    private const val TIMER_THRESHOLD_IN_MINUTES = 5
    private const val TICKS_PER_CHECK = 10
    const val RESET_COMMAND = "feeshResetBarnFishingTimer"
    private val PERSONAL_CAP_PATTERN = Regex("^There is not enough space for another Sea Creature! Kill some to make space for new ones!$")

    override val trackerName = "Barn fishing timer"
    override val resetCommand = RESET_COMMAND

    private var mobsCount = 0
    private var startTime: Long? = null
    private var countNotificationShownAt: Date? = null
    private var timerNotificationShownAt: Date? = null

    private var tickCounter = 0

    private val allSeaCreaturesNames = SeaCreatures.allSeaCreatures
        .map { it.name }
        .filter { it != "Vanquisher" }
        .plus("Mithril Grubber") // A sea creature is called Small Mithril Grubber, but corrupted one is Corrupted Mithril Grubber (without "Small")
        .plus("Jawbus Follower")

    private val gui = FeeshGui()
        .setCoordsDataKey("barnFishingTimer")
        .setClickable(true)
        .setSampleLines(listOf(
            "${WHITE}25 ${GRAY}sea creatures ${DARK_GRAY}(${WHITE}2m 30s${DARK_GRAY})",
        ))
        .setSettingsKey { Overlays.barnFishingTimerOverlay }
        .setApplyCustomStyleKey { Overlays.barnFishingTimerCustomStyle }
        .setCondition {
            WorldUtils.isInFishingWorld() &&
            PlayerUtils.hasFishingRodInHotbar() &&
            !PlayerUtils.isInTrophyArmor()
        }

    fun init() {
        TrackerResetUtils.registerResetCommand(this)
        EventBus.subscribe(ChatEvent::class, ::onChat)
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    override fun hasData(): Boolean {
        return mobsCount > 0 || startTime != null
    }

    override fun resetData(force: Boolean) {
        startTime = null
        mobsCount = 0
        countNotificationShownAt = null
        timerNotificationShownAt = null
    }

    override fun refreshGui() {
        updateGuiLines()
    }

    fun triggerResetKeybind() {
        requestReset(false)
    }

    private fun onChat(event: ChatEvent) {
        if (!WorldUtils.isInSkyblock()) return

        CommonUtils.runWithCatching("Failed to handle personal cap chat message") {
            if (PERSONAL_CAP_PATTERN.matches(event.unformattedText)) {
                CommonUtils.showTitle("${RED}Kill sea creatures", "${WHITE}Personal cap reached")
                if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(Sounds.FEESH_NOTIFICATION_BELL)
                else SoundUtils.playSound()
            }
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        resetData()
        gui.clearLines()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        if (Alerts.alertOnSeaCreaturesCountThreshold ||  Alerts.alertOnSeaCreaturesTimerThreshold || Overlays.barnFishingTimerOverlay) {
            trackSeaCreaturesCount()
        }

        if (Alerts.alertOnSeaCreaturesCountThreshold || Alerts.alertOnSeaCreaturesTimerThreshold) {
            alertOnSeaCreaturesCountThreshold()
            alertOnSeaCreaturesTimerThreshold()
        }

        updateGuiLines()
    }

    private fun trackSeaCreaturesCount() {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

        val world = FeeshMod.mc.level ?: return
        val entities = world.entitiesForRendering().filterIsInstance<ArmorStand>()

        var newMobsCount = 0
        entities.forEach { entity ->
            val customName = entity.customName?.getFormattedString() ?: return@forEach
            val plainName = customName.removeFormatting()

            // Mobs / corrupted mobs have prefix like [Lv100]
            // This check is needed to exclude Necromancy souls and pets
            val hasLevelPrefix = plainName.contains("[Lv") && plainName.contains("❤")

            if ((hasLevelPrefix && allSeaCreaturesNames.any { sc -> plainName.contains(sc) })) {
                if (plainName.contains("Rider of the Deep")) {
                    newMobsCount += 2
                } else {
                    newMobsCount++
                }
            }
        }

        if (mobsCount == 0 && newMobsCount > 0) {
            startTime = System.currentTimeMillis()
        }

        if (newMobsCount == 0) {
            startTime = null
        }

        mobsCount = newMobsCount
    }

    private fun alertOnSeaCreaturesCountThreshold() {
        if (!Alerts.alertOnSeaCreaturesCountThreshold ||
            !WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld() ||
            PlayerUtils.isInTrophyArmor() ||
            !PlayerUtils.hasFishingRodInHotbar()
        ) return

        if (countNotificationShownAt != null && Date().time - countNotificationShownAt!!.time < 5_000) return

        val seaCreaturesCountThreshold = getSeaCreaturesCountThreshold()

        if (mobsCount >= seaCreaturesCountThreshold && countNotificationShownAt == null) {
            countNotificationShownAt = Date()
            CommonUtils.showTitle("${RED}Kill sea creatures", "${WHITE}${seaCreaturesCountThreshold}+ mobs")
            if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(Sounds.FEESH_NOTIFICATION_BELL)
            else SoundUtils.playSound()
        } else if (mobsCount < seaCreaturesCountThreshold && countNotificationShownAt != null) {
            countNotificationShownAt = null
        }
    }

    private fun alertOnSeaCreaturesTimerThreshold() {
        if (startTime == null ||
            !Alerts.alertOnSeaCreaturesTimerThreshold ||
            !WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld() ||
            PlayerUtils.isInTrophyArmor() ||
            !PlayerUtils.hasFishingRodInHotbar()
        ) return

        if (timerNotificationShownAt != null && Date().time - timerNotificationShownAt!!.time < 5_000) return

        val deltaInSeconds = ((System.currentTimeMillis() - startTime!!) / 1000).toInt()
        val thresholdInSeconds = TIMER_THRESHOLD_IN_MINUTES * 60

        if (deltaInSeconds >= thresholdInSeconds && timerNotificationShownAt == null) {
            timerNotificationShownAt = Date()
            CommonUtils.showTitle("${RED}Kill sea creatures", "${WHITE}${TIMER_THRESHOLD_IN_MINUTES}+ minutes")
            if (General.soundMode == SoundMode.MEME) SoundUtils.playCustomSound(Sounds.FEESH_NOTIFICATION_BELL)
            else SoundUtils.playSound()
        } else if (deltaInSeconds < thresholdInSeconds && timerNotificationShownAt != null) {
            timerNotificationShownAt = null
        }
    }

    private fun updateGuiLines() {
        gui.clearLines()

        if (!Overlays.barnFishingTimerOverlay ||
            mobsCount == 0 ||
            startTime == null ||
            !WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld() ||
            !PlayerUtils.hasFishingRodInHotbar() ||
            PlayerUtils.isInTrophyArmor()
        ) return

        val deltaInMillis = System.currentTimeMillis() - startTime!!
        val deltaInSeconds = (deltaInMillis / 1000).toInt()

        if (deltaInSeconds == 0) return

        val minutes = deltaInSeconds / 60
        val seconds = deltaInSeconds % 60

        val timerText = buildString {
            if (minutes > 0) append("${minutes}m ")
            if (seconds > 0 || minutes > 0) append("${seconds}s")
        }

        val timerColor = if (minutes >= TIMER_THRESHOLD_IN_MINUTES) RED else WHITE
        val seaCreaturesText = if (mobsCount > 1) "sea creatures" else "sea creature"
        val seaCreaturesColor = if (mobsCount >= getSeaCreaturesCountThreshold()) RED else WHITE

        val overlayText = "${seaCreaturesColor}${mobsCount} ${GRAY}${seaCreaturesText} ${DARK_GRAY}(${timerColor}${timerText}${DARK_GRAY})"

        gui.setLines(listOf(LineInfo(overlayText)))
        gui.setButtons(listOf(TrackerResetUtils.getResetGuiButton { requestReset(isConfirmed = true, needsChatFeedback = false) }))
    }

    private fun getSeaCreaturesCountThreshold(): Int {
        val worldName = WorldUtils.getWorldName() ?: return Alerts.seaCreaturesCountThreshold_Default

        return when (worldName) {
            WorldUtils.HUB -> Alerts.seaCreaturesCountThreshold_Hub
            WorldUtils.CRIMSON_ISLE -> Alerts.seaCreaturesCountThreshold_CrimsonIsle
            WorldUtils.CRYSTAL_HOLLOWS -> Alerts.seaCreaturesCountThreshold_CrystalHollows
            WorldUtils.GALATEA -> Alerts.seaCreaturesCountThreshold_Galatea
            else -> Alerts.seaCreaturesCountThreshold_Default
        }
    }
}
