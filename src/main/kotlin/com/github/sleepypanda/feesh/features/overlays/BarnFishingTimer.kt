package com.github.sleepypanda.feesh.features.overlays

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.ClientTickEvent
import com.github.sleepypanda.feesh.events.WorldChangedEvent
import com.github.sleepypanda.feesh.settings.categories.Overlays
import com.github.sleepypanda.feesh.settings.categories.Alerts
import com.github.sleepypanda.feesh.utils.WorldUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.EntityUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.ChatUtils.getFormattedString
import com.github.sleepypanda.feesh.utils.ChatUtils.removeFormatting
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.gui.MoveGuisScreen
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.KeybindUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.Sounds
import org.lwjgl.glfw.GLFW
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.component.DataComponentTypes
import java.util.Date

object BarnFishingTimer {
    private const val TIMER_THRESHOLD_IN_MINUTES = 5
    private const val TICKS_PER_CHECK = 10

    private var mobsCount = 0
    private var startTime: Long? = null
    private var countNotificationShownAt: Date? = null
    private var timerNotificationShownAt: Date? = null

    private var tickCounter = 0

    private val allSeaCreaturesNames = SeaCreatures.allSeaCreatures.map { it.name }
        .plus("Mithril Grubber") // A sea creature is called Small Mithril Grubber, but corrupted one is Corrupted Mithril Grubber (without "Small")
        .plus("Jawbus Follower")

    private val gui = FeeshGui()
        .setCoordsDataKey("barnFishingTimer")
        .setClickable(true)
        .setSampleLines(listOf(
            "${GREEN}25 ${GRAY}sea creatures ${DARK_GRAY}(${GREEN}2m 30s${DARK_GRAY})",
        ))
        .setSettingsKey { Overlays.barnFishingTimerOverlay }
        .setCondition {
            WorldUtils.isInFishingWorld() &&
            PlayerUtils.hasFishingRodInHotbar() &&
            !isInHunterArmor()
        }

    fun init() {
        EventBus.subscribe(ClientTickEvent::class, ::onClientTick)
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
        registerCommands()
        registerKeybinds()
    }

    private fun registerCommands() {
        RegisterUtils.command("feeshResetBarnFishingTimer") {
            resetSeaCreaturesCountAndTimer()
        }
    }

    private fun registerKeybinds() {
        KeybindUtils.registerKeybind("key.feesh.resetBarnFishingTimer", GLFW.GLFW_KEY_UNKNOWN) {
            resetSeaCreaturesCountAndTimer()
        }
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        resetSeaCreaturesCountAndTimer()
    }

    private fun onClientTick(@Suppress("UNUSED_PARAMETER") event: ClientTickEvent) {
        tickCounter++
        if (tickCounter < TICKS_PER_CHECK) return
        tickCounter = 0

        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) {
            gui.clearLines()
            return
        }

        if (Alerts.alertOnSeaCreaturesCountThreshold ||  Alerts.alertOnSeaCreaturesTimerThreshold || Overlays.barnFishingTimerOverlay) {
            trackSeaCreaturesCount()
        }

        if (Alerts.alertOnSeaCreaturesCountThreshold || Alerts.alertOnSeaCreaturesTimerThreshold) {
            alertOnSeaCreaturesCountThreshold()
            alertOnSeaCreaturesTimerThreshold()
        }

        updateGuiLines()
    }

    private fun resetSeaCreaturesCountAndTimer() {
        startTime = null
        mobsCount = 0
        countNotificationShownAt = null
        timerNotificationShownAt = null
        gui.clearLines()
    }

    private fun trackSeaCreaturesCount() {
        if (!WorldUtils.isInSkyblock() || !WorldUtils.isInFishingWorld()) return

        val world = FeeshMod.mc.world ?: return
        val entities = world.entities.filterIsInstance<ArmorStandEntity>()

        var newMobsCount = 0
        entities.forEach { entity ->
            val customName = entity.customName?.getFormattedString() ?: return@forEach
            val plainName = customName.removeFormatting()

            // Mobs / corrupted mobs have prefix like [Lv100], only Grinch does not have it
            // This check is needed to exclude Necromancy souls and pets
            val hasLevelPrefix = plainName.contains("[Lv") && plainName.contains("❤")
            val isGrinch = plainName.contains("Grinch  ❤")
            
            if ((hasLevelPrefix && allSeaCreaturesNames.any { sc -> plainName.contains(sc) }) || isGrinch) {
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
            isInHunterArmor() ||
            !PlayerUtils.hasFishingRodInHotbar()
        ) return

        if (countNotificationShownAt != null && Date().time - countNotificationShownAt!!.time < 5_000) return

        val seaCreaturesCountThreshold = getSeaCreaturesCountThreshold()

        if (mobsCount >= seaCreaturesCountThreshold && countNotificationShownAt == null) {
            countNotificationShownAt = Date()
            CommonUtils.showTitle("${RED}Kill sea creatures", "${WHITE}${seaCreaturesCountThreshold}+ mobs")
            SoundUtils.playCustomSound(Sounds.FEESH_NOTIFICATION_BELL)
        } else if (mobsCount < seaCreaturesCountThreshold && countNotificationShownAt != null) {
            countNotificationShownAt = null
        }
    }

    private fun alertOnSeaCreaturesTimerThreshold() {
        if (startTime == null ||
            !Alerts.alertOnSeaCreaturesTimerThreshold ||
            !WorldUtils.isInSkyblock() ||
            !WorldUtils.isInFishingWorld() ||
            isInHunterArmor() ||
            !PlayerUtils.hasFishingRodInHotbar()
        ) return

        if (timerNotificationShownAt != null && Date().time - timerNotificationShownAt!!.time < 5_000) return

        val deltaInSeconds = ((System.currentTimeMillis() - startTime!!) / 1000).toInt()
        val thresholdInSeconds = TIMER_THRESHOLD_IN_MINUTES * 60

        if (deltaInSeconds >= thresholdInSeconds && timerNotificationShownAt == null) {
            timerNotificationShownAt = Date()
            CommonUtils.showTitle("${RED}Kill sea creatures", "${WHITE}${TIMER_THRESHOLD_IN_MINUTES}+ minutes")
            SoundUtils.playCustomSound(Sounds.FEESH_NOTIFICATION_BELL)
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
            isInHunterArmor() ||
            FeeshMod.mc.currentScreen is MoveGuisScreen
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
        
        val timerColor = if (minutes >= TIMER_THRESHOLD_IN_MINUTES) RED else GREEN
        val seaCreaturesText = if (mobsCount > 1) "sea creatures" else "sea creature"
        val seaCreaturesColor = if (mobsCount >= getSeaCreaturesCountThreshold()) RED else GREEN

        val overlayText = "${seaCreaturesColor}${mobsCount} ${GRAY}${seaCreaturesText} ${DARK_GRAY}(${timerColor}${timerText}${DARK_GRAY})"
        
        val resetCommand = "/feeshResetBarnFishingTimer"
        val resetButtonText = "${RED}${BOLD}[Click to reset] ${DARK_GRAY}($resetCommand)"

        gui.setLines(listOf(overlayText, resetButtonText))
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

    private fun isInHunterArmor(): Boolean {
        val player = FeeshMod.mc.player ?: return false
        
        val helmet = player.getEquippedStack(EquipmentSlot.HEAD)
        val chestplate = player.getEquippedStack(EquipmentSlot.CHEST)
        val leggings = player.getEquippedStack(EquipmentSlot.LEGS)
        val boots = player.getEquippedStack(EquipmentSlot.FEET)
        
        val armorPieces = listOf(helmet, chestplate, leggings, boots)
        return armorPieces.all { armorPiece ->
            if (armorPiece == null || armorPiece.isEmpty) return false
            
            val itemName = armorPiece.name.string
            return itemName.contains("Hunter", ignoreCase = true)
        }
    }
}