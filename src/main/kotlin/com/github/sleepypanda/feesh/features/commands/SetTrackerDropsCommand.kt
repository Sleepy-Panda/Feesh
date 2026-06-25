package com.github.sleepypanda.feesh.features.commands

import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.features.overlays.BayouTracker
import com.github.sleepypanda.feesh.features.overlays.WaterHotspotsTracker
import com.github.sleepypanda.feesh.features.overlays.CrimsonIsleTracker
import com.github.sleepypanda.feesh.features.overlays.LotusAtollTracker
import com.github.sleepypanda.feesh.features.overlays.TreasureFishingTracker
import com.github.sleepypanda.feesh.features.overlays.TreasureFishingTracker.TreasureCatchesData
import java.util.Date
import java.text.SimpleDateFormat

// feeshSetTrackerDrops <DROP_ID> <COUNT> <SC_COUNT_SINCE_LAST> [LAST_ON_DATE]
// feeshSetTrackerDrops DYE_TREASURE <COUNT> <GOOD>/<GREAT>/<OUTSTANDING> [LAST_ON_DATE]
object SetTrackerDropsCommand {
    const val COMMAND_NAME = "feeshSetTrackerDrops"
    const val GUIDE_URL = "https://github.com/Sleepy-Panda/Feesh/blob/develop/docs/Editing%20tracker%20drops.md"

    fun init() {
        RegisterUtils.command(COMMAND_NAME) { args ->
            if (args.isEmpty()) {
                sendScBasedCommandGuide()
                return@command
            }

            val dropId = args[0]
            if (dropId.isNullOrEmpty()) {
                ChatUtils.sendLocalChat("${RED}Please specify correct drop ID.", true)
                return@command
            }

            when (dropId) {
                "DYE_TREASURE" -> handleTreasureDye(args)
                "TITANOBOA_SHED", "SNAKE_EYES", "TIKI_MASK", "RADIOACTIVE_VIAL", "PRINCE_CROWN_JEWEL" -> handleScBasedDrop(dropId, args)
                else -> ChatUtils.sendLocalChat("${RED}Unknown drop ID: $dropId.", true)
            }
        }
    }

    private fun handleScBasedDrop(dropId: String, args: Array<String>) {
        if (args.size < 3) {
            sendScBasedCommandGuide()
            return
        }

        val count = args[1].toIntOrNull()
        if (count == null || count <= 0) {
            ChatUtils.sendLocalChat("${RED}Please specify correct DROP_COUNT. Must be a positive number.", true)
            return
        }

        val catchesSinceLast = args[2].toIntOrNull()
        if (catchesSinceLast == null || catchesSinceLast < 0) {
            ChatUtils.sendLocalChat("${RED}Please specify correct SC_COUNT_SINCE_LAST. Must be a non-negative number (sea creatures since last drop).", true)
            return
        }

        val lastOnStr = if (args.size >= 4) args.drop(3).joinToString(" ") else null
        val lastOn = if (lastOnStr.isNullOrEmpty()) null else parseLastOnDate(lastOnStr) ?: return

        when (dropId) {
            "TITANOBOA_SHED" -> BayouTracker.setTitanoboaSheds(count, catchesSinceLast, lastOn)
            "SNAKE_EYES" -> BayouTracker.setSnakeEyes(count, catchesSinceLast, lastOn)
            "TIKI_MASK" -> WaterHotspotsTracker.setTikiMasks(count, catchesSinceLast, lastOn)
            "RADIOACTIVE_VIAL" -> CrimsonIsleTracker.setRadioactiveVials(count, catchesSinceLast, lastOn)
            "PRINCE_CROWN_JEWEL" -> LotusAtollTracker.setPrincesCrownJewels(count, catchesSinceLast, lastOn)
        }
    }

    private fun handleTreasureDye(args: Array<String>) {
        if (args.size < 3) {
            sendTreasureCommandGuide()
            return
        }

        val count = args[1].toIntOrNull()
        if (count == null || count <= 0) {
            ChatUtils.sendLocalChat("${RED}Please specify correct DROP_COUNT. Must be a positive number.", true)
            return
        }

        val catchesBreakdown = parseTreasureBreakdown(args[2])
        if (catchesBreakdown == null) {
            ChatUtils.sendLocalChat("${RED}Please specify correct treasures breakdown in format good/great/outstanding, e.g. 1234/123/12.", true)
            return
        }

        val lastOnStr = if (args.size >= 4) args.drop(3).joinToString(" ") else null
        val lastOn = if (lastOnStr.isNullOrEmpty()) null else parseLastOnDate(lastOnStr) ?: return

        TreasureFishingTracker.setTreasureDyes(count, catchesBreakdown, lastOn)
    }

    private fun parseTreasureBreakdown(value: String): TreasureCatchesData? {
        val parts = value.split("/")
        if (parts.size != 3) return null

        val good = parts[0].toIntOrNull() ?: return null
        val great = parts[1].toIntOrNull() ?: return null
        val outstanding = parts[2].toIntOrNull() ?: return null
        if (good < 0 || great < 0 || outstanding < 0) return null

        return TreasureCatchesData(good = good, great = great, outstanding = outstanding)
    }

    private fun parseLastOnDate(lastOnStr: String?): Date? {
        if (lastOnStr.isNullOrEmpty()) return null

        val lastOn = parseDate(lastOnStr)
        if (lastOn == null) {
            ChatUtils.sendLocalChat("${RED}Please specify correct LAST_ON date in format YYYY-MM-DD hh:mm:ss, e.g. 2024-03-18 14:05:00. Can not be a future date!", true)
        }
        return lastOn
    }

    private fun parseDate(date: String): Date? {
        return try {
            if (date.isNullOrEmpty()) return null

            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val parsedDate = formatter.parse(date)
            val now = Date()
            if (parsedDate > now) null else parsedDate
        } catch (e: Exception) {
            null
        }
    }

    private fun sendScBasedCommandGuide() {
        ChatUtils.sendLocalChat("${RED}Usage: /${COMMAND_NAME} <DROP_ID> <DROP_COUNT> <SC_COUNT_SINCE_LAST> [LAST_ON_DATE]", true)
        ChatUtils.sendLocalChat("${GRAY}Example: /${COMMAND_NAME} TITANOBOA_SHED 5 30 2025-05-30 23:59:00", true)
        ChatUtils.sendLocalChat("${GRAY}Guide: ${GUIDE_URL}", true)
    }

    private fun sendTreasureCommandGuide() {
        ChatUtils.sendLocalChat("${RED}Usage: /${COMMAND_NAME} DYE_TREASURE <DROP_COUNT> <GOOD>/<GREAT>/<OUTSTANDING> [LAST_ON_DATE]", true)
        ChatUtils.sendLocalChat("${GRAY}Example: /${COMMAND_NAME} DYE_TREASURE 2 1234/123/12 2025-05-30 23:59:00", true)
        ChatUtils.sendLocalChat("${GRAY}Guide: ${GUIDE_URL}", true)
    }
}
